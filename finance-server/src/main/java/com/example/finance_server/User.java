package com.example.finance_server;

import jakarta.persistence.*; 

@Entity
@Table(name = "users") // 기존 DB의 'users' 테이블과 연결
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    public User() {
    }
    
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }
    
    public User(int userId, String username, String passwordHash) {
        this.id = userId; 
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }

    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}