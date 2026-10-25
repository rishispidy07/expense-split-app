package com.expensesplit.model;

import java.sql.Timestamp;

public class Expense {
    private int id;
    private Integer groupId;
    private int paidBy;
    private String description;
    private double amount;
    private String category;
    private String splitType;
    private int expenseYear;
    private int expenseMonth;
    private Timestamp createdAt;
    private String paidByName;

    public Expense() {}

    public Expense(int id, Integer groupId, int paidBy, String description, double amount, String category, String splitType) {
        this.id = id;
        this.groupId = groupId;
        this.paidBy = paidBy;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.splitType = splitType;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
    public int getPaidBy() { return paidBy; }
    public void setPaidBy(int paidBy) { this.paidBy = paidBy; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSplitType() { return splitType; }
    public void setSplitType(String splitType) { this.splitType = splitType; }
    public int getExpenseYear() { return expenseYear; }
    public void setExpenseYear(int expenseYear) { this.expenseYear = expenseYear; }
    public int getExpenseMonth() { return expenseMonth; }
    public void setExpenseMonth(int expenseMonth) { this.expenseMonth = expenseMonth; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getPaidByName() { return paidByName; }
    public void setPaidByName(String paidByName) { this.paidByName = paidByName; }
}
