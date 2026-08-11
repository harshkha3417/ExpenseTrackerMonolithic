package com.projects.ExpenseTracker.Repository;

import com.projects.ExpenseTracker.Entity.Expense;
import com.projects.ExpenseTracker.ExpenseTrackerApplication;
import com.projects.ExpenseTracker.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Disabled
@Import(TestContainerConfig.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    private Expense expense;

    @BeforeEach
    void setup(){
        expense= Expense.builder()
                .expenseDate(LocalDate.now())
                .title("Ladakh")
                .category("Fuel")
                .amount(500.675)
                .notes("should be divided")
                .build();
    }

    @Test
    void testFindByCategory_whenValidCategoryIsGiven_thenReturnCategory() {
        //arrange
        expenseRepository.save(expense);
        //act
        List<Expense> expenseList=expenseRepository.findByCategory("Fuel");
        //assert
        assertThat(expenseList).isNotNull();
        assertThat(expenseList.get(0).getTitle()).isEqualTo("Ladakh");
    }
}