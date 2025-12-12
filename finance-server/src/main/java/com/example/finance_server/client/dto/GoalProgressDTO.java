package com.example.finance_server.client.dto;
public class GoalProgressDTO {

    // 서버의 DTO 필드명과 일치
    private Goal goal; 
    private double currentProgress;
    private double achievementRate;

    // Getters (GoalView가 이 메소드를 호출하여 값을 꺼냄)

    public Goal getGoal() {
        return goal;
    }

    public double getCurrentProgress() {
        return currentProgress;
    }

    public double getAchievementRate() {
        return achievementRate;
    }
    
}