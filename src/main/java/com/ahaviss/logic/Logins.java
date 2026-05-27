/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.logic;

import com.ahaviss.database.Account;
import com.ahaviss.database.Admin;
import com.ahaviss.enums.LoginEnums;
import com.ahaviss.exceptions.AccountLockedException;
import com.ahaviss.exceptions.LoginFailedException;
import com.ahaviss.logs.enums.Action;
import com.ahaviss.logs.enums.User;
import com.ahaviss.logs.manager.LogManager;
import com.ahaviss.session.Session;
import com.ahaviss.utilities.ProjectUtils;

public class Logins {
    private final LoginSystem loginSystem;
    public Logins (LoginSystem loginSystem) {this.loginSystem = loginSystem;}
    //Account login
    public void accountLogin () {
        try {
            //Check if the accounts list is empty
            if (!ProjectUtils.checkMap(Session.getAccounts())) {
                System.out.println("No accounts available. Please create an account.");
                return;
            }
            //Call the login system for accounts
            Account tempUser;
            try {
                tempUser = loginSystem.accountLogin(Session.getAccounts());
            }
            catch (AccountLockedException e) {
                //Traces account
                Account account = e.traceAccount();
                String prevStatus;
                //If account isn't null
                if (account != null) {
                    prevStatus = String.valueOf(account.getAccountStatus());
                    //Locks account
                    AccountLogic.lockAccount(account);
                    //Prints error message
                    System.out.println(e.getMessage());
                    //Tells user duration of lock
                    if (account.getDurationLocked() == Integer.MAX_VALUE) {
                        System.out.println("Locked permanently.");
                        return;
                    }
                    System.out.printf("Locked for: %d minutes.%n", account.getDurationLocked());
                    LogManager.addLog(Action.ACCOUNT_AUTO_LOCKED, User.USER, String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), null, prevStatus, String.valueOf(account.getAccountStatus()));
                    return;
                    //If account is null
                } else {
                    System.out.println(e.getMessage());
                    return;
                }
            }
            catch (LoginFailedException e) {
                System.out.println(e.getMessage());
                return;
            }
            //Login user if the above conditions aren't true
            System.out.println("Login successful!");
            Session.setCurrentAccount(tempUser);
            //Welcome message
            System.out.printf("Welcome back, %s!%n", Session.getCurrentAccount().getAccountHolder());
            //Role is set to user
            Session.setRole(LoginEnums.USER);
        }
        //Catch invalid input
        catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
        catch (Exception e) {
            System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
        }
    }
    public void adminLogin () {
        try {
            //Calls the login system for admins
            Admin admin = loginSystem.adminLogin(Session.getAdmins(), Session.getOwner());
            //If the admin was validated as the owner
            if (admin == null) {
                System.out.println("Welcome back, owner!");
                //Role is set
                Session.setRole(LoginEnums.OWNER);
                return;
            }
            //If the admin is logged in successfully
            Session.setCurrentAdmin(admin);
            System.out.printf("Welcome back %s!%n", Session.getCurrentAdmin().getAdminName());
            //Role is set
            Session.setRole(LoginEnums.ADMIN);
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
