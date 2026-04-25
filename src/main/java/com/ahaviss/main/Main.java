package com.ahaviss.main;
//Java imports
import java.util.ArrayList;
//Local imports
import com.ahaviss.logs.enums.*;
import com.ahaviss.logs.manager.LogManager;
import com.ahaviss.database.Owner;
import com.ahaviss.exceptions.*;
import com.ahaviss.save.SaveData;
import com.ahaviss.database.Account;
import com.ahaviss.database.Admin;
import com.ahaviss.enums.*;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.logic.*;
import com.ahaviss.utilities.SecurityUtils;

public class Main {
    //Unlock password
    private static final String masterPassword = "123RandomPassword";
    //Owner's credentials'
    private static Owner owner = new Owner();
    //Current account index
    private static Account currentAccount;
    private static Admin currentAdmin;
    //Current role
    private static LoginEnums role = LoginEnums.NONE;
    //Account and admin lists
    private static ArrayList<Account> accounts = new ArrayList<>();
    private static ArrayList<Admin> admins = new ArrayList<>();
    //Killswitch boolean
    private static boolean killswitch = false;
    //Account login
    public static void accountLogin () {
        while (true) {
            try {
                //Check if the accounts list is empty
                if (!ProjectUtils.checkArrayList(accounts)) {
                    System.out.println("No accounts available. Please create an account.");
                    return;
                }
                //Call the login system for accounts
                Account tempUser;
                try {
                    tempUser = LoginSystem.accountLogin(accounts);
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
                currentAccount = tempUser;
                //Welcome message
                System.out.printf("Welcome back, %s!%n", currentAccount.getAccountHolder());
                //Role is set to user
                role = LoginEnums.USER;
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
                int adminIndex = LoginSystem.adminLogin(admins, owner);
                //If the admin was validated as the owner
                if (adminIndex == Integer.MIN_VALUE) {
                    System.out.println("Welcome back, owner!");
                    //Role is set
                    role = LoginEnums.OWNER;
                    break;
                }
                //If the admin is logged in successfully
                currentAdmin = admins.get(adminIndex);
                System.out.printf("Welcome back %s!%n", currentAdmin.getAdminName());
                //Role is set
                role = LoginEnums.ADMIN;
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
    public static ControlFlow accountPanel () {
        while (true) {
            //Account holder options
            System.out.println("Account Panel");
            String option = ProjectUtils.getValidString("Deposit, Withdraw, Transfer, View Balance, View History, View Account Info, Logout, Change Password, Quit Program");
            switch (option.toLowerCase()) {
                case "view account info":
                    //Print account information
                    currentAccount.printInfo();
                    break;
                case "deposit":
                    //Call deposit method
                    AccountLogic.deposit(currentAccount);
                    break;
                case "withdraw":
                    //Call withdraw method
                    AccountLogic.withdraw(currentAccount);
                    break;
                case "transfer":
                    //Call transfer method
                    AccountLogic.transfer(accounts, currentAccount);
                    break;
                case "view balance":
                    //Get user balance
                    System.out.println("$" + currentAccount.getBalance());
                    break;
                case "view history":
                    //Print account logs
                    currentAccount.printHistory();
                    break;
                case "logout":
                    //Logs out the user
                    System.out.println("Logging out...");
                    //Sets user role to none
                    role = LoginEnums.NONE;
                    currentAccount = null;
                    return ControlFlow.MAIN_MENU;
                case "change password":
                    //Call edit method
                    Account newAcc = AccountLogic.editPassword(currentAccount);
                    if (newAcc != null) {
                        //Check if the account isn't null
                        currentAccount = newAcc;
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
        if (!ProjectUtils.checkArrayList(accounts)) {
            System.out.println("No accounts available. Please create an account.");
            return;
        }
        //Gets the number of accounts to edit
        int amountOfAccountToEdit = ProjectUtils.getValidInt(String.format("Enter the amount of the accounts you want to edit (%d total accounts): ", accounts.size()));
        //Gets a valid input
        if (amountOfAccountToEdit > accounts.size()) {
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
                    accountIndex = AccountLogic.loopThroughAccounts(accounts, accountId);
                }
                //If the account isn't found
                catch (UserNotFoundException e) {
                    System.out.println(e.getMessage());
                    continue;
                }
                while (true) {
                    //Account editing options
                    Admin admin = null;
                    if (role == LoginEnums.ADMIN) {
                        admin = currentAdmin;
                    }
                    String whatToEdit = ProjectUtils.getValidString("Edit Holder, Edit Password, Edit Credit Score, Edit Account Status, Quit Editing");
                    switch (whatToEdit.toLowerCase()) {
                        case "edit holder":
                            //Call editAccountHolder method
                            Account tempAccount = AccountLogic.editAccountHolder(accounts.get(accountIndex), admin);
                            accounts.set(accountIndex, tempAccount);
                            break;
                        case "edit password":
                            //Call editPassword method
                            Account tempAccount2 = AccountLogic.editPasswordAdmin(accounts.get(accountIndex), admin);
                            accounts.set(accountIndex, tempAccount2);
                            break;
                        case "edit credit score":
                            //Call editCreditScore method
                            Account tempAccount3 = AccountLogic.editCreditScore(accounts.get(accountIndex), admin);
                            accounts.set(accountIndex, tempAccount3);
                            break;
                        case "edit account status":
                            //Call editAccountStatus method
                            Account tempAccount4 = AccountLogic.editAccountStatus(accounts.get(accountIndex), admin);
                            accounts.set(accountIndex, tempAccount4);
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
    public static void editAdmin () {
        //Owner option to edit admins
        while (true) {
            try {
                while (true) {
                    //Checks admin list
                    if (!ProjectUtils.checkArrayList(admins)) {
                        System.out.println("No admins available. Please create an admin.");
                        return;
                    }
                    int amountOfAdminsToEdit = ProjectUtils.getValidInt(String.format("Enter the amount of the admins you want to edit (%d total admins): ", admins.size()));
                    //Gets valid input
                    if (amountOfAdminsToEdit > admins.size()) {
                        System.out.println("Invalid input. Please enter a number less than or equal to the number of admins.");
                        continue;
                    } else if (amountOfAdminsToEdit == 0) {
                        System.out.println("No admins edited.");
                        return;
                    }
                    for (int i = 0; i < amountOfAdminsToEdit; i++) {
                        int adminIndex;
                        while (true) {
                            //Gets the ID of the admin to edit
                            int adminId = ProjectUtils.getValidInt("Enter the ID of the admin you want to edit: ");
                            try {
                                adminIndex = AdminLogic.loopThroughAdmins(admins, adminId);
                            }
                            //If admin not found
                            catch (UserNotFoundException e) {
                                System.out.println(e.getMessage());
                                continue;
                            }
                            break;
                        }

                        while (true) {
                            //Admin editing options
                            String option = ProjectUtils.getValidString("Edit Name, Edit Password, Quit editing");
                            switch (option.toLowerCase()) {
                                case "edit name":
                                    //Calls editAdminName method
                                    Admin tempAdmin = AdminLogic.editAdminName(admins.get(adminIndex));
                                    admins.set(adminIndex, tempAdmin);
                                    break;
                                case "edit password":
                                    //Calls editPassword method
                                    Admin tempAdmin2 = AdminLogic.editPassword(admins.get(adminIndex));
                                    admins.set(adminIndex, tempAdmin2);
                                    break;
                                case "quit editing":
                                    //Returns to the main menu
                                    return;
                                default:
                                    //Invalid option
                                    System.out.println("Invalid option. Please try again.");
                                    continue;
                            }
                            //Ask to make more changes
                            if (!ProjectUtils.askToContinue()) {
                                return;
                            }
                            break;
                        }
                    }
                    break;
                }
            }
            catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public static void editOwner () {
        //Asks for and validates the current password
        boolean validated = false;
        for (int i = 0; i < 3; i++) {
            String currentPassword = ProjectUtils.getValidString("Please enter current owner password.");
            if (SecurityUtils.verifyPassword(currentPassword, owner.getPassword())) {
                validated = true;
                break;
            }
            else System.out.println("Invalid password. Please try again.");
        }
        //If not validated
        if (!validated) {
            System.out.println("Password change failed. Please try again.");
            return;
        }
        while (true) {
            try {
                //Asks for current action
                String option = ProjectUtils.getValidString("Edit Username, Edit Password, Quit editing");
                switch (option.toLowerCase()) {
                    case "edit username":
                        //Sets username
                        owner.setUsername(ProjectUtils.getValidString("Enter new username:"));
                        break;
                    case "edit password":
                        //Sets password
                        owner.setPasswordFromUser(ProjectUtils.getValidPassword("Enter new password:"));
                        break;
                    case "quit editing":
                        return;
                    default:
                        //Invalid option
                        System.out.println("Invalid option. Please try again.");
                }
            }
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public static void manageLogs () {
        while (true) {
            try {
                String option = ProjectUtils.getValidString("Print logs, Clear all logs, Quit managing.");
                if (option.equalsIgnoreCase("print logs")) {
                    //Prints logs
                    LogManager.printLogs();
                } else if (option.equalsIgnoreCase("clear all logs")) {
                    //Clears current arraylist and deletes file
                    LogManager.clearLogs();
                    SaveData.clearLogs();
                } else if (option.equalsIgnoreCase("quit managing")) {
                    return;
                //Invalid input
                } else {
                    System.out.println("Invalid option. Please try again.");
                }
            }
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public static ControlFlow ownerPanel () {
        //Owner panel options
        while (true) {
            try {
                String option = ProjectUtils.getValidString("Add Admins, Delete Admins, Edit Admins, Logout, Quit Owner Panel, Killswitch\nEdit Owner Account, Manage Logs, Quit Program");
                switch (option.toLowerCase()) {
                    case "add admins":
                        //Calls addAdmin method
                        AdminLogic.addAdmins(admins);
                        break;
                    case "delete admins":
                        //Calls deleteAdmin method
                        ArrayList<Admin> tempAdmins = AdminLogic.deleteAdmins(admins);
                        if (tempAdmins != null) {
                            //Edits the admin list only if tempAdmins is not null
                            admins = tempAdmins;
                        }
                        break;
                    case "edit admins":
                        //Calls editAdmin method
                        editAdmin();
                        break;
                    case "logout":
                        //Logs out the user
                        System.out.println("Logging out...");
                        //Sets user role to none
                        role = LoginEnums.NONE;
                        //Send a main menu message
                        return ControlFlow.MAIN_MENU;
                    case "quit owner panel":
                        //Send a back message
                        return ControlFlow.BACK;
                    case "quit program":
                        System.out.println("Terminating program...");
                        //Send a quit message
                        return ControlFlow.QUIT;
                    case "killswitch":
                        //Assigns killswitch to true
                        if (SaveData.killswitch()) {
                            killswitch = true;
                            //Terminates the JVM
                            System.exit(0);
                        }
                        break;
                    case "edit owner account":
                        //Calls editOwner method
                        editOwner();
                        break;
                    case "manage logs":
                        //Calls manageLogs method
                        manageLogs();
                        break;
                    default:
                        //Invalid option
                        System.out.println("Invalid option. Please try again.");
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
    public static ControlFlow adminPanel () {
        while (true) {
            //If not admin or owner
            if (role != LoginEnums.ADMIN && role != LoginEnums.OWNER) {
                System.out.println("You are not authorized to access this panel.");
                continue;
            }
            //Option declared outside the if-else, allowing both conditions to edit the String option
            String option;
            //Owner panel option for the owner
            if (role == LoginEnums.OWNER) {
                option = ProjectUtils.getValidString("Add Accounts, Delete Accounts, Edit accounts, Logout, Owner Panel, Quit program");
            }
            //General admin panel option
            else {
                option = ProjectUtils.getValidString("Add Accounts, Delete Accounts, Edit accounts, Logout, Quit program");
            }
            switch (option.toLowerCase()) {
                case "add accounts":
                    //Calls addAccount method
                    AccountLogic.createAccount(accounts, currentAdmin);
                    break;
                case "delete accounts":
                    //Calls deleteAccount method
                    ArrayList <Account> tempAccount = AccountLogic.deleteAccounts(accounts, currentAdmin);
                    if (tempAccount != null) {
                        //Edits the accounts list only if tempAccount is not null
                        accounts = tempAccount;
                    }
                    break;
                case "edit accounts":
                    //Calls editAccount method
                    editAccount();
                    break;
                case "logout":
                    //Logs out the user
                    System.out.println("Logging out...");
                    //Sets user role to none
                    role = LoginEnums.NONE;
                    currentAdmin = null;
                    return ControlFlow.MAIN_MENU;
                case "quit program":
                    System.out.println("Terminating program...");
                    //Send a quit message
                    return ControlFlow.QUIT;
                default:
                    //Access the owner panel option only if the role is the owner
                    if (option.equalsIgnoreCase("owner panel") && role == LoginEnums.OWNER) {
                        ControlFlow controlFlow = ownerPanel();
                        if (controlFlow == ControlFlow.MAIN_MENU) return ControlFlow.MAIN_MENU;
                        if (controlFlow == ControlFlow.BACK) continue;
                        if (controlFlow == ControlFlow.QUIT) return ControlFlow.QUIT;
                    }
                    //General admin panel error message
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    public static void loadData () {
        String password = ProjectUtils.getValidString("Password:");
        if (!password.equals(masterPassword)) {
            System.out.println("Wrong password.");
            killswitch = true;
            System.exit(1);
        }
        //Loads data
        accounts = SaveData.loadAccountData(masterPassword);
        admins = SaveData.loadAdminData(masterPassword);
        owner = SaveData.loadOwnerData(masterPassword);
        LogManager.loadLogs(SaveData.loadAuditData(masterPassword));
    }
    //Main method
    public static void main(String[] args) {
        //Calls loadData method
        loadData();
        //Adds a shutdown hook to save data
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(() -> {
            if (killswitch) return;
            SaveData.saveData(admins, accounts, owner, LogManager.getLogs(), masterPassword);
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
                if (role == LoginEnums.ADMIN || role == LoginEnums.OWNER) {
                    ControlFlow controlFlow = adminPanel();
                    if (controlFlow == ControlFlow.QUIT) return;
                    else continue;
                //If the role is user, call the accountPanel method
                } else if (role == LoginEnums.USER) {
                    ControlFlow controlFlow = accountPanel();
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
                    Account account = AccountLogic.createOneAccount(accounts);
                    //Stores the account in the accounts list
                    accounts.add(account);
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
