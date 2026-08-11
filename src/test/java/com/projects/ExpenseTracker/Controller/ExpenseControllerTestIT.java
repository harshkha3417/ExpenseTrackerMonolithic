package com.projects.ExpenseTracker.Controller;

import com.projects.ExpenseTracker.Dto.ExpenseDTO;
import com.projects.ExpenseTracker.Entity.Expense;
import com.projects.ExpenseTracker.Entity.User;
import com.projects.ExpenseTracker.Repository.ExpenseRepository;
import com.projects.ExpenseTracker.Repository.UserRepository;
import com.projects.ExpenseTracker.Security.JwtUtil;
import com.projects.ExpenseTracker.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@AutoConfigureWebTestClient(timeout = "100000")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfig.class)
class ExpenseControllerTestIT {

    @Autowired
    private WebTestClient webTestClient;
    private Expense mockexpense;
    private ExpenseDTO mockexpenseDTO;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;
    private String adminToken;
    @BeforeEach
    void setup(){
        expenseRepository.deleteAll();
        userRepository.deleteAll();
        User user = User.builder()
                .username("Harsh")
                .password("Harsh")
                .email("harsh@gmail.com")
                .build();
        userRepository.save(user);
        adminToken = "Bearer " + jwtUtil.generateToken(user);
        mockexpense= Expense.builder()
                .expenseDate(LocalDate.now())
                .title("Ladakh")
                .category("Fuel")
                .amount(500.675)
                .notes("should be divided")
                .build();
        mockexpenseDTO= ExpenseDTO.builder()
                .expenseDate(LocalDate.now())
                .title("Ladakh")
                .category("Fuel")
                .amount(500.675)
                .notes("should be divided")
                .build();
    }
    @Test
    void testGetById_success(){
        Expense savedExpense=expenseRepository.save(mockexpense);
        Long savedid=savedExpense.getId();
        webTestClient.get()
                .uri("/api/expenses/{id}",savedid)
                .header("Authorization", adminToken)
                .exchange() //make the api call and get the response
                .expectStatus().isOk()
                .expectBody()//return a json file
                .jsonPath("$.id").isEqualTo(savedid)
                .jsonPath("$.title").isEqualTo("Ladakh")
                .jsonPath("$.amount").isEqualTo(500.675);
  }
    @Test
    void testGetById_failure(){
        webTestClient.get()
                .uri("/api/expenses/1")
                .header("Authorization", adminToken)
                .exchange()
                .expectStatus().isNotFound();
    }
    @Test
    void testAddExpense_success(){
        webTestClient.post()
                .uri("/api/expenses")
                .bodyValue(mockexpenseDTO)
                .header("Authorization", adminToken)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.title").isEqualTo(mockexpenseDTO.getTitle())
                .jsonPath("$.category").isEqualTo(mockexpenseDTO.getCategory());
    }
}