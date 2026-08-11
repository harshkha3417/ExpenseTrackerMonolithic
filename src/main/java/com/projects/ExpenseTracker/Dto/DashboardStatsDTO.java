package com.projects.ExpenseTracker.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardStatsDTO {
    private Double totalSpent;
    private String topCategory;
    private Double highestExpenseAmount;
    private String highestExpenseTitle;
    private long totalTransactions;
    private Double previousMonthTotal;
    private String percentageChange;
    private List<CategoryData> expenseBreakdown;
}