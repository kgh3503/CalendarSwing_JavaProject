package com.example.finance_server; 

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Integer> {

    /**
     * [DAO 기능 대체 1: getGoalsByMonth]
     * Spring Data JPA가 메서드 이름을 분석하여 쿼리를 자동 생성
     * "WHERE user_id = ? AND year = ? AND month = ?"
     */
    List<Goal> findByUserIdAndYearAndMonth(int userId, int year, int month);
}