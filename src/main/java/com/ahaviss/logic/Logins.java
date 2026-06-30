/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.logic;

import com.ahaviss.database.Account;
import com.ahaviss.enums.LoginEnums;
import com.ahaviss.exceptions.AccountLockedException;
import com.ahaviss.exceptions.LoginFailedException;
import com.ahaviss.logs.enums.Action;
import com.ahaviss.logs.enums.User;
import com.ahaviss.session.Session;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.utilities.SQLExecutor;
import net.sf.jsqlparser.JSQLParserException;

import java.sql.SQLException;
import java.util.List;

public class Logins {
    private final LoginSystem loginSystem;
    private final ProjectUtils projectUtils;
    private final SQLExecutor executor;
    private final AccountLogic accountLogic;
    public Logins (LoginSystem loginSystem, ProjectUtils projectUtils, SQLExecutor executor, AccountLogic accountLogic) {
        this.loginSystem = loginSystem;
        this.projectUtils = projectUtils;
        this.executor = executor;
        this.accountLogic = accountLogic;
    }
    //Account login
    public void accountLogin () throws SQLException, JSQLParserException {
        try {
            //Check if the accounts list is empty
            if (!projectUtils.tableHasContents(Account.class)) {
                System.out.println("No accounts available. Please create an account.");
                return;
            }
            //Call the login system for accounts
            int tempUser;
            try {tempUser = loginSystem.accountLogin();}
            catch (AccountLockedException e) {
                //Traces account
                int account = e.traceAccount();
                String prevStatus;
                //If account is -1
                if (account == -1) {System.out.println(e.getMessage()); return;}
                prevStatus = executor.executeSQL("SELECT account_status FROM accounts WHERE account_id = ?", List.of(List.of(account))).getFirst().getFirst().get("account_status").toString();
                //Locks account
                accountLogic.lockAccount(account);
                //Prints error message
                System.out.println(e.getMessage());
                //Tells user duration of lock
                int durationLocked = projectUtils.verifyInstanceOf(executor.executeSQL("SELECT duration_locked FROM accounts WHERE account_id = ?", List.of(List.of(account))).getFirst().getFirst().get("duration_locked"), Integer.class, () -> new SQLException("Incorrect return type given from database"));
                if (durationLocked == Integer.MAX_VALUE) {
                    System.out.println("Locked permanently.");
                    return;
                }
                System.out.printf("Locked for: %d minutes.%n", durationLocked);
                String source = String.format("%d (%s)", account, executor.executeSQL("SELECT account_holder FROM accounts WHERE account_id = ?", List.of(List.of(account))).getFirst().getFirst().get("account_holder").toString());
                executor.executeSQL("INSERT INTO audit_logs (action, performed_by, source, before_value, after_value) VALUES (?, ?, ?, ?, ?)", List.of(List.of(Action.ACCOUNT_AUTO_LOCKED.getAction(),
                        User.USER.getValue(),
                        source,
                        prevStatus,
                        "LOCKED")));
                return;
            }
            catch (LoginFailedException e) {
                System.out.println(e.getMessage());
                return;
            }
            //Login user if the above conditions aren't true
            System.out.println("Login successful!");
            Session.setCurrentAccount(tempUser);
            //Welcome message
            System.out.printf("Welcome back, %s!%n", executor.executeSQL("SELECT account_holder FROM accounts WHERE account_id = ?", List.of(List.of(tempUser))).getFirst().getFirst().get("account_holder").toString());
            //Role is set to user
            Session.setRole(LoginEnums.USER);
        }
        //Catch invalid input
        catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
        catch (SQLException e) {throw new SQLException(e);}
        catch (JSQLParserException e) {throw new JSQLParserException(e);}
        catch (Exception e) {
            System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
        }
    }
    public void adminLogin () {
        try {
            //Calls the login system for admins
            int admin = loginSystem.adminLogin();
            //If the admin was validated as the owner
            if (admin == Integer.MIN_VALUE) {
                System.out.println("Welcome back, owner!");
                //Role is set
                Session.setRole(LoginEnums.OWNER);
                return;
            }
            //If the admin is logged in successfully
            Session.setCurrentAdmin(admin);
            System.out.printf("Welcome back %s!%n", executor.executeSQL("SELECT admin_name FROM admins WHERE admin_id = ?", List.of(List.of(admin))).getFirst().getFirst().get("admin_name").toString());
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
