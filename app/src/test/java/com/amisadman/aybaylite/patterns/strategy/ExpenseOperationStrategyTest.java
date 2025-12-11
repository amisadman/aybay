package com.amisadman.aybaylite.patterns.strategy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;

import android.content.Context;

import com.amisadman.aybaylite.Repo.DatabaseHelper;
import com.amisadman.aybaylite.model.Transaction;
import com.amisadman.aybaylite.patterns.factory.TransactionFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class ExpenseOperationStrategyTest {
    @Mock
    private DatabaseHelper mockDbHelper;
    @Mock
    private Context mockContext;

    private ExpenseOperationStrategy expenseStrategy;

    @BeforeEach
    void setUp() {
        expenseStrategy = new ExpenseOperationStrategy();
        expenseStrategy.setDatabaseHelper(mockDbHelper);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testConstructorWithContext() {
        // Strategy doesn't have Context constructor anymore, skipping this specific
        // test or adapting
    }

    @ParameterizedTest
    @ValueSource(doubles = { 0.01, 1.00, 1000.00, 999999.99 })
    void testAddExpanseDiverseValues(double amount) {
        Transaction t = TransactionFactory.createTransaction("EXPENSE", "id", amount, "Test reason", 0);
        expenseStrategy.addTransaction(mockContext, t);
        verify(mockDbHelper).addExpense(eq(amount), eq("Test reason"), anyLong());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "A", "Long reason with spaces and special chars: !@#$%^&*()" })
    void testAddDataWithVariousReasons(String reason) {
        Transaction t = TransactionFactory.createTransaction("EXPENSE", "id", 100.00, reason, 0);
        expenseStrategy.addTransaction(mockContext, t);

        verify(mockDbHelper).addExpense(eq(100.00), eq(reason), anyLong());
    }

    @ParameterizedTest
    @CsvSource({
            "1200.00, Rent payment",
            "89.99, Utility bill",
            "45.50, Coffee with client"
    })
    void testAddExpense(double amount, String reason) {
        Transaction t = TransactionFactory.createTransaction("EXPENSE", "id", amount, reason, 0);
        expenseStrategy.addTransaction(mockContext, t);
        verify(mockDbHelper).addExpense(eq(amount), eq(reason), anyLong());
    }

    @ParameterizedTest
    @CsvSource({
            "1200.00, Rent payment",
            "89.99, Utility bill",
            "45.50, Coffee with client"
    })
    void testUpdateExpense(double amount, String reason) {
        Transaction t = TransactionFactory.createTransaction("EXPENSE", "id", amount, reason, 0);
        expenseStrategy.addTransaction(mockContext, t);
        verify(mockDbHelper).addExpense(eq(amount), eq(reason), anyLong());
    }

    @ParameterizedTest
    @MethodSource("provideAddExpenseTestCases")
    void testAddExpenseWithExtraParameter(double amount, String reason) {
        Transaction t = TransactionFactory.createTransaction("EXPENSE", "id", amount, reason, 0);
        expenseStrategy.addTransaction(mockContext, t);
        verify(mockDbHelper).addExpense(eq(amount), eq(reason), anyLong());
    }

    private static Stream<Arguments> provideAddExpenseTestCases() {
        return Stream.of(
                // Normal cases
                Arguments.of(950.00, "House rent"),
                Arguments.of(150.75, "Electricity bill"),

                // Edge cases
                Arguments.of(0.01, "Rounding fix"),
                Arguments.of(1_000_000.00, "Big purchase"),
                Arguments.of(300.00, "🍔 Lunch"),
                Arguments.of(55.55, "E"),
                Arguments.of(275.00, "Refund for faulty product from online vendor"),
                Arguments.of(69.42, "Misc"));
    }

    @ParameterizedTest
    @MethodSource("provideUpdateExpenseTestCases")
    void testUpdateExpenseDataWithVariousCases(String id, double amount, String reason) {
        Transaction t = TransactionFactory.createTransaction("EXPENSE", id, amount, reason, 0);
        expenseStrategy.updateTransaction(mockContext, t);
        verify(mockDbHelper).updateExpense(eq(id), eq(amount), eq(reason), anyLong());
    }

    private static Stream<Arguments> provideUpdateExpenseTestCases() {
        return Stream.of(
                // Normal cases
                Arguments.of("exp_1", 200.75, "Grocery shopping"),
                Arguments.of("exp_2", 99.99, "Bus fare"),

                // Edge cases
                Arguments.of("id-dash-case", 1.00, "Tiny purchase"),
                Arguments.of("EXP_ID_999", 999999.99, "Medical emergency expense"),
                Arguments.of("weird$id#chars", 500.00, "ID with special chars"),
                Arguments.of("no_reason", 45.00, ""),
                Arguments.of("ridiculously_long_id_x1234567890", 1200.00,
                        "Extremely verbose reason text to test overflow and resilience under verbose user input"));
    }
    // ===============================================================================================

    // Boundary value testing

    @ParameterizedTest
    @MethodSource("provideAmountTestCases")
    void testAddData_BoundaryValues(double amount, boolean shouldSucceed) {
        String reason = "Test income";
        Transaction t = TransactionFactory.createTransaction("EXPENSE", "id", amount, reason, 0);

        if (shouldSucceed) {
            // Test valid cases
            assertDoesNotThrow(() -> expenseStrategy.addTransaction(mockContext, t));
            verify(mockDbHelper).addExpense(eq(amount), eq(reason), anyLong());
        } else {
            // Test invalid cases
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> expenseStrategy.addTransaction(mockContext, t));

            assertTrue(ex.getMessage().contains("Amount is outside valid range"));
            verify(mockDbHelper, never()).addExpense(anyDouble(), anyString(), anyLong());
        }
    }

    private static Stream<Arguments> provideAmountTestCases() {
        return Stream.of(
                // Boundary Test Cases (amount, shouldSucceed)

                // Lower Boundary
                Arguments.of(0.99, false), // min- (1 cent below minimum)
                Arguments.of(1.00, true), // exact minimum
                Arguments.of(1.01, true), // min+ (1 cent above minimum)

                // Middle Range
                Arguments.of(500_000_000.00, true), // nominal value
                Arguments.of(999_999_999.99, true), // max- (1 cent below max)

                // Upper Boundary
                Arguments.of(1_000_000_000.00, true), // exact maximum
                Arguments.of(1_000_000_000.01, false), // max+ (1 cent above max)

                // Special Cases
                Arguments.of(Double.MIN_VALUE, false), // smallest possible double
                Arguments.of(Double.MAX_VALUE, false) // largest possible double
        );
    }

    private static Stream<Arguments> provideUpdateAmountTestCases() {
        return Stream.of(
                // Lower Boundary
                Arguments.of(0.99, false),
                Arguments.of(1.00, true),
                Arguments.of(1.01, true),

                // Mid Range
                Arguments.of(500_000_000.00, true),
                Arguments.of(999_999_999.99, true),

                // Upper Boundary
                Arguments.of(1_000_000_000.00, true),
                Arguments.of(1_000_000_000.01, false),

                // Special Cases
                Arguments.of(Double.MIN_VALUE, false),
                Arguments.of(Double.MAX_VALUE, false));
    }

    @ParameterizedTest
    @MethodSource("provideUpdateAmountTestCases")
    void testUpdateData_BoundaryValues(double amount, boolean shouldSucceed) {
        String id = "test_id";
        String reason = "Updated reason";
        Transaction t = TransactionFactory.createTransaction("EXPENSE", id, amount, reason, 0);

        if (shouldSucceed) {
            assertDoesNotThrow(() -> expenseStrategy.updateTransaction(mockContext, t));
            verify(mockDbHelper).updateExpense(eq(id), eq(amount), eq(reason), anyLong());
        } else {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> expenseStrategy.updateTransaction(mockContext, t));
            assertTrue(ex.getMessage().contains("Amount is outside valid range"));
            verify(mockDbHelper, never()).updateExpense(anyString(), anyDouble(), anyString(), anyLong());
        }
    }

    // ===============================================================================================
    @ParameterizedTest
    @CsvFileSource(resources = "/combined_test_data.csv", numLinesToSkip = 1)
    void testAddData_WithCsv(double amount, String reason, String rangeType) {
        System.out.printf("Running test for amount: %.2f, reason: %s, rangeType: %s%n",
                amount, reason, rangeType);

        Transaction t = TransactionFactory.createTransaction("EXPENSE", "id", amount, reason, 0);
        expenseStrategy.addTransaction(mockContext, t);
        verify(mockDbHelper).addExpense(eq(amount), eq(reason), anyLong());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/min_to_nominal.csv", numLinesToSkip = 1)
    void testAddData_WithCsv_MinToNominal(double amount, String reason, String rangeType) {
        System.out.printf("Running test for amount: %.2f, reason: %s, rangeType: %s%n", amount, reason, rangeType);

        Transaction t = TransactionFactory.createTransaction("EXPENSE", "id", amount, reason, 0);
        expenseStrategy.addTransaction(mockContext, t);
        verify(mockDbHelper).addExpense(eq(amount), eq(reason), anyLong());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/nominal_to_max.csv", numLinesToSkip = 1)
    void testAddData_WithCsv_NominalToMax(double amount, String reason, String rangeType) {
        System.out.printf("Running test for amount: %.2f, reason: %s, rangeType: %s%n", amount, reason, rangeType);

        Transaction t = TransactionFactory.createTransaction("EXPENSE", "id", amount, reason, 0);
        expenseStrategy.addTransaction(mockContext, t);
        verify(mockDbHelper).addExpense(eq(amount), eq(reason), anyLong());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/combined_test_data.csv", numLinesToSkip = 1)
    void testUpdateData_WithCsv(double amount, String reason, String rangeType) {
        System.out.printf("Running test for amount: %.2f, reason: %s, rangeType: %s%n", amount, reason, rangeType);
        String id = "1";

        Transaction t = TransactionFactory.createTransaction("EXPENSE", id, amount, reason, 0);
        expenseStrategy.updateTransaction(mockContext, t);
        verify(mockDbHelper).updateExpense(eq(id), eq(amount), eq(reason), anyLong());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/min_to_nominal.csv", numLinesToSkip = 1)
    void testUpdateData_WithCsv_MinTONominal(double amount, String reason, String rangeType) {
        System.out.printf("Running test for amount: %.2f, reason: %s, rangeType: %s%n",
                amount, reason, rangeType);
        String id = "1";

        Transaction t = TransactionFactory.createTransaction("EXPENSE", id, amount, reason, 0);
        expenseStrategy.updateTransaction(mockContext, t);
        verify(mockDbHelper).updateExpense(eq(id), eq(amount), eq(reason), anyLong());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/nominal_to_max.csv", numLinesToSkip = 1)
    void testUpdateData_WithCsv_NominalToMax(double amount, String reason, String rangeType) {
        System.out.printf("Running test for amount: %.2f, reason: %s, rangeType: %s%n",
                amount, reason, rangeType);
        String id = "1";

        Transaction t = TransactionFactory.createTransaction("EXPENSE", id, amount, reason, 0);
        expenseStrategy.updateTransaction(mockContext, t);
        verify(mockDbHelper).updateExpense(eq(id), eq(amount), eq(reason), anyLong());
    }

}
