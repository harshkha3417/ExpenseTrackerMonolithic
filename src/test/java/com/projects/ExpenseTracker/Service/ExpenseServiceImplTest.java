package com.projects.ExpenseTracker.Service;

import com.projects.ExpenseTracker.Dto.ExpenseDTO;
import com.projects.ExpenseTracker.Entity.Expense;
import com.projects.ExpenseTracker.Exception.ResourceNotFoundException;
import com.projects.ExpenseTracker.Repository.ExpenseRepository;
//import com.projects.ExpenseTracker.TestContainerConfig;
import com.projects.ExpenseTracker.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled
@Import(TestContainerConfig.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private Expense updatedmockexpense;
    private Expense mockexpense;
    private ExpenseDTO mockexpenseDTO;
    private ExpenseDTO updatedmockexpenseDTO;

    @BeforeEach
    void setup(){
        mockexpense=Expense.builder()
                .id(1L)
                .expenseDate(LocalDate.now())
                .title("Food")
                .notes("divided equally")
                .amount(459.35)
                .category("travel")
                .build();
        mockexpenseDTO=modelMapper.map(mockexpense,ExpenseDTO.class);
        updatedmockexpenseDTO=ExpenseDTO.builder()
                .id(1L)
                .expenseDate(LocalDate.now())
                .title("Movies")
                .notes("divided equally")
                .amount(459.35)
                .category("travel")
                .build();
        updatedmockexpense=modelMapper.map(updatedmockexpenseDTO,Expense.class);
    }
    @Test
    void testAddExpense_whenExpenseIsGiven_ThenAddExpense(){
        //assign all @mock component what they have to do as they are dummy asses
        when(expenseRepository.save(any(Expense.class))).thenReturn(mockexpense);
        //act on the service u are trying to check
        ExpenseDTO expenseDTO=expenseService.addExpense(mockexpenseDTO);

        //assert on the values u get and the actual one
        assertThat(expenseDTO).isNotNull();
        assertThat(expenseDTO.getAmount()).isEqualTo(mockexpenseDTO.getAmount());
         //verify on the @mock what they have performed
        verify(expenseRepository).save(any(Expense.class));//save method has been called
    }

    @Test
    void testGetExpenseById_whenIdIsValid_ThenReturnExpense(){
        Long id=mockexpense.getId();
        //assign
        when(expenseRepository.findById(id)).thenReturn(Optional.of(mockexpense));

        //act
        ExpenseDTO expenseDTO=expenseService.getExpenseById(1L);
        //assert
        assertThat(expenseDTO.getId()).isEqualTo(1L);
        assertThat(expenseDTO.getTitle()).isEqualTo("Food");

    }
    @Test
    void testGetExpenseById_whenIdIsInValid_ThenException(){
        //assign
        when(expenseRepository.findById(anyLong())).thenReturn(Optional.empty());
        //act and assert
        assertThatThrownBy(()->expenseService.getExpenseById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Expense not found with id:1");
        verify(expenseRepository).findById(1L);
    }



    @Test
    void testUpdateExpense_WhenUpdatedExpenseIsGivenWithValidId(){
        //assign
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(mockexpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(updatedmockexpense);

        //act
        ExpenseDTO expenseDTO=expenseService.updateExpense(updatedmockexpenseDTO);

        //assert
        assertThat(expenseDTO.getTitle()).isEqualTo(updatedmockexpenseDTO.getTitle());
        verify(expenseRepository).findById(1L);
    }

    @Test
    void testDeleteExpenseByValidId(){
        //assign
        when(expenseRepository.existsById(1L)).thenReturn(true);
        //act
        assertThatCode(()->expenseService.deleteExpense()).doesNotThrowAnyException();
        verify(expenseRepository).deleteById(1L);

    }

    @Test
    void testDeleteExpenseByInvalidId(){
        //assign
        when(expenseRepository.existsById(anyLong())).thenReturn(false);
        //act and assert
        assertThatThrownBy(()->expenseService.deleteExpense())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Expense not found with id:1");
        verify(expenseRepository).existsById(1L);
    }
}