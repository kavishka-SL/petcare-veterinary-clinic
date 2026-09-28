package ead_cw;

import java.math.BigDecimal;

/**
 * Model class representing one item charged to an appointment.
 */
public class BillingItem {

    private final int lineId;
    private final String itemName;
    private final String itemType;
    private final String unitName;
    private final int quantity;
    private final BigDecimal unitPrice;

    public BillingItem(int lineId, String itemName, String itemType,
            String unitName, int quantity, BigDecimal unitPrice) {
        this.lineId = lineId;
        this.itemName = itemName;
        this.itemType = itemType;
        this.unitName = unitName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getLineId() {
        return lineId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemType() {
        return itemType;
    }

    public String getUnitName() {
        return unitName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
