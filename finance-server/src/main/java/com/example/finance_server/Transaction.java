package com.example.finance_server;

import jakarta.persistence.*; // DB 연결을 위한 필수 라이브러리

@Entity // 이 클래스는 DB 테이블과 연결된다는 뜻
@Table(name = "transactions") // 연결될 테이블 이름
public class Transaction {

    @Id // 기본키(PK) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 
    private int id; // 내부적으로는 id로 관리 

    private int userId;        // 사용자 ID
    private String date;       // 날짜
    private String type;       // 수입 or 지출
    private double amount;     // 금액
    private String category;   // 카테고리
    private String content;    // 내용

    // 1. JPA용 기본 생성자 (이게 없으면 에러남)
    public Transaction() {
    }

    // 2. 저장용 생성자 (ID 없음)
    public Transaction(int userId, String date, String type, double amount, String category, String content) {
        this.userId = userId;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.content = content;
    }

    // 3. 조회용 생성자 (ID 포함)
    public Transaction(int id, int userId, String date, String type, double amount, String category, String content) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.content = content;
    }


    // 기존 코드 호환용: getTransactionId()를 호출하면 id를 줌
    public int getTransactionId() { return id; }
    
    // JPA 표준 Getter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}