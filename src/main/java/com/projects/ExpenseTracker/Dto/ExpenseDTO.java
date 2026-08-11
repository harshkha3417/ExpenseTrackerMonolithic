package com.projects.ExpenseTracker.Dto;

import com.projects.ExpenseTracker.Config.RecurringType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseDTO implements  Serializable{
    private Long id;
    private String title;
    private String category;
    private Double amount;
    private LocalDate expenseDate;
    private String notes;
    private boolean recurring = false;
    @Enumerated(EnumType.STRING)
    private RecurringType frequency;

}

