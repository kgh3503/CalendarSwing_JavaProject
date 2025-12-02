package com.example.finance_server; 

import jakarta.persistence.*; 

@Entity
@Table(name = "transactions") // 기존 'transactions' 테이블과 연결
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; // DB 컬럼명 'id'와 일치시킴

    private int userId; // DB의 'user_id' 컬럼과 매칭
    
    private String date; // "yyyy-MM-dd"
    private String type; // "수입" 또는 "지출"
    private double amount;
    private String category;
    private String content;

    /**
     * JPA용 기본 생성자
     */
    public Transaction() {
    }

    // (기존 코드의 생성자들)
    public Transaction(int userId, String date, String type, double amount, String category, String content) {
        this.userId = userId;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.content = content;
    }

    public Transaction(int transactionId, int userId, String date, String type, double amount, String category, String content) {
        this.id = transactionId; // DB 컬럼명 'id'에 맞게 수정
        this.userId = userId;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.content = content;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getDate() { return date; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getContent() { return content; }
    
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setDate(String date) { this.date = date; }
    public void setType(String type) { this.type = type; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setCategory(String category) { this.category = category; }
    public void setContent(String content) { this.content = content; }
}