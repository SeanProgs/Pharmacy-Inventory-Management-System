package com.healthfirst.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Sale {
    private int saleId;
    private LocalDateTime saleDate;
    private BigDecimal totalAmount;
    private int userId;
    private String cashierName; // Joined for display

    public Sale() {}

    public Sale(int saleId, LocalDateTime saleDate, BigDecimal totalAmount, int userId) {
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.userId = userId;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }
}
