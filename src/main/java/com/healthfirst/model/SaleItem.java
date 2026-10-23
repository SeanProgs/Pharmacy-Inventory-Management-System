package com.healthfirst.model;

import java.math.BigDecimal;

public class SaleItem {
    private int saleItemId;
    private int saleId;
    private int medicineId;
    private int quantitySold;
    private BigDecimal priceAtSale;
    private String medicineName; // Joined for display

    public SaleItem() {}

    public SaleItem(int saleItemId, int saleId, int medicineId, int quantitySold, BigDecimal priceAtSale) {
        this.saleItemId = saleItemId;
        this.saleId = saleId;
        this.medicineId = medicineId;
        this.quantitySold = quantitySold;
        this.priceAtSale = priceAtSale;
    }

    public int getSaleItemId() {
        return saleItemId;
    }

    public void setSaleItemId(int saleItemId) {
        this.saleItemId = saleItemId;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }

    public BigDecimal getPriceAtSale() {
        return priceAtSale;
    }

    public void setPriceAtSale(BigDecimal priceAtSale) {
        this.priceAtSale = priceAtSale;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public BigDecimal getSubtotal() {
        if (priceAtSale == null) return BigDecimal.ZERO;
        return priceAtSale.multiply(BigDecimal.valueOf(quantitySold));
    }
}
