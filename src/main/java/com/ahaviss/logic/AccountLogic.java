package com.ahaviss.logic;
//Local imports
import com.ahaviss.database.*;
import com.ahaviss.enums.AccountStatus;
import com.ahaviss.enums.LoginEnums;
import com.ahaviss.enums.TransferDirection;
import com.ahaviss.exceptions.UserNotFoundException;
import com.ahaviss.logs.enums.Action;
import com.ahaviss.logs.enums.User;
import com.ahaviss.logs.manager.LogManager;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.utilities.SecurityUtils;
//Java imports
import java.util.Random;
import java.util.ArrayList;
import java.time.LocalDateTime;
public class AccountLogic {
    //RNG for account ID
    private static final Random random = new Random();
    public static void withdraw (Account account) {
        while (true) {
            double prevBalance = account.getBalance();
            //Asks for the withdrawal amount
            double withdrawAmount = ProjectUtils.getValidDouble(String.format("Enter the amount you want to withdraw (%.2f available): ", account.getBalance()));
            //Validates the withdrawal amount
            if (withdrawAmount > account.getBalance()) {
                System.out.println("Insufficient balance.");
                //Asks to retry the withdrawal
                if (!ProjectUtils.askToContinue()) {
                    return;
                }
                continue;
            }
            //Sets user balance
            account.setBalance(account.getBalance() - withdrawAmount);
            //Adds the withdrawal to history
            account.addWithdraw(new Withdraw(withdrawAmount, account.getAccountId()));
            LogManager.addLog(Action.WITHDRAW, User.USER, String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), null, String.valueOf(prevBalance), String.valueOf(account.getBalance()));
            while (true) {
                //Asks if the user wants to make another withdrawal
                String answer = ProjectUtils.getValidString("Withdrawal successful. Do you want to make another withdrawal? Y/N");
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
    public static void deposit (Account account) {
        while (true) {
            //Asks for the deposit amount
            double prevBalance = account.getBalance();
            double depositAmount = ProjectUtils.getValidDouble("Enter the amount you want to deposit: ");
            account.setBalance(account.getBalance() + depositAmount);
            //Adds the deposit to history
            account.addDeposit(new Deposit(depositAmount, account.getAccountId()));
            LogManager.addLog(Action.DEPOSIT, User.USER, String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), null, String.valueOf(prevBalance), String.valueOf(account.getBalance()));
            //Asks if the user wants to make another deposit
            while (true) {
                String answer = ProjectUtils.getValidString("Deposit successful. Do you want to make another deposit? Y/N");
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
    public static void transfer (ArrayList<Account> accounts, Account currentAccount) {
        //Asks the user for the recipient ID and amount to transfer
        while (true) {
            double prevBalance1 = currentAccount.getBalance();
            int recipientAccountId = ProjectUtils.getValidInt("Enter the ID of the recipient account: ");
            double transferAmount = ProjectUtils.getValidDouble(String.format("Enter the amount you want to transfer (%.2f available): ", currentAccount.getBalance()));
            //Validates amount to transfer
            if (transferAmount > currentAccount.getBalance()) {
                System.out.println("Insufficient balance.");
                continue;
            }
            int recipientIndex;
            try {
                recipientIndex = loopThroughAccounts(accounts, recipientAccountId);
            }
            catch (UserNotFoundException e) {
                System.out.println(e.getMessage());
                continue;
            }
            double prevBalance2 = accounts.get(recipientIndex).getBalance();
            //Updates the balance
            currentAccount.setBalance(currentAccount.getBalance() - transferAmount);
            accounts.get(recipientIndex).setBalance(accounts.get(recipientIndex).getBalance() + transferAmount);
            //Adds the transfer to history
            currentAccount.addTransfer(new Transfer(transferAmount, recipientAccountId, currentAccount.getAccountId(), TransferDirection.OUTGOING));
            accounts.get(recipientIndex).addTransfer(new Transfer(transferAmount, recipientAccountId, currentAccount.getAccountId(), TransferDirection.INCOMING));
            LogManager.addLog(Action.TRANSFER, User.USER, String.format("%d (%s) -> %d (%s)", currentAccount.getAccountId(), currentAccount.getAccountHolder(), accounts.get(recipientIndex).getAccountId(), accounts.get(recipientIndex).getAccountHolder()), null, String.format("(Source) %.2f & (Recipient) %.2f", prevBalance1, prevBalance2), String.format("(Source) %.2f & (Recipient) %.2f", currentAccount.getBalance(), accounts.get(recipientIndex).getBalance()));
            while (true) {
                String answer = ProjectUtils.getValidString("Transfer successful. Do you want to make another transfer? Y/N");
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
    private static Account getAccountDetails (ArrayList<Account> accounts) {
        while (true) {
            //Asks for the account details
            String name = ProjectUtils.getValidString("Enter the account holder's name: ");
            double balance = ProjectUtils.getValidDouble("Enter the account holder's balance: ");
            int creditScore = ProjectUtils.getValidInt("Enter the account holder's credit score");
            //Validates the credit score
            if (creditScore < 500 || creditScore > 800) {
                System.out.println("Invalid credit score. Please enter a number between 500 and 800.");
                continue;
            }
            //Asks for the account password
            String tempAccountPassword = ProjectUtils.getValidPassword("Enter the account holder's password: ");
            String accountPassword = SecurityUtils.hashPassword(tempAccountPassword, SecurityUtils.generateSalt());
            //Generates a random account ID
            int accountId = random.nextInt(9999999 - 1000000 + 1) + 1000000;
            //Makes sure that the ID is not already taken
            while (true) {
                try {
                    loopThroughAccounts(accounts, accountId);
                }
                catch (UserNotFoundException e) {
                    break;
                }
                accountId++;
                if (accountId > 9999999) {
                    accountId = 1000000;
                }
            }
            //Return the created account
            return new Account (accountId, name, balance, accountPassword, AccountStatus.ACTIVE, creditScore);

        }
    }
    public static void createAccount (ArrayList<Account> accounts, Admin admin) {
        //Asks the user for the number of accounts to add
        int amountOfAccountToAdd = ProjectUtils.getValidInt("Enter the amount of accounts you want to add: ");
        //Gets account details
        for (int i = 0; i < amountOfAccountToAdd; i++) {
            //Call getAccountDetails method
            Account tempAccount = getAccountDetails(accounts);
            //Print success message and student ID
            System.out.println("Account ID: " + tempAccount.getAccountId());
            System.out.println("Account created successfully!");
            accounts.add(tempAccount);
            if (admin != null) {
                LogManager.addLog(Action.CREATE_ACCOUNT, User.ADMIN, String.format("%d (%s)", admin.getAdminId(), admin.getAdminName()), String.format("%d (%s)", tempAccount.getAccountId(), tempAccount.getAccountHolder()), "N/A", "N/A");
            }
        }
    }
    public static int loopThroughAccounts (ArrayList<Account> accounts, int accountId) throws UserNotFoundException {
        //Finds a specific account using the account ID
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getAccountId() == accountId) {
                return i;
            }
        }
        throw new UserNotFoundException(LoginEnums.USER, String.format("Account ID %d not found.", accountId));
    }
    public static ArrayList<Account> deleteAccounts (ArrayList<Account> accounts, Admin admin) {
        //Checks if the accounts list is empty
        if (!ProjectUtils.checkArrayList(accounts)) {
            System.out.println("No accounts available. Please create an account.");
            return null;
        }
        while (true) {
            try {
                //Asks the number of accounts to delete
                int amountOfPeople = ProjectUtils.getValidInt(String.format("Enter the amount of accounts you want to delete (%d total accounts): ", accounts.size()));
                //Validates input
                if (amountOfPeople > accounts.size()) {
                    System.out.println("Invalid input. Please enter a number less than or equal to the number of accounts.");
                    continue;
                }
                for (int i = 0; i < amountOfPeople; i++) {
                    while (true) {
                        //Asks for the account ID to delete
                        int accountId = ProjectUtils.getValidInt("Enter the ID of the account you want to delete: ");
                        int accountIndex;
                        try {
                            accountIndex = loopThroughAccounts(accounts, accountId);
                        }
                        //Validates input
                        catch (UserNotFoundException e) {
                            System.out.println(e.getMessage());
                            continue;
                        }
                        //Deletes the account
                        LogManager.addLog(Action.DELETE_ACCOUNT, User.ADMIN, String.format("%d (%s)", admin.getAdminId(), admin.getAdminName()), String.format("%d (%s)", accounts.get(accountIndex).getAccountId(), accounts.get(accountIndex).getAccountHolder()), "N/A", "N/A");
                        accounts.remove(accountIndex);
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
    public static Account editPassword (Account account) {
        while (true) {
            try {
                String currentPassword = null;
                boolean passwordValidated = false;
                for (int i = 0; i < 3; i++) {
                    //Asks the user for the current password and validates it
                    System.out.printf("Password change attempt %d/3%n", i + 1);
                    currentPassword = ProjectUtils.getValidString("Enter the current password: ");
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
                String tempPassword = ProjectUtils.getValidPassword("Enter the new password: ");
                String password = SecurityUtils.hashPassword(tempPassword, SecurityUtils.generateSalt());
                account.setAccountPassword(password);
                LogManager.addLog(Action.CHANGE_PASSWORD, User.USER, String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), null, currentPassword, tempPassword);
                return account;
                //Catch invalid input
            } catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public static Account editPasswordAdmin (Account account, Admin admin) {
        while (true) {
            try {
                String oldPassword = account.getAccountPassword();
                String tempNewPassword = ProjectUtils.getValidPassword("Please enter the new account password: ");
                String newPassword = SecurityUtils.hashPassword(tempNewPassword, SecurityUtils.generateSalt());
                account.setAccountPassword(newPassword);
                if (admin != null) {
                    LogManager.addLog(Action.CHANGE_PASSWORD, User.ADMIN, String.format("%d (%s)", admin.getAdminId(), admin.getAdminName()), String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), oldPassword, newPassword);
                }
                return account;
            } catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public static Account editAccountHolder(Account account, Admin admin) {
        //Asks the user for the new account holder's name and sets it
        String oldName = account.getAccountHolder();
        String name = ProjectUtils.getValidString("Enter the new account holder's name: ");
        account.setAccountHolder(name);
        if (admin != null) {
            LogManager.addLog(Action.CHANGE_HOLDER, User.ADMIN, String.format("%d (%s)", admin.getAdminId(), admin.getAdminName()), String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), oldName, name);
        }
        return account;
    }
    public static Account editAccountStatus (Account account, Admin admin) {
        while (true) {
            try {
                //Asks the user for the new account status and validates it
                String oldStatus = String.valueOf(account.getAccountStatus());
                String status = ProjectUtils.getValidString("Enter the new account status (active/locked): ");
                if (status.equalsIgnoreCase("active")) {
                    account.setAccountStatus(AccountStatus.ACTIVE);
                    account.setDurationLocked(0);
                    account.setAccountLockedTime(null);
                    account.setAmountOfTimesLocked(0);
                } else if (status.equalsIgnoreCase("locked")) {
                    account.setAccountStatus(AccountStatus.LOCKED);
                    account.setDurationLocked(Integer.MAX_VALUE);
                } else {
                    System.out.println("Invalid input. Please enter 'active' or 'inactive'.");
                    continue;
                }
                if (admin != null) {
                    LogManager.addLog(Action.CHANGE_ACCOUNT_STATUS, User.ADMIN, String.format("%d (%s)", admin.getAdminId(), admin.getAdminName()), String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), oldStatus, String.valueOf(account.getAccountStatus()));
                }
                return account;
            }
            //Catch invalid input
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public static Account editCreditScore (Account account, Admin admin) {
        while (true) {
            try {
                //Asks the user for the new credit score and validates it
                int oldCreditScore = account.getCreditScore();
                int creditScore = ProjectUtils.getValidInt("Enter the new credit score: ");
                if (creditScore < 500 || creditScore > 800) {
                    System.out.println("Invalid credit score. Please enter a number between 500 and 800.");
                    continue;
                }
                account.setCreditScore(creditScore);
                LogManager.addLog(Action.CHANGE_CREDIT_SCORE, User.ADMIN, String.format("%d (%s)", admin.getAdminId(), admin.getAdminName()), String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), String.valueOf(oldCreditScore), String.valueOf(account.getCreditScore()));
                return account;
            }
            //Catch invalid input
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public static Account createOneAccount (ArrayList<Account> accounts) {
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
