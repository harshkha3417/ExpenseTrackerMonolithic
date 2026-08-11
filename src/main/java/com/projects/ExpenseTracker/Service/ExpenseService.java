package com.projects.ExpenseTracker.Service;



import com.projects.ExpenseTracker.Dto.DashboardStatsDTO;
import com.projects.ExpenseTracker.Dto.ExpenseDTO;
import com.projects.ExpenseTracker.Entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ExpenseService {
    List<ExpenseDTO> getAllExpensesForCurrentUser();
    ExpenseDTO addExpense(ExpenseDTO dto);
    Double getTotalSpent(String category, LocalDate startDate, LocalDate endDate);
    ExpenseDTO getExpenseById(Long id);
    byte[] getCategoryChart(LocalDate startDate, LocalDate endDate);
    ExpenseDTO updateExpense(ExpenseDTO dto);

    void deleteExpense(Long id);
    Map<String, Double> getCategoryReport(LocalDate startDate,LocalDate endDate);
    void processRecurringExpenses();
    List<ExpenseDTO> getAllSubscriptions();
    DashboardStatsDTO getQuickStats();
}