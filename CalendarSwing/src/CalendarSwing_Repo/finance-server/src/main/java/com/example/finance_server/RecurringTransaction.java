package com.example.finance_server;

import jakarta.persistence.*;

@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int userId;
    private String type;            // "수입" or "지출"
    private double amount;
    private String category;
    private String content;
    private int dayOfMonth;         // 매월 며칠 (1~31)

    public RecurringTransaction() {}

    public RecurringTransaction(int userId, String type, double amount, String category, String content, int dayOfMonth) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.content = content;
        this.dayOfMonth = dayOfMonth;
    }

    // Getters and Setters
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
    public int getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(int dayOfMonth) { this.dayOfMonth = dayOfMonth; }
}