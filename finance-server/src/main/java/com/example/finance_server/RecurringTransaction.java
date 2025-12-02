package com.example.finance_server;

import jakarta.persistence.*;

@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int userId;
    private String type;
    private double amount;
    private String category;
    private String content;
    
    private String startDate; // "yyyy-MM-dd"
    private String endDate;   // "yyyy-MM-dd"

    public RecurringTransaction() {}

    public RecurringTransaction(int userId, String type, double amount, String category, String content, String startDate, String endDate) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}