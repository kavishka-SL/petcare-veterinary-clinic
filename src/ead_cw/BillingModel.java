package ead_cw;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model class that stores the billing data for the selected appointment.
 */
public class BillingModel {

    private List<BillingItem> items = new ArrayList<>();

    public List<BillingItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void setItems(List<BillingItem> items) {
        this.items = new ArrayList<>(items);
    }

    public void clearItems() {
        items.clear();
    }

    public BigDecimal getFinalTotal() {
        return items.stream()
                .map(BillingItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
