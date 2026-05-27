/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.main;

import com.ahaviss.database.Account;
import com.ahaviss.enums.ControlFlow;
import com.ahaviss.enums.LoginEnums;
import com.ahaviss.logic.AccountLogic;
import com.ahaviss.logic.Logins;
import com.ahaviss.logs.manager.LogManager;
import com.ahaviss.menus.AccountMenus;
import com.ahaviss.menus.AdminMenus;
import com.ahaviss.save.SaveData;
import com.ahaviss.session.Session;
import com.ahaviss.utilities.ProjectUtils;

import javax.crypto.AEADBadTagException;

class App {
    private final ProjectUtils projectUtils;
    private final AccountLogic accountLogic;
    private final AccountMenus accountMenus;
    private final AdminMenus adminMenus;
    private final Logins logins;
    App (ProjectUtils projectUtils, AccountLogic accountLogic, AccountMenus accountMenus, AdminMenus adminMenus, Logins logins) {
        this.projectUtils = projectUtils;
        this.accountLogic = accountLogic;
        this.accountMenus = accountMenus;
        this.adminMenus = adminMenus;
        this.logins = logins;
    }
    void loadData () {
        String password = projectUtils.getValidString("Password:");
        //Loads data
        try {
            Session.setAccounts(SaveData.loadAccountData(password));
            Session.setAdmins(SaveData.loadAdminData(password));
            Session.setOwner(SaveData.loadOwnerData(password));
            LogManager.loadLogs(SaveData.loadAuditData(password));
        }
        catch (AEADBadTagException e) {
            System.out.println("Critical Error: Cause: Key mismatched or files corrupted/tampered");
            Session.setKillswitch(true);
            System.exit(1);
        }
        catch (Exception e) {
            System.out.println("Fatal Error: Cause: " + e.getMessage());
            e.printStackTrace();
            Session.setKillswitch(true);
            System.exit(1);
        }
    }
    void start () {
        loadData();
        preStartTasks();
        menu();
    }
    void menu () {
        while (true) {
            try {
                //If the role is admin or owner, call the adminPanel method
                if (Session.getRole() == LoginEnums.ADMIN || Session.getRole() == LoginEnums.OWNER) {
                    ControlFlow controlFlow = adminMenus.adminPanel();
                    if (controlFlow == ControlFlow.QUIT) return;
                    else continue;
                    //If the role is user, call the accountPanel method
                } else if (Session.getRole() == LoginEnums.USER) {
                    ControlFlow controlFlow = accountMenus.accountPanel();
                    if (controlFlow == ControlFlow.QUIT) return;
                    else continue;
                }
                //If the role is none
                System.out.println("Welcome to the Banking System!");
                //Ask the user to log in, create or quit
                String answer = projectUtils.getValidString("Would you like to login, create an account, or quit? (login/create/quit)");
                if (answer.equalsIgnoreCase("login")) {
                    String login = projectUtils.getValidString("Would you like to login as an account holder or an admin? (account holder/admin)");
                    if (login.equalsIgnoreCase("account holder")) {
                        //Calls the accountLogin method
                        logins.accountLogin();
                    } else if (login.equalsIgnoreCase("admin")) {
                        //Calls the adminLogin method
                        logins.adminLogin();
                    } else {
                        //Invalid input
                        System.out.println("Invalid input. Please enter 'account holder', 'admin', or 'quit'.");
                    }
                } else if (answer.equalsIgnoreCase("create")) {
                    //Calls the createAccount method
                    Account account = accountLogic.createOneAccount(Session.getAccounts());
                    //Stores the account in the accounts list
                    Session.getAccounts().put(account.getAccountId(), account);
                } else if (answer.equalsIgnoreCase("quit")) {
                    System.out.println("Terminating program...");
                    //Ends the program
                    return;
                } else {
                    //Invalid input
                    System.out.println("Invalid input. Please enter 'login' or 'create'.");
                }

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
    void preStartTasks () {
        //Adds a shutdown hook to save data
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(() -> {
            if (Session.getKillswitch()) return;
            SaveData.saveData(Session.getAdmins(), Session.getAccounts(), Session.getOwner(), LogManager.getLogs());
            projectUtils.closeReader();
        }));
        int version = Runtime.version().feature();
        System.out.println("Version: JDK " + version);
        if (version < 21) {
            System.out.println("WARNING: This code is recommended for JDK 21 and above.");
        }

    }
}
