package com.example.finance_server; 

import jakarta.persistence.*;

@Entity
@Table(name = "goals") // 기존 'goals' 테이블과 연결
public class Goal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; // DB 컬럼명 'id'와 일치시킴

    private int userId;
    private String type;
    private String category;
    private double targetAmount;
    private int year;
    private int month;

    /**
     * JPA용 기본 생성자
     */
    public Goal() {
    }

    public Goal(int userId, String type, String category, double targetAmount, int year, int month) {
        this.userId = userId;
        this.type = (type == null || type.trim().isEmpty()) ? null : type;
        this.category = (category == null || "전체".equals(category) || category.trim().isEmpty()) ? null : category;
        this.targetAmount = targetAmount;
        this.year = year;
        this.month = month;
    }

    public Goal(int goalId, int userId, String type, String category, double targetAmount, int year, int month) {
        this(userId, type, category, targetAmount, year, month); // 위 생성자 호출
        this.id = goalId; 
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public double getTargetAmount() { return targetAmount; }
    public int getYear() { return year; }
    public int getMonth() { return month; }

    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setType(String type) { 
        this.type = (type == null || type.trim().isEmpty()) ? null : type; 
    }
    public void setCategory(String category) { 
        this.category = (category == null || "전체".equals(category) || category.trim().isEmpty()) ? null : category; 
    }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }
    public void setYear(int year) { this.year = year; }
    public void setMonth(int month) { this.month = month; }
}