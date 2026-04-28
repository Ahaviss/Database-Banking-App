package com.ahaviss.menus;

import com.ahaviss.database.Account;
import com.ahaviss.database.Admin;
import com.ahaviss.enums.ControlFlow;
import com.ahaviss.enums.LoginEnums;
import com.ahaviss.exceptions.UserNotFoundException;
import com.ahaviss.logic.AccountLogic;
import com.ahaviss.session.Session;
import com.ahaviss.utilities.ProjectUtils;

public class AccountMenus {
    public static ControlFlow accountPanel () {
        while (true) {
            //Account holder options
            System.out.println("Account Panel");
            String option = ProjectUtils.getValidString("Deposit, Withdraw, Transfer, View Balance, View History, View Account Info, Logout, Change Password, Quit Program");
            switch (option.toLowerCase()) {
                case "view account info":
                    //Print account information
                    Session.getCurrentAccount().printInfo();
                    break;
                case "deposit":
                    //Call deposit method
                    AccountLogic.deposit(Session.getCurrentAccount());
                    break;
                case "withdraw":
                    //Call withdraw method
                    AccountLogic.withdraw(Session.getCurrentAccount());
                    break;
                case "transfer":
                    //Call transfer method
                    AccountLogic.transfer(Session.getAccounts(), Session.getCurrentAccount());
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
                    Account newAcc = AccountLogic.editPassword(Session.getCurrentAccount());
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
    public static void editAccount () {
        //Checks if the accounts list is empty
        if (!ProjectUtils.checkArrayList(Session.getAccounts())) {
            System.out.println("No accounts available. Please create an account.");
            return;
        }
        //Gets the number of accounts to edit
        int amountOfAccountToEdit = ProjectUtils.getValidInt(String.format("Enter the amount of the accounts you want to edit (%d total accounts): ", Session.getAccounts().size()));
        //Gets a valid input
        if (amountOfAccountToEdit > Session.getAccounts().size()) {
            System.out.println("Invalid input. Please enter a number less than or equal to the number of accounts.");
            return;
        } else if (amountOfAccountToEdit ==0) {
            System.out.println("No accounts edited.");
            return;
        }
        for (int i = 0; i < amountOfAccountToEdit; i++) {
            while (true) {
                //Gets the ID of the account to edit
                int accountId = ProjectUtils.getValidInt("Enter the ID of the account you want to edit: ");
                int accountIndex;
                try {
                    accountIndex = AccountLogic.loopThroughAccounts(Session.getAccounts(), accountId);
                }
                //If the account isn't found
                catch (UserNotFoundException e) {
                    System.out.println(e.getMessage());
                    continue;
                }
                while (true) {
                    //Account editing options
                    Admin admin = null;
                    if (Session.getRole() == LoginEnums.ADMIN) {
                        admin = Session.getCurrentAdmin();
                    }
                    String whatToEdit = ProjectUtils.getValidString("Edit Holder, Edit Password, Edit Credit Score, Edit Account Status, Quit Editing");
                    switch (whatToEdit.toLowerCase()) {
                        case "edit holder":
                            //Call editAccountHolder method
                            Account tempAccount = AccountLogic.editAccountHolder(Session.getAccounts().get(accountIndex), admin);
                            Session.getAccounts().set(accountIndex, tempAccount);
                            break;
                        case "edit password":
                            //Call editPassword method
                            Account tempAccount2 = AccountLogic.editPasswordAdmin(Session.getAccounts().get(accountIndex), admin);
                            Session.getAccounts().set(accountIndex, tempAccount2);
                            break;
                        case "edit credit score":
                            //Call editCreditScore method
                            Account tempAccount3 = AccountLogic.editCreditScore(Session.getAccounts().get(accountIndex), admin);
                            Session.getAccounts().set(accountIndex, tempAccount3);
                            break;
                        case "edit account status":
                            //Call editAccountStatus method
                            Account tempAccount4 = AccountLogic.editAccountStatus(Session.getAccounts().get(accountIndex), admin);
                            Session.getAccounts().set(accountIndex, tempAccount4);
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
                if (!ProjectUtils.askToContinue()) {
                    return;
                }
                break;
            }
        }
    }

}
