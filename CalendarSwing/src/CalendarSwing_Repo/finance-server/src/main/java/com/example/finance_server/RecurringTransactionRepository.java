package com.example.finance_server;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Integer> {
    // 특정 사용자의 정기 거래 목록 조회 (날짜순 정렬)
    List<RecurringTransaction> findByUserIdOrderByDayOfMonthAsc(int userId);
}