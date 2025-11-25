package com.example.finance_server; 

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map; // (참고용)

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    List<Transaction> findByUserIdAndDateOrderByIdAsc(int userId, String date);

    List<Transaction> findByUserIdAndDateStartingWithOrderByDateAsc(int userId, String datePrefix);

    @Query("SELECT t.type, SUM(t.amount) as totalAmount " +
           "FROM Transaction t " +
           "WHERE t.userId = :userId AND t.date LIKE CONCAT(:datePrefix, '%') " + // '%'를 쿼리 내부에 추가
           "GROUP BY t.type")
    List<Object[]> getMonthlySummaryRaw(
            @Param("userId") int userId, 
            @Param("datePrefix") String datePrefix
    );

    @Query("SELECT t.category, SUM(t.amount) as totalAmount " +
           "FROM Transaction t " +
           "WHERE t.userId = :userId AND t.date LIKE CONCAT(:datePrefix, '%') AND t.type = :type " + // '%'를 쿼리 내부에 추가
           "GROUP BY t.category " +
           "ORDER BY totalAmount DESC")
    List<Object[]> getCategorySummaryRaw(
            @Param("userId") int userId, 
            @Param("datePrefix") String datePrefix, 
            @Param("type") String type
    );

    @Query("SELECT MONTH(DATE(t.date)) as month, t.type, SUM(t.amount) as totalAmount " +
           "FROM Transaction t " +
           "WHERE t.userId = :userId AND YEAR(DATE(t.date)) = :year " +
           "GROUP BY MONTH(DATE(t.date)), t.type")
    List<Object[]> getYearlySummaryRaw(
            @Param("userId") int userId, 
            @Param("year") int year
    );

    /*
     * 특정 월의 '전체' 수입/지출 합계를 계산
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) " + 
           "FROM Transaction t " +
           "WHERE t.userId = :userId " +
           "AND t.date LIKE CONCAT(:datePrefix, '%') " +
           "AND t.type = :type")
    double findTotalSumByType(
            @Param("userId") int userId, 
            @Param("datePrefix") String datePrefix, 
            @Param("type") String type
    );

    /*
     * 특정 월의 '특정 카테고리' 합계를 계산
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) " + 
           "FROM Transaction t " +
           "WHERE t.userId = :userId " +
           "AND t.date LIKE CONCAT(:datePrefix, '%') " +
           "AND t.type = :type " +
           "AND t.category = :category")
    double findTotalSumByTypeAndCategory(
            @Param("userId") int userId, 
            @Param("datePrefix") String datePrefix, 
            @Param("type") String type, 
            @Param("category") String category
    );


    // 사용자 비교 기능용 신규 쿼리 

    /*
     * '나'를 제외한 모든 사용자의 월별/카테고리별 '평균(AVG)' 지출/수입액을 계산
     * (예: 다른 모든 사용자의 '식비' 평균액)
     */
    @Query("SELECT t.category, AVG(t.amount) as averageAmount " +
           "FROM Transaction t " +
           "WHERE t.userId != :userId " + 
           "AND t.date LIKE CONCAT(:datePrefix, '%') " + 
           "AND t.type = :type " +
           "GROUP BY t.category")
    List<Object[]> getAverageCategorySummaryExcludingUser(
            @Param("userId") int userId, 
            @Param("datePrefix") String datePrefix, 
            @Param("type") String type
    );
}