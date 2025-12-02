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

    // findAll() -> findByUserId... 로 변경 
    @GetMapping("/user/{userId}")
    public List<RecurringTransaction> getList(@PathVariable int userId) {
        return repository.findByUserIdOrderByStartDateAsc(userId);
    }

    @PostMapping
    public RecurringTransaction add(@RequestBody RecurringTransaction rt) {
        return repository.save(rt);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id, 
            @RequestParam(required = false) Integer sYear, @RequestParam(required = false) Integer sMonth, @RequestParam(required = false) Integer sDay,
            @RequestParam(required = false) Integer eYear, @RequestParam(required = false) Integer eMonth, @RequestParam(required = false) Integer eDay) {
        
        // (필요 시 여기서 transactionRepository.deleteTransactionsOutsideRange 호출 가능)
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    // 반영할 때도 내 것만 가져와서 처리하도록 최적화
    @PostMapping("/apply")
    public ResponseEntity<String> applyRecurringTransactions(@RequestParam int userId, @RequestParam int year, @RequestParam int month) {
        // [수정] findAll() 대신 내 것만 가져오기
        List<RecurringTransaction> rules = repository.findByUserIdOrderByStartDateAsc(userId);
        int count = 0;
        
        YearMonth currentYm = YearMonth.of(year, month);
        int lastDayOfMonth = currentYm.lengthOfMonth();

        for (RecurringTransaction rt : rules) {
            // userId 체크 로직은 쿼리에서 이미 걸러졌지만, 이중 안전장치로 둠
            if (rt.getUserId() != userId) continue;
            
            if (rt.getStartDate() == null || rt.getEndDate() == null) continue;

            LocalDate start = LocalDate.parse(rt.getStartDate()); 
            LocalDate end = LocalDate.parse(rt.getEndDate());     
            
            YearMonth startYm = YearMonth.from(start);
            YearMonth endYm = YearMonth.from(end);
            
            if (!currentYm.isBefore(startYm) && !currentYm.isAfter(endYm)) {
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