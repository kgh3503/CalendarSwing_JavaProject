package com.example.finance_server; 
public class GoalProgressDTO {

    // 1. 원본 목표 객체
    private Goal goal; 
    
    // 2. 서버가 계산한 현재 진행액 (예: 400,000원)
    private double currentProgress;
    
    // 3. 서버가 계산한 달성률 (예: 133.3)
    private double achievementRate;
    
    public GoalProgressDTO(Goal goal, double currentProgress, double achievementRate) {
        this.goal = goal;
        this.currentProgress = currentProgress;
        this.achievementRate = achievementRate;
    }

    public Goal getGoal() {
        return goal;
    }

    public double getCurrentProgress() {
        return currentProgress;
    }

    public double getAchievementRate() {
        return achievementRate;
    }

    // --- Setters (필요시) ---

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public void setCurrentProgress(double currentProgress) {
        this.currentProgress = currentProgress;
    }

    public void setAchievementRate(double achievementRate) {
        this.achievementRate = achievementRate;
    }
}