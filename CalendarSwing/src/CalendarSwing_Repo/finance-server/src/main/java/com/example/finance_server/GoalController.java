package com.example.finance_server; // 님의 패키지 이름입니다.

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList; // ⚠️ [추가] DTO 리스트 생성을 위해


@RestController
@RequestMapping("/api/goals")
public class GoalController {

    // GoalRepository를 스프링이 연결
    @Autowired
    private GoalRepository goalRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * [GET /api/goals/user/{userId}/month/{year}/{month}]
     * GoalView의 'loadGoalData' (월별 목표 조회)가 호출할 API
     * 1. 단순히 Goal 목록만 반환하는 것이 아니라,
     * 2. TransactionRepository를 사용해 '현재 진행액'을 계산하고,
     * 3. 'GoalProgressDTO' 리스트를 반환하도록 변경
     */
    @GetMapping("/user/{userId}/month/{year}/{month}")
    public List<GoalProgressDTO> getMonthlyGoalsWithProgress( 
            @PathVariable int userId,
            @PathVariable int year,
            @PathVariable int month) {
        
        // yyyy-MM-
        String datePrefix = String.format("%d-%02d-", year, month);

        // 1. DB에서 이번 달 '목표' 목록을 모두 조회
        List<Goal> goals = goalRepository.findByUserIdAndYearAndMonth(userId, year, month);
        
        // 2. 반환할 DTO 리스트를 준비
        List<GoalProgressDTO> progressList = new ArrayList<>();

        // 3. 각 목표를 순회하며 '현재 진행액'을 계산합니다.
        for (Goal goal : goals) {
            
            double currentProgress = 0.0; // 현재 진행액 (예: 40만원)
            
            // 4. 1단계에서 만든 쿼리(DAO)를 호출.
            if (goal.getCategory() == null) {
                // [카테고리: 전체] 목표일 경우 (예: '전체 지출' 목표)
                currentProgress = transactionRepository.findTotalSumByType(
                        userId, datePrefix, goal.getType()
                );
            } else {
                // [카테고리: 식비] 목표일 경우 (예: '식비' 목표)
                currentProgress = transactionRepository.findTotalSumByTypeAndCategory(
                        userId, datePrefix, goal.getType(), goal.getCategory()
                );
            }

            // 5. 달성률(%)을 계산합니다.
            double achievementRate = 0.0;
            if (goal.getTargetAmount() > 0) {
                // (현재 진행액 / 목표액) * 100
                achievementRate = (currentProgress / goal.getTargetAmount()) * 100.0;
            }

            // 6. DTO에 (목표, 진행액, 달성률)을 담아 리스트에 추가
            progressList.add(new GoalProgressDTO(goal, currentProgress, achievementRate));
        }
        
        // 7. '진행률'이 모두 계산된 DTO 리스트를 클라이언트에 반환
        return progressList;
    }

    @PostMapping
    public ResponseEntity<Goal> addGoal(@RequestBody Goal goal) {
        try {
            Goal savedGoal = goalRepository.save(goal);
            return ResponseEntity.ok(savedGoal);
            
        } catch (Exception e) {
            // (아이디 중복 등)
            return ResponseEntity.status(409).build(); // 409 Conflict
        }
    }
}