package com.projects.ExpenseTracker.Service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.ExpenseTracker.Config.RecurringType;
import com.projects.ExpenseTracker.Config.UserContextHolder;
import com.projects.ExpenseTracker.Dto.CategoryData;
import com.projects.ExpenseTracker.Dto.DashboardStatsDTO;
import com.projects.ExpenseTracker.Dto.ExpenseDTO;
import com.projects.ExpenseTracker.Entity.Expense;
import com.projects.ExpenseTracker.Entity.User;
import com.projects.ExpenseTracker.Exception.ResourceNotFoundException;
import com.projects.ExpenseTracker.Repository.ExpenseRepository;
import com.projects.ExpenseTracker.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository repository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @CacheEvict(value = "Expense", allEntries = true)
    public ExpenseDTO addExpense(ExpenseDTO dto) {
        log.info("Adding new Expense");
        Long id=UserContextHolder.getCurrentUserId();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Expense expense = modelMapper.map(dto, Expense.class);
        expense.setUser(user);
        Expense savedExpense = repository.save(expense);
        log.info("Successfully added expense");
        return modelMapper.map(savedExpense,ExpenseDTO.class);
    }
    public Map<String, Double> getCategoryReport(LocalDate startDate, LocalDate endDate) {
        // 1. Fetch current logged-in user from UserContextHolder
        Long userId  = UserContextHolder.getCurrentUserId();

        // 2. Fetch category sums from database
        List<Object[]> results = repository.getCategorySummaries(userId, startDate, endDate);

        // 3. Convert List<Object[]> to Map<String, Double> using Java Streams
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],              // Category
                        result -> result[1] != null ? (Double) result[1] : 0.0 // Amount
                ));
    }
    public byte[] getCategoryChart(LocalDate startDate, LocalDate endDate) {
        log.info("Generating pie chart in service layer");
        Map<String, Double> data = getCategoryReport(startDate, endDate);

        try {
            String jsonPayload = objectMapper.writeValueAsString(data);

            // 1. Initialize process without merging error stream
            ProcessBuilder pb = new ProcessBuilder("python3", "scripts/generate_chart.py");
            Process process = pb.start();

            // 2. Pass JSON payload directly into Python's standard input (stdin)
            try (OutputStream os = process.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // 3. Read stdout (Base64 string) from Python
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            // 4. Read stderr separately to log Python errors if process fails
            StringBuilder errorOutput = new StringBuilder();
            try (BufferedReader errReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String errLine;
                while ((errLine = errReader.readLine()) != null) {
                    errorOutput.append(errLine);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("Python script failed with exit code {}: {}", exitCode, errorOutput);
                return null;
            }

            // 5. Decode Base64 string into byte[] for ResponseEntity<byte[]>
            String base64Image = output.toString().trim();
            return Base64.getDecoder().decode(base64Image);

        } catch (Exception e) {
            log.error("Error generating pie chart", e);
            return null;
        }
    }
    public Double getTotalSpent(String category, LocalDate startDate, LocalDate endDate) {
        Long id=UserContextHolder.getCurrentUserId();
        return repository.sumByUserIdAndCategoryAndDateRange(id, category, startDate, endDate);
    }
    @Override
    @Cacheable(value = "Expense", key = "T(com.projects.ExpenseTracker.Config.UserContextHolder).getCurrentUserId()")
    public List<ExpenseDTO> getAllExpensesForCurrentUser() {
        Long currentUserId = UserContextHolder.getCurrentUserId();

        if (currentUserId == null) {
            throw new IllegalStateException("User is not authenticated");
        }

        log.info("Fetching all expenses from database for user ID: {}", currentUserId);

        List<Expense> expenses = repository.findByUserIdOrderByExpenseDateDesc(currentUserId);

        return expenses.stream()
                .map(expense -> modelMapper.map(expense, ExpenseDTO.class))
                .toList();
    }
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processRecurringExpenses() {
        log.info("Starting recurring expense processing...");

        LocalDate today = LocalDate.now();
        List<Expense> dueExpenses = repository.findByRecurringTrueAndExpenseDateLessThanEqual(today);

        for (Expense parent : dueExpenses) {
            // Process all missed occurrences up to today
            while (!parent.getExpenseDate().isAfter(today)) {

                // 1. Create auto-generated expense entry
                Expense clone = new Expense();
                clone.setTitle(parent.getTitle() + " (Auto)");
                clone.setAmount(parent.getAmount());
                clone.setCategory(parent.getCategory());
                clone.setExpenseDate(parent.getExpenseDate());
                clone.setNotes(parent.getNotes());
                clone.setUser(parent.getUser());
                clone.setRecurring(false);

                repository.save(clone);

                LocalDate nextDate = calculateNextDate(parent.getExpenseDate(), parent.getFrequency());
                parent.setExpenseDate(nextDate);
            }

            // 3. Persist updated parent date
            repository.save(parent);

            log.info("Processed recurring expense ID: {} for user ID: {}",
                    parent.getId(),
                    parent.getUser() != null ? parent.getUser().getId() : "N/A");
        }
    }

    private LocalDate calculateNextDate(LocalDate currentDate, RecurringType frequency) {
        if (frequency == null) return currentDate.plusMonths(1);

        return switch (frequency) {
            case NONE -> currentDate;
            case DAILY -> currentDate.plusDays(1);
            case WEEKLY -> currentDate.plusWeeks(1);
            case MONTHLY -> currentDate.plusMonths(1);
            case YEARLY -> currentDate.plusYears(1);
        };
    }
    private String assignColor(String category) {
        if (category == null) return "hsl(221, 83%, 53%)";
        switch (category.toLowerCase()) {
            case "housing": return "hsl(340, 75%, 55%)";
            case "food & drink": case "food": return "hsl(43, 74%, 66%)";
            case "transport": return "hsl(197, 37%, 24%)";
            case "entertainment": return "hsl(280, 65%, 60%)";
            case "shopping": return "hsl(160, 60%, 45%)";
            default: return "hsl(221, 83%, 53%)";
        }
    }
    public DashboardStatsDTO getQuickStats() {
        // 1. Get current user from UserContextHolder instead of passing username/principal
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId==null) {
            throw new IllegalStateException("No authenticated user found in UserContext");
        }
        LocalDate today = LocalDate.now();

        // Current Month range
        LocalDate currentMonthStart = today.withDayOfMonth(1);
        LocalDate currentMonthEnd = today.withDayOfMonth(today.lengthOfMonth());

        // Previous Month range
        LocalDate prevMonthStart = today.minusMonths(1).withDayOfMonth(1);
        LocalDate prevMonthEnd = prevMonthStart.withDayOfMonth(prevMonthStart.lengthOfMonth());

        // 3. Fetch Totals using LocalDate parameters in repository
        Double currentTotal = repository.sumAmountByUserIdAndDateRange(userId, currentMonthStart, currentMonthEnd);
        Double prevTotal = repository.sumAmountByUserIdAndDateRange(userId, prevMonthStart, prevMonthEnd);

        currentTotal = (currentTotal == null) ? 0.0 : currentTotal;
        prevTotal = (prevTotal == null) ? 0.0 : prevTotal;

        // 4. Calculate Percentage Change
        String percentageChange = "0%";
        if (prevTotal > 0) {
            double change = ((currentTotal - prevTotal) / prevTotal) * 100;
            percentageChange = String.format("%.1f%%", change);
            if (change > 0) percentageChange = "+" + percentageChange;
        } else if (currentTotal > 0) {
            percentageChange = "+100%";
        }

        // 5. Fetch expenses using userId
        List<Expense> expenses = repository.findByUserId(userId);

        if (expenses.isEmpty()) {
            return new DashboardStatsDTO(0.0, "N/A", 0.0, "N/A", 0, 0.0, "0%", Collections.emptyList());
        }

        // 6. Find Highest Expense Details
        Expense highest = expenses.stream()
                .max(Comparator.comparing(Expense::getAmount))
                .orElseThrow();

        Double highestAmt = highest.getAmount();
        String highestTitle = highest.getTitle();
        int totalCount = expenses.size();

        // 7. Get Top Category
        List<String> categories = repository.findTopCategoryByUserId(userId, PageRequest.of(0, 1));
        String topCat = categories.isEmpty() ? "None" : categories.get(0);

        // 8. Build Category Breakdown
        Map<String, Double> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));

        List<CategoryData> breakdown = categoryTotals.entrySet().stream()
                .map(entry -> {
                    String color = assignColor(entry.getKey());
                    return new CategoryData(entry.getKey(), entry.getValue(), color);
                })
                .collect(Collectors.toList());

        return new DashboardStatsDTO(
                currentTotal,
                topCat,
                highestAmt,
                highestTitle,
                totalCount,
                prevTotal,
                percentageChange,
                breakdown
        );
    }
    public List<ExpenseDTO> getAllSubscriptions() {
        // 1. Get authenticated user identity from UserContextHolder
        Long userId=UserContextHolder.getCurrentUserId();
        // 2. Fetch recurring expenses using userId
        List<Expense> subscriptions = repository.findByUserIdAndRecurringTrue(userId);
        // 3. Convert List of Entities to List of DTOs
        return subscriptions.stream()
                .map(expense -> modelMapper.map(expense, ExpenseDTO.class))
                .collect(Collectors.toList());
    }
    @Override
    @Cacheable(cacheNames = "singleExpense" , key ="#id")
    public ExpenseDTO getExpenseById(Long id) {
        Expense expense = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        return modelMapper.map(expense,ExpenseDTO.class);
    }

    @Override
    public ExpenseDTO updateExpense(ExpenseDTO dto) {
        Long id=UserContextHolder.getCurrentUserId();
        Expense expense=repository.findById(dto.getId())
                .orElseThrow(()-> new ResourceNotFoundException("Expense not found with id:"+id));
        modelMapper.map(dto,expense);
        Expense updatedExpense= repository.save(expense);
        return modelMapper.map(updatedExpense,ExpenseDTO.class);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "Expense",allEntries = true),
    })
    public void deleteExpense(Long expenseId) {
        Expense expense = repository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
           repository.deleteById(expenseId);
    }
}
