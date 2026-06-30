/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.menus;

import com.ahaviss.database.Account;
import com.ahaviss.enums.AccountStatus;
import com.ahaviss.enums.ControlFlow;
import com.ahaviss.enums.LoginEnums;
import com.ahaviss.logic.AccountLogic;
import com.ahaviss.session.Session;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.utilities.SQLExecutor;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AccountMenus {
    private final AccountLogic accountLogic;
    private final ProjectUtils projectUtils;
    private final SQLExecutor executor;
    public AccountMenus(AccountLogic accountLogic, ProjectUtils projectUtils, SQLExecutor executor) {
        this.accountLogic = accountLogic; 
        this.projectUtils = projectUtils;
        this.executor = executor;
    }
    public ControlFlow accountPanel () throws Exception {
        while (true) {
            //Account holder options
            System.out.println("Account Panel");
            String option = projectUtils.getValidString("Deposit, Withdraw, Transfer, View Balance, View History, View Account Info, Logout, Change Password, Quit Program");
            switch (option.toLowerCase()) {
                case "view account info":
                    //Print account information
                    int id = Session.getCurrentAccount();
                    Map<String, Object> info = executor.executeSQL("SELECT * from accounts WHERE account_id = ?", List.of(List.of(id))).getFirst().getFirst();
                    String accountHolder = info.get("account_holder").toString();
                    double balance = projectUtils.verifyInstanceOf(info.get("balance"), BigDecimal.class, () -> new SQLException("Incorrect return type given from database")).doubleValue();
                    String password = info.get("account_password").toString();
                    String status = info.get("account_status").toString();
                    int creditScore = projectUtils.verifyInstanceOf(info.get("credit_score"), Integer.class, () -> new SQLException("Incorrect return type given from database"));
                    Account account = new Account(Session.getCurrentAccount(), accountHolder, balance, password, status.equalsIgnoreCase("active") ? AccountStatus.ACTIVE : AccountStatus.LOCKED, creditScore);
                    account.printInfo();
                    break;
                case "deposit":
                    //Call deposit method
                    accountLogic.deposit(Session.getCurrentAccount());
                    break;
                case "withdraw":
                    //Call withdraw method
                    accountLogic.withdraw(Session.getCurrentAccount());
                    break;
                case "transfer":
                    //Call transfer method
                    accountLogic.transfer(Session.getCurrentAccount());
                    break;
                case "view balance":
                    //Get user balance
                    System.out.println("$" + executor.executeSQL("SELECT balance FROM accounts WHERE account_id = ?", List.of(List.of(Session.getCurrentAccount()))).getFirst().getFirst().get("balance").toString());
                    break;
                case "view history":
                    //Print account logs
                    printHistory(executor.executeSQL("""
                        SELECT * FROM withdrawals WHERE account_id = ?;
                        SELECT * FROM deposits WHERE account_id = ?;
                        SELECT * FROM transfers WHERE source_account_id = ?;
                        SELECT * from transfers WHERE target_account_id = ?;
                    """, List.of(List.of(Session.getCurrentAccount()), List.of(Session.getCurrentAccount()), List.of(Session.getCurrentAccount()), List.of(Session.getCurrentAccount()))));
                    break;
                case "logout":
                    //Logs out the user
                    System.out.println("Logging out...");
                    //Sets user role to none
                    Session.setRole(LoginEnums.NONE);
                    Session.setCurrentAccount(-1);
                    return ControlFlow.MAIN_MENU;
                case "change password":
                    //Call edit method
                    accountLogic.editPassword(Session.getCurrentAccount());
                    break;
                case "quit program":
                    System.out.println("Terminating program...");
                    //Send a quit message
                    return ControlFlow.QUIT;
                default:
                    //Invalid option
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    private void printHistory(List<List<Map<String, Object>>> history) {
        System.out.println("History:");
        System.out.println("Withdrawals:");
        List<Map<String, Object>> withdrawals = history.getFirst();
        if (withdrawals.isEmpty()) System.out.println("Empty.");
        else withdrawals.forEach(map -> {
            System.out.println("    Account ID: " + map.get("account_id"));
            System.out.println("    Amount Withdrew: " + map.get("amount"));
            System.out.println();
        });
        System.out.println("Deposits:");
        List<Map<String, Object>> deposits = history.get(1);
        if (deposits.isEmpty()) System.out.println("Empty.");
        else deposits.forEach(map -> {
            System.out.println("    Account ID: " + map.get("account_id"));
            System.out.println("    Amount Deposited: " + map.get("amount"));
            System.out.println();
        });
        System.out.println("Incoming Transfers:");
        List<Map<String, Object>> incomingTransfers = history.get(3);
        if (incomingTransfers.isEmpty()) System.out.println("Empty.");
        else incomingTransfers.forEach(map -> {
            System.out.println("    Source Account ID: " + map.get("source_account_id"));
            System.out.println("    Target Account ID: " + map.get("target_account_id"));
            System.out.println("    Amount Transferred: " + map.get("amount"));
            System.out.println();
        });
        System.out.println("Outgoing Transfers:");
        List<Map<String, Object>> outgoingTransfers = history.get(2);
        if (outgoingTransfers.isEmpty()) System.out.println("Empty.");
        else outgoingTransfers.forEach(map -> {
            System.out.println("    Source Account ID: " + map.get("source_account_id"));
            System.out.println("    Target Account ID: " + map.get("target_account_id"));
            System.out.println("    Amount Transferred: " + map.get("amount"));
            System.out.println();
        });
    }
    public void editAccount () throws Exception {
        //Checks if the accounts list is empty
        if (!projectUtils.tableHasContents(Account.class)) {
            System.out.println("No accounts available. Please create an account.");
            return;
        }
        int amountOfAccounts = projectUtils.sizeOfTable(Account.class);
        //Gets the number of accounts to edit
        int amountOfAccountToEdit = projectUtils.getValidInt(String.format("Enter the amount of the accounts you want to edit (%d total accounts): ", amountOfAccounts));
        //Gets a valid input
        if (amountOfAccountToEdit > amountOfAccounts) {
            System.out.println("Invalid input. Please enter a number less than or equal to the number of accounts.");
            return;
        } else if (amountOfAccountToEdit == 0) {
            System.out.println("No accounts edited.");
            return;
        }
        for (int i = 0; i < amountOfAccountToEdit; i++) {
            while (true) {
                //Gets the ID of the account to edit
                int account = projectUtils.getValidInt("Enter the ID of the account you want to edit: ");
                //Checks if account is found
                if (!projectUtils.idExists(account, Account.class)) {
                    System.out.printf("Account ID %d not found.%n", account);
                    continue;
                }
                while (true) {
                    //Account editing options
                    int admin = 0;
                    if (Session.getRole() == LoginEnums.ADMIN) {
                        admin = Session.getCurrentAdmin();
                    }
                    String whatToEdit = projectUtils.getValidString("Edit Holder, Edit Password, Edit Credit Score, Edit Account Status, Quit Editing");
                    switch (whatToEdit.toLowerCase()) {
                        case "edit holder":
                            //Call editAccountHolder method
                            accountLogic.editAccountHolder(account, admin);
                            break;
                        case "edit password":
                            //Call editPassword method
                            accountLogic.editPasswordAdmin(account, admin);
                            break;
                        case "edit credit score":
                            //Call editCreditScore method
                            accountLogic.editCreditScore(account, admin);
                            break;
                        case "edit account status":
                            //Call editAccountStatus method
                            accountLogic.editAccountStatus(account, admin);
                            break;
                        case "quit editing":
                            //Return to the main menu
                            return;
                        default:
                            //Invalid option
                            System.out.println("Invalid option. Please try again.");
                            continue;
                    }
                    break;
                }
                //Ask to make more changes
                if (!projectUtils.askToContinue()) {
                    return;
                }
                break;
            }
        }
    }

}
