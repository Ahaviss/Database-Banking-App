package com.ahaviss.logic;
//Local imports
import com.ahaviss.database.Admin;
import com.ahaviss.database.Owner;
import com.ahaviss.enums.AccountStatus;
import com.ahaviss.exceptions.*;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.database.Account;
import com.ahaviss.utilities.SecurityUtils;
//Java imports
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
public class LoginSystem {
    //Total tries
    public static Account accountLogin (ArrayList<Account> accounts) throws AccountLockedException, LoginFailedException {
        //Arrays to track if the same username is targeted multiple times
        int[] foundUsernames = new int[3];
        int[] indexes = new int[3];
        for (int i = 0; i < 3; i++) {
            System.out.printf("Login attempt %d/3%n", i + 1);
            //Gets the account ID and password
            String tempAccountId = ProjectUtils.getValidString("Enter your account ID: ");
            String accountPassword = ProjectUtils.getValidString("Enter your account password: ");
            //Checks if the ID is a number
            boolean isNumber = tempAccountId.matches("\\d+");
            if (!isNumber) {
                System.out.println("Invalid admin ID. Please enter a valid admin ID.");
                continue;
            }
            //Parses the ID to an integer
            int accountId = Integer.parseInt(tempAccountId);
            for (int j = 0; j < accounts.size(); j++) {
                //Checks if the account ID and password match the current account which is being checked
                if (accounts.get(j).getAccountId() == accountId && SecurityUtils.verifyPassword(accountPassword, accounts.get(j).getAccountPassword())) {
                    //Checks if the account is locked if the above is true
                    if (accounts.get(j).getAccountStatus() == AccountStatus.LOCKED) {
                        Account account = accounts.get(j);
                        LocalDateTime lockedTime = account.getAccountLockedTime();
                        if (lockedTime == null && account.getDurationLocked() == Integer.MAX_VALUE) throw new AccountLockedException(accounts.get(j).getAccountId(), Integer.MAX_VALUE);
                        Duration duration = Duration.between(lockedTime, LocalDateTime.now());
                        if (account.getDurationLocked() == Integer.MAX_VALUE) throw new AccountLockedException(accounts.get(j).getAccountId(), Integer.MAX_VALUE);
                        if (duration.toMinutes() >= account.getDurationLocked()) {
                            account.setAccountStatus(AccountStatus.ACTIVE);
                            account.setDurationLocked(0);
                            account.setAccountLockedTime(null);
                            account.setAmountOfTimesLocked(0);
                            return accounts.get(j);
                        }
                        throw new AccountLockedException(accounts.get(j).getAccountId(), account.getDurationLocked() - (int) duration.toMinutes());
                    }
                    //Otherwise, return the index
                    return accounts.get(j);
                }
                //If none of the above is true, check if the account ID matches input
                if (accounts.get(j).getAccountId() == accountId) {
                    foundUsernames[i] = accountId;
                    indexes[i] = j;
                    break;
                }
                else foundUsernames[i] = -1;
            }
            System.out.println("Invalid account ID or password. Please try again.");
        }
        //If attempts are exceeded
        System.out.println("Unauthorised access. Please try again.");
        int amountOfTimes = 0;
        //Checks if the same ID was targeted multiple times
        int repeatedUsername = foundUsernames[0];
        for (int foundUsername : foundUsernames) {
            if (foundUsername == repeatedUsername) {
                amountOfTimes++;
            }
        }
        //Returns the value to indicate the account should be locked
        if (amountOfTimes >= 3) {
            throw new AccountLockedException(accounts.get(indexes[0]));
        }
        //Otherwise
        throw new LoginFailedException();
    }
    public static int adminLogin (ArrayList<Admin> admins, Owner owner) {
        for (int i = 0; i < 3; i++) {
            System.out.printf("Login attempt %d/3%n", i + 1);
            //Gets the admin ID and password
            String adminId = ProjectUtils.getValidString("Enter your admin ID: ");
            String adminPassword = ProjectUtils.getValidString("Enter your admin password: ");
            //Checks if the ID and password match the owner
            if (adminId.equals(owner.getUsername()) && SecurityUtils.verifyPassword(adminPassword, owner.getPassword())) {
                return Integer.MIN_VALUE;
            }
            //Checks if the ID is a number
            boolean isNumber = adminId.matches("[0-9]+");
            if (!isNumber) {
                System.out.println("Invalid admin ID. Please enter a valid admin ID.");
                continue;
            }
            //Checks if the input matches the current admin being checked
            int adminIdInt = Integer.parseInt(adminId);
            for (int j = 0; j < admins.size(); j++) {
                if (admins.get(j).getAdminId() == adminIdInt && SecurityUtils.verifyPassword(adminPassword, admins.get(j).getAdminPassword())) {
                    //If the input matches, return the index
                    return j;
                }
            }
        }
        //If attempts are exceeded
        System.out.println("Unauthorised access. Defaulting...");
        //Terminates the JVM
        System.exit(0);
        return -1;
    }
}
