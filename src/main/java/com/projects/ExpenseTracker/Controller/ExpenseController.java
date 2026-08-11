package com.projects.ExpenseTracker.Controller;

import com.projects.ExpenseTracker.Dto.DashboardStatsDTO;
import com.projects.ExpenseTracker.Dto.ExpenseDTO;
import com.projects.ExpenseTracker.Service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;

    @PostMapping
    public ResponseEntity<ExpenseDTO> create(@RequestBody ExpenseDTO dto) {
        return ResponseEntity.ok(service.addExpense(dto));
    }
    @GetMapping("/my-Expenses/total")
    public ResponseEntity<Double> getTotalSpent(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        log.info("Getting total Expense");
        Double total = service.getTotalSpent(category,
                startDate,
                endDate
        );

        return ResponseEntity.ok(total);
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(service.getQuickStats());
    }
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getExpenseById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO> update(@RequestBody ExpenseDTO dto) {
        return ResponseEntity.ok(service.updateExpense(dto));
    }
    @GetMapping("/summary/by-category")
    public ResponseEntity<Map<String, Double>> getCategoryReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {

        return ResponseEntity.ok(service.getCategoryReport(startDate, endDate));
    }
    @GetMapping(value = "/summary/chart", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getPieChart(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] imageBytes = service.getCategoryChart(startDate, endDate);
        if (imageBytes == null || imageBytes.length == 0) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes);
    }
    @GetMapping("/subscriptions")
    public ResponseEntity<List<ExpenseDTO>> getMySubscriptions() {
        List<ExpenseDTO> subs = service.getAllSubscriptions();
        return ResponseEntity.ok(subs);
    }
    @GetMapping("/all-Expenses")
    public ResponseEntity<List<ExpenseDTO>> getAllExpenses() {
        List<ExpenseDTO> expenses = service.getAllExpensesForCurrentUser();
        return ResponseEntity.ok(expenses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}