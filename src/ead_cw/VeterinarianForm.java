/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ead_cw;

import ead_cw.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author kavis
 */
public class VeterinarianForm extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VeterinarianForm.class.getName());

    /**
     * Creates new form VeterinarianForm
     */
    public VeterinarianForm() {
        initComponents();
        setLocationRelativeTo(null);
        tblVeterinarians.setDefaultEditor(Object.class, null);
        loadVeterinarians();
        clearFields();

        tblVeterinarians.getSelectionModel()
                .addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                fillSelectedVeterinarian();
            }
        });
    }

    private void loadVeterinarians() {
        DefaultTableModel model =
                (DefaultTableModel) tblVeterinarians.getModel();
        model.setRowCount(0);

        String sql = "SELECT veterinarian_id, full_name, phone, email, "
                + "specialization, active FROM veterinarians "
                + "ORDER BY veterinarian_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                model.addRow(new Object[]{
                    result.getInt("veterinarian_id"),
                    result.getString("full_name"),
                    result.getString("phone"),
                    result.getString("email"),
                    result.getString("specialization"),
                    result.getBoolean("active") ? "Yes" : "No"
                });
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load veterinarians: "
                    + exception.getMessage());
        }
    }

    private boolean validateFields() {
        String fullName = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String specialization = txtSpecialization.getText().trim();

        if (!fullName.matches("^[\\p{L} .'-]{2,100}$")) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid full name using 2 to 100 characters.");
            txtFullName.requestFocus();
            return false;
        }
        if (!phone.matches("^0\\d{9}$")) {
            JOptionPane.showMessageDialog(this,
                    "Phone number must start with 0 and contain 10 digits.");
            txtPhone.requestFocus();
            return false;
        }
        if (!email.isEmpty()
                && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid email address or leave it empty.");
            txtEmail.requestFocus();
            return false;
        }
        if (specialization.length() < 2
                || specialization.length() > 100) {
            JOptionPane.showMessageDialog(this,
                    "Specialization must contain 2 to 100 characters.");
            txtSpecialization.requestFocus();
            return false;
        }
        return true;
    }

    private void clearFields() {
        txtFullName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtSpecialization.setText("");
        chkActive.setSelected(true);
        tblVeterinarians.clearSelection();
        txtFullName.requestFocus();
    }

    private void fillSelectedVeterinarian() {
        int selectedRow = tblVeterinarians.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        txtFullName.setText(
                tblVeterinarians.getValueAt(selectedRow, 1).toString());
        txtPhone.setText(
                tblVeterinarians.getValueAt(selectedRow, 2).toString());

        Object email = tblVeterinarians.getValueAt(selectedRow, 3);
        txtEmail.setText(email == null ? "" : email.toString());

        txtSpecialization.setText(
                tblVeterinarians.getValueAt(selectedRow, 4).toString());
        chkActive.setSelected("Yes".equals(
                tblVeterinarians.getValueAt(selectedRow, 5).toString()));
    }

    private Integer getSelectedVeterinarianId() {
        int selectedRow = tblVeterinarians.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a veterinarian from the table.");
            return null;
        }
        return Integer.valueOf(
                tblVeterinarians.getValueAt(selectedRow, 0).toString());
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
        txtEmail = new javax.swing.JTextField();
        txtSpecialization = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtFullName = new javax.swing.JTextField();
        txtPhone = new javax.swing.JTextField();
        chkActive = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        btnUpdate = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnDeactivate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblVeterinarians = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtSpecialization.addActionListener(this::txtSpecializationActionPerformed);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Full Name");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Phone ");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Email");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Specialization");

        txtFullName.addActionListener(this::txtFullNameActionPerformed);

        chkActive.addActionListener(this::chkActiveActionPerformed);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Active");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Veterinarian");
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        btnUpdate.setBackground(new java.awt.Color(184, 218, 252));
        btnUpdate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnUpdate.setText("Update");
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);

        btnAdd.setBackground(new java.awt.Color(184, 218, 252));
        btnAdd.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnAdd.setText("Add");
        btnAdd.addActionListener(this::btnAddActionPerformed);

        btnBack.setBackground(new java.awt.Color(132, 194, 214));
        btnBack.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnBack.setText("Back");
        btnBack.addActionListener(this::btnBackActionPerformed);

        btnClear.setBackground(new java.awt.Color(132, 194, 214));
        btnClear.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnClear.setText("Clear");
        btnClear.addActionListener(this::btnClearActionPerformed);

        btnDeactivate.setBackground(new java.awt.Color(102, 153, 255));
        btnDeactivate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnDeactivate.setText("Deactivate");
        btnDeactivate.addActionListener(this::btnDeactivateActionPerformed);

        btnDelete.setBackground(new java.awt.Color(184, 218, 252));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

        tblVeterinarians.setBackground(new java.awt.Color(0, 204, 204));
        tblVeterinarians.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Vet ID", "Full Name", "Phone", "Email", "Specialization", "Active"
            }
        ));
        tblVeterinarians.setShowVerticalLines(true);
        jScrollPane1.setViewportView(tblVeterinarians);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtFullName, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(chkActive))
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSpecialization)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdate)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnDelete)
                    .addComponent(btnDeactivate))
                .addGap(83, 83, 83))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 616, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(38, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtFullName, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtSpecialization, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6)
                            .addComponent(chkActive)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnDeactivate, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 680, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtFullNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFullNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFullNameActionPerformed

    private void txtSpecializationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSpecializationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSpecializationActionPerformed

    private void chkActiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkActiveActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chkActiveActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        Integer veterinarianId = getSelectedVeterinarianId();
        if (veterinarianId == null || !validateFields()) {
            return;
        }

        String sql = "UPDATE veterinarians SET full_name = ?, phone = ?, "
                + "email = ?, specialization = ?, active = ? "
                + "WHERE veterinarian_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, txtFullName.getText().trim());
            statement.setString(2, txtPhone.getText().trim());
            statement.setString(3, txtEmail.getText().trim().isEmpty()
                    ? null : txtEmail.getText().trim());
            statement.setString(4, txtSpecialization.getText().trim());
            statement.setBoolean(5, chkActive.isSelected());
            statement.setInt(6, veterinarianId);
            statement.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Veterinarian updated successfully.");
            loadVeterinarians();
            clearFields();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to update veterinarian: "
                    + exception.getMessage());
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if (!validateFields()) {
            return;
        }

        String sql = "INSERT INTO veterinarians "
                + "(full_name, phone, email, specialization, active) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, txtFullName.getText().trim());
            statement.setString(2, txtPhone.getText().trim());
            statement.setString(3, txtEmail.getText().trim().isEmpty()
                    ? null : txtEmail.getText().trim());
            statement.setString(4, txtSpecialization.getText().trim());
            statement.setBoolean(5, chkActive.isSelected());
            statement.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Veterinarian added successfully.");
            loadVeterinarians();
            clearFields();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to add veterinarian: "
                    + exception.getMessage());
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        new DashboardForm().setVisible(true);
        dispose();
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearFields();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnDeactivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeactivateActionPerformed
        Integer veterinarianId = getSelectedVeterinarianId();
        if (veterinarianId == null) {
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "Deactivate the selected veterinarian?",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "UPDATE veterinarians SET active = FALSE "
                + "WHERE veterinarian_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, veterinarianId);
            statement.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Veterinarian deactivated successfully.");
            loadVeterinarians();
            clearFields();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to deactivate veterinarian: "
                    + exception.getMessage());
        }
    }//GEN-LAST:event_btnDeactivateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        Integer veterinarianId = getSelectedVeterinarianId();
        if (veterinarianId == null) {
            return;
        }

        String checkSql = "SELECT COUNT(*) FROM appointments "
                + "WHERE veterinarian_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement checkStatement =
                     connection.prepareStatement(checkSql)) {
            checkStatement.setInt(1, veterinarianId);
            try (ResultSet result = checkStatement.executeQuery()) {
                result.next();
                if (result.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this,
                            "This veterinarian already has appointments. "
                            + "Please deactivate the record instead.");
                    return;
                }
            }

            int answer = JOptionPane.showConfirmDialog(this,
                    "Permanently delete the selected veterinarian?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }

            String deleteSql = "DELETE FROM veterinarians "
                    + "WHERE veterinarian_id = ?";
            try (PreparedStatement deleteStatement =
                    connection.prepareStatement(deleteSql)) {
                deleteStatement.setInt(1, veterinarianId);
                deleteStatement.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Veterinarian deleted successfully.");
            loadVeterinarians();
            clearFields();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to delete veterinarian: "
                    + exception.getMessage());
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new VeterinarianForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDeactivate;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JCheckBox chkActive;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblVeterinarians;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFullName;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtSpecialization;
    // End of variables declaration//GEN-END:variables
}
