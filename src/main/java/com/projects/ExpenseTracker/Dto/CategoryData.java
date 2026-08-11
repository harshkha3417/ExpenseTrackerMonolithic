package com.projects.ExpenseTracker.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryData {
    private String name;
    private Double value;
    private String color; // Optional: can be assigned in Java or React
}
