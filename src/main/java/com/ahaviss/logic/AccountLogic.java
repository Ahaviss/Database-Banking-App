/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.logic;
//Local imports
import com.ahaviss.database.*;
import com.ahaviss.enums.AccountStatus;
import com.ahaviss.enums.TransferDirection;
import com.ahaviss.logs.enums.Action;
import com.ahaviss.logs.enums.User;
import com.ahaviss.logs.manager.LogManager;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.utilities.SecurityUtils;
//Java imports
import java.util.Map;
import java.util.Random;
import java.time.LocalDateTime;
public class AccountLogic {
    //RNG for account ID
    private final Random random = new Random();
    private final ProjectUtils projectUtils;
    public AccountLogic (ProjectUtils projectUtils) {this.projectUtils = projectUtils;}
    public void withdraw (Account account) {
        double prevBalance = account.getBalance();
        //Asks for the withdrawal amount
        double withdrawAmount = projectUtils.getValidDouble(String.format("Enter the amount you want to withdraw (%.2f available): ", account.getBalance()));
        //Validates the withdrawal amount
        if (withdrawAmount > account.getBalance()) {
            System.out.println("Insufficient balance.");
            return;
        }
        if (withdrawAmount == 0) {
            System.out.println("No money taken out");
            return;
        }
        //Sets user balance
        account.setBalance(account.getBalance() - withdrawAmount);
        //Adds the withdrawal to history
        account.addWithdraw(new Withdraw(withdrawAmount, account.getAccountId()));
        LogManager.addLog(Action.WITHDRAW, User.USER, String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), null, String.valueOf(prevBalance), String.valueOf(account.getBalance()));
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
    public void deposit (Account account) {
        //Asks for the deposit amount
        double prevBalance = account.getBalance();
        double depositAmount = projectUtils.getValidDouble("Enter the amount you want to deposit: ");
        if (depositAmount == 0) {
            System.out.println("No money added");
            return;
        }
        account.setBalance(account.getBalance() + depositAmount);
        //Adds the deposit to history
        account.addDeposit(new Deposit(depositAmount, account.getAccountId()));
        LogManager.addLog(Action.DEPOSIT, User.USER, String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), null, String.valueOf(prevBalance), String.valueOf(account.getBalance()));
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
    public void transfer (Map<Integer, Account> accounts, Account currentAccount) {
        //Asks the user for the recipient ID and amount to transfer
        while (true) {
            double prevBalance1 = currentAccount.getBalance();
            int recipientAccountId = projectUtils.getValidInt("Enter the ID of the recipient account: ");
            if (recipientAccountId == currentAccount.getAccountId()) {
                System.out.println("You cannot transfer money to yourself.");
                return;
            }
            double transferAmount = projectUtils.getValidDouble(String.format("Enter the amount you want to transfer (%.2f available): ", currentAccount.getBalance()));
            if (transferAmount == 0) {
                System.out.println("No money transferred");
                return;
            }
            //Validates amount to transfer
            if (transferAmount > currentAccount.getBalance()) {
                System.out.println("Insufficient balance.");
                return;
            }
            Account recipientAccount = accounts.get(recipientAccountId);
            //Checks if the account is found
            if (recipientAccount == null) {
                System.out.printf("Account ID %d not found.%n", recipientAccountId);
                continue;
            }
            double prevBalance2 = recipientAccount.getBalance();
            //Updates the balance
            currentAccount.setBalance(currentAccount.getBalance() - transferAmount);
            recipientAccount.setBalance(recipientAccount.getBalance() + transferAmount);
            //Adds the transfer to history
            currentAccount.addTransfer(new Transfer(transferAmount, recipientAccountId, currentAccount.getAccountId(), TransferDirection.OUTGOING));
            recipientAccount.addTransfer(new Transfer(transferAmount, recipientAccountId, currentAccount.getAccountId(), TransferDirection.INCOMING));
            LogManager.addLog(Action.TRANSFER, User.USER, String.format("%d (%s) -> %d (%s)", currentAccount.getAccountId(), currentAccount.getAccountHolder(), recipientAccount.getAccountId(), recipientAccount.getAccountHolder()), null, String.format("(Source) %.2f & (Recipient) %.2f", prevBalance1, prevBalance2), String.format("(Source) %.2f & (Recipient) %.2f", currentAccount.getBalance(), recipientAccount.getBalance()));
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
    private Account getAccountDetails (Map<Integer, Account> accounts) {
        while (true) {
            //Asks for the account details
            String name = projectUtils.getValidString("Enter the account holder's name: ");
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
            while (accounts.containsKey(accountId)) {
                accountId++;
                if (accountId > 9999999) {
                    accountId = 1000000;
                }
            }
            //Return the created account
            return new Account (accountId, name, balance, accountPassword, AccountStatus.ACTIVE, creditScore);

        }
    }
    public void createAccount (Map<Integer, Account> accounts, Admin admin) {
        //Asks the user for the number of accounts to add
        int amountOfAccountToAdd = projectUtils.getValidInt("Enter the amount of accounts you want to add: ");
        //Gets account details
        for (int i = 0; i < amountOfAccountToAdd; i++) {
            //Call getAccountDetails method
            Account tempAccount = getAccountDetails(accounts);
            //Print success message and student ID
            System.out.println("Account ID: " + tempAccount.getAccountId());
            System.out.println("Account created successfully!");
            accounts.put(tempAccount.getAccountId(), tempAccount);
            if (admin != null) {
                LogManager.addLog(Action.CREATE_ACCOUNT, User.ADMIN, String.format("%d (%s)", admin.getAdminId(), admin.getAdminName()), String.format("%d (%s)", tempAccount.getAccountId(), tempAccount.getAccountHolder()), "N/A", "N/A");
            }
        }
    }
    public Map<Integer, Account> deleteAccounts (Map<Integer, Account> accounts, Admin admin) {
        //Checks if the accounts list is empty
        if (!ProjectUtils.checkMap(accounts)) {
            System.out.println("No accounts available. Please create an account.");
            return null;
        }
        while (true) {
            try {
                //Asks the number of accounts to delete
                int amountOfPeople = projectUtils.getValidInt(String.format("Enter the amount of accounts you want to delete (%d total accounts): ", accounts.size()));
                //Validates input
                if (amountOfPeople > accounts.size()) {
                    System.out.println("Invalid input. Please enter a number less than or equal to the number of accounts.");
                    continue;
                }
                for (int i = 0; i < amountOfPeople; i++) {
                    while (true) {
                        //Asks for the account ID to delete
                        int accountId = projectUtils.getValidInt("Enter the ID of the account you want to delete: ");
                        Account account = accounts.get(accountId);
                        //Checks if the account is found
                        if (account == null) {
                            System.out.printf("Account ID %d not found.%n", accountId);
                            continue;
                        }
                        //Deletes the account
                        LogManager.addLog(Action.DELETE_ACCOUNT, User.ADMIN, String.format("%d (%s)", admin.getAdminId(), admin.getAdminName()), String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), "N/A", "N/A");
                        accounts.remove(accountId);
                        System.out.println("Account deleted successfully!");
                        break;
                    }
                }
                return accounts;
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
    public Account editPassword (Account account) {
        while (true) {
            try {
                String currentPassword;
                boolean passwordValidated = false;
                for (int i = 0; i < 3; i++) {
                    //Asks the user for the current password and validates it
                    System.out.printf("Password change attempt %d/3%n", i + 1);
                    currentPassword = projectUtils.getValidString("Enter the current password: ");
                    if (!SecurityUtils.verifyPassword(currentPassword, account.getAccountPassword())) {
                        System.out.println("Incorrect password. Please try again.");
                    } else {
                        passwordValidated = true;
                        break;
                    }
                }
                //If the password is not validated, return null
                if (!passwordValidated) {
                    System.out.println("Password change failed. Please try again.");
                    return null;
                }
                //Asks the user for the new password, validates it and sets it
                String tempPassword = projectUtils.getValidPassword("Enter the new password: ");
                String password = SecurityUtils.hashPassword(tempPassword);
                account.setAccountPassword(password);
                LogManager.addLog(Action.CHANGE_PASSWORD, User.USER, String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), null, "[REDACTED]", "[REDACTED]");
                return account;
                //Catch invalid input
            } catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public void editPasswordAdmin (Account account, Admin admin) {
        try {
            String tempNewPassword = projectUtils.getValidPassword("Please enter the new account password: ");
            String newPassword = SecurityUtils.hashPassword(tempNewPassword);
            account.setAccountPassword(newPassword);
            if (admin != null) {
                LogManager.addLog(Action.CHANGE_PASSWORD, User.ADMIN, String.format("%d (%s)", admin.getAdminId(), admin.getAdminName()), String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), "[REDACTED]", "[REDACTED]");
            }
        } catch (Exception e) {
            System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
        }
    }
    public void editAccountHolder(Account account, Admin admin) {
        //Asks the user for the new account holder's name and sets it
        String oldName = account.getAccountHolder();
        String name = projectUtils.getValidString("Enter the new account holder's name: ");
        account.setAccountHolder(name);
        if (admin != null) {
            LogManager.addLog(Action.CHANGE_HOLDER, User.ADMIN, String.format("%d (%s)", admin.getAdminId(), admin.getAdminName()), String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), oldName, name);
        }
    }
    public void editAccountStatus (Account account, Admin admin) {
        while (true) {
            try {
                //Asks the user for the new account status and validates it
                String oldStatus = String.valueOf(account.getAccountStatus());
                String status = projectUtils.getValidString("Enter the new account status (active/inactive): ");
                if (status.equalsIgnoreCase("active")) {
                    account.setAccountStatus(AccountStatus.ACTIVE);
                    account.setDurationLocked(0);
                    account.setAccountLockedTime(null);
                    account.setAmountOfTimesLocked(0);
                } else if (status.equalsIgnoreCase("inactive")) {
                    account.setAccountStatus(AccountStatus.LOCKED);
                    account.setDurationLocked(Integer.MAX_VALUE);
                } else {
                    System.out.println("Invalid input. Please enter 'active' or 'inactive'.");
                    continue;
                }
                if (admin != null) {
                    LogManager.addLog(Action.CHANGE_ACCOUNT_STATUS, User.ADMIN, String.format("%d (%s)", admin.getAdminId(), admin.getAdminName()), String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), oldStatus, String.valueOf(account.getAccountStatus()));
                }
                break;
            }
            //Catch invalid input
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public void editCreditScore (Account account, Admin admin) {
        while (true) {
            try {
                //Asks the user for the new credit score and validates it
                int oldCreditScore = account.getCreditScore();
                int creditScore = projectUtils.getValidInt("Enter the new credit score: ");
                if (creditScore < 500 || creditScore > 800) {
                    System.out.println("Invalid credit score. Please enter a number between 500 and 800.");
                    continue;
                }
                account.setCreditScore(creditScore);
                LogManager.addLog(Action.CHANGE_CREDIT_SCORE, User.ADMIN, String.format("%d (%s)", admin.getAdminId(), admin.getAdminName()), String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), String.valueOf(oldCreditScore), String.valueOf(account.getCreditScore()));
                break;
            }
            //Catch invalid input
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public Account createOneAccount (Map<Integer, Account> accounts) {
        while (true) {
            try {
                Account tempAccount = getAccountDetails(accounts);
                //Prints the account ID returns the new account
                System.out.println("Account ID: " + tempAccount.getAccountId());
                System.out.println("Account created successfully!");
                LogManager.addLog(Action.CREATE_ACCOUNT, User.USER, String.format("%d (%s)", tempAccount.getAccountId(), tempAccount.getAccountHolder()), null, "N/A", "N/A");
                return tempAccount;
            }
            //Catch invalid input
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public static void lockAccount (Account account) {
        try {
            if (account.getAmountOfTimesLocked() == 0) account.setDurationLocked(30);
            else if (account.getAmountOfTimesLocked() == 1) account.setDurationLocked(60);
            else if (account.getAmountOfTimesLocked() == 2) account.setDurationLocked(120);
            else if (account.getAmountOfTimesLocked() >= 3) account.setDurationLocked(Integer.MAX_VALUE);
            account.setAccountLockedTime(LocalDateTime.now());
            account.setAccountStatus(AccountStatus.LOCKED);
            account.setAmountOfTimesLocked(account.getAmountOfTimesLocked() + 1);
        }
        catch (Exception e) {
            System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
        }
    }
}
