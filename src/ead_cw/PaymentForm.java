/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ead_cw;

import ead_cw.database.DBConnection;
import java.math.BigDecimal;
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
public class PaymentForm extends javax.swing.JFrame {

    private final Map<String, Integer> appointmentIds = new HashMap<>();
    private final Map<String, BigDecimal> appointmentAmounts = new HashMap<>();
    private final Map<Integer, String> appointmentDisplays = new HashMap<>();
    private final Integer previousAppointmentId;
    private final boolean openedFromBilling;
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PaymentForm.class.getName());

    /**
     * Creates new form PaymentForm
     */
    public PaymentForm() {
        this(null, false);
    }

    public PaymentForm(Integer selectedAppointmentId) {
        this(selectedAppointmentId, true);
    }

    private PaymentForm(Integer selectedAppointmentId,
            boolean cameFromBilling) {
        previousAppointmentId = selectedAppointmentId;
        openedFromBilling = cameFromBilling;
        initComponents();
        setLocationRelativeTo(null);
        spnPaymentDate.setEditor(
                new javax.swing.JSpinner.DateEditor(
                        spnPaymentDate, "yyyy-MM-dd"));
        tblPayments.setDefaultEditor(Object.class, null);
        loadAppointmentChoices();
        loadPayments();

        cmbAppointment.addActionListener(event -> updateAmount());
        tblPayments.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                fillSelectedPayment();
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
        appointmentAmounts.clear();
        appointmentDisplays.clear();

        String sql = "SELECT a.appointment_id, c.full_name AS customer, "
                + "p.pet_name, s.service_name, "
                + "COALESCE(SUM(ai.quantity * ai.unit_price), 0) AS total "
                + "FROM appointments a "
                + "INNER JOIN pets p ON a.pet_id = p.pet_id "
                + "INNER JOIN customers c ON p.customer_id = c.customer_id "
                + "INNER JOIN services s ON a.service_id = s.service_id "
                + "LEFT JOIN appointment_items ai "
                + "ON a.appointment_id = ai.appointment_id "
                + "WHERE a.status <> 'CANCELLED' "
                + "AND EXISTS (SELECT 1 FROM treatments t "
                + "WHERE t.appointment_id = a.appointment_id) "
                + "GROUP BY a.appointment_id, c.full_name, p.pet_name, "
                + "s.service_name "
                + "HAVING total > 0 "
                + "ORDER BY a.appointment_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                int appointmentId = result.getInt("appointment_id");
                String displayName = appointmentId + " - "
                        + result.getString("pet_name") + " / "
                        + result.getString("customer") + " / "
                        + result.getString("service_name");

                cmbAppointment.addItem(displayName);
                appointmentIds.put(displayName, appointmentId);
                appointmentAmounts.put(
                        displayName, result.getBigDecimal("total"));
                appointmentDisplays.put(appointmentId, displayName);
            }
        } catch (SQLException exception) {
            showDatabaseError("load appointments", exception);
        }
    }

    private void updateAmount() {
        String appointment = (String) cmbAppointment.getSelectedItem();
        BigDecimal amount = appointmentAmounts.get(appointment);
        txtAmount.setText(amount == null ? "" : amount.toPlainString());
    }

    private void loadPayments() {
        DefaultTableModel model =
                (DefaultTableModel) tblPayments.getModel();
        model.setRowCount(0);

        String sql = "SELECT py.payment_id, py.appointment_id, "
                + "c.full_name AS customer, p.pet_name, s.service_name, "
                + "py.amount, py.payment_method, py.payment_date "
                + "FROM payments py "
                + "INNER JOIN appointments a "
                + "ON py.appointment_id = a.appointment_id "
                + "INNER JOIN pets p ON a.pet_id = p.pet_id "
                + "INNER JOIN customers c ON p.customer_id = c.customer_id "
                + "INNER JOIN services s ON a.service_id = s.service_id "
                + "ORDER BY py.payment_date DESC, py.payment_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                model.addRow(new Object[]{
                    result.getInt("payment_id"),
                    result.getInt("appointment_id"),
                    result.getString("customer"),
                    result.getString("pet_name"),
                    result.getString("service_name"),
                    result.getBigDecimal("amount"),
                    result.getString("payment_method"),
                    result.getDate("payment_date")
                });
            }
        } catch (SQLException exception) {
            showDatabaseError("load payments", exception);
        }
    }

    private String getPaymentMethod() {
        if (radioCash.isSelected()) {
            return "CASH";
        }
        if (radioCard.isSelected()) {
            return "CARD";
        }
        return null;
    }

    private Date getPaymentDate() {
        java.util.Date selectedDate =
                (java.util.Date) spnPaymentDate.getValue();
        return new Date(selectedDate.getTime());
    }

    private boolean paymentExists(Connection connection,
            int appointmentId, int excludedPaymentId) throws SQLException {

        String sql = "SELECT COUNT(*) FROM payments "
                + "WHERE appointment_id = ? AND payment_id <> ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, appointmentId);
            statement.setInt(2, excludedPaymentId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0;
            }
        }
    }

    private void clearFields() {
        cmbAppointment.setSelectedIndex(0);
        txtAmount.setText("");
        gpPaymentMethod.clearSelection();
        spnPaymentDate.setValue(new java.util.Date());
        tblPayments.clearSelection();
    }

    private void fillSelectedPayment() {
        int selectedRow = tblPayments.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        int appointmentId = Integer.parseInt(
                tblPayments.getValueAt(selectedRow, 1).toString());
        cmbAppointment.setSelectedItem(
                appointmentDisplays.get(appointmentId));
        txtAmount.setText(
                tblPayments.getValueAt(selectedRow, 5).toString());

        String method = tblPayments.getValueAt(selectedRow, 6).toString();
        radioCash.setSelected("CASH".equals(method));
        radioCard.setSelected("CARD".equals(method));

        java.util.Date paymentDate = (java.util.Date)
                tblPayments.getValueAt(selectedRow, 7);
        spnPaymentDate.setValue(paymentDate);
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

        gpPaymentMethod = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cmbAppointment = new javax.swing.JComboBox<>();
        txtAmount = new javax.swing.JTextField();
        radioCash = new javax.swing.JRadioButton();
        radioCard = new javax.swing.JRadioButton();
        spnPaymentDate = new javax.swing.JSpinner();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPayments = new javax.swing.JTable();
        btnSave = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Payment Management");
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Appointment");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Amount");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Payment Method");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Payment Date");

        cmbAppointment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select an Appointment--", " " }));

        txtAmount.setEditable(false);
        txtAmount.addActionListener(this::txtAmountActionPerformed);

        gpPaymentMethod.add(radioCash);
        radioCash.setText("Cash");

        gpPaymentMethod.add(radioCard);
        radioCard.setText("Card");

        spnPaymentDate.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_YEAR));

        tblPayments.setBackground(new java.awt.Color(0, 153, 204));
        tblPayments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Payment ID", "Appointment ID", "Customer ", "Pet ", "Service", "Amount", "Payment Method", "Payment Date"
            }
        ));
        tblPayments.setShowVerticalLines(true);
        jScrollPane1.setViewportView(tblPayments);

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(57, 57, 57)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel2)
                                            .addComponent(jLabel3))
                                        .addGap(36, 36, 36)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(cmbAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel4)
                                            .addComponent(jLabel5))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(spnPaymentDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(radioCash)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(radioCard)))))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnUpdate)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(42, 42, 42)
                                        .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 649, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(44, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(cmbAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(radioCash)
                            .addComponent(radioCard))
                        .addGap(20, 20, 20)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(spnPaymentDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(78, 78, 78)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(42, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 720, 510));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtAmountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAmountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAmountActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        String appointment = (String) cmbAppointment.getSelectedItem();
        Integer appointmentId = appointmentIds.get(appointment);
        BigDecimal amount = appointmentAmounts.get(appointment);
        String paymentMethod = getPaymentMethod();
        Date paymentDate = getPaymentDate();

        if (appointmentId == null || amount == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment.");
            return;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this,
                    "This appointment does not have any billing items.");
            return;
        }
        if (paymentMethod == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select Cash or Card.");
            return;
        }
        if (paymentDate.toLocalDate().isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(this,
                    "Payment date cannot be in the future.");
            return;
        }

        String sql = "INSERT INTO payments "
                + "(appointment_id, amount, payment_method, payment_date) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            if (paymentExists(connection, appointmentId, 0)) {
                JOptionPane.showMessageDialog(this,
                        "This appointment already has a payment.");
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, appointmentId);
                statement.setBigDecimal(2, amount);
                statement.setString(3, paymentMethod);
                statement.setDate(4, paymentDate);
                statement.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Payment saved successfully.");
            clearFields();
            loadPayments();
        } catch (SQLException exception) {
            showDatabaseError("save payment", exception);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        int selectedRow = tblPayments.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a payment to update.");
            return;
        }

        String appointment = (String) cmbAppointment.getSelectedItem();
        Integer appointmentId = appointmentIds.get(appointment);
        BigDecimal amount = appointmentAmounts.get(appointment);
        String paymentMethod = getPaymentMethod();
        Date paymentDate = getPaymentDate();

        if (appointmentId == null || amount == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment.");
            return;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this,
                    "This appointment does not have any billing items.");
            return;
        }
        if (paymentMethod == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select Cash or Card.");
            return;
        }
        if (paymentDate.toLocalDate().isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(this,
                    "Payment date cannot be in the future.");
            return;
        }

        int paymentId = Integer.parseInt(
                tblPayments.getValueAt(selectedRow, 0).toString());
        String sql = "UPDATE payments SET appointment_id = ?, amount = ?, "
                + "payment_method = ?, payment_date = ? "
                + "WHERE payment_id = ?";

        try (Connection connection = DBConnection.getConnection()) {
            if (paymentExists(connection, appointmentId, paymentId)) {
                JOptionPane.showMessageDialog(this,
                        "This appointment already has another payment.");
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, appointmentId);
                statement.setBigDecimal(2, amount);
                statement.setString(3, paymentMethod);
                statement.setDate(4, paymentDate);
                statement.setInt(5, paymentId);
                statement.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Payment updated successfully.");
            clearFields();
            loadPayments();
        } catch (SQLException exception) {
            showDatabaseError("update payment", exception);
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int selectedRow = tblPayments.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a payment to delete.");
            return;
        }

        int paymentId = Integer.parseInt(
                tblPayments.getValueAt(selectedRow, 0).toString());
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete the selected payment?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM payments WHERE payment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, paymentId);
            statement.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Payment deleted successfully.");
            clearFields();
            loadPayments();
        } catch (SQLException exception) {
            showDatabaseError("delete payment", exception);
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearFields();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        if (openedFromBilling && previousAppointmentId != null) {
            new AppointmentBillingForm(previousAppointmentId)
                    .setVisible(true);
        } else {
            new DashboardForm().setVisible(true);
        }
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
        java.awt.EventQueue.invokeLater(() -> new PaymentForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cmbAppointment;
    private javax.swing.ButtonGroup gpPaymentMethod;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton radioCard;
    private javax.swing.JRadioButton radioCash;
    private javax.swing.JSpinner spnPaymentDate;
    private javax.swing.JTable tblPayments;
    private javax.swing.JTextField txtAmount;
    // End of variables declaration//GEN-END:variables
}
