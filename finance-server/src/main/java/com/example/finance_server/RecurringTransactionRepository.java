package com.example.finance_server;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Integer> {
    // 시작일 순서대로 조회
    List<RecurringTransaction> findByUserIdOrderByStartDateAsc(int userId);
}