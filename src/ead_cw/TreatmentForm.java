/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ead_cw;

import ead_cw.database.DBConnection;
import ead_cw.session.UserSession;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author kavis
 */
public class TreatmentForm extends javax.swing.JFrame {

    private final Map<String, Integer> appointmentIds = new HashMap<>();
    private final Map<Integer, String> appointmentDisplays = new HashMap<>();
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TreatmentForm.class.getName());

    /**
     * Creates new form TreatmentForm
     */
    public TreatmentForm() {
        this(null);
    }

    public TreatmentForm(Integer selectedAppointmentId) {
        initComponents();
        setLocationRelativeTo(null);
        spnTreatmentDate.setEditor(
                new javax.swing.JSpinner.DateEditor(
                        spnTreatmentDate, "yyyy-MM-dd"));
        tblTreatments.setDefaultEditor(Object.class, null);
        loadAppointmentChoices();
        loadTreatments();

        tblTreatments.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                fillSelectedTreatment();
            }
        });

        if (selectedAppointmentId != null) {
            String display = appointmentDisplays.get(selectedAppointmentId);
            if (display != null) {
                cmbAppointment.setSelectedItem(display);
            }
        }
    }

    private void loadAppointmentChoices() {
        cmbAppointment.removeAllItems();
        cmbAppointment.addItem("-- Select Appointment --");
        appointmentIds.clear();
        appointmentDisplays.clear();

        boolean filterByVeterinarian =
                UserSession.hasRole("VETERINARIAN")
                && UserSession.getVeterinarianId() != null;

        String sql = "SELECT a.appointment_id, c.full_name AS customer, "
                + "p.pet_name, a.appointment_date "
                + "FROM appointments a "
                + "INNER JOIN pets p ON a.pet_id = p.pet_id "
                + "INNER JOIN customers c ON p.customer_id = c.customer_id "
                + "WHERE a.status <> 'CANCELLED' "
                + (filterByVeterinarian
                        ? "AND a.veterinarian_id = ? " : "")
                + "ORDER BY a.appointment_date, a.appointment_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (filterByVeterinarian) {
                statement.setInt(1, UserSession.getVeterinarianId());
            }

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int appointmentId = result.getInt("appointment_id");
                    String displayName = appointmentId + " - "
                            + result.getString("pet_name") + " / "
                            + result.getString("customer") + " / "
                            + result.getDate("appointment_date");

                    cmbAppointment.addItem(displayName);
                    appointmentIds.put(displayName, appointmentId);
                    appointmentDisplays.put(appointmentId, displayName);
                }
            }
        } catch (SQLException exception) {
            showDatabaseError("load appointments", exception);
        }
    }

    private void loadTreatments() {
        DefaultTableModel model =
                (DefaultTableModel) tblTreatments.getModel();
        model.setRowCount(0);

        boolean filterByVeterinarian =
                UserSession.hasRole("VETERINARIAN")
                && UserSession.getVeterinarianId() != null;

        String sql = "SELECT t.treatment_id, t.appointment_id, "
                + "c.full_name AS customer, p.pet_name, "
                + "v.full_name AS veterinarian, t.diagnosis, "
                + "t.notes, t.treatment_date "
                + "FROM treatments t "
                + "INNER JOIN appointments a "
                + "ON t.appointment_id = a.appointment_id "
                + "INNER JOIN pets p ON a.pet_id = p.pet_id "
                + "INNER JOIN customers c ON p.customer_id = c.customer_id "
                + "INNER JOIN veterinarians v "
                + "ON a.veterinarian_id = v.veterinarian_id "
                + (filterByVeterinarian
                        ? "WHERE a.veterinarian_id = ? " : "")
                + "ORDER BY t.treatment_date DESC, t.treatment_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (filterByVeterinarian) {
                statement.setInt(1, UserSession.getVeterinarianId());
            }

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    model.addRow(new Object[]{
                        result.getInt("treatment_id"),
                        result.getInt("appointment_id"),
                        result.getString("customer"),
                        result.getString("pet_name"),
                        result.getString("veterinarian"),
                        result.getString("diagnosis"),
                        result.getString("notes"),
                        result.getDate("treatment_date")
                    });
                }
            }
        } catch (SQLException exception) {
            showDatabaseError("load treatments", exception);
        }
    }

    private Date readTreatmentDate() {
        java.util.Date selectedDate =
                (java.util.Date) spnTreatmentDate.getValue();
        return new Date(selectedDate.getTime());
    }

    private boolean validateTreatmentText() {
        String diagnosis = txtDiagnosis.getText().trim();
        String notes = txtNotes.getText().trim();

        if (diagnosis.isEmpty() || diagnosis.length() > 255) {
            JOptionPane.showMessageDialog(this,
                    "Diagnosis is required and cannot exceed 255 characters.");
            txtDiagnosis.requestFocus();
            return false;
        }
        if (notes.length() > 500) {
            JOptionPane.showMessageDialog(this,
                    "Notes cannot exceed 500 characters.");
            txtNotes.requestFocus();
            return false;
        }
        return true;
    }

    private void clearFields() {
        cmbAppointment.setSelectedIndex(0);
        txtDiagnosis.setText("");
        txtNotes.setText("");
        spnTreatmentDate.setValue(new java.util.Date());
        tblTreatments.clearSelection();
    }

    private void fillSelectedTreatment() {
        int selectedRow = tblTreatments.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        int appointmentId = Integer.parseInt(
                tblTreatments.getValueAt(selectedRow, 1).toString());
        cmbAppointment.setSelectedItem(
                appointmentDisplays.get(appointmentId));
        txtDiagnosis.setText(
                tblTreatments.getValueAt(selectedRow, 5).toString());

        Object notes = tblTreatments.getValueAt(selectedRow, 6);
        txtNotes.setText(notes == null ? "" : notes.toString());
        java.util.Date treatmentDate = (java.util.Date)
                tblTreatments.getValueAt(selectedRow, 7);
        spnTreatmentDate.setValue(treatmentDate);
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
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cmbAppointment = new javax.swing.JComboBox<>();
        txtDiagnosis = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtNotes = new javax.swing.JTextArea();
        btnSave = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblTreatments = new javax.swing.JTable();
        spnTreatmentDate = new javax.swing.JSpinner();
        btnContinueToBilling = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Treatments");
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Appointment");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Diagnosis");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Notes");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Treatment Date");

        cmbAppointment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select an Appointment--" }));
        cmbAppointment.addActionListener(this::cmbAppointmentActionPerformed);

        txtDiagnosis.addActionListener(this::txtDiagnosisActionPerformed);

        txtNotes.setColumns(20);
        txtNotes.setRows(5);
        jScrollPane1.setViewportView(txtNotes);

        btnSave.setBackground(new java.awt.Color(184, 218, 252));
        btnSave.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnSave.setText("Save");
        btnSave.addActionListener(this::btnSaveActionPerformed);

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

        tblTreatments.setBackground(new java.awt.Color(51, 204, 255));
        tblTreatments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Treatment ID", "Appointment ID", "Customer ", "Pet", "Veterinarian", "Diagnosis", "Notes", "Treatment Date"
            }
        ));
        tblTreatments.setShowVerticalLines(true);
        jScrollPane2.setViewportView(tblTreatments);

        spnTreatmentDate.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_YEAR));

        btnContinueToBilling.setBackground(new java.awt.Color(204, 255, 51));
        btnContinueToBilling.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnContinueToBilling.setText("Continue to Billing");
        btnContinueToBilling.addActionListener(this::btnContinueToBillingActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(txtDiagnosis, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(cmbAppointment, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spnTreatmentDate, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 101, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdate)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnContinueToBilling, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(79, 79, 79))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 730, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(cmbAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtDiagnosis, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(spnTreatmentDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(71, 71, 71))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(121, 121, 121)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(45, 45, 45)
                        .addComponent(btnContinueToBilling, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(26, 26, 26)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(96, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 760, 580));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAppointmentActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbAppointmentActionPerformed

    private void txtDiagnosisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDiagnosisActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDiagnosisActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        Integer appointmentId = appointmentIds.get(
                (String) cmbAppointment.getSelectedItem());

        if (appointmentId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment.");
            return;
        }
        if (!validateTreatmentText()) {
            return;
        }

        Date treatmentDate = readTreatmentDate();

        if (treatmentDate.toLocalDate().isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(this,
                    "Treatment date cannot be in the future.");
            return;
        }

        String insertSql = "INSERT INTO treatments "
                + "(appointment_id, diagnosis, notes, treatment_date) "
                + "VALUES (?, ?, ?, ?)";
        String statusSql = "UPDATE appointments SET status = 'COMPLETED' "
                + "WHERE appointment_id = ?";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement insertStatement =
                        connection.prepareStatement(insertSql);
                 PreparedStatement statusStatement =
                        connection.prepareStatement(statusSql)) {

                insertStatement.setInt(1, appointmentId);
                insertStatement.setString(
                        2, txtDiagnosis.getText().trim());
                insertStatement.setString(3, txtNotes.getText().trim());
                insertStatement.setDate(4, treatmentDate);
                insertStatement.executeUpdate();

                statusStatement.setInt(1, appointmentId);
                statusStatement.executeUpdate();
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }

            JOptionPane.showMessageDialog(this,
                    "Treatment saved successfully.");
            clearFields();
            loadTreatments();
        } catch (SQLException exception) {
            showDatabaseError("save treatment", exception);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        int selectedRow = tblTreatments.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a treatment to update.");
            return;
        }

        Integer appointmentId = appointmentIds.get(
                (String) cmbAppointment.getSelectedItem());
        if (appointmentId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment.");
            return;
        }
        if (!validateTreatmentText()) {
            return;
        }

        Date treatmentDate = readTreatmentDate();

        int treatmentId = Integer.parseInt(
                tblTreatments.getValueAt(selectedRow, 0).toString());
        String sql = "UPDATE treatments SET appointment_id = ?, "
                + "diagnosis = ?, notes = ?, treatment_date = ? "
                + "WHERE treatment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, appointmentId);
            statement.setString(2, txtDiagnosis.getText().trim());
            statement.setString(3, txtNotes.getText().trim());
            statement.setDate(4, treatmentDate);
            statement.setInt(5, treatmentId);
            statement.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Treatment updated successfully.");
            clearFields();
            loadTreatments();
        } catch (SQLException exception) {
            showDatabaseError("update treatment", exception);
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int selectedRow = tblTreatments.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a treatment to delete.");
            return;
        }

        int treatmentId = Integer.parseInt(
                tblTreatments.getValueAt(selectedRow, 0).toString());
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete the selected treatment?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM treatments WHERE treatment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, treatmentId);
            statement.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Treatment deleted successfully.");
            clearFields();
            loadTreatments();
        } catch (SQLException exception) {
            showDatabaseError("delete treatment", exception);
        }
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

    private void btnContinueToBillingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContinueToBillingActionPerformed
        String selectedAppointment =
                (String) cmbAppointment.getSelectedItem();
        Integer appointmentId = appointmentIds.get(selectedAppointment);

        if (appointmentId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment first.");
            return;
        }

        String sql = "SELECT COUNT(*) FROM treatments "
                + "WHERE appointment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, appointmentId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                if (result.getInt(1) == 0) {
                    JOptionPane.showMessageDialog(this,
                            "Please save the treatment before continuing "
                            + "to billing.");
                    return;
                }
            }

            new AppointmentBillingForm(appointmentId).setVisible(true);
            dispose();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to open appointment billing: "
                    + exception.getMessage());
        }
    }//GEN-LAST:event_btnContinueToBillingActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new TreatmentForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnContinueToBilling;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cmbAppointment;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner spnTreatmentDate;
    private javax.swing.JTable tblTreatments;
    private javax.swing.JTextField txtDiagnosis;
    private javax.swing.JTextArea txtNotes;
    // End of variables declaration//GEN-END:variables
}
