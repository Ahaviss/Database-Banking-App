/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

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
import java.util.Map;

public class LoginSystem {
    //Total tries
    private final ProjectUtils projectUtils;
    public LoginSystem (ProjectUtils projectUtils) {this.projectUtils = projectUtils;}
    public Account accountLogin (Map<Integer, Account> accounts) throws AccountLockedException, LoginFailedException {
        //Arrays to track if the same username is targeted multiple times
        int[] foundUsernames = new int[3];
        for (int i = 0; i < 3; i++) {
            System.out.printf("Login attempt %d/3%n", i + 1);
            //Gets the account ID and password
            String tempAccountId = projectUtils.getValidString("Enter your account ID: ");
            String accountPassword = projectUtils.getValidString("Enter your account password: ");
            //Checks if the ID is a number
            boolean isNumber = tempAccountId.matches("\\d+");
            if (!isNumber) {
                System.out.println("Invalid admin ID. Please enter a valid admin ID.");
                continue;
            }
            //Parses the ID to an integer
            int accountId = Integer.parseInt(tempAccountId);
            Account account = accounts.get(accountId);
            if (account != null) {
                if (SecurityUtils.verifyPassword(accountPassword, account.getAccountPassword())) {
                    //Checks if the account is locked if the above is true
                    if (account.getAccountStatus() == AccountStatus.LOCKED) {
                        LocalDateTime lockedTime = account.getAccountLockedTime();
                        if (lockedTime == null && account.getDurationLocked() == Integer.MAX_VALUE) throw new AccountLockedException(account.getAccountId(), Integer.MAX_VALUE);
                        else if (lockedTime == null) throw new IllegalStateException("Locked time is in an illegal state. Duration locked is not Integer.MAX_VAUE and locked time is null.");
                        Duration duration = Duration.between(lockedTime, LocalDateTime.now());
                        if (account.getDurationLocked() == Integer.MAX_VALUE) throw new AccountLockedException(account.getAccountId(), Integer.MAX_VALUE);
                        if (duration.toMinutes() >= account.getDurationLocked()) {
                            account.setAccountStatus(AccountStatus.ACTIVE);
                            account.setDurationLocked(0);
                            account.setAccountLockedTime(null);
                            account.setAmountOfTimesLocked(0);
                            return account;
                        }
                        throw new AccountLockedException(account.getAccountId(), account.getDurationLocked() - (int) duration.toMinutes());
                    }
                    //Otherwise, return the account
                    return account;
                }
                foundUsernames[i] = accountId;
            }
            else foundUsernames[i] = -1;
            System.out.println("Invalid account ID or password. Please try again.");
        }
        //If attempts are exceeded
        System.out.println("Unauthorised access. Please try again.");
        int amountOfTimes = 0;
        //Checks if the same ID was targeted multiple times
        int repeatedUsername = foundUsernames[0];
        if (repeatedUsername == -1) throw new LoginFailedException();
        for (int foundUsername : foundUsernames) {
            if (foundUsername == repeatedUsername) {
                amountOfTimes++;
            }
        }
        //Returns the value to indicate the account should be locked
        if (amountOfTimes >= 3) {
            throw new AccountLockedException(accounts.get(repeatedUsername));
        }
        //Otherwise
        throw new LoginFailedException();
    }
    public Admin adminLogin (Map<Integer, Admin> admins, Owner owner) {
        for (int i = 0; i < 3; i++) {
            System.out.printf("Login attempt %d/3%n", i + 1);
            //Gets the admin ID and password
            String adminId = projectUtils.getValidString("Enter your admin ID: ");
            String adminPassword = projectUtils.getValidString("Enter your admin password: ");
            //Checks if the ID and password match the owner
            if (adminId.equals(owner.getUsername()) && SecurityUtils.verifyPassword(adminPassword, owner.getPassword())) {
                return null;
            }
            //Checks if the ID is a number
            boolean isNumber = adminId.matches("[0-9]+");
            if (!isNumber) {
                System.out.println("Invalid admin ID. Please enter a valid admin ID.");
                continue;
            }
            //Checks if the input matches the current admin being checked
            int adminIdInt = Integer.parseInt(adminId);
            Admin admin = admins.get(adminIdInt);
            if (admin != null && SecurityUtils.verifyPassword(adminPassword, admin.getAdminPassword())) return admin;
            System.out.println("Invalid admin ID or password. Please try again.");
        }
        //If attempts are exceeded
        System.out.println("Unauthorised access. Defaulting...");
        //Terminates the JVM
        System.exit(0);
        return null;
    }
}
