package com.ahaviss.tests;

import com.ahaviss.database.Account;
import com.ahaviss.database.Admin;
import com.ahaviss.enums.AccountStatus;
import com.ahaviss.exceptions.AccountLockedException;
import com.ahaviss.logic.AccountLogic;
import com.ahaviss.logic.LoginSystem;
import com.ahaviss.testutilities.TestUtils;
import com.ahaviss.utilities.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@Timeout(5)
class AccountTests {
    private Account account;
    @BeforeEach
    void setUp() {
        account = new Account(5555555, "John", 100, SecurityUtils.hashPassword("123Password"), AccountStatus.ACTIVE, 800);
    }
    @AfterEach
    void tearDown() {
        account = null;
        ProjectUtils.resetReader();
    }
    @Nested
    class UserInputTests {
        @Test
        @DisplayName("Test Account Creation")
        void testAccountCreation() {
            assertAll(
                    () -> assertEquals(5555555, account.getAccountId()),
                    () -> assertEquals("John", account.getAccountHolder()),
                    () -> assertEquals(100, account.getBalance()),
                    () -> assertTrue(SecurityUtils.verifyPassword("123Password", account.getAccountPassword())),
                    () -> assertEquals(AccountStatus.ACTIVE, account.getAccountStatus()),
                    () -> assertEquals(800, account.getCreditScore())
            );
        }

        @ParameterizedTest
        @CsvSource({
                "-1000, 10",
                "-10, 10",
                "-5, 5",
                "-2, 2"
        })
        @DisplayName("Test deposits")
        void testDeposits(int transferAmountInvalid, int transferAmountValid) {
            String simulatedInput2 = String.format("%s\n%s\nn\n", transferAmountInvalid, transferAmountValid);
            TestUtils.mockInput(simulatedInput2);
            AccountLogic.deposit(account);
            assertEquals(100 + transferAmountValid, account.getBalance(), "Negative check failed");
        }

        @Test
        @DisplayName("Test lockout reliability")
        void testLockoutReliability() {
            String simulatedInput = "5555555\n1\n5555555\n1\n5555555\n1\n";
            TestUtils.mockInput(simulatedInput);
            assertThrows(AccountLockedException.class, () -> {
                Map<Integer, Account> list = new HashMap<>();
                list.put(account.getAccountId(), account);
                LoginSystem.accountLogin(list);
            });
        }

        @ParameterizedTest
        @ValueSource(ints = {500, 100, 50, 1000})
        @DisplayName("Test Transfers")
        void testTransfers(int transferAmount) {
            String hashedPassword = SecurityUtils.hashPassword("123Password");
            Account account2 = new Account(5555556, "John", 100, hashedPassword, AccountStatus.ACTIVE, 800);
            Map<Integer, Account> accounts = new HashMap<>();
            accounts.put(account.getAccountId(), account);
            accounts.put(account2.getAccountId(), account2);
            String simulatedInput;
            if (transferAmount <= 100)
                simulatedInput = String.format("5555555\n%s\nn\n", transferAmount);
            else
                simulatedInput = String.format("5555555\n%s\n", transferAmount);

            TestUtils.mockInput(simulatedInput);
            AccountLogic.transfer(accounts, account2);
            assertEquals(100 - (transferAmount <= 100 ? transferAmount : 0), account2.getBalance(), "Source account balance is incorrect");
            assertEquals(100 + (transferAmount <= 100 ? transferAmount : 0), account.getBalance(), "Recipient account balance is incorrect");
        }
    }
    @Nested
    class AdminInputTests {
        private static final Admin admin = new Admin(5555555, "ahaviss", SecurityUtils.hashPassword("123Password"));
        @ParameterizedTest
        @CsvSource({
                "1, 123Passwordd",
                "123password, 999Password",
                "Password, 1000Secure",
                "12Pass, 1001SecurePassword"
        })
        @DisplayName("Test Changing password")
        void testChangingPassword(String newPasswordInvalid, String newPasswordValid) {
            String simulatedInput = String.format("%s\n%s\n", newPasswordInvalid, newPasswordValid);
            TestUtils.mockInput(simulatedInput);
            AccountLogic.editPasswordAdmin(account, admin);
            assertTrue(SecurityUtils.verifyPassword(newPasswordValid, account.getAccountPassword()), "Incorrect password change.");
        }
        @ParameterizedTest
        @ValueSource(ints = {5555555, 1000000, 3434343})
        @DisplayName("Test Deleting Accounts")
        void testDeletingAccounts(int accountId) {
            final Map<Integer, Account> accounts = new HashMap<>();
            String hashedPassword = SecurityUtils.hashPassword("123Password");
            accounts.put(accountId, new Account (accountId, "John", 100, hashedPassword, AccountStatus.ACTIVE, 800));
            String simulatedInput = String.format("1\n%s\n", accountId);
            TestUtils.mockInput(simulatedInput);
            AccountLogic.deleteAccounts(accounts, admin);
            assertNull(accounts.get(accountId));
        }
        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4})
        @DisplayName("Test Locking Accounts")
        void testLockingAccounts (int amountOfTimesLocked) {
            account.setAmountOfTimesLocked(amountOfTimesLocked);
            AccountLogic.lockAccount(account);
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
            assertEquals(expected, account.getDurationLocked());
        }
        @ParameterizedTest
        @CsvSource({
                "'', David",
                "'', Bob",
                "'', Emily",
                "'', Robert"
        })
        @DisplayName("Test Changing Account Holder")
        void testChangingAccountHolder (String accountHolderInvalid, String accountHolderValid) {
            String simulatedInput = String.format("%s\n%s\n", accountHolderInvalid, accountHolderValid);
            TestUtils.mockInput(simulatedInput);
            AccountLogic.editAccountHolder(account, admin);
            assertEquals(accountHolderValid, account.getAccountHolder());
        }
    }
}
