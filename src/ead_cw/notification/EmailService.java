package ead_cw.notification;

import ead_cw.database.DBConnection;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class EmailService {

    public enum EmailResult {
        SENT,
        NO_CUSTOMER_EMAIL,
        NOT_CONFIGURED
    }

    public enum AppointmentEmailType {
        CONFIRMED,
        UPDATED,
        CANCELLED,
        COMPLETED,
        DELETED
    }

    private static final String API_URL =
            "https://api.brevo.com/v3/smtp/email";
    private static final String API_KEY_VARIABLE =
            "PETCARE_BREVO_API_KEY";
    private static final String SENDER_EMAIL_VARIABLE =
            "PETCARE_SENDER_EMAIL";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    public EmailResult sendAppointmentConfirmation(int petId,
            int veterinarianId, int serviceId, Date date, Time time)
            throws SQLException, IOException {

        return sendAppointmentNotification(petId, veterinarianId,
                serviceId, date, time, AppointmentEmailType.CONFIRMED);
    }

    public EmailResult sendAppointmentNotification(int appointmentId,
            AppointmentEmailType emailType)
            throws SQLException, IOException {

        String sql = "SELECT pet_id, veterinarian_id, service_id, "
                + "appointment_date, appointment_time "
                + "FROM appointments WHERE appointment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, appointmentId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Appointment was not found.");
                }

                return sendAppointmentNotification(
                        result.getInt("pet_id"),
                        result.getInt("veterinarian_id"),
                        result.getInt("service_id"),
                        result.getDate("appointment_date"),
                        result.getTime("appointment_time"),
                        emailType);
            }
        }
    }

    public EmailResult sendAppointmentNotification(int petId,
            int veterinarianId, int serviceId, Date date, Time time,
            AppointmentEmailType emailType)
            throws SQLException, IOException {

        String apiKey = System.getenv(API_KEY_VARIABLE);
        String senderEmail = System.getenv(SENDER_EMAIL_VARIABLE);

        if (isBlank(apiKey) || isBlank(senderEmail)) {
            return EmailResult.NOT_CONFIGURED;
        }

        AppointmentEmailDetails details = loadAppointmentDetails(
                petId, veterinarianId, serviceId);

        if (isBlank(details.customerEmail)) {
            return EmailResult.NO_CUSTOMER_EMAIL;
        }

        String subject = buildSubject(emailType);
        String message = buildMessage(
                details, date, time, emailType);

        String json = "{"
                + "\"sender\":{\"name\":\"PetCare Veterinary Clinic\","
                + "\"email\":\"" + escapeJson(senderEmail) + "\"},"
                + "\"to\":[{\"email\":\""
                + escapeJson(details.customerEmail) + "\","
                + "\"name\":\"" + escapeJson(details.customerName)
                + "\"}],"
                + "\"subject\":\"" + escapeJson(subject) + "\","
                + "\"textContent\":\"" + escapeJson(message) + "\""
                + "}";

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(15))
                .header("accept", "application/json")
                .header("api-key", apiKey)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {
                throw new IOException(
                        "Email service returned status "
                        + response.statusCode() + ".");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Email request was interrupted.", exception);
        }

        return EmailResult.SENT;
    }

    private String buildSubject(AppointmentEmailType emailType) {
        return switch (emailType) {
            case CONFIRMED -> "PetCare Appointment Confirmation";
            case UPDATED -> "PetCare Appointment Updated";
            case CANCELLED -> "PetCare Appointment Cancelled";
            case COMPLETED -> "PetCare Appointment Completed";
            case DELETED -> "PetCare Appointment Removed";
        };
    }

    private String buildMessage(AppointmentEmailDetails details,
            Date date, Time time, AppointmentEmailType emailType) {

        String appointmentTime = date.toLocalDate().format(DATE_FORMAT)
                + " at " + time.toLocalTime().format(TIME_FORMAT);

        String changeMessage = switch (emailType) {
            case CONFIRMED -> "Your appointment for " + details.petName
                    + " has been scheduled for " + appointmentTime + ".";
            case UPDATED -> "Your appointment for " + details.petName
                    + " has been updated to " + appointmentTime + ".";
            case CANCELLED -> "Your appointment for " + details.petName
                    + " on " + appointmentTime + " has been cancelled.";
            case COMPLETED -> "The appointment for " + details.petName
                    + " has been completed successfully.";
            case DELETED -> "The appointment for " + details.petName
                    + " on " + appointmentTime
                    + " has been removed from the clinic schedule.";
        };

        return "Dear " + details.customerName + ",\n\n"
                + changeMessage + "\n"
                + "Veterinarian: " + details.veterinarianName + "\n"
                + "Service: " + details.serviceName + "\n\n"
                + "Please contact the clinic if you have any questions.\n\n"
                + "PetCare Veterinary Clinic";
    }

    private AppointmentEmailDetails loadAppointmentDetails(int petId,
            int veterinarianId, int serviceId) throws SQLException {

        String sql = "SELECT c.full_name AS customer_name, c.email, "
                + "p.pet_name, v.full_name AS veterinarian_name, "
                + "s.service_name "
                + "FROM pets p "
                + "INNER JOIN customers c ON p.customer_id = c.customer_id "
                + "INNER JOIN veterinarians v ON v.veterinarian_id = ? "
                + "INNER JOIN services s ON s.service_id = ? "
                + "WHERE p.pet_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, veterinarianId);
            statement.setInt(2, serviceId);
            statement.setInt(3, petId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException(
                            "Appointment email details were not found.");
                }

                return new AppointmentEmailDetails(
                        result.getString("customer_name"),
                        result.getString("email"),
                        result.getString("pet_name"),
                        result.getString("veterinarian_name"),
                        result.getString("service_name"));
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private static class AppointmentEmailDetails {

        private final String customerName;
        private final String customerEmail;
        private final String petName;
        private final String veterinarianName;
        private final String serviceName;

        private AppointmentEmailDetails(String customerName,
                String customerEmail, String petName,
                String veterinarianName, String serviceName) {
            this.customerName = customerName;
            this.customerEmail = customerEmail;
            this.petName = petName;
            this.veterinarianName = veterinarianName;
            this.serviceName = serviceName;
        }
    }
}
