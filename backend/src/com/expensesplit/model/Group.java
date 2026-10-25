package com.expensesplit.model;

import java.sql.Timestamp;

public class Group {
    private int id;
    private String name;
    private String description;
    private int createdBy;
    private double walletBalance;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Group() {}

    public Group(int id, String name, String description, int createdBy, double walletBalance) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.walletBalance = walletBalance;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(double walletBalance) { this.walletBalance = walletBalance; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
