package com.ahaviss.main;
//Local imports
import com.ahaviss.logs.enums.*;
import com.ahaviss.logs.manager.LogManager;
import com.ahaviss.exceptions.*;
import com.ahaviss.save.SaveData;
import com.ahaviss.database.Account;
import com.ahaviss.enums.*;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.logic.*;
import com.ahaviss.session.Session;
import com.ahaviss.menus.*;
public class Main {
    //Account login
    public static void accountLogin () {
        while (true) {
            try {
                //Check if the accounts list is empty
                if (!ProjectUtils.checkArrayList(Session.getAccounts())) {
                    System.out.println("No accounts available. Please create an account.");
                    return;
                }
                //Call the login system for accounts
                Account tempUser;
                try {
                    tempUser = LoginSystem.accountLogin(Session.getAccounts());
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
                            break;
                        }
                        System.out.printf("Locked for: %d minutes.%n", account.getDurationLocked());
                        LogManager.addLog(Action.ACCOUNT_AUTO_LOCKED, User.USER, String.format("%d (%s)", account.getAccountId(), account.getAccountHolder()), null, prevStatus, String.valueOf(account.getAccountStatus()));
                        return;
                    //If account is null
                    } else {
                        System.out.println(e.getMessage());
                        break;
                    }
                }
                catch (LoginFailedException e) {
                    System.out.println(e.getMessage());
                    break;
                }
                //Login user if the above conditions aren't true
                System.out.println("Login successful!");
                Session.setCurrentAccount(tempUser);
                //Welcome message
                System.out.printf("Welcome back, %s!%n", Session.getCurrentAccount().getAccountHolder());
                //Role is set to user
                Session.setRole(LoginEnums.USER);
                //End loop
                break;
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
    public static void adminLogin () {
        while (true) {
            try {
                //Calls the login system for admins
                int adminIndex = LoginSystem.adminLogin(Session.getAdmins(), Session.getOwner());
                //If the admin was validated as the owner
                if (adminIndex == Integer.MIN_VALUE) {
                    System.out.println("Welcome back, owner!");
                    //Role is set
                    Session.setRole(LoginEnums.OWNER);
                    break;
                }
                //If the admin is logged in successfully
                Session.setCurrentAdmin(Session.getAdmins().get(adminIndex));
                System.out.printf("Welcome back %s!%n", Session.getCurrentAdmin().getAdminName());
                //Role is set
                Session.setRole(LoginEnums.ADMIN);
                break;
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
    public static void loadData () {
        String password = ProjectUtils.getValidString("Password:");
        //Loads data
        try {
            Session.setAccounts(SaveData.loadAccountData(password));
            Session.setAdmins(SaveData.loadAdminData(password));
            Session.setOwner(SaveData.loadOwnerData(password));
            LogManager.loadLogs(SaveData.loadAuditData(password));
        }
        catch (Exception e) {
            System.out.println("Critical Error: Key corrupted or mismatched");
            Session.setKillswitch(true);
            System.exit(1);
        }
    }
    //Main method
    public static void main(String[] args) {
        //Calls loadData method
        loadData();
        //Adds a shutdown hook to save data
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(() -> {
            if (Session.getKillswitch()) return;
            SaveData.saveData(Session.getAdmins(), Session.getAccounts(), Session.getOwner(), LogManager.getLogs());
            ProjectUtils.closeReader();
        }));
        int version = Runtime.version().feature();
        System.out.println("Version: JDK " + version);
        if (version < 23) {
            System.out.println("WARNING: This code is recommended for JDK 23 and above.");
        }
        while (true) {
            try {
                //If the role is admin or owner, call the adminPanel method
                if (Session.getRole() == LoginEnums.ADMIN || Session.getRole() == LoginEnums.OWNER) {
                    ControlFlow controlFlow = AdminMenus.adminPanel();
                    if (controlFlow == ControlFlow.QUIT) return;
                    else continue;
                //If the role is user, call the accountPanel method
                } else if (Session.getRole() == LoginEnums.USER) {
                    ControlFlow controlFlow = AccountMenus.accountPanel();
                    if (controlFlow == ControlFlow.QUIT) return;
                    else continue;
                }
                //If the role is none
                System.out.println("Welcome to the Banking System!");
                //Ask the user to log in, create or quit
                String answer = ProjectUtils.getValidString("Would you like to login, create an account, or quit? (login/create/quit)");
                if (answer.equalsIgnoreCase("login")) {
                    String login = ProjectUtils.getValidString("Would you like to login as an account holder or an admin? (account holder/admin)");
                    if (login.equalsIgnoreCase("account holder")) {
                        //Calls the accountLogin method
                        accountLogin();
                    } else if (login.equalsIgnoreCase("admin")) {
                        //Calls the adminLogin method
                        adminLogin();
                    } else {
                        //Invalid input
                        System.out.println("Invalid input. Please enter 'account holder', 'admin', or 'quit'.");
                    }
                } else if (answer.equalsIgnoreCase("create")) {
                    //Calls the createAccount method
                    Account account = AccountLogic.createOneAccount(Session.getAccounts());
                    //Stores the account in the accounts list
                    Session.getAccounts().add(account);
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
}
