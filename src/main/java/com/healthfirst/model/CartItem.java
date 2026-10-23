package com.healthfirst.model;

import java.math.BigDecimal;

public class CartItem {
    private Medicine medicine;
    private int quantity;
    private BigDecimal unitPrice;

    public CartItem(Medicine medicine, int quantity) {
        this.medicine = medicine;
        this.quantity = quantity;
        this.unitPrice = medicine.getPrice();
    }

    public CartItem(Medicine medicine, int quantity, BigDecimal unitPrice) {
        this.medicine = medicine;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
