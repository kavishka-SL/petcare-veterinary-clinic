/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ead_cw;

import ead_cw.database.DBConnection;
import ead_cw.notification.EmailService;
import ead_cw.notification.EmailService.AppointmentEmailType;
import ead_cw.notification.EmailService.EmailResult;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author kavis
 */
public class AppointmentForm extends javax.swing.JFrame {

    private final Map<String, Integer> petIds = new HashMap<>();
    private final Map<String, Integer> veterinarianIds = new HashMap<>();
    private final Map<String, Integer> serviceIds = new HashMap<>();
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AppointmentForm.class.getName());

    /**
     * Creates new form AppointmentForm
     */
    public AppointmentForm() {
        initComponents();
        txtAppointmentDate.setToolTipText(
                "Example: 2026-10-15 (yyyy-MM-dd)");
        txtAppointmentTime.setToolTipText("Example: 14:30 (HH:mm)");
        tblAppointments.setDefaultEditor(Object.class, null);

        loadPets();
        loadVeterinarians();
        loadServices();
        loadAppointments();

        tblAppointments.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                fillSelectedAppointment();
            }
        });
    }

    private void loadPets() {
        cmbPet.removeAllItems();
        cmbPet.addItem("-- Select Pet --");
        petIds.clear();

        String sql = "SELECT p.pet_id, p.pet_name, c.full_name AS owner_name "
                + "FROM pets p INNER JOIN customers c "
                + "ON p.customer_id = c.customer_id ORDER BY p.pet_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                int petId = result.getInt("pet_id");
                String displayName = petId + " - "
                        + result.getString("pet_name") + " ("
                        + result.getString("owner_name") + ")";
                cmbPet.addItem(displayName);
                petIds.put(displayName, petId);
            }
        } catch (SQLException exception) {
            showDatabaseError("load pets", exception);
        }
    }

    private void loadVeterinarians() {
        cmbVeterinarian.removeAllItems();
        cmbVeterinarian.addItem("-- Select Veterinarian --");
        veterinarianIds.clear();

        String sql = "SELECT veterinarian_id, full_name FROM veterinarians "
                + "WHERE active = TRUE ORDER BY full_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                int veterinarianId = result.getInt("veterinarian_id");
                String displayName = veterinarianId + " - "
                        + result.getString("full_name");
                cmbVeterinarian.addItem(displayName);
                veterinarianIds.put(displayName, veterinarianId);
            }
        } catch (SQLException exception) {
            showDatabaseError("load veterinarians", exception);
        }
    }

    private void loadServices() {
        cmbService.removeAllItems();
        cmbService.addItem("-- Select Service --");
        serviceIds.clear();

        String sql = "SELECT service_id, service_name FROM services "
                + "WHERE active = TRUE ORDER BY service_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                int serviceId = result.getInt("service_id");
                String displayName = serviceId + " - "
                        + result.getString("service_name");
                cmbService.addItem(displayName);
                serviceIds.put(displayName, serviceId);
            }
        } catch (SQLException exception) {
            showDatabaseError("load services", exception);
        }
    }

    private void loadAppointments() {
        DefaultTableModel model =
                (DefaultTableModel) tblAppointments.getModel();
        model.setRowCount(0);

        String sql = "SELECT a.appointment_id, a.pet_id, "
                + "a.veterinarian_id, a.service_id, c.full_name AS customer, "
                + "p.pet_name, v.full_name AS veterinarian, s.service_name, "
                + "a.appointment_date, a.appointment_time, a.symptoms, a.status "
                + "FROM appointments a "
                + "INNER JOIN pets p ON a.pet_id = p.pet_id "
                + "INNER JOIN customers c ON p.customer_id = c.customer_id "
                + "INNER JOIN veterinarians v "
                + "ON a.veterinarian_id = v.veterinarian_id "
                + "INNER JOIN services s ON a.service_id = s.service_id "
                + "ORDER BY a.appointment_date, a.appointment_time";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                String pet = result.getInt("pet_id") + " - "
                        + result.getString("pet_name") + " ("
                        + result.getString("customer") + ")";
                String veterinarian = result.getInt("veterinarian_id")
                        + " - " + result.getString("veterinarian");
                String service = result.getInt("service_id") + " - "
                        + result.getString("service_name");
                String time = result.getTime("appointment_time")
                        .toLocalTime().format(TIME_FORMAT);

                model.addRow(new Object[]{
                    result.getInt("appointment_id"),
                    result.getString("customer"),
                    pet,
                    veterinarian,
                    service,
                    result.getDate("appointment_date"),
                    time,
                    result.getString("symptoms"),
                    result.getString("status")
                });
            }
        } catch (SQLException exception) {
            showDatabaseError("load appointments", exception);
        }
    }

    private Date readAppointmentDate() {
        LocalDate date = LocalDate.parse(
                txtAppointmentDate.getText().trim());
        return Date.valueOf(date);
    }

    private Time readAppointmentTime() {
        LocalTime time = LocalTime.parse(
                txtAppointmentTime.getText().trim(), TIME_FORMAT);
        return Time.valueOf(time);
    }

    private boolean hasScheduleConflict(Connection connection,
            int veterinarianId, Date date, Time time,
            int excludedAppointmentId) throws SQLException {

        String sql = "SELECT COUNT(*) FROM appointments "
                + "WHERE veterinarian_id = ? AND appointment_date = ? "
                + "AND appointment_time = ? AND appointment_id <> ? "
                + "AND status = 'SCHEDULED'";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, veterinarianId);
            statement.setDate(2, date);
            statement.setTime(3, time);
            statement.setInt(4, excludedAppointmentId);

            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0;
            }
        }
    }

    private void clearFields() {
        cmbPet.setSelectedIndex(0);
        cmbVeterinarian.setSelectedIndex(0);
        cmbService.setSelectedIndex(0);
        txtAppointmentDate.setText("");
        txtAppointmentTime.setText("");
        txtSymptoms.setText("");
        cmbStatus.setSelectedItem("SCHEDULED");
        tblAppointments.clearSelection();
    }

    private void fillSelectedAppointment() {
        int selectedRow = tblAppointments.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        cmbPet.setSelectedItem(
                tblAppointments.getValueAt(selectedRow, 2).toString());
        cmbVeterinarian.setSelectedItem(
                tblAppointments.getValueAt(selectedRow, 3).toString());
        cmbService.setSelectedItem(
                tblAppointments.getValueAt(selectedRow, 4).toString());
        txtAppointmentDate.setText(
                tblAppointments.getValueAt(selectedRow, 5).toString());
        txtAppointmentTime.setText(
                tblAppointments.getValueAt(selectedRow, 6).toString());

        Object symptoms = tblAppointments.getValueAt(selectedRow, 7);
        txtSymptoms.setText(symptoms == null ? "" : symptoms.toString());
        cmbStatus.setSelectedItem(
                tblAppointments.getValueAt(selectedRow, 8).toString());
    }

    private void showDatabaseError(String operation, SQLException exception) {
        JOptionPane.showMessageDialog(this,
                "Unable to " + operation + ": " + exception.getMessage());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        cmbPet = new javax.swing.JComboBox<>();
        cmbVeterinarian = new javax.swing.JComboBox<>();
        cmbService = new javax.swing.JComboBox<>();
        txtAppointmentDate = new javax.swing.JTextField();
        txtAppointmentTime = new javax.swing.JTextField();
        txtSymptoms = new javax.swing.JTextField();
        cmbStatus = new javax.swing.JComboBox<>();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblAppointments = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Appointment");
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Pet");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Veterinarian");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Service");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Appointment Date");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Appointment Time");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Symptoms");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Status");

        cmbPet.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select a Pet--" }));
        cmbPet.addActionListener(this::cmbPetActionPerformed);

        cmbVeterinarian.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select a Veterinarian--", " " }));
        cmbVeterinarian.addActionListener(this::cmbVeterinarianActionPerformed);

        cmbService.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select a Service--", " " }));
        cmbService.addActionListener(this::cmbServiceActionPerformed);

        txtAppointmentDate.setToolTipText("dd-mm-yyyy");
        txtAppointmentDate.addActionListener(this::txtAppointmentDateActionPerformed);

        txtAppointmentTime.setToolTipText("hh:mm(00:30)");

        txtSymptoms.addActionListener(this::txtSymptomsActionPerformed);

        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "SCHEDULED", "COMPLETED", "CANCELLED" }));
        cmbStatus.addActionListener(this::cmbStatusActionPerformed);

        btnAdd.setBackground(new java.awt.Color(184, 218, 252));
        btnAdd.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnAdd.setText("Add");
        btnAdd.addActionListener(this::btnAddActionPerformed);

        btnUpdate.setBackground(new java.awt.Color(184, 218, 252));
        btnUpdate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnUpdate.setText("Update");
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);

        btnDelete.setBackground(new java.awt.Color(184, 218, 252));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

        btnClear.setBackground(new java.awt.Color(132, 194, 214));
        btnClear.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnClear.setText("Clear");
        btnClear.addActionListener(this::btnClearActionPerformed);

        btnBack.setBackground(new java.awt.Color(132, 194, 214));
        btnBack.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnBack.setText("Back");
        btnBack.addActionListener(this::btnBackActionPerformed);

        tblAppointments.setBackground(new java.awt.Color(102, 204, 255));
        tblAppointments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Appointment ID ", "Customer ", "Pet", "Veterinarian", "Service", "Date ", "Time", "Symptoms", "Status"
            }
        ));
        tblAppointments.setShowVerticalLines(true);
        jScrollPane1.setViewportView(tblAppointments);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addGap(37, 37, 37)
                                        .addComponent(cmbService, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtAppointmentTime, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel7)
                                            .addComponent(jLabel8))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtSymptoms, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtAppointmentDate, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(50, 50, 50)
                                        .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnUpdate)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addGap(59, 59, 59)
                                        .addComponent(cmbPet, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cmbVeterinarian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(285, 285, 285))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 724, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(cmbPet, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(cmbVeterinarian, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbService, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtAppointmentDate, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtAppointmentTime, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSymptoms, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 780, 570));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbPetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPetActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbPetActionPerformed

    private void cmbVeterinarianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbVeterinarianActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbVeterinarianActionPerformed

    private void cmbServiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbServiceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbServiceActionPerformed

    private void cmbStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbStatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbStatusActionPerformed

    private void txtSymptomsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSymptomsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSymptomsActionPerformed

    private void txtAppointmentDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAppointmentDateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAppointmentDateActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        Integer petId = petIds.get((String) cmbPet.getSelectedItem());
        Integer veterinarianId = veterinarianIds.get(
                (String) cmbVeterinarian.getSelectedItem());
        Integer serviceId = serviceIds.get(
                (String) cmbService.getSelectedItem());
        String symptoms = txtSymptoms.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();

        if (petId == null || veterinarianId == null || serviceId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a pet, veterinarian and service.");
            return;
        }
        if (symptoms.length() > 255) {
            JOptionPane.showMessageDialog(this,
                    "Symptoms cannot exceed 255 characters.");
            return;
        }

        Date date;
        Time time;
        try {
            date = readAppointmentDate();
            time = readAppointmentTime();
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this,
                    "Use yyyy-MM-dd for date and HH:mm for time.");
            return;
        }

        if (date.toLocalDate().isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this,
                    "Appointment date cannot be in the past.");
            return;
        }

        String sql = "INSERT INTO appointments "
                + "(pet_id, veterinarian_id, service_id, appointment_date, "
                + "appointment_time, symptoms, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            if (hasScheduleConflict(connection, veterinarianId,
                    date, time, 0)) {
                JOptionPane.showMessageDialog(this,
                        "The veterinarian already has an appointment at this time.");
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, petId);
                statement.setInt(2, veterinarianId);
                statement.setInt(3, serviceId);
                statement.setDate(4, date);
                statement.setTime(5, time);
                statement.setString(6, symptoms);
                statement.setString(7, status);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            showDatabaseError("add appointment", exception);
            return;
        }

        String message = "Appointment added successfully.";

        try {
            EmailService emailService = new EmailService();
            EmailResult result = emailService.sendAppointmentConfirmation(
                    petId, veterinarianId, serviceId, date, time);

            if (result == EmailResult.SENT) {
                message += "\nConfirmation email sent successfully.";
            } else if (result == EmailResult.NO_CUSTOMER_EMAIL) {
                message += "\nNo customer email is available.";
            } else {
                message += "\nEmail notification is not configured.";
            }
        } catch (SQLException | IOException exception) {
            message += "\nThe confirmation email could not be sent: "
                    + exception.getMessage();
        }

        JOptionPane.showMessageDialog(this, message);
        clearFields();
        loadAppointments();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        int selectedRow = tblAppointments.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment to update.");
            return;
        }

        Integer petId = petIds.get((String) cmbPet.getSelectedItem());
        Integer veterinarianId = veterinarianIds.get(
                (String) cmbVeterinarian.getSelectedItem());
        Integer serviceId = serviceIds.get(
                (String) cmbService.getSelectedItem());
        String symptoms = txtSymptoms.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();

        if (petId == null || veterinarianId == null || serviceId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a pet, veterinarian and service.");
            return;
        }
        if (symptoms.length() > 255) {
            JOptionPane.showMessageDialog(this,
                    "Symptoms cannot exceed 255 characters.");
            return;
        }

        Date date;
        Time time;
        try {
            date = readAppointmentDate();
            time = readAppointmentTime();
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this,
                    "Use yyyy-MM-dd for date and HH:mm for time.");
            return;
        }

        int appointmentId = Integer.parseInt(
                tblAppointments.getValueAt(selectedRow, 0).toString());
        String sql = "UPDATE appointments SET pet_id = ?, "
                + "veterinarian_id = ?, service_id = ?, appointment_date = ?, "
                + "appointment_time = ?, symptoms = ?, status = ? "
                + "WHERE appointment_id = ?";

        try (Connection connection = DBConnection.getConnection()) {
            if (hasScheduleConflict(connection, veterinarianId,
                    date, time, appointmentId)) {
                JOptionPane.showMessageDialog(this,
                        "The veterinarian already has an appointment at this time.");
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, petId);
                statement.setInt(2, veterinarianId);
                statement.setInt(3, serviceId);
                statement.setDate(4, date);
                statement.setTime(5, time);
                statement.setString(6, symptoms);
                statement.setString(7, status);
                statement.setInt(8, appointmentId);
                statement.executeUpdate();
            }

        } catch (SQLException exception) {
            showDatabaseError("update appointment", exception);
            return;
        }

        AppointmentEmailType emailType;
        if ("CANCELLED".equals(status)) {
            emailType = AppointmentEmailType.CANCELLED;
        } else if ("COMPLETED".equals(status)) {
            emailType = AppointmentEmailType.COMPLETED;
        } else {
            emailType = AppointmentEmailType.UPDATED;
        }

        String message = "Appointment updated successfully.";

        try {
            EmailService emailService = new EmailService();
            EmailResult result = emailService.sendAppointmentNotification(
                    petId, veterinarianId, serviceId, date, time, emailType);

            if (result == EmailResult.SENT) {
                message += "\nCustomer notification email sent successfully.";
            } else if (result == EmailResult.NO_CUSTOMER_EMAIL) {
                message += "\nNo customer email is available.";
            } else {
                message += "\nEmail notification is not configured.";
            }
        } catch (SQLException | IOException exception) {
            message += "\nThe customer email could not be sent: "
                    + exception.getMessage();
        }

        JOptionPane.showMessageDialog(this, message);
        clearFields();
        loadAppointments();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int selectedRow = tblAppointments.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment to delete.");
            return;
        }

        int appointmentId = Integer.parseInt(
                tblAppointments.getValueAt(selectedRow, 0).toString());

        Integer petId = petIds.get((String) cmbPet.getSelectedItem());
        Integer veterinarianId = veterinarianIds.get(
                (String) cmbVeterinarian.getSelectedItem());
        Integer serviceId = serviceIds.get(
                (String) cmbService.getSelectedItem());

        Date date;
        Time time;
        try {
            date = readAppointmentDate();
            time = readAppointmentTime();
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to read the selected appointment date and time.");
            return;
        }

        if (petId == null || veterinarianId == null || serviceId == null) {
            JOptionPane.showMessageDialog(this,
                    "Unable to read the selected appointment details.");
            return;
        }

        String checkSql = "SELECT "
                + "(SELECT COUNT(*) FROM treatments WHERE appointment_id = ?) + "
                + "(SELECT COUNT(*) FROM appointment_items WHERE appointment_id = ?) + "
                + "(SELECT COUNT(*) FROM payments WHERE appointment_id = ?) "
                + "AS related_records";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement checkStatement =
                     connection.prepareStatement(checkSql)) {

            checkStatement.setInt(1, appointmentId);
            checkStatement.setInt(2, appointmentId);
            checkStatement.setInt(3, appointmentId);

            try (ResultSet result = checkStatement.executeQuery()) {
                result.next();
                if (result.getInt("related_records") > 0) {
                    JOptionPane.showMessageDialog(this,
                            "This appointment has treatment, billing, or "
                            + "payment history and cannot be deleted.");
                    return;
                }
            }

            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "Permanently delete this unused appointment?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (answer != JOptionPane.YES_OPTION) {
                return;
            }

            String deleteSql =
                    "DELETE FROM appointments WHERE appointment_id = ?";
            try (PreparedStatement deleteStatement =
                    connection.prepareStatement(deleteSql)) {
                deleteStatement.setInt(1, appointmentId);
                deleteStatement.executeUpdate();
            }

        } catch (SQLException exception) {
            showDatabaseError("delete appointment", exception);
            return;
        }

        String message = "Appointment deleted successfully.";

        try {
            EmailService emailService = new EmailService();
            EmailResult result = emailService.sendAppointmentNotification(
                    petId, veterinarianId, serviceId, date, time,
                    AppointmentEmailType.DELETED);

            if (result == EmailResult.SENT) {
                message += "\nCustomer notification email sent successfully.";
            } else if (result == EmailResult.NO_CUSTOMER_EMAIL) {
                message += "\nNo customer email is available.";
            } else {
                message += "\nEmail notification is not configured.";
            }
        } catch (SQLException | IOException exception) {
            message += "\nThe customer email could not be sent: "
                    + exception.getMessage();
        }

        JOptionPane.showMessageDialog(this, message);
        clearFields();
        loadAppointments();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearFields();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        DashboardForm dashboard = new DashboardForm();
        dashboard.setLocationRelativeTo(null);
        dashboard.setVisible(true);
        dispose();
    }//GEN-LAST:event_btnBackActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new AppointmentForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cmbPet;
    private javax.swing.JComboBox<String> cmbService;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JComboBox<String> cmbVeterinarian;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblAppointments;
    private javax.swing.JTextField txtAppointmentDate;
    private javax.swing.JTextField txtAppointmentTime;
    private javax.swing.JTextField txtSymptoms;
    // End of variables declaration//GEN-END:variables
}
