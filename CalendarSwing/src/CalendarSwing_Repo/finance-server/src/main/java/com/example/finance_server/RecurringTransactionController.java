package com.example.finance_server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/recurring")
public class RecurringTransactionController {

    @Autowired
    private RecurringTransactionRepository repository;
    
    @Autowired
    private TransactionRepository transactionRepository;

    // 조회
    @GetMapping("/user/{userId}")
    public List<RecurringTransaction> getList(@PathVariable int userId) {
        return repository.findByUserIdOrderByDayOfMonthAsc(userId);
    }

    // 추가
    @PostMapping
    public RecurringTransaction add(@RequestBody RecurringTransaction rt) {
        return repository.save(rt);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    // 실제 내역 반영
    @PostMapping("/apply")
    public ResponseEntity<String> applyRecurringTransactions(
            @RequestParam int userId,
            @RequestParam int year,
            @RequestParam int month) {
        
        // 1. 해당 사용자의 모든 정기 거래 규칙 가져오기
        List<RecurringTransaction> rules = repository.findByUserIdOrderByDayOfMonthAsc(userId);
        
        int count = 0;
        YearMonth targetYearMonth = YearMonth.of(year, month);
        int lastDayOfMonth = targetYearMonth.lengthOfMonth(); // 그 달의 마지막 날 (28, 30, 31 등)

        for (RecurringTransaction rt : rules) {
            // 2. 날짜 생성 (예: 2월인데 30일로 설정된 경우 -> 2월 29일/28일로 조정)
            int day = Math.min(rt.getDayOfMonth(), lastDayOfMonth);
            String dateStr = String.format("%d-%02d-%02d", year, month, day);

            // 3. 이미 등록된 내역인지 확인 (중복 방지)
            boolean exists = transactionRepository.existsByUserIdAndDateAndContentAndAmount(
                    userId, dateStr, rt.getContent(), rt.getAmount()
            );

            if (!exists) {
                // 4. 존재하지 않으면 실제 거래 내역(Transaction)으로 저장
                Transaction newTrans = new Transaction(
                        userId,
                        dateStr,
                        rt.getType(),
                        rt.getAmount(),
                        rt.getCategory(),
                        rt.getContent()
                );
                transactionRepository.save(newTrans);
                count++;
            }
        }
        
        return ResponseEntity.ok("Applied " + count + " recurring transactions.");
    }
}