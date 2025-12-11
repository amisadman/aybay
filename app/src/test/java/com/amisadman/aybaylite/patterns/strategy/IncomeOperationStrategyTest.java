package com.amisadman.aybaylite.patterns.strategy;

import static org.junit.jupiter.api.Assertions.*;

import android.content.Context;

import com.amisadman.aybaylite.Repo.DatabaseHelper;
import com.amisadman.aybaylite.model.Transaction;
import com.amisadman.aybaylite.patterns.factory.TransactionFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;

import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class IncomeOperationStrategyTest {

    @Mock
    private DatabaseHelper mockDbHelper;

    @Mock
    private Context mockContext;

    private IncomeOperationStrategy incomeStrategy;

    @BeforeEach
    void setUp() {
        incomeStrategy = new IncomeOperationStrategy();
        incomeStrategy.setDatabaseHelper(mockDbHelper);
    }

    @Test
    void testConstructorWithContext() {
        // Strategy doesn't have Context constructor anymore
    }

    @ParameterizedTest
    @CsvSource({
            "'1', 100.50, Salary bonus",
            "'2', 250.75, Freelance work",
            "'3', 99.99, Sold item"
    })
    void testUpdateData(String id, double amount, String reason) {
        // Act
        Transaction t = TransactionFactory.createTransaction("INCOME", id, amount, reason, 0);
        incomeStrategy.updateTransaction(mockContext, t);

        // Assert
        verify(mockDbHelper).updateIncome(eq(id), eq(amount), eq(reason), anyLong());
    }

    @ParameterizedTest
    @CsvSource({
            "500.00, Monthly salary",
            "150.50, Consulting fee",
            "75.25, Gift received"
    })
    void testAddData(double amount, String reason) {
        // Act
        Transaction t = TransactionFactory.createTransaction("INCOME", "id", amount, reason, 0);
        incomeStrategy.addTransaction(mockContext, t);

        // Assert
        verify(mockDbHelper).addIncome(eq(amount), eq(reason), anyLong());
    }

    @ParameterizedTest
    @ValueSource(doubles = { 0.01, 1.00, 1000.00, 999999.99 })
    void testAddDataWithVariousAmounts(double amount) {
        // Act
        Transaction t = TransactionFactory.createTransaction("INCOME", "id", amount, "Test reason", 0);
        incomeStrategy.addTransaction(mockContext, t);

        // Assert
        verify(mockDbHelper).addIncome(eq(amount), eq("Test reason"), anyLong());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "A", "Long reason with spaces and special chars: !@#$%^&*()" })
    void testAddDataWithVariousReasons(String reason) {
        // Act
        Transaction t = TransactionFactory.createTransaction("INCOME", "id", 100.00, reason, 0);
        incomeStrategy.addTransaction(mockContext, t);

        // Assert
        verify(mockDbHelper).addIncome(eq(100.00), eq(reason), anyLong());
    }

    @ParameterizedTest
    @MethodSource("provideUpdateDataTestCases")
    void testUpdateDataWithVariousCases(String id, double amount, String reason) {
        Transaction t = TransactionFactory.createTransaction("INCOME", id, amount, reason, 0);
        incomeStrategy.updateTransaction(mockContext, t);
        verify(mockDbHelper).updateIncome(eq(id), eq(amount), eq(reason), anyLong()); // Original test expected
        // updateExpense call? Wait,
        // verify(mockDbHelper).updateExpense(id, amount,
        // reason); was in Helper test.
        // Checking Helper Logic: AddIncomeHelper used updateIncome internally in the
        // code I last saw, but test verify called updateExpense? Let me double check
        // that.
        // AddIncomeHelper.java said: dbHelper.updateIncome(id, amount, reason);
        // AddIncomeHelperTest.java said: verify(mockDbHelper).updateExpense(id, amount,
        // reason); <-- ERROR IN ORIGINAL TEST OR HELPER?
        // AddIncomeHelper calls dbHelper.updateIncome.
        // AddIncomeHelperTest verify calls mockDbHelper.updateExpense. This would have
        // FAILED if run before. Mismatch?
        // Ah, looking at AddIncomeHelperTest.java provided content:
        // verify(mockDbHelper).updateExpense...
        // This suggests copy-paste error in original test. I will correct it to verify
        // updateIncome, which is correct for IncomeStrategy.
        // Actually, looking closely at AddIncomeHelperTest content provided earlier:
        // line 62: verify(mockDbHelper).updateExpense...
        // This suggests copy-paste error in original test. I will correct it to verify
        // updateIncome.
    }

    private static Stream<Arguments> provideUpdateDataTestCases() {
        return Stream.of(
                // Normal cases
                Arguments.of("1", 100.50, "Salary bonus"),
                Arguments.of("inc_2", 250.75, "Freelance work"),

                // Edge cases
                Arguments.of("id-with-dash", 1.00, "Minimum amount"),
                Arguments.of("ID_WITH_UNDERSCORE", 999999.99, "Maximum reasonable amount"),
                Arguments.of("special!@#", 500.00, "Special chars in ID"),
                Arguments.of("empty_reason", 75.25, ""),
                Arguments.of("long_id_1234567890", 300.00, "Very long reason text that might exceed normal limits " +
                        "and test how the system handles lengthy input strings for the reason field"));
    }

    @ParameterizedTest
    @MethodSource("provideAddDataTestCases")
    void testAddDataWithExtraParameter(double amount, String reason) {
        Transaction t = TransactionFactory.createTransaction("INCOME", "id", amount, reason, 0);
        incomeStrategy.addTransaction(mockContext, t);
        verify(mockDbHelper).addIncome(eq(amount), eq(reason), anyLong());
    }

    private static Stream<Arguments> provideAddDataTestCases() {
        return Stream.of(

                Arguments.of(2000.00, "Monthly salary"),
                Arguments.of(150.50, "Consulting fee"),

                Arguments.of(0.01, "Rounding adjustment"),
                Arguments.of(1_000_000.00, "Annual bonus"),
                Arguments.of(500.00, "Gift "),
                Arguments.of(75.25, "A"),
                Arguments.of(300.00, "Refund for cancelled service in Q1 2023"),
                Arguments.of(420.69, "Miscellaneous"));
    }
    // ===============================================================================================

    // Boundary value testing

    @ParameterizedTest
    @MethodSource("provideAmountTestCases")
    void testAddData_BoundaryValues(double amount, boolean shouldSucceed) {
        String reason = "Test income";
        Transaction t = TransactionFactory.createTransaction("INCOME", "id", amount, reason, 0);

        if (shouldSucceed) {
            // Test valid cases
            assertDoesNotThrow(() -> incomeStrategy.addTransaction(mockContext, t));
            verify(mockDbHelper).addIncome(eq(amount), eq(reason), anyLong());
        } else {
            // Test invalid cases
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> incomeStrategy.addTransaction(mockContext, t));

            assertTrue(ex.getMessage().contains("Amount is outside valid range"));
            verify(mockDbHelper, never()).addIncome(anyDouble(), anyString(), anyLong());
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

    private static Stream<Arguments> provideIncomeUpdateTestCases() {
        return Stream.of(
                // Lower Boundary
                Arguments.of(0.99, false),
                Arguments.of(1.00, true),
                Arguments.of(1.01, true),

                // Middle Range
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
    @MethodSource("provideIncomeUpdateTestCases")
    void testUpdateData_BoundaryValues(double amount, boolean shouldSucceed) {
        String id = "income_id";
        String reason = "Updated income reason";
        Transaction t = TransactionFactory.createTransaction("INCOME", id, amount, reason, 0);

        if (shouldSucceed) {
            assertDoesNotThrow(() -> incomeStrategy.updateTransaction(mockContext, t));
            verify(mockDbHelper).updateIncome(eq(id), eq(amount), eq(reason), anyLong());
        } else {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> incomeStrategy.updateTransaction(mockContext, t));

            assertTrue(ex.getMessage().contains("Amount is outside valid range"));
            verify(mockDbHelper, never()).updateIncome(anyString(), anyDouble(), anyString(), anyLong());
        }
    }

    // ===============================================================================================
    // Random generated data
    @ParameterizedTest
    @CsvFileSource(resources = "/combined_test_data.csv", numLinesToSkip = 1)
    void testAddData_WithCsv(double amount, String reason, String rangeType) {
        System.out.printf("Running test for amount: %.2f, reason: %s, rangeType: %s%n",
                amount, reason, rangeType);

        Transaction t = TransactionFactory.createTransaction("INCOME", "id", amount, reason, 0);
        incomeStrategy.addTransaction(mockContext, t);
        verify(mockDbHelper).addIncome(eq(amount), eq(reason), anyLong());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/min_to_nominal.csv", numLinesToSkip = 1)
    void testAddData_WithCsv_MinToNominal(double amount, String reason, String rangeType) {
        System.out.printf("Running test for amount: %.2f, reason: %s, rangeType: %s%n", amount, reason, rangeType);

        Transaction t = TransactionFactory.createTransaction("INCOME", "id", amount, reason, 0);
        incomeStrategy.addTransaction(mockContext, t);
        verify(mockDbHelper).addIncome(eq(amount), eq(reason), anyLong());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/nominal_to_max.csv", numLinesToSkip = 1)
    void testAddData_WithCsv_MaxToNominal(double amount, String reason, String rangeType) {
        System.out.printf("Running test for amount: %.2f, reason: %s, rangeType: %s%n", amount, reason, rangeType);

        Transaction t = TransactionFactory.createTransaction("INCOME", "id", amount, reason, 0);
        incomeStrategy.addTransaction(mockContext, t);
        verify(mockDbHelper).addIncome(eq(amount), eq(reason), anyLong());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/combined_test_data.csv", numLinesToSkip = 1)
    void testUpdateData_WithCsv(double amount, String reason, String rangeType) {
        System.out.printf("Running test for amount: %.2f, reason: %s, rangeType: %s%n", amount, reason, rangeType);
        String id = "1";

        Transaction t = TransactionFactory.createTransaction("INCOME", id, amount, reason, 0);
        incomeStrategy.updateTransaction(mockContext, t);
        verify(mockDbHelper).updateIncome(eq(id), eq(amount), eq(reason), anyLong());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/min_to_nominal.csv", numLinesToSkip = 1)
    void testUpdateData_WithCsv_MinTONominal(double amount, String reason, String rangeType) {
        System.out.printf("Running test for amount: %.2f, reason: %s, rangeType: %s%n",
                amount, reason, rangeType);
        String id = "1";

        Transaction t = TransactionFactory.createTransaction("INCOME", id, amount, reason, 0);
        incomeStrategy.updateTransaction(mockContext, t);
        verify(mockDbHelper).updateIncome(eq(id), eq(amount), eq(reason), anyLong());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/nominal_to_max.csv", numLinesToSkip = 1)
    void testUpdateData_WithCsv_NominalToMax(double amount, String reason, String rangeType) {
        System.out.printf("Running test for amount: %.2f, reason: %s, rangeType: %s%n",
                amount, reason, rangeType);
        String id = "1";

        Transaction t = TransactionFactory.createTransaction("INCOME", id, amount, reason, 0);
        incomeStrategy.updateTransaction(mockContext, t);
        verify(mockDbHelper).updateIncome(eq(id), eq(amount), eq(reason), anyLong());
    }
}
