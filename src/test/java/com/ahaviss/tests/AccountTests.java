/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.tests;

import com.ahaviss.database.Account;
import com.ahaviss.exceptions.AccountLockedException;
import com.ahaviss.logic.AccountLogic;
import com.ahaviss.logic.LoginSystem;
import com.ahaviss.testutilities.TestUtils;
import com.ahaviss.utilities.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.ahaviss.testutilities.TestUtils.executor;
import static com.ahaviss.testutilities.TestUtils.exception;
@ExtendWith(MockitoExtension.class)
@Timeout(5)
class AccountTests {
    private static String sharedHash;
    private ProjectUtils projectUtils;
    @BeforeAll
    @SuppressWarnings({"unused", "EmptyTryBlock"})
    static void beforeAll() throws Exception {
        //To connect Mockito before tests
        try (var ignored = Mockito.mockStatic(SecurityUtils.class)) {}
        sharedHash = SecurityUtils.hashPassword("123Password");
        Class<?> warmup = AdminInputTests.class;
    }
    @BeforeEach
    void setUp() throws Exception {
        executor().executeSQL("INSERT INTO accounts (account_id, account_holder, balance, account_password, credit_score) VALUES (?, ?, ?, ?, ?)", List.of(List.of(5555555, "John", 100, sharedHash, 800)));
    }
    @AfterEach
    void tearDown() throws Exception {
        executor().executeSQL("SET REFERENTIAL_INTEGRITY FALSE", null);
        executor().executeSQL("TRUNCATE TABLE accounts", null);
        executor().executeSQL("SET REFERENTIAL_INTEGRITY TRUE", null);
        projectUtils = null;
    }
    private AccountLogic createAccountLogic(ProjectUtils projectUtils) {
        return new AccountLogic(projectUtils, executor());
    }
    @Nested
    class UserInputTests {
        @Test
        @DisplayName("Test Account Creation")
        void testAccountCreation() throws Exception {
            try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
                mockedStatic.when(() -> SecurityUtils.hashPassword("123Password")).thenReturn(sharedHash);
                mockedStatic.when(() -> SecurityUtils.verifyPassword("123Password", sharedHash)).thenReturn(true);
                Map<String, Object> info = executor().executeSQL("SELECT * FROM accounts WHERE account_id = 5555555", null).getFirst().getFirst();
                assertAll(
                        () -> assertEquals(5555555, TestUtils.mockInput("").verifyInstanceOf(info.get("account_id"), Integer.class, exception())),
                        () -> assertEquals("John", info.get("account_holder").toString()),
                        () -> assertEquals(100, TestUtils.mockInput("").verifyInstanceOf(info.get("balance"), BigDecimal.class, exception()).doubleValue()),
                        () -> assertTrue(SecurityUtils.verifyPassword("123Password", info.get("account_password").toString())),
                        () -> assertEquals("ACTIVE", info.get("account_status").toString()),
                        () -> assertEquals(800, TestUtils.mockInput("").verifyInstanceOf(info.get("credit_score"), Integer.class, exception()))
                );
            }
        }
        @Test
        @DisplayName("Test logging in")
        void testLoggingIn () {
            try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
                mockedStatic.when(() -> SecurityUtils.verifyPassword("123Password", sharedHash)).thenReturn(true);
                String simulatedInput = "5555555\n123Password";
                assertDoesNotThrow(() -> new LoginSystem(TestUtils.mockInput(simulatedInput), executor()).accountLogin());
            }
        }
        @ParameterizedTest
        @CsvSource({
                "-1000, 10",
                "-10, 10",
                "-5, 5",
                "-2, 2"
        })
        @DisplayName("Test Deposits")
        void testDeposits(int transferAmountInvalid, int transferAmountValid) throws Exception {
            String simulatedInput = String.format("%s\n%s\nn\n", transferAmountInvalid, transferAmountValid);
            projectUtils = TestUtils.mockInput(simulatedInput);
            createAccountLogic(projectUtils).deposit(5555555);
            double balance = projectUtils.verifyInstanceOf(executor().executeSQL("SELECT balance FROM accounts WHERE account_id = 5555555", null).getFirst().getFirst().get("balance"), BigDecimal.class, exception()).doubleValue();
            assertEquals(100 + transferAmountValid, balance, "Negative check failed");
        }

        @Test
        @DisplayName("Test Lockout Reliability")
        void testLockoutReliability() {
            try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
                mockedStatic.when(() -> SecurityUtils.hashPassword("123Password")).thenReturn(sharedHash);
                mockedStatic.when(() -> SecurityUtils.verifyPassword("1", sharedHash)).thenReturn(false);
                String simulatedInput = "5555555\n1\n5555555\n1\n5555555\n1\n";
                assertThrows(AccountLockedException.class, () -> {
                    new LoginSystem(TestUtils.mockInput(simulatedInput), executor()).accountLogin();
                }, "Lockout reliability test failed");
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {500, 100, 50, 1000})
        @DisplayName("Test Transfers")
        void testTransfers(int transferAmount) throws Exception {
            executor().executeSQL("INSERT INTO accounts (account_id, account_holder, balance, account_password, credit_score) VALUES (?, ?, ?, ?, ?)", List.of(List.of(5555556, "John", 100, sharedHash, 800)));
            String simulatedInput;
            if (transferAmount <= 100)
                simulatedInput = String.format("5555555\n%s\nn\n", transferAmount);
            else
                simulatedInput = String.format("5555555\n%s\n", transferAmount);
            projectUtils = TestUtils.mockInput(simulatedInput);
            createAccountLogic(projectUtils).transfer(5555556);
            double source = projectUtils.verifyInstanceOf(executor().executeSQL("SELECT balance FROM accounts WHERE account_id = 5555556", null).getFirst().getFirst().get("balance"), BigDecimal.class, exception()).doubleValue();
            double recipient = projectUtils.verifyInstanceOf(executor().executeSQL("SELECT balance FROM accounts WHERE account_id = 5555555", null).getFirst().getFirst().get("balance"), BigDecimal.class, exception()).doubleValue();
            assertEquals(100 - (transferAmount <= 100 ? transferAmount : 0), source, "Source account balance is incorrect");
            assertEquals(100 + (transferAmount <= 100 ? transferAmount : 0), recipient, "Recipient account balance is incorrect");
        }
        @ParameterizedTest
        @ValueSource(ints = {100, 50, 10, 5})
        @DisplayName("Test Withdraws")
        void testWithdraws(int withdrawAmount) throws Exception{
            String simulatedInput = String.format("%s\nn", withdrawAmount);
            projectUtils = TestUtils.mockInput(simulatedInput);
            createAccountLogic(TestUtils.mockInput(simulatedInput)).withdraw(5555555);
            double balance = projectUtils.verifyInstanceOf(executor().executeSQL("SELECT balance FROM accounts WHERE account_id = 5555555", null).getFirst().getFirst().get("balance"), BigDecimal.class, exception()).doubleValue();
            assertEquals((100 - withdrawAmount), balance, "Withdraw test failed");
        }
        @ParameterizedTest
        @CsvSource({
                "incorrect, whatever, 123Password, UnguessablePassword@123",
                "random, 123, 123Password, SecretPassword1",
                "wrong, 123Paswordd, 123Password, BankPassword123",
                "stopTrying, 12GetMeIn, 123Password, NewPassword123"
        })
        @DisplayName("Test User Changing Password")
        void testUserChangingPassword (String incorrectPassword1, String incorrectPassword2, String correctPassword, String passwordToHash) throws Exception {
            try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
                mockedStatic.when(() -> SecurityUtils.verifyPassword(anyString(), eq(sharedHash))).thenAnswer(arg -> {
                    String password = arg.getArgument(0);
                    if (correctPassword.equals(password)) return true;
                    else return false;
                });
                mockedStatic.when(() -> SecurityUtils.hashPassword(passwordToHash)).thenReturn("mocked_hash");
                String simulatedInput = String.format("%s\n%s\n%s\n%s", incorrectPassword1, incorrectPassword2, correctPassword, passwordToHash);
                createAccountLogic(TestUtils.mockInput(simulatedInput)).editPassword(5555555);
                String password = executor().executeSQL("SELECT account_password FROM accounts WHERE account_id = 5555555", null).getFirst().getFirst().get("account_password").toString();
                assertEquals("mocked_hash", password, "Incorrect account password change.");
            }
        }
    }
    @Nested
    class AdminInputTests {
        @BeforeEach
        void setUp () throws Exception {
            executor().executeSQL("INSERT INTO admins (admin_id, admin_name, admin_password) VALUES (?, ?, ?)", List.of(List.of(5555555, "ahaviss", sharedHash)));
        }
        @AfterEach
        void tearDown () throws Exception {
            executor().executeSQL("SET REFERENTIAL_INTEGRITY FALSE", null);
            executor().executeSQL("TRUNCATE TABLE admins", null);
            executor().executeSQL("SET REFERENTIAL_INTEGRITY TRUE", null);
        }
        @ParameterizedTest
        @CsvSource({
                "1, 123Passwordd",
                "123password, 999Password",
                "Password, 1000Secure",
                "12Pass, 1001SecurePassword"
        })
        @DisplayName("Test Changing Password")
        void testChangingPassword(String newPasswordInvalid, String newPasswordValid) throws Exception{
            try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
                mockedStatic.when(() -> SecurityUtils.hashPassword(newPasswordValid))
                        .thenReturn("completely_mocked_hash");
                mockedStatic.when(() -> SecurityUtils.verifyPassword(newPasswordValid, "completely_mocked_hash"))
                        .thenReturn(true);
                String simulatedInput = String.format("%s\n%s\n", newPasswordInvalid, newPasswordValid);
                createAccountLogic(TestUtils.mockInput(simulatedInput)).editPasswordAdmin(5555555, 5555555);
                String password = executor().executeSQL("SELECT account_password FROM accounts WHERE account_id = 5555555", null).getFirst().getFirst().get("account_password").toString();
                assertTrue(SecurityUtils.verifyPassword(newPasswordValid, password), "Incorrect account password change.");
            }
        }
        @ParameterizedTest
        @ValueSource(ints = {5555555, 1000000, 3434343, 9999999})
        @DisplayName("Test Deleting Accounts")
        void testDeletingAccounts(int accountId) throws Exception {
            executor().executeSQL("REPLACE INTO accounts (account_id, account_holder, balance, account_password, credit_score) VALUES (?, ?, ?, ?, ?)", List.of(List.of(accountId, "John", 100, sharedHash, 800)));
            String simulatedInput = String.format("1\n%s\n", accountId);
            projectUtils = TestUtils.mockInput(simulatedInput);
            createAccountLogic(projectUtils).deleteAccounts(5555555);
            assertFalse(projectUtils.idExists(accountId, Account.class));
        }
        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4})
        @DisplayName("Test Locking Accounts")
        void testLockingAccounts (int amountOfTimesLocked) throws Exception {
            executor().executeSQL("UPDATE accounts SET times_locked = ? WHERE account_id = 5555555", List.of(List.of(amountOfTimesLocked)));
            projectUtils = TestUtils.mockInput("");
            createAccountLogic(projectUtils).lockAccount(5555555);
            int expected;
            if (amountOfTimesLocked == 0)
                expected = 30;
            else if (amountOfTimesLocked == 1)
                expected = 60;
            else if (amountOfTimesLocked == 2)
                expected = 120;
            else if (amountOfTimesLocked >= 3)
                expected = Integer.MAX_VALUE;
            else
                expected = Integer.MIN_VALUE;
            int duration = projectUtils.verifyInstanceOf(executor().executeSQL("SELECT duration_locked FROM accounts WHERE account_id = 5555555", null).getFirst().getFirst().get("duration_locked"), Integer.class, exception());
            assertEquals(expected, duration);
        }
        @ParameterizedTest
        @CsvSource({
                "'', David",
                "'', Bob",
                "'', Emily",
                "'', Robert"
        })
        @DisplayName("Test Changing Account Holder")
        void testChangingAccountHolder (String accountHolderInvalid, String accountHolderValid) throws Exception {
            String simulatedInput = String.format("%s\n%s\n", accountHolderInvalid, accountHolderValid);
            createAccountLogic(TestUtils.mockInput(simulatedInput)).editAccountHolder(5555555, 5555555);
            String accountHolder = executor().executeSQL("SELECT account_holder FROM accounts WHERE account_id = 5555555", null).getFirst().getFirst().get("account_holder").toString();
            assertEquals(accountHolderValid, accountHolder, "Incorrect account holder change");
        }
        @ParameterizedTest
        @CsvSource ({
                "100, 1000, 650",
                "0, 900, 800",
                "499, 801, 800",
                "109, 10029, 750"
        })
        @DisplayName("Test Changing Credit Score")
        void testChangingCreditScore (int invalidCreditScore1, int invalidCreditScore2, int validCreditScore) throws Exception {
            String simulatedInput = String.format("%s\n%s\n%s", invalidCreditScore1, invalidCreditScore2, validCreditScore);
            projectUtils = TestUtils.mockInput(simulatedInput);
            createAccountLogic(projectUtils).editCreditScore(5555555, 5555555);
            int creditScore = projectUtils.verifyInstanceOf(executor().executeSQL("SELECT credit_score FROM accounts WHERE account_id = 5555555", null).getFirst().getFirst().get("credit_score"), Integer.class, exception());
            assertEquals(validCreditScore, creditScore);
        }
    }
    @AfterAll
    static void afterAll() {executor().close();}
}
