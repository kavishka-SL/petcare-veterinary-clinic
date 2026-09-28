/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ead_cw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author kavis
 */
public class PetForm extends javax.swing.JFrame {

    private final Map<String, Integer> ownerIds = new HashMap<>();
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PetForm.class.getName());

    /**
     * Creates new form PetForm
     */
    public PetForm() {
        initComponents();
        txtDateofBirth.setToolTipText("Example: 2024-05-20 (yyyy-MM-dd)");
        tblPets.setDefaultEditor(Object.class, null);
        loadOwners();
        loadPets();

        tblPets.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                fillSelectedPet();
            }
        });
    }

    private void loadOwners() {
        cmbOwner.removeAllItems();
        cmbOwner.addItem("-- Select Owner --");
        ownerIds.clear();

        String sql = "SELECT customer_id, full_name "
                + "FROM customers ORDER BY full_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                int customerId = result.getInt("customer_id");
                String displayName = customerId + " - "
                        + result.getString("full_name");

                cmbOwner.addItem(displayName);
                ownerIds.put(displayName, customerId);
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load owners: " + exception.getMessage());
        }
    }

    private void loadPets() {
        DefaultTableModel model = (DefaultTableModel) tblPets.getModel();
        model.setRowCount(0);

        String sql = "SELECT p.pet_id, p.customer_id, c.full_name AS owner_name, "
                + "p.pet_name, p.species, p.breed, p.date_of_birth, p.sex "
                + "FROM pets p INNER JOIN customers c "
                + "ON p.customer_id = c.customer_id ORDER BY p.pet_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                String owner = result.getInt("customer_id") + " - "
                        + result.getString("owner_name");

                model.addRow(new Object[]{
                    result.getInt("pet_id"),
                    owner,
                    result.getString("pet_name"),
                    result.getString("species"),
                    result.getString("breed"),
                    result.getDate("date_of_birth"),
                    result.getString("sex")
                });
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load pets: " + exception.getMessage());
        }
    }

    private String getSelectedSex() {
        if (radioMale.isSelected()) {
            return "MALE";
        }
        if (radioFemale.isSelected()) {
            return "FEMALE";
        }
        return null;
    }

    private java.sql.Date readDateOfBirth() {
        String dateText = txtDateofBirth.getText().trim();
        if (dateText.isEmpty()) {
            return null;
        }
        return java.sql.Date.valueOf(dateText);
    }

    private void clearFields() {
        cmbOwner.setSelectedIndex(0);
        txtPetName.setText("");
        txtSpecies.setText("");
        txtBreed.setText("");
        txtDateofBirth.setText("");
        btnGroup.clearSelection();
        tblPets.clearSelection();
        txtPetName.requestFocus();
    }

    private void fillSelectedPet() {
        int selectedRow = tblPets.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        cmbOwner.setSelectedItem(
                tblPets.getValueAt(selectedRow, 1).toString());
        txtPetName.setText(
                tblPets.getValueAt(selectedRow, 2).toString());
        txtSpecies.setText(
                tblPets.getValueAt(selectedRow, 3).toString());

        Object breed = tblPets.getValueAt(selectedRow, 4);
        txtBreed.setText(breed == null ? "" : breed.toString());

        Object dateOfBirth = tblPets.getValueAt(selectedRow, 5);
        txtDateofBirth.setText(
                dateOfBirth == null ? "" : dateOfBirth.toString());

        String sex = tblPets.getValueAt(selectedRow, 6).toString();
        radioMale.setSelected("MALE".equals(sex));
        radioFemale.setSelected("FEMALE".equals(sex));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        cmbOwner = new javax.swing.JComboBox<>();
        radioMale = new javax.swing.JRadioButton();
        radioFemale = new javax.swing.JRadioButton();
        txtSpecies = new javax.swing.JTextField();
        txtPetName = new javax.swing.JTextField();
        txtDateofBirth = new javax.swing.JTextField();
        txtBreed = new javax.swing.JTextField();
        btnDelete = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPets = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Pet Management");
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Owner");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Pet Name");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Species");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Breed");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Date of Birth");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Sex");

        cmbOwner.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--Select the Owner--" }));

        btnGroup.add(radioMale);
        radioMale.setText("Male");
        radioMale.addActionListener(this::radioMaleActionPerformed);

        btnGroup.add(radioFemale);
        radioFemale.setText("Female");
        radioFemale.addActionListener(this::radioFemaleActionPerformed);

        txtSpecies.addActionListener(this::txtSpeciesActionPerformed);

        txtPetName.addActionListener(this::txtPetNameActionPerformed);

        txtDateofBirth.setToolTipText("dd-mm-yyyy");

        btnDelete.setBackground(new java.awt.Color(184, 218, 252));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

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

        tblPets.setModel(new javax.swing.table.DefaultTableModel(
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
                "Pet ID", "Owner", "Pet Name ", "Species", "Breed", "Date of Birth", "Sex"
            }
        ));
        tblPets.setShowGrid(false);
        tblPets.setShowVerticalLines(true);
        jScrollPane1.setViewportView(tblPets);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPetName))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(55, 55, 55)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(txtBreed, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(txtSpecies)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtDateofBirth, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(radioMale)
                                        .addGap(18, 18, 18)
                                        .addComponent(radioFemale))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(55, 55, 55)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbOwner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(98, 98, 98))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 597, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(50, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(cmbOwner, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtPetName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtSpecies, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtBreed, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtDateofBirth, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(radioMale)
                    .addComponent(radioFemale))
                .addGap(51, 51, 51)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(129, 129, 129))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 680, 560));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearFields();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        DashboardForm dashboard = new DashboardForm();
        dashboard.setLocationRelativeTo(null);
        dashboard.setVisible(true);
        dispose();
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        String owner = (String) cmbOwner.getSelectedItem();
        Integer ownerId = ownerIds.get(owner);
        String petName = txtPetName.getText().trim();
        String species = txtSpecies.getText().trim();
        String breed = txtBreed.getText().trim();
        String sex = getSelectedSex();

        if (ownerId == null || petName.isEmpty() || species.isEmpty()
                || sex == null) {
            JOptionPane.showMessageDialog(this,
                    "Owner, pet name, species and sex are required.");
            return;
        }

        java.sql.Date dateOfBirth;
        try {
            dateOfBirth = readDateOfBirth();
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this,
                    "Use yyyy-MM-dd for the date of birth.");
            return;
        }

        String sql = "INSERT INTO pets "
                + "(customer_id, pet_name, species, breed, date_of_birth, sex) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, ownerId);
            statement.setString(2, petName);
            statement.setString(3, species);
            statement.setString(4, breed);
            if (dateOfBirth == null) {
                statement.setNull(5, Types.DATE);
            } else {
                statement.setDate(5, dateOfBirth);
            }
            statement.setString(6, sex);
            statement.executeUpdate();

            JOptionPane.showMessageDialog(this, "Pet added successfully.");
            clearFields();
            loadPets();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to add pet: " + exception.getMessage());
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        int selectedRow = tblPets.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a pet to update.");
            return;
        }

        String owner = (String) cmbOwner.getSelectedItem();
        Integer ownerId = ownerIds.get(owner);
        String petName = txtPetName.getText().trim();
        String species = txtSpecies.getText().trim();
        String breed = txtBreed.getText().trim();
        String sex = getSelectedSex();

        if (ownerId == null || petName.isEmpty() || species.isEmpty()
                || sex == null) {
            JOptionPane.showMessageDialog(this,
                    "Owner, pet name, species and sex are required.");
            return;
        }

        java.sql.Date dateOfBirth;
        try {
            dateOfBirth = readDateOfBirth();
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this,
                    "Use yyyy-MM-dd for the date of birth.");
            return;
        }

        int petId = Integer.parseInt(
                tblPets.getValueAt(selectedRow, 0).toString());
        String sql = "UPDATE pets SET customer_id = ?, pet_name = ?, "
                + "species = ?, breed = ?, date_of_birth = ?, sex = ? "
                + "WHERE pet_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, ownerId);
            statement.setString(2, petName);
            statement.setString(3, species);
            statement.setString(4, breed);
            if (dateOfBirth == null) {
                statement.setNull(5, Types.DATE);
            } else {
                statement.setDate(5, dateOfBirth);
            }
            statement.setString(6, sex);
            statement.setInt(7, petId);
            statement.executeUpdate();

            JOptionPane.showMessageDialog(this, "Pet updated successfully.");
            clearFields();
            loadPets();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to update pet: " + exception.getMessage());
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int selectedRow = tblPets.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a pet to delete.");
            return;
        }

        int petId = Integer.parseInt(
                tblPets.getValueAt(selectedRow, 0).toString());
        String petName = tblPets.getValueAt(selectedRow, 2).toString();

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete pet " + petName + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM pets WHERE pet_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, petId);
            statement.executeUpdate();

            JOptionPane.showMessageDialog(this, "Pet deleted successfully.");
            clearFields();
            loadPets();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Unable to delete pet: " + exception.getMessage());
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void txtPetNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPetNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPetNameActionPerformed

    private void txtSpeciesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSpeciesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSpeciesActionPerformed

    private void radioFemaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioFemaleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_radioFemaleActionPerformed

    private void radioMaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioMaleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_radioMaleActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new PetForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDelete;
    private javax.swing.ButtonGroup btnGroup;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cmbOwner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton radioFemale;
    private javax.swing.JRadioButton radioMale;
    private javax.swing.JTable tblPets;
    private javax.swing.JTextField txtBreed;
    private javax.swing.JTextField txtDateofBirth;
    private javax.swing.JTextField txtPetName;
    private javax.swing.JTextField txtSpecies;
    // End of variables declaration//GEN-END:variables
}
