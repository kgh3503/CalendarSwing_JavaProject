package com.example.finance_server; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CalendarSwing, AnalysisView의 모든 Transaction 관련 요청을 처리
 * /api/transactions 경로의 모든 요청을 이 클래스가 처리
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * CalendarSwing의 '입력 추가' 버튼 (addTransaction)이 호출할 API
     */
    @PostMapping
    public Transaction addTransaction(@RequestBody Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    /**
     * CalendarSwing의 '내용 삭제' 버튼 (deleteTransaction)이 호출할 API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Integer id) {
        transactionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * CalendarSwing의 날짜 버튼 (updateDetailsPanel)이 호출할 API
     */
    @GetMapping("/user/{userId}/date/{date}")
    public List<Transaction> getDailyTransactions(
            @PathVariable int userId,
            @PathVariable String date) { // date는 "yyyy-MM-dd" 형식
        return transactionRepository.findByUserIdAndDateOrderByIdAsc(userId, date);
    }

    /**
     * CalendarSwing의 loadMonthData, AnalysisView의 Excel 출력이 호출할 API
     */
    @GetMapping("/user/{userId}/month/{year}/{month}")
    public List<Transaction> getMonthlyTransactions(
            @PathVariable int userId,
            @PathVariable int year,
            @PathVariable int month) {
        // Repository의 "findBy...StartingWith"를 사용하기 위해 "yyyy-MM-" 접두사 생성
        String datePrefix = String.format("%d-%02d-", year, month);
        return transactionRepository.findByUserIdAndDateStartingWithOrderByDateAsc(userId, datePrefix);
    }

    // AnalysisView의 분석 기능을 위한 API

    @GetMapping("/summary/monthly/{userId}/{year}/{month}")
    public Map<String, Double> getMonthlySummary(
            @PathVariable int userId,
            @PathVariable int year,
            @PathVariable int month) {
        
        String datePrefix = String.format("%d-%02d-", year, month);
        List<Object[]> rawResult = transactionRepository.getMonthlySummaryRaw(userId, datePrefix);
        
        Map<String, Double> summary = new HashMap<>();
        summary.put("수입", 0.0);
        summary.put("지출", 0.0);
        
        for (Object[] row : rawResult) {
            String type = (String) row[0];
            Double totalAmount = (Double) row[1];
            if (type != null) {
                summary.put(type, totalAmount);
            }
        }
        return summary;
    }

    @GetMapping("/summary/category/{userId}/{year}/{month}")
    public Map<String, Double> getCategorySummary(
            @PathVariable int userId,
            @PathVariable int year,
            @PathVariable int month,
            @RequestParam String type) { // ?type=지출
        
        String datePrefix = String.format("%d-%02d-", year, month);
        List<Object[]> rawResult = transactionRepository.getCategorySummaryRaw(userId, datePrefix, type);
        
        return rawResult.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],  // Key: 카테고리명
                        row -> (Double) row[1]   // Value: 합계 금액
                ));
    }

    @GetMapping("/summary/yearly/{userId}/{year}")
    public Map<String, double[]> getYearlySummary(
            @PathVariable int userId,
            @PathVariable int year) {
        
        List<Object[]> rawResult = transactionRepository.getYearlySummaryRaw(userId, year);
        
        Map<String, double[]> yearlyData = new HashMap<>();
        yearlyData.put("수입", new double[12]);
        yearlyData.put("지출", new double[12]);

        for (Object[] row : rawResult) {
            Integer month = (Integer) row[0]; // 1~12월
            String type = (String) row[1];
            Double totalAmount = (Double) row[2];

            if (month != null && type != null && yearlyData.containsKey(type)) {
                yearlyData.get(type)[month - 1] = totalAmount; // 1월 -> 0번 인덱스
            }
        }
        return yearlyData;
    }


    /**
     * '나'를 제외한 다른 모든 사용자의 '카테고리별 평균 지출'을 조회
     * (AnalysisView의 '사용자 비교' 기능이 호출할 API)
     * userId 제거 및 전체 평균 로직 적용
     */
    @GetMapping("/summary/average/{year}/{month}")
    public Map<String, Double> getAverageCategorySummary(
            @PathVariable int year,
            @PathVariable int month,
            @RequestParam String type) { // ?type=지출
        
        String datePrefix = String.format("%d-%02d-", year, month);
        
        // getAverageCategorySummaryAllUsers 호출 (모든 사용자 대상)
        List<Object[]> rawResult = transactionRepository.getAverageCategorySummaryAllUsers(
                datePrefix, type
        );
        
        return rawResult.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],  // Key: 카테고리명
                        row -> (Double) row[1]   // Value: 평균 금액
                ));
    }
}