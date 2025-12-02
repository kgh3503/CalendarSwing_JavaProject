package com.example.finance_server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/recurring")
public class RecurringTransactionController {

    @Autowired private RecurringTransactionRepository repository;
    @Autowired private TransactionRepository transactionRepository;

    @GetMapping("/user/{userId}")
    public List<RecurringTransaction> getList(@PathVariable int userId) {
        return repository.findAll(); // 전체 조회
    }

    @PostMapping
    public RecurringTransaction add(@RequestBody RecurringTransaction rt) {
        return repository.save(rt);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    // 기간 체크 후 내역 생성 로직
    @PostMapping("/apply")
    public ResponseEntity<String> applyRecurringTransactions(@RequestParam int userId, @RequestParam int year, @RequestParam int month) {
        List<RecurringTransaction> rules = repository.findAll();
        int count = 0;
        
        YearMonth currentYm = YearMonth.of(year, month);
        int lastDayOfMonth = currentYm.lengthOfMonth();

        for (RecurringTransaction rt : rules) {
            if (rt.getUserId() != userId) continue;
            
            // 날짜가 없거나 형식이 이상하면 건너뜀 (null 방지)
            if (rt.getStartDate() == null || rt.getEndDate() == null) continue;

            LocalDate start = LocalDate.parse(rt.getStartDate()); 
            LocalDate end = LocalDate.parse(rt.getEndDate());     
            
            YearMonth startYm = YearMonth.from(start);
            YearMonth endYm = YearMonth.from(end);
            
            // 현재 달이 기간 내에 포함되는지 확인
            if (!currentYm.isBefore(startYm) && !currentYm.isAfter(endYm)) {
                
                // 시작일의 '일'자를 따옴
                int targetDay = start.getDayOfMonth();
                targetDay = Math.min(targetDay, lastDayOfMonth);
                
                String dateStr = String.format("%d-%02d-%02d", year, month, targetDay);

                if (!transactionRepository.existsByUserIdAndDateAndContentAndAmount(
                        userId, dateStr, rt.getContent(), rt.getAmount())) {
                    
                    Transaction newTrans = new Transaction(
                            userId, dateStr, rt.getType(), rt.getAmount(), rt.getCategory(), rt.getContent()
                    );
                    transactionRepository.save(newTrans);
                    count++;
                }
            }
        }
        return ResponseEntity.ok("Applied " + count);
    }
}