package com.projects.ExpenseTracker.Repository;
import com.projects.ExpenseTracker.Entity.Expense;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserIdOrderByExpenseDateDesc(Long userId);
    List<Expense> findByCategory(String category);
    @Query("SELECT COALESCE(SUM(e.amount), 0.0) FROM Expense e WHERE e.user.id = :userId AND e.category = :category AND e.expenseDate BETWEEN :startDate AND :endDate")
    Double sumByUserIdAndCategoryAndDateRange(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    SELECT e.category AS category, SUM(e.amount) AS total 
    FROM Expense e 
    WHERE e.user.id = :userId 
      AND (CAST(:startDate AS java.time.LocalDate) IS NULL OR e.expenseDate >= :startDate) 
      AND (CAST(:endDate AS java.time.LocalDate) IS NULL OR e.expenseDate <= :endDate) 
    GROUP BY e.category
    """)
    List<Object[]> getCategorySummaries(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    List<Expense> findByRecurringTrueAndExpenseDateLessThanEqual(LocalDate date);
    List<Expense> findByUserIdAndRecurringTrue(Long userId);
    @Query("SELECT COALESCE(SUM(e.amount), 0.0) FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate")
    Double sumAmountByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    List<Expense> findByUserId(Long userId);
    @Query("SELECT e.category FROM Expense e WHERE e.user.id = :userId GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<String> findTopCategoryByUserId(@Param("userId") Long userId, Pageable pageable);

}