/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.menus;

import com.ahaviss.database.Account;
import com.ahaviss.database.Admin;
import com.ahaviss.enums.ControlFlow;
import com.ahaviss.enums.LoginEnums;
import com.ahaviss.logic.AccountLogic;
import com.ahaviss.session.Session;
import com.ahaviss.utilities.ProjectUtils;

public class AccountMenus {
    private final AccountLogic accountLogic;
    private final ProjectUtils projectUtils;
    public AccountMenus(AccountLogic accountLogic, ProjectUtils projectUtils) {this.accountLogic = accountLogic; this.projectUtils = projectUtils;}
    public ControlFlow accountPanel () {
        while (true) {
            //Account holder options
            System.out.println("Account Panel");
            String option = projectUtils.getValidString("Deposit, Withdraw, Transfer, View Balance, View History, View Account Info, Logout, Change Password, Quit Program");
            switch (option.toLowerCase()) {
                case "view account info":
                    //Print account information
                    Session.getCurrentAccount().printInfo();
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
                    accountLogic.transfer(Session.getAccounts(), Session.getCurrentAccount());
                    break;
                case "view balance":
                    //Get user balance
                    System.out.println("$" + Session.getCurrentAccount().getBalance());
                    break;
                case "view history":
                    //Print account logs
                    Session.getCurrentAccount().printHistory();
                    break;
                case "logout":
                    //Logs out the user
                    System.out.println("Logging out...");
                    //Sets user role to none
                    Session.setRole(LoginEnums.NONE);
                    Session.setCurrentAccount(null);
                    return ControlFlow.MAIN_MENU;
                case "change password":
                    //Call edit method
                    Account newAcc = accountLogic.editPassword(Session.getCurrentAccount());
                    if (newAcc != null) {
                        //Check if the account isn't null
                        Session.setCurrentAccount(newAcc);
                    } else {
                        continue;
                    }
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
    public void editAccount () {
        //Checks if the accounts list is empty
        if (!ProjectUtils.checkMap(Session.getAccounts())) {
            System.out.println("No accounts available. Please create an account.");
            return;
        }
        //Gets the number of accounts to edit
        int amountOfAccountToEdit = projectUtils.getValidInt(String.format("Enter the amount of the accounts you want to edit (%d total accounts): ", Session.getAccounts().size()));
        //Gets a valid input
        if (amountOfAccountToEdit > Session.getAccounts().size()) {
            System.out.println("Invalid input. Please enter a number less than or equal to the number of accounts.");
            return;
        } else if (amountOfAccountToEdit == 0) {
            System.out.println("No accounts edited.");
            return;
        }
        for (int i = 0; i < amountOfAccountToEdit; i++) {
            while (true) {
                //Gets the ID of the account to edit
                int accountId = projectUtils.getValidInt("Enter the ID of the account you want to edit: ");
                Account account = Session.getAccounts().get(accountId);
                //Checks if account is found
                if (account == null) {
                    System.out.printf("Account ID %d not found.%n", accountId);
                    continue;
                }
                while (true) {
                    //Account editing options
                    Admin admin = null;
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
