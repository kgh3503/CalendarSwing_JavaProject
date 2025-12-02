package com.example.finance_server;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Integer> {
    
    // 나의 ID(userId)와 일치하는 것만 가져오는 메서드 
    List<RecurringTransaction> findByUserIdOrderByStartDateAsc(int userId);
}