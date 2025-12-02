package com.example.finance_server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    // 1. 중복 확인 
    boolean existsByUserIdAndDateAndContentAndAmount(int userId, String date, String content, double amount);

    // 2. 범위 밖 삭제 (Start 이전 OR End 이후)
    @Modifying
    @Transactional
    @Query("DELETE FROM Transaction t WHERE t.userId = :userId AND t.content = :content AND t.amount = :amount AND (t.date < :startDate OR t.date > :endDate)")
    void deleteTransactionsOutsideRange(
            @Param("userId") int userId, 
            @Param("content") String content, 
            @Param("amount") double amount,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    List<Transaction> findByUserIdAndDateOrderByIdAsc(int userId, String date);
    List<Transaction> findByUserIdAndDateStartingWithOrderByDateAsc(int userId, String datePrefix);
    @Query("SELECT t.type, SUM(t.amount) as totalAmount FROM Transaction t WHERE t.userId = :userId AND t.date LIKE CONCAT(:datePrefix, '%') GROUP BY t.type")
    List<Object[]> getMonthlySummaryRaw(@Param("userId") int userId, @Param("datePrefix") String datePrefix);
    @Query("SELECT t.category, SUM(t.amount) as totalAmount FROM Transaction t WHERE t.userId = :userId AND t.date LIKE CONCAT(:datePrefix, '%') AND t.type = :type GROUP BY t.category ORDER BY totalAmount DESC")
    List<Object[]> getCategorySummaryRaw(@Param("userId") int userId, @Param("datePrefix") String datePrefix, @Param("type") String type);
    @Query("SELECT MONTH(DATE(t.date)) as month, t.type, SUM(t.amount) as totalAmount FROM Transaction t WHERE t.userId = :userId AND YEAR(DATE(t.date)) = :year GROUP BY MONTH(DATE(t.date)), t.type")
    List<Object[]> getYearlySummaryRaw(@Param("userId") int userId, @Param("year") int year);
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.userId = :userId AND t.date LIKE CONCAT(:datePrefix, '%') AND t.type = :type")
    double findTotalSumByType(@Param("userId") int userId, @Param("datePrefix") String datePrefix, @Param("type") String type);
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.userId = :userId AND t.date LIKE CONCAT(:datePrefix, '%') AND t.type = :type AND t.category = :category")
    double findTotalSumByTypeAndCategory(@Param("userId") int userId, @Param("datePrefix") String datePrefix, @Param("type") String type, @Param("category") String category);
    @Query("SELECT t.category, AVG(t.amount) as averageAmount FROM Transaction t WHERE t.userId != :userId AND t.date LIKE CONCAT(:datePrefix, '%') AND t.type = :type GROUP BY t.category")
    List<Object[]> getAverageCategorySummaryExcludingUser(@Param("userId") int userId, @Param("datePrefix") String datePrefix, @Param("type") String type);
}