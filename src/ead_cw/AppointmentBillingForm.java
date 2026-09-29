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
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author kavis
 */
public class AppointmentBillingForm extends javax.swing.JFrame {

    private final Map<String, Integer> appointmentIds = new HashMap<>();
    private final Map<String, String> appointmentServices = new HashMap<>();
    private final Map<Integer, String> appointmentDisplays = new HashMap<>();
    private final Map<String, Integer> itemIds = new HashMap<>();
    private final Map<String, BigDecimal> itemPrices = new HashMap<>();
    private final BillingModel billingModel;
    private final BillingController billingController;
    private final Integer previousAppointmentId;
    private final boolean openedFromTreatment;
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AppointmentBillingForm.class.getName());

    /**
     * Creates new form AppointmentBillingForm
     */
    public AppointmentBillingForm() {
        this(null, false);
    }

    public AppointmentBillingForm(Integer selectedAppointmentId) {
        this(selectedAppointmentId, true);
    }

    private AppointmentBillingForm(Integer selectedAppointmentId,
            boolean cameFromTreatment) {
        previousAppointmentId = selectedAppointmentId;
        openedFromTreatment = cameFromTreatment;
        initComponents();
        billingModel = new BillingModel();
        billingController = new BillingController(billingModel, this);
        setLocationRelativeTo(null);
        tblAppointmentItems.setDefaultEditor(Object.class, null);
        if (UserSession.hasRole("VETERINARIAN")) {
            btnProceedtoPayment.setText("Finish Billing");
        }
        loadAppointments();
        loadItemTypes();

        cmbAppointment.addActionListener(event -> appointmentChanged());
        cmbItemType.addActionListener(event -> loadBillableItems());
        cmbBillableItem.addActionListener(event -> updateUnitPrice());

        clearForm();

        if (selectedAppointmentId != null) {
            String display = appointmentDisplays.get(selectedAppointmentId);
            if (display != null) {
                cmbAppointment.setSelectedItem(display);
            }
        }
    }

    private void loadAppointments() {
        cmbAppointment.removeAllItems();
        cmbAppointment.addItem("-- Select an Appointment --");
        appointmentIds.clear();
        appointmentServices.clear();
        appointmentDisplays.clear();

        boolean filterByVeterinarian =
                UserSession.hasRole("VETERINARIAN")
                && UserSession.getVeterinarianId() != null;

        String sql = "SELECT a.appointment_id, p.pet_name, c.full_name, "
                + "s.service_name, a.appointment_date FROM appointments a "
                + "INNER JOIN pets p ON a.pet_id = p.pet_id "
                + "INNER JOIN customers c ON p.customer_id = c.customer_id "
                + "INNER JOIN services s ON a.service_id = s.service_id "
                + "WHERE a.status <> 'CANCELLED' "
                + "AND EXISTS (SELECT 1 FROM treatments t "
                + "WHERE t.appointment_id = a.appointment_id) "
                + (filterByVeterinarian
                        ? "AND a.veterinarian_id = ? " : "")
                + "ORDER BY a.appointment_date DESC, a.appointment_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (filterByVeterinarian) {
                statement.setInt(1, UserSession.getVeterinarianId());
            }

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int appointmentId = result.getInt("appointment_id");
                    String display = appointmentId + " - "
                            + result.getString("pet_name") + " - "
                            + result.getString("full_name") + " - "
                            + result.getDate("appointment_date");

                    cmbAppointment.addItem(display);
                    appointmentIds.put(display, appointmentId);
                    appointmentServices.put(display,
                            result.getString("service_name"));
                    appointmentDisplays.put(appointmentId, display);
                }
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load appointments: " + exception.getMessage());
        }
    }

    private void loadItemTypes() {
        cmbItemType.removeAllItems();
        cmbItemType.addItem("-- Select an Item Type --");
        cmbItemType.addItem("CONSULTATION");
        cmbItemType.addItem("VACCINE");
        cmbItemType.addItem("MEDICINE");
        cmbItemType.addItem("PROCEDURE");
    }

    private void appointmentChanged() {
        String selected = (String) cmbAppointment.getSelectedItem();
        Integer appointmentId = appointmentIds.get(selected);

        if (appointmentId == null) {
            txtService.setText("");
            billingController.clearItems();
            return;
        }

        txtService.setText(appointmentServices.get(selected));
        loadAppointmentItems(appointmentId);
    }

    private void loadBillableItems() {
        cmbBillableItem.removeAllItems();
        cmbBillableItem.addItem("-- Select a Billable Item --");
        itemIds.clear();
        itemPrices.clear();
        txtUnitPrice.setText("");

        if (cmbItemType.getSelectedIndex() <= 0) {
            return;
        }

        String sql = "SELECT item_id, item_name, unit_price "
                + "FROM billable_items WHERE item_type = ? "
                + "AND active = TRUE ORDER BY item_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cmbItemType.getSelectedItem().toString());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int itemId = result.getInt("item_id");
                    String display = itemId + " - "
                            + result.getString("item_name");

                    cmbBillableItem.addItem(display);
                    itemIds.put(display, itemId);
                    itemPrices.put(display,
                            result.getBigDecimal("unit_price"));
                }
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load billable items: "
                    + exception.getMessage());
        }
    }

    private void updateUnitPrice() {
        String selected = (String) cmbBillableItem.getSelectedItem();
        BigDecimal price = itemPrices.get(selected);
        txtUnitPrice.setText(price == null ? "" : price.toPlainString());
    }

    private void clearBillingTable() {
        DefaultTableModel model =
                (DefaultTableModel) tblAppointmentItems.getModel();
        model.setRowCount(0);
        txtFinalTotal.setText("0.00");
    }

    private void loadAppointmentItems(int appointmentId) {
        try {
            billingController.loadItems(appointmentId);
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load appointment items: "
                    + exception.getMessage());
        }
    }

    public void displayBillingItems(List<BillingItem> items,
            BigDecimal finalTotal) {
        clearBillingTable();
        DefaultTableModel tableModel =
                (DefaultTableModel) tblAppointmentItems.getModel();

        for (BillingItem item : items) {
            tableModel.addRow(new Object[]{
                item.getLineId(),
                item.getItemName(),
                item.getItemType(),
                item.getUnitName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
            });
        }

        txtFinalTotal.setText(finalTotal.toPlainString());
    }

    private void clearItemFields() {
        cmbItemType.setSelectedIndex(0);
        spnQuantity.setValue(1);
        tblAppointmentItems.clearSelection();
    }

    private void clearForm() {
        cmbAppointment.setSelectedIndex(0);
        txtService.setText("");
        clearItemFields();
        clearBillingTable();
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
        cmbAppointment = new javax.swing.JComboBox<>();
        txtService = new javax.swing.JTextField();
        cmbItemType = new javax.swing.JComboBox<>();
        cmbBillableItem = new javax.swing.JComboBox<>();
        txtUnitPrice = new javax.swing.JTextField();
        spnQuantity = new javax.swing.JSpinner();
        btnRemoveItem = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        btnAddItem = new javax.swing.JButton();
        txtFinalTotal = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblAppointmentItems = new javax.swing.JTable();
        btnProceedtoPayment = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Appointment Billing");
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Appointment");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Main Service");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Item type");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Billable Item");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Unit Price");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Quantity");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel8.setText("Final Total");

        cmbAppointment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select an Appointment--" }));

        txtService.setEditable(false);
        txtService.addActionListener(this::txtServiceActionPerformed);

        cmbItemType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select a Item Type--" }));

        cmbBillableItem.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select a Billable Item--" }));

        txtUnitPrice.setEditable(false);

        spnQuantity.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));

        btnRemoveItem.setBackground(new java.awt.Color(184, 218, 252));
        btnRemoveItem.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnRemoveItem.setText("Remove Item");
        btnRemoveItem.addActionListener(this::btnRemoveItemActionPerformed);

        btnClear.setBackground(new java.awt.Color(132, 194, 214));
        btnClear.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnClear.setText("Clear");
        btnClear.addActionListener(this::btnClearActionPerformed);

        btnBack.setBackground(new java.awt.Color(132, 194, 214));
        btnBack.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnBack.setText("Back");
        btnBack.addActionListener(this::btnBackActionPerformed);

        btnAddItem.setBackground(new java.awt.Color(184, 218, 252));
        btnAddItem.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnAddItem.setText("Add Item");
        btnAddItem.addActionListener(this::btnAddItemActionPerformed);

        txtFinalTotal.setEditable(false);
        txtFinalTotal.setBackground(new java.awt.Color(153, 255, 255));
        txtFinalTotal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtFinalTotal.addActionListener(this::txtFinalTotalActionPerformed);

        tblAppointmentItems.setBackground(new java.awt.Color(0, 153, 153));
        tblAppointmentItems.setModel(new javax.swing.table.DefaultTableModel(
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
                "Line ID", "Item", "Type", "Unit", "Quantity", "Unit Price", "Subtotal"
            }
        ));
        tblAppointmentItems.setShowVerticalLines(true);
        jScrollPane1.setViewportView(tblAppointmentItems);

        btnProceedtoPayment.setBackground(new java.awt.Color(204, 255, 0));
        btnProceedtoPayment.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnProceedtoPayment.setText("Proceed to Payment");
        btnProceedtoPayment.addActionListener(this::btnProceedtoPaymentActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel4)
                        .addComponent(jLabel3))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel6)
                        .addComponent(jLabel5)
                        .addComponent(jLabel7)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cmbAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtService, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbItemType, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbBillableItem, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(spnQuantity, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtUnitPrice, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(btnAddItem, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnRemoveItem, javax.swing.GroupLayout.Alignment.LEADING))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28)
                                .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(122, 122, 122))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 640, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(36, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFinalTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(135, 135, 135)
                .addComponent(btnProceedtoPayment)
                .addGap(153, 153, 153))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtService, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbItemType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbBillableItem, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtUnitPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spnQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAddItem, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnRemoveItem, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(46, 46, 46)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(46, 46, 46)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnProceedtoPayment)
                    .addComponent(txtFinalTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtServiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtServiceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtServiceActionPerformed

    private void btnRemoveItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveItemActionPerformed
        String selectedAppointment =
                (String) cmbAppointment.getSelectedItem();
        Integer appointmentId = appointmentIds.get(selectedAppointment);
        int selectedRow = tblAppointmentItems.getSelectedRow();

        if (appointmentId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment.");
            return;
        }
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item from the table.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "Remove the selected item from this appointment?",
                "Confirm Remove",
                JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        int lineId = Integer.parseInt(
                tblAppointmentItems.getValueAt(selectedRow, 0).toString());

        try {
            billingController.removeItem(lineId, appointmentId);
            JOptionPane.showMessageDialog(this,
                    "Item removed successfully.");
        } catch (BillingValidationException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to remove the item: "
                    + exception.getMessage());
        }
    }//GEN-LAST:event_btnRemoveItemActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearForm();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        if (openedFromTreatment && previousAppointmentId != null) {
            new TreatmentForm(previousAppointmentId).setVisible(true);
        } else {
            new DashboardForm().setVisible(true);
        }
        dispose();
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnAddItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddItemActionPerformed
        String selectedAppointment =
                (String) cmbAppointment.getSelectedItem();
        Integer appointmentId = appointmentIds.get(selectedAppointment);
        String selectedItem = (String) cmbBillableItem.getSelectedItem();
        Integer itemId = itemIds.get(selectedItem);
        BigDecimal unitPrice = itemPrices.get(selectedItem);

        if (appointmentId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment.");
            cmbAppointment.requestFocus();
            return;
        }
        if (cmbItemType.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item type.");
            cmbItemType.requestFocus();
            return;
        }
        if (itemId == null || unitPrice == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a billable item.");
            cmbBillableItem.requestFocus();
            return;
        }

        int quantity = (Integer) spnQuantity.getValue();
        try {
            billingController.addItem(
                    appointmentId, itemId, quantity, unitPrice);
            JOptionPane.showMessageDialog(this,
                    "Item added successfully.");
            clearItemFields();
        } catch (BillingValidationException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to add the item: " + exception.getMessage());
        }
    }//GEN-LAST:event_btnAddItemActionPerformed

    private void txtFinalTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFinalTotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFinalTotalActionPerformed

    private void btnProceedtoPaymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProceedtoPaymentActionPerformed
        String selectedAppointment =
                (String) cmbAppointment.getSelectedItem();
        Integer appointmentId = appointmentIds.get(selectedAppointment);

        if (appointmentId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment first.");
            return;
        }

        BigDecimal finalTotal = new BigDecimal(txtFinalTotal.getText());
        if (finalTotal.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Add at least one billing item before proceeding.");
            return;
        }

        if (UserSession.hasRole("VETERINARIAN")) {
            JOptionPane.showMessageDialog(this,
                    "Billing saved successfully. The receptionist can now "
                    + "process the payment.");
            new DashboardForm().setVisible(true);
            dispose();
        } else {
            new PaymentForm(appointmentId).setVisible(true);
            dispose();
        }
    }//GEN-LAST:event_btnProceedtoPaymentActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new AppointmentBillingForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddItem;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnProceedtoPayment;
    private javax.swing.JButton btnRemoveItem;
    private javax.swing.JComboBox<String> cmbAppointment;
    private javax.swing.JComboBox<String> cmbBillableItem;
    private javax.swing.JComboBox<String> cmbItemType;
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
    private javax.swing.JSpinner spnQuantity;
    private javax.swing.JTable tblAppointmentItems;
    private javax.swing.JTextField txtFinalTotal;
    private javax.swing.JTextField txtService;
    private javax.swing.JTextField txtUnitPrice;
    // End of variables declaration//GEN-END:variables
}
