/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ead_cw;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author kavis
 */
public class PriceScheduleForm extends javax.swing.JFrame {

    private final Map<String, Integer> serviceIds = new HashMap<>();
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PriceScheduleForm.class.getName());

    /**
     * Creates new form PriceScheduleForm
     */
    public PriceScheduleForm() {
        initComponents();
        setLocationRelativeTo(null);
        tblPriceSchedule.setDefaultEditor(Object.class, null);
        loadServices();
        loadItemTypes();
        loadPriceSchedule();
        clearFields();

        tblPriceSchedule.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                fillSelectedItem();
            }
        });
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
            JOptionPane.showMessageDialog(this,
                    "Unable to load services: " + exception.getMessage());
        }
    }

    private void loadItemTypes() {
        cmbItemType.removeAllItems();
        cmbItemType.addItem("-- Select Item Type --");
        cmbItemType.addItem("CONSULTATION");
        cmbItemType.addItem("VACCINE");
        cmbItemType.addItem("MEDICINE");
        cmbItemType.addItem("PROCEDURE");
    }

    private void loadPriceSchedule() {
        DefaultTableModel model =
                (DefaultTableModel) tblPriceSchedule.getModel();
        model.setRowCount(0);

        String sql = "SELECT b.item_id, b.service_id, s.service_name, "
                + "b.item_type, b.item_name, b.unit_name, "
                + "b.unit_price, b.active FROM billable_items b "
                + "INNER JOIN services s ON b.service_id = s.service_id "
                + "ORDER BY b.item_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                String service = result.getInt("service_id") + " - "
                        + result.getString("service_name");

                model.addRow(new Object[]{
                    result.getInt("item_id"),
                    service,
                    result.getString("item_type"),
                    result.getString("item_name"),
                    result.getString("unit_name"),
                    result.getBigDecimal("unit_price"),
                    result.getBoolean("active") ? "Yes" : "No"
                });
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load the price schedule: "
                    + exception.getMessage());
        }
    }

    private boolean validateFields() {
        if (cmbService.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a service.");
            cmbService.requestFocus();
            return false;
        }

        if (cmbItemType.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Please select an item type.");
            cmbItemType.requestFocus();
            return false;
        }

        String itemName = txtItemName.getText().trim();
        if (itemName.length() < 2 || itemName.length() > 100) {
            JOptionPane.showMessageDialog(this,
                    "Item name must contain 2 to 100 characters.");
            txtItemName.requestFocus();
            return false;
        }

        String unitName = txtUnitName.getText().trim();
        if (unitName.isEmpty() || unitName.length() > 30) {
            JOptionPane.showMessageDialog(this,
                    "Unit name is required and cannot exceed 30 characters.");
            txtUnitName.requestFocus();
            return false;
        }

        try {
            BigDecimal price = new BigDecimal(txtUnitPrice.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this,
                        "Unit price cannot be negative.");
                txtUnitPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid unit price, for example 2500.00.");
            txtUnitPrice.requestFocus();
            return false;
        }

        return true;
    }

    private void clearFields() {
        cmbService.setSelectedIndex(0);
        cmbItemType.setSelectedIndex(0);
        txtItemName.setText("");
        txtUnitName.setText("");
        txtUnitPrice.setText("");
        chkActive.setSelected(true);
        tblPriceSchedule.clearSelection();
        cmbService.requestFocus();
    }

    private void fillSelectedItem() {
        int selectedRow = tblPriceSchedule.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        cmbService.setSelectedItem(
                tblPriceSchedule.getValueAt(selectedRow, 1).toString());
        cmbItemType.setSelectedItem(
                tblPriceSchedule.getValueAt(selectedRow, 2).toString());
        txtItemName.setText(
                tblPriceSchedule.getValueAt(selectedRow, 3).toString());
        txtUnitName.setText(
                tblPriceSchedule.getValueAt(selectedRow, 4).toString());
        txtUnitPrice.setText(
                tblPriceSchedule.getValueAt(selectedRow, 5).toString());
        chkActive.setSelected("Yes".equals(
                tblPriceSchedule.getValueAt(selectedRow, 6).toString()));
    }

    private Integer getSelectedItemId() {
        int selectedRow = tblPriceSchedule.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item from the table.");
            return null;
        }
        return Integer.valueOf(
                tblPriceSchedule.getValueAt(selectedRow, 0).toString());
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
        cmbService = new javax.swing.JComboBox<>();
        cmbItemType = new javax.swing.JComboBox<>();
        txtItemName = new javax.swing.JTextField();
        txtUnitName = new javax.swing.JTextField();
        txtUnitPrice = new javax.swing.JTextField();
        chkActive = new javax.swing.JCheckBox();
        btnDeactivate = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPriceSchedule = new javax.swing.JTable();
        btnDelete = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Price Schedule");
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Service");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Item Type");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Item Name");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Unit Name");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Unit Price");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Active");

        cmbService.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select a Service--", " " }));

        cmbItemType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select Item Type--" }));
        cmbItemType.addActionListener(this::cmbItemTypeActionPerformed);

        txtItemName.addActionListener(this::txtItemNameActionPerformed);

        txtUnitName.addActionListener(this::txtUnitNameActionPerformed);

        txtUnitPrice.addActionListener(this::txtUnitPriceActionPerformed);

        chkActive.addActionListener(this::chkActiveActionPerformed);

        btnDeactivate.setBackground(new java.awt.Color(102, 153, 255));
        btnDeactivate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnDeactivate.setText("Deactivate");
        btnDeactivate.addActionListener(this::btnDeactivateActionPerformed);

        btnClear.setBackground(new java.awt.Color(132, 194, 214));
        btnClear.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnClear.setText("Clear");
        btnClear.addActionListener(this::btnClearActionPerformed);

        btnBack.setBackground(new java.awt.Color(132, 194, 214));
        btnBack.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnBack.setText("Back");
        btnBack.addActionListener(this::btnBackActionPerformed);

        btnAdd.setBackground(new java.awt.Color(184, 218, 252));
        btnAdd.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnAdd.setText("Add");
        btnAdd.addActionListener(this::btnAddActionPerformed);

        btnUpdate.setBackground(new java.awt.Color(184, 218, 252));
        btnUpdate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnUpdate.setText("Update");
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);

        tblPriceSchedule.setBackground(new java.awt.Color(0, 204, 255));
        tblPriceSchedule.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Item ID", "Service", "Item Type", "Item Name", "Unit", "Unit Price", "Active"
            }
        ));
        tblPriceSchedule.setShowVerticalLines(true);
        jScrollPane1.setViewportView(tblPriceSchedule);

        btnDelete.setBackground(new java.awt.Color(184, 218, 252));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtItemName, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUnitPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkActive)
                    .addComponent(txtUnitName, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbService, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbItemType, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addGap(154, 154, 154))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 633, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(cmbService, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(cmbItemType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtItemName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtUnitName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtUnitPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
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
                            .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(1, 1, 1)))
                .addGap(31, 31, 31)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(66, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 680, 550));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbItemTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbItemTypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbItemTypeActionPerformed

    private void txtItemNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtItemNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtItemNameActionPerformed

    private void txtUnitNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUnitNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtUnitNameActionPerformed

    private void txtUnitPriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUnitPriceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtUnitPriceActionPerformed

    private void chkActiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkActiveActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chkActiveActionPerformed

    private void btnDeactivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeactivateActionPerformed
        Integer itemId = getSelectedItemId();
        if (itemId == null) {
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "Deactivate the selected price item?",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "UPDATE billable_items SET active = FALSE "
                + "WHERE item_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, itemId);
            statement.executeUpdate();
            JOptionPane.showMessageDialog(this,
                    "Price item deactivated successfully.");
            loadPriceSchedule();
            clearFields();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to deactivate the item: "
                    + exception.getMessage());
        }
    }//GEN-LAST:event_btnDeactivateActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearFields();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        new DashboardForm().setVisible(true);
        dispose();
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        if (!validateFields()) {
            return;
        }

        String selectedService = cmbService.getSelectedItem().toString();
        int serviceId = serviceIds.get(selectedService);
        String sql = "INSERT INTO billable_items "
                + "(service_id, item_type, item_name, unit_name, "
                + "unit_price, active) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, serviceId);
            statement.setString(2, cmbItemType.getSelectedItem().toString());
            statement.setString(3, txtItemName.getText().trim());
            statement.setString(4, txtUnitName.getText().trim());
            statement.setBigDecimal(5,
                    new BigDecimal(txtUnitPrice.getText().trim()));
            statement.setBoolean(6, chkActive.isSelected());
            statement.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Price item added successfully.");
            loadPriceSchedule();
            clearFields();
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(this,
                        "An item with this name already exists.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Unable to add the item: "
                        + exception.getMessage());
            }
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        Integer itemId = getSelectedItemId();
        if (itemId == null || !validateFields()) {
            return;
        }

        String selectedService = cmbService.getSelectedItem().toString();
        int serviceId = serviceIds.get(selectedService);
        String sql = "UPDATE billable_items SET service_id = ?, "
                + "item_type = ?, item_name = ?, unit_name = ?, "
                + "unit_price = ?, active = ? WHERE item_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, serviceId);
            statement.setString(2, cmbItemType.getSelectedItem().toString());
            statement.setString(3, txtItemName.getText().trim());
            statement.setString(4, txtUnitName.getText().trim());
            statement.setBigDecimal(5,
                    new BigDecimal(txtUnitPrice.getText().trim()));
            statement.setBoolean(6, chkActive.isSelected());
            statement.setInt(7, itemId);
            statement.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Price item updated successfully.");
            loadPriceSchedule();
            clearFields();
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(this,
                        "An item with this name already exists.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Unable to update the item: "
                        + exception.getMessage());
            }
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        Integer itemId = getSelectedItemId();
        if (itemId == null) {
            return;
        }

        String checkSql = "SELECT COUNT(*) FROM appointment_items "
                + "WHERE item_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement checkStatement =
                     connection.prepareStatement(checkSql)) {

            checkStatement.setInt(1, itemId);
            try (ResultSet result = checkStatement.executeQuery()) {
                result.next();
                if (result.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this,
                            "This item is already used in an appointment bill. "
                            + "Please deactivate it instead.");
                    return;
                }
            }

            int answer = JOptionPane.showConfirmDialog(this,
                    "Permanently delete the selected price item?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (answer != JOptionPane.YES_OPTION) {
                return;
            }

            String deleteSql = "DELETE FROM billable_items WHERE item_id = ?";
            try (PreparedStatement deleteStatement =
                    connection.prepareStatement(deleteSql)) {
                deleteStatement.setInt(1, itemId);
                deleteStatement.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Price item deleted successfully.");
            loadPriceSchedule();
            clearFields();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to delete the item: " + exception.getMessage());
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
        java.awt.EventQueue.invokeLater(() -> new PriceScheduleForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDeactivate;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JCheckBox chkActive;
    private javax.swing.JComboBox<String> cmbItemType;
    private javax.swing.JComboBox<String> cmbService;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblPriceSchedule;
    private javax.swing.JTextField txtItemName;
    private javax.swing.JTextField txtUnitName;
    private javax.swing.JTextField txtUnitPrice;
    // End of variables declaration//GEN-END:variables
}
