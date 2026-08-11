package com.projects.ExpenseTracker.Entity;

import com.projects.ExpenseTracker.Config.RecurringType;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.CreationTimestamp;



import java.time.LocalDate;
@Entity
@Table(name = "expenses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String category;

    @Column(nullable = false)
    private Double amount;

    @CreationTimestamp
    private LocalDate expenseDate;

    private String notes;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private boolean recurring;
    @Enumerated(EnumType.STRING)
    private RecurringType frequency;
}