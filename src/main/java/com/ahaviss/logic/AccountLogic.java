/*
 * Copyright [2026] [Ahaviss]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ahaviss.logic;
//Local imports
import com.ahaviss.database.*;
import com.ahaviss.enums.AccountStatus;
import com.ahaviss.logs.enums.Action;
import com.ahaviss.logs.enums.User;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.utilities.SQLExecutor;
import com.ahaviss.utilities.SecurityUtils;
import net.sf.jsqlparser.JSQLParserException;
//Java imports
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.time.LocalDateTime;
public class AccountLogic {
    //RNG for account ID
    private final Random random = new Random();
    private final ProjectUtils projectUtils;
    private final SQLExecutor executor;
    public AccountLogic (ProjectUtils projectUtils, SQLExecutor executor) {
        this.projectUtils = projectUtils;
        this.executor = executor;
    }
    public void withdraw (int accountId) throws SQLException, JSQLParserException {
        while (true) {
            //Fetches account data
            Map<String, Object> accountData = executor
                    .executeSQL("SELECT balance, account_holder FROM accounts WHERE account_id = ?", List.of(List.of(accountId)))
                    .getFirst().getFirst();
            //Gets previous balance, account holder, and withdraw amount
            double prevBalance = ((BigDecimal) accountData.get("balance")).doubleValue();
            String accountHolder = accountData.get("account_holder").toString();
            double withdrawAmount = projectUtils.getValidDouble(String.format("Enter the amount you want to withdraw (%.2f available): ", prevBalance));
            //Verifies withdraw amount
            if (withdrawAmount > prevBalance) { System.out.println("Insufficient balance."); return; }
            if (withdrawAmount == 0) { System.out.println("No money taken out"); return; }

            String source = String.format("%d (%s)", accountId, accountHolder);
            //Updates balance, adds withdrawal, and inserts log
            executor.executeSQL("""
                UPDATE accounts SET balance = balance - ? WHERE account_id = ? AND balance >= ?;
                INSERT INTO withdrawals (account_id, amount) VALUES (?, ?);
                INSERT INTO audit_logs (action, performed_by, source, before_value, after_value) VALUES (?, ?, ?, ?, ?)
                """,
                    List.of(
                            List.of(withdrawAmount, accountId, withdrawAmount),
                            List.of(accountId, withdrawAmount),
                            List.of(Action.WITHDRAW.getAction(),
                                    User.USER.getValue(),
                                    source,
                                    String.valueOf(prevBalance),
                                    String.valueOf(prevBalance - withdrawAmount))
                    )
            );
            while (true) {
                //Asks if the user wants to make another withdrawal
                String answer = projectUtils.getValidString("Withdrawal successful. Do you want to make another withdrawal? Y/N");
                if (answer.equalsIgnoreCase("N")) {
                    return;
                } else if (answer.equalsIgnoreCase("Y")) {
                    break;
                } else {
                    System.out.println("Invalid input. Please enter Y or N.");
                }
            }
        }
    }
    public void deposit (int accountId) throws SQLException, JSQLParserException {
        while (true) {
            Map<String, Object> accountData = executor
                    .executeSQL("SELECT balance, account_holder FROM accounts WHERE account_id = ?", List.of(List.of(accountId)))
                    .getFirst().getFirst();
            double prevBalance = ((BigDecimal) accountData.get("balance")).doubleValue();
            double depositAmount = projectUtils.getValidDouble("Enter the amount you want to deposit: ");
            if (depositAmount == 0) {
                System.out.println("No money added");
                return;
            }
            String accountHolder = accountData.get("account_holder").toString();
            String source = String.format("%d (%s)", accountId, accountHolder);

            executor.executeSQL("""
                UPDATE accounts SET balance = balance + ? WHERE account_id = ?;
                INSERT INTO deposits (account_id, amount) VALUES (?, ?);
                INSERT INTO audit_logs (action, performed_by, source, before_value, after_value) VALUES (?, ?, ?, ?, ?)
                """,
                    List.of(
                            List.of(depositAmount, accountId),
                            List.of(accountId, depositAmount),
                            List.of(Action.DEPOSIT.getAction(), User.USER.getValue(), source, String.valueOf(prevBalance), String.valueOf(prevBalance + depositAmount))
                    )
            );
            //Asks if the user wants to make another deposit
            while (true) {
                String answer = projectUtils.getValidString("Deposit successful. Do you want to make another deposit? Y/N");
                if (answer.equalsIgnoreCase("Y")) {
                    break;
                } else if (answer.equalsIgnoreCase("N")) {
                    return;
                } else {
                    System.out.println("Invalid input. Please enter Y or N.");
                }
            }
        }
    }
    public void transfer (int currentAccountId) throws SQLException, JSQLParserException {
        //Asks the user for the recipient ID and amount to transfer
        while (true) {
            int recipientAccountId = projectUtils.getValidInt("Enter the ID of the recipient account: ");
            if (recipientAccountId == currentAccountId) {
                System.out.println("You cannot transfer money to yourself.");
                return;
            }
            List<List<Map<String, Object>>> accountDataList = executor
                    .executeSQL("""
                            SELECT balance, account_holder FROM accounts WHERE account_id = ?;
                            SELECT balance, account_holder FROM accounts WHERE account_id = ?
                            """, List.of(List.of(currentAccountId), List.of(recipientAccountId)));
            double prevBalance1 = ((BigDecimal) accountDataList.getFirst().getFirst().get("balance")).doubleValue();
            double transferAmount = projectUtils.getValidDouble(String.format("Enter the amount you want to transfer (%.2f available): ", prevBalance1));
            if (transferAmount == 0) {
                System.out.println("No money transferred");
                return;
            }
            //Validates amount to transfer
            if (transferAmount > prevBalance1) {
                System.out.println("Insufficient balance.");
                return;
            }
            //Checks if the account is found
            if (accountDataList.get(1).isEmpty()) {
                System.out.printf("Account ID %d not found.%n", recipientAccountId);
                continue;
            }
            double prevBalance2 = ((BigDecimal) accountDataList.get(1).getFirst().get("balance")).doubleValue();
            try (Connection connection = executor.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    String sourceAccount = String.format("%d (%s)", currentAccountId, accountDataList.getFirst().getFirst().get("account_holder"));
                    String recipientAccount = String.format("%d (%s)", recipientAccountId, accountDataList.get(1).getFirst().get("account_holder"));

                    executor.executeTransactionalSQL(connection, """
                        UPDATE accounts SET balance = balance - ? WHERE account_id = ? AND balance >= ?;
                        UPDATE accounts SET balance = balance + ? WHERE account_id = ?;
                        INSERT INTO transfers (source_account_id, target_account_id, amount) VALUES (?, ?, ?);
                        INSERT INTO audit_logs (action, performed_by, source, target, before_value, after_value) VALUES (?, ?, ?, ?, ?, ?)
                    """,
                            List.of(
                                    List.of(transferAmount, currentAccountId, transferAmount),
                                    List.of(transferAmount, recipientAccountId),
                                    List.of(currentAccountId, recipientAccountId, transferAmount),
                                    List.of(Action.TRANSFER.getAction(),
                                            User.USER.getValue(),
                                            sourceAccount,
                                            recipientAccount,
                                            String.format("(Source) %.2f, (Recipient) %.2f", prevBalance1, prevBalance2),
                                            String.format("(Source) %.2f, (Recipient) %.2f", prevBalance1 - transferAmount, prevBalance2 + transferAmount))
                            )
                    );

                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            }
            while (true) {
                String answer = projectUtils.getValidString("Transfer successful. Do you want to make another transfer? Y/N");
                if (answer.equalsIgnoreCase("Y")) {
                    break;
                } else if (answer.equalsIgnoreCase("N")) {
                    return;
                } else {
                    System.out.println("Invalid input. Please enter Y or N.");
                }
            }
        }
    }
    private Account getAccountDetails () throws SQLException, JSQLParserException {
        while (true) {
            //Asks for the account details
            String accountHolder = projectUtils.getValidUsername("Enter the account holder's name: ", 100);
            double balance = projectUtils.getValidDouble("Enter the account holder's balance: ");
            int creditScore = projectUtils.getValidInt("Enter the account holder's credit score");
            //Validates the credit score
            if (creditScore < 500 || creditScore > 800) {
                System.out.println("Invalid credit score. Please enter a number between 500 and 800.");
                continue;
            }
            //Asks for the account password
            String tempAccountPassword = projectUtils.getValidPassword("Enter the account holder's password: ");
            String accountPassword = SecurityUtils.hashPassword(tempAccountPassword);
            //Generates a random account ID
            int accountId = random.nextInt(9999999 - 1000000 + 1) + 1000000;
            //Makes sure that the ID is not already taken
            while (projectUtils.idExists(accountId, Account.class)) {
                accountId++;
                if (accountId > 9999999) {
                    accountId = 1000000;
                }
            }
            executor.executeSQL("INSERT INTO accounts (account_id, account_holder, balance, account_password, credit_score) VALUES (?, ?, ?, ?, ?)", List.of(List.of(accountId, accountHolder, balance, accountPassword, creditScore)));
            return new Account(accountId, accountHolder, balance, accountPassword, AccountStatus.ACTIVE, creditScore);
        }
    }
    public void createAccount (int adminId) throws SQLException, JSQLParserException {
        //Asks the user for the number of accounts to add
        int amountOfAccountToAdd = projectUtils.getValidInt("Enter the amount of accounts you want to add: ");
        //Gets account details
        for (int i = 0; i < amountOfAccountToAdd; i++) {
            //Call getAccountDetails method
            Account account = getAccountDetails();
            //Print success message and student ID
            System.out.println("Account ID: " + account.getAccountId());
            System.out.println("Account created successfully!");
            if (adminId == -1) return;
            String adminName = executor.executeSQL("SELECT admin_name FROM admins WHERE admin_id = ?", List.of(List.of(adminId))).getFirst().getFirst().get("admin_name").toString();
            String source = String.format("%d (%s)", adminId, adminName);
            String target = String.format("%d (%s)", account.getAccountId(), account.getAccountHolder());
            executor.executeSQL("INSERT INTO audit_logs (action, performed_by, source, target) VALUES (?, ?, ?, ?)", List.of(List.of(Action.CREATE_ACCOUNT.getAction(), User.ADMIN.getValue(), source, target)));
        }
    }
    public void deleteAccounts (int adminId) throws SQLException, JSQLParserException {
        //Checks if the accounts list is empty
        if (!projectUtils.tableHasContents(Account.class)) {
            System.out.println("No accounts available. Please create an account.");
            return;
        }
        while (true) {
            try {
                //Asks the number of accounts to delete
                int totalAccounts = projectUtils.sizeOfTable(Account.class);
                int amountOfPeople = projectUtils.getValidInt(String.format("Enter the amount of accounts you want to delete (%d total accounts): ", totalAccounts));
                //Validates input
                if (amountOfPeople > totalAccounts) {
                    System.out.println("Invalid input. Please enter a number less than or equal to the number of accounts.");
                    continue;
                }
                for (int i = 0; i < amountOfPeople; i++) {
                    while (true) {
                        //Asks for the account ID to delete
                        int accountId = projectUtils.getValidInt("Enter the ID of the account you want to delete: ");
                        //Checks if the account is found
                        if (executor.executeSQL("SELECT account_id FROM accounts WHERE account_id = ?", List.of(List.of(accountId))).getFirst().isEmpty()) {
                            System.out.printf("Account ID %d not found.%n", accountId);
                            continue;
                        }
                        String[] info = getCommonInfo(accountId, adminId);
                        executor.executeSQL("DELETE FROM accounts WHERE account_id = ?", List.of(List.of(accountId)));
                        if (adminId == -1) return;
                        //Deletes the account
                        executor.executeSQL("""
                                INSERT INTO audit_logs (action, performed_by, source, target) VALUES (?, ?, ?, ?)
                        """, List.of(List.of(Action.DELETE_ACCOUNT.getAction(), User.ADMIN.getValue(), info[0], info[1])));
                        System.out.println("Account deleted successfully!");
                        break;
                    }
                }
                return;
            }
            //Catch invalid input
            catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    private String[] getCommonInfo (int accountId, int adminId) throws SQLException, JSQLParserException {
        List<List<Map<String, Object>>> info = executor.executeSQL("""
                            SELECT account_holder FROM accounts WHERE account_id = ?;
                            SELECT admin_name FROM admins WHERE admin_id = ?
                        """, List.of(List.of(accountId), List.of(adminId)));
        return new String[]{String.format("%d (%s)", adminId, info.get(1).getFirst().get("admin_name").toString()),
                String.format("%d (%s)", accountId, info.getFirst().getFirst().get("account_holder").toString())};
    }
    public void editPassword (int accountId) throws SQLException, JSQLParserException {
        String prevPassword = executor.executeSQL("SELECT account_password FROM accounts WHERE account_id = ?", List.of(List.of(accountId))).getFirst().getFirst().get("account_password").toString();
        while (true) {
            try {
                String currentPassword;
                boolean passwordValidated = false;
                for (int i = 0; i < 3; i++) {
                    //Asks the user for the current password and validates it
                    System.out.printf("Password change attempt %d/3%n", i + 1);
                    currentPassword = projectUtils.getValidString("Enter the current password: ");
                    if (!SecurityUtils.verifyPassword(currentPassword, prevPassword)) {
                        System.out.println("Incorrect password. Please try again.");
                    } else {
                        passwordValidated = true;
                        break;
                    }
                }
                //If the password is not validated, return
                if (!passwordValidated) {
                    System.out.println("Password change failed. Please try again.");
                    return;
                }
                //Asks the user for the new password, validates it and sets it
                String tempPassword = projectUtils.getValidPassword("Enter the new password: ");
                String password = SecurityUtils.hashPassword(tempPassword);
                String source = String.format("%d (%s)", accountId, executor.executeSQL("SELECT account_holder FROM accounts WHERE account_id = ?", List.of(List.of(accountId))).getFirst().getFirst().get("account_holder").toString());
                executor.executeSQL("""
                    UPDATE accounts SET account_password = ? WHERE account_id = ?;
                    INSERT INTO audit_logs (action, performed_by, source, before_value, after_value) VALUES (?, ?, ?, ?, ?)
                """, List.of(List.of(password, accountId),
                        List.of(Action.CHANGE_PASSWORD.getAction(),
                        User.USER.getValue(),
                        source,
                        "[REDACTED]",
                        "[REDACTED]")));
                break;
                //Catch invalid input
            } catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public void editPasswordAdmin (int accountId, int adminId) throws SQLException, JSQLParserException {
        try {
            String tempNewPassword = projectUtils.getValidPassword("Please enter the new account password: ");
            String newPassword = SecurityUtils.hashPassword(tempNewPassword);
            executor.executeSQL("UPDATE accounts SET account_password = ? WHERE account_id = ?", List.of(List.of(newPassword, accountId)));
            if (adminId == -1) return;
            String[] info = getCommonInfo(accountId, adminId);
            executor.executeSQL("INSERT INTO audit_logs (action, performed_by, source, target, before_value, after_value) VALUES (?, ?, ?, ?, ?, ?)",
                    List.of(List.of(Action.CHANGE_ADMIN_PASSWORD.getAction(),
                            User.ADMIN.getValue(),
                            info[0],
                            info[1],
                            "[REDACTED]",
                            "[REDACTED]")));
        }
        catch (SQLException e) {throw new SQLException(e);}
        catch (JSQLParserException e) {throw new JSQLParserException(e);}
        catch (Exception e) {
            System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
        }
    }
    public void editAccountHolder(int accountId, int adminId) throws SQLException, JSQLParserException {
        //Asks the user for the new account holder's name and sets it
        String[] info = getCommonInfo(accountId, adminId);
        String oldName = executor.executeSQL("SELECT account_holder FROM accounts WHERE account_id = ?", List.of(List.of(accountId))).getFirst().getFirst().get("account_holder").toString();
        String name = projectUtils.getValidUsername("Enter the new account holder's name: ", 100);
        executor.executeSQL("UPDATE accounts SET account_holder = ? WHERE account_id = ?", List.of(List.of(name, accountId)));
        if (adminId == -1) return;
        executor.executeSQL("INSERT INTO audit_logs (action, performed_by, source, target, before_value, after_value) VALUES (?, ?, ?, ?, ?, ?)", List.of(List.of(Action.CHANGE_HOLDER.getAction(),
                User.ADMIN.getValue(),
                info[0],
                info[1],
                oldName,
                name)));
    }
    public void editAccountStatus (int accountId, int adminId) throws SQLException, JSQLParserException {
        while (true) {
            try {
                //Asks the user for the new account status and validates it
                String oldStatus = executor.executeSQL("SELECT account_status FROM accounts WHERE account_id = ?", List.of(List.of(accountId))).getFirst().getFirst().get("account_status").toString();
                String status = projectUtils.getValidString("Enter the new account status (active/locked): ");
                if (status.equalsIgnoreCase("active")) {
                    executor.executeSQL("""
                        UPDATE accounts SET account_status = ?,
                        duration_locked = ?,
                        locked_time = NULL,
                        times_locked = ?
                        WHERE account_id = ?;
                    """, List.of(List.of("ACTIVE", 0, 0, accountId)));
                } else if (status.equalsIgnoreCase("locked")) {
                    executor.executeSQL("""
                        UPDATE accounts SET account_status = 'LOCKED',
                        duration_locked = ?
                        WHERE account_id = ?;
                    """, List.of(List.of(Integer.MAX_VALUE, accountId)));
                } else {
                    System.out.println("Invalid input. Please enter 'active' or 'locked'.");
                    continue;
                }
                if (adminId == -1) return;
                String[] info = getCommonInfo(accountId, adminId);
                executor.executeSQL("INSERT INTO audit_logs (action, performed_by, source, target, before_value, after_value) VALUES (?, ?, ?, ?, ?, ?)", List.of(List.of(Action.CHANGE_ACCOUNT_STATUS.getAction(),
                        User.ADMIN.getValue(),
                        info[0],
                        info[1],
                        oldStatus,
                        status.equalsIgnoreCase("active") ? "ACTIVE" : "LOCKED")));
                break;
            }
            //Catch invalid input
            catch (SQLException e) {throw new SQLException(e);}
            catch (JSQLParserException e) {throw new JSQLParserException(e);}
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public void editCreditScore (int accountId, int adminId) throws SQLException, JSQLParserException {
        while (true) {
            try {
                int oldCreditScore = projectUtils.verifyInstanceOf(executor.executeSQL("SELECT credit_score FROM accounts WHERE account_id = ?", List.of(List.of(accountId))).getFirst().getFirst().get("credit_score"), Integer.class, () -> new SQLException("Incorrect return type given from database"));
                //Asks the user for the new credit score and validates it
                int creditScore = projectUtils.getValidInt("Enter the new credit score: ");
                if (creditScore < 500 || creditScore > 800) {
                    System.out.println("Invalid credit score. Please enter a number between 500 and 800.");
                    continue;
                }
                executor.executeSQL("UPDATE accounts SET credit_score = ? WHERE account_id = ?", List.of(List.of(creditScore, accountId)));
                if (adminId == -1) return;
                String[] info = getCommonInfo(accountId, adminId);
                executor.executeSQL("INSERT INTO audit_logs (action, performed_by, source, target, before_value, after_value) VALUES (?, ?, ?, ?, ?, ?)", List.of(List.of(Action.CHANGE_CREDIT_SCORE.getAction(),
                        User.ADMIN.getValue(),
                        info[0],
                        info[1],
                        oldCreditScore,
                        creditScore)));
                break;
            }
            //Catch invalid input
            catch (SQLException e) {throw new SQLException(e);}
            catch (JSQLParserException e) {throw new JSQLParserException(e);}
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public void createOneAccount () throws SQLException, JSQLParserException {
        while (true) {
            try {
                Account tempAccount = getAccountDetails();
                //Prints the account ID returns the new account
                System.out.println("Account ID: " + tempAccount.getAccountId());
                System.out.println("Account created successfully!");
                executor.executeSQL("INSERT INTO audit_logs (action, performed_by, source) VALUES (?, ?, ?)", List.of(List.of(Action.CREATE_ACCOUNT.getAction(),
                        User.USER.getValue(),
                        String.format("%d (%s)", tempAccount.getAccountId(), tempAccount.getAccountHolder()))));
                return;
            }
            //Catch invalid input
            catch (SQLException e) {throw new SQLException(e);}
            catch (JSQLParserException e) {throw new JSQLParserException(e);}
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public void lockAccount (int accountId) throws Exception {
        int duration;
        int amountOfTimesLocked = projectUtils.verifyInstanceOf(executor.executeSQL("SELECT times_locked FROM accounts WHERE account_id = ?", List.of(List.of(accountId))).getFirst().getFirst().get("times_locked"), Integer.class, () -> new SQLException("Incorrect return type given from database"));
        switch (amountOfTimesLocked) {
            case 0 -> duration = 30;
            case 1 -> duration = 60;
            case 2 -> duration = 120;
            default -> duration = Integer.MAX_VALUE;
        }
        executor.executeSQL("""
                UPDATE accounts SET duration_locked = ?,
                locked_time = ?,
                account_status = ?,
                times_locked = ?
                WHERE account_id = ?;
        """, List.of(List.of(duration, LocalDateTime.now(), "LOCKED", amountOfTimesLocked + 1, accountId)));
    }
}
