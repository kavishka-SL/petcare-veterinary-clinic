package ead_cw.mvc;

import ead_cw.AppointmentBillingForm;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller class containing appointment billing business rules.
 */
public class BillingController {

    private final BillingModel model;
    private final AppointmentBillingForm view;
    private final BillingDAO billingDAO;

    public BillingController(BillingModel model,
            AppointmentBillingForm view) {
        this.model = model;
        this.view = view;
        this.billingDAO = new BillingDAO();
    }

    public void loadItems(int appointmentId) throws SQLException {
        List<BillingItem> items = billingDAO.getItems(appointmentId);
        model.setItems(items);
        updateView();
    }

    public void clearItems() {
        model.clearItems();
        updateView();
    }

    private void updateView() {
        view.displayBillingItems(model.getItems(), model.getFinalTotal());
    }

    public void addItem(int appointmentId, int itemId, int quantity,
            BigDecimal unitPrice)
            throws SQLException, BillingValidationException {
        validateIdsAndQuantity(appointmentId, itemId, quantity);
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BillingValidationException(
                    "The selected item must have a valid price.");
        }
        if (!billingDAO.hasTreatment(appointmentId)) {
            throw new BillingValidationException(
                    "Save the treatment before adding billing items.");
        }
        ensureAppointmentIsNotPaid(appointmentId);
        billingDAO.addItem(appointmentId, itemId, quantity, unitPrice);
        loadItems(appointmentId);
    }

    public void removeItem(int lineId, int appointmentId)
            throws SQLException, BillingValidationException {
        if (lineId <= 0 || appointmentId <= 0) {
            throw new BillingValidationException(
                    "Please select a valid billing item.");
        }
        ensureAppointmentIsNotPaid(appointmentId);
        billingDAO.removeItem(lineId, appointmentId);
        loadItems(appointmentId);
    }

    private void validateIdsAndQuantity(int appointmentId, int itemId,
            int quantity) throws BillingValidationException {
        if (appointmentId <= 0 || itemId <= 0) {
            throw new BillingValidationException(
                    "Please select a valid appointment and billing item.");
        }
        if (quantity <= 0) {
            throw new BillingValidationException(
                    "Quantity must be greater than zero.");
        }
    }

    private void ensureAppointmentIsNotPaid(int appointmentId)
            throws SQLException, BillingValidationException {
        if (billingDAO.hasPayment(appointmentId)) {
            throw new BillingValidationException(
                    "This appointment is already paid. "
                    + "Its billing items cannot be changed.");
        }
    }
}
