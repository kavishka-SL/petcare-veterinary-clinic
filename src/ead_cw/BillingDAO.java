package ead_cw;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pattern: keeps billing database operations outside the Swing form.
 */
public class BillingDAO {

    public List<BillingItem> getItems(int appointmentId)
            throws SQLException {
        List<BillingItem> items = new ArrayList<>();
        String sql = "SELECT ai.appointment_item_id, b.item_name, "
                + "b.item_type, b.unit_name, ai.quantity, ai.unit_price "
                + "FROM appointment_items ai "
                + "INNER JOIN billable_items b ON ai.item_id = b.item_id "
                + "WHERE ai.appointment_id = ? "
                + "ORDER BY ai.appointment_item_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, appointmentId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    items.add(new BillingItem(
                            result.getInt("appointment_item_id"),
                            result.getString("item_name"),
                            result.getString("item_type"),
                            result.getString("unit_name"),
                            result.getInt("quantity"),
                            result.getBigDecimal("unit_price")));
                }
            }
        }
        return items;
    }

    public boolean hasPayment(int appointmentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM payments WHERE appointment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, appointmentId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0;
            }
        }
    }

    public boolean hasTreatment(int appointmentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM treatments "
                + "WHERE appointment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, appointmentId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0;
            }
        }
    }

    public void addItem(int appointmentId, int itemId, int quantity,
            BigDecimal unitPrice) throws SQLException {
        String sql = "INSERT INTO appointment_items "
                + "(appointment_id, item_id, quantity, unit_price) "
                + "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
                + "quantity = quantity + VALUES(quantity)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, appointmentId);
            statement.setInt(2, itemId);
            statement.setInt(3, quantity);
            statement.setBigDecimal(4, unitPrice);
            statement.executeUpdate();
        }
    }

    public void removeItem(int lineId, int appointmentId)
            throws SQLException {
        String sql = "DELETE FROM appointment_items "
                + "WHERE appointment_item_id = ? AND appointment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, lineId);
            statement.setInt(2, appointmentId);
            statement.executeUpdate();
        }
    }
}
