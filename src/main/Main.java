package main;
//Java imports
import java.util.ArrayList;
//Local imports
import logs.enums.*;
import logs.manager.LogManager;
import database.Owner;
import exceptions.*;
import save.SaveData;
import database.Account;
import database.Admin;
import enums.*;
import utilities.ProjectUtils;
import logic.*;
public class Main {
    //Owner's credentials'
    private static Owner owner = new Owner();
    //Current account index
    private static int currentAccount = -1;
    private static int currentAdmin = -1;
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
                int tempUser;
                try {
                    tempUser = LoginSystem.accountLogin(accounts);
                }
                catch (AccountLockedException e) {
                    System.out.println(e.getMessage());
                    int accountIndex = e.traceAccountIndex();
                    String prevStatus = String.valueOf(accounts.get(accountIndex).getAccountStatus());
                    if (accountIndex != -1) {
                        accounts.get(accountIndex).setAccountStatus(AccountStatus.LOCKED);
                    }
                    LogManager.addLog(Action.ACCOUNT_AUTO_LOCKED, User.USER, String.format("%d (%s)", accounts.get(accountIndex).getAccountId(), accounts.get(accountIndex).getAccountHolder()), null, prevStatus, String.valueOf(accounts.get(accountIndex).getAccountStatus()));
                    break;
                }
                catch (LoginFailedException e) {
                    System.out.println(e.getMessage());
                    break;
                }
                //Login user if the above conditions aren't true
                System.out.println("Login successful!");
                //Welcome message
                System.out.printf("Welcome back, %s!%n", accounts.get(tempUser).getAccountHolder());
                //Role is set to user
                role = LoginEnums.USER;
                currentAccount = tempUser;
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
                currentAdmin = adminIndex;
                System.out.printf("Welcome back %s!", admins.get(adminIndex).getAdminName());
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
                    accounts.get(currentAccount).printInfo();
                    break;
                case "deposit":
                    //Call deposit method
                    AccountLogic.deposit(accounts.get(currentAccount));
                    break;
                case "withdraw":
                    //Call withdraw method
                    AccountLogic.withdraw(accounts.get(currentAccount));
                    break;
                case "transfer":
                    //Call transfer method
                    AccountLogic.transfer(accounts, currentAccount);
                    break;
                case "view balance":
                    //Get user balance
                    System.out.println("$" + accounts.get(currentAccount).getBalance());
                    break;
                case "view history":
                    //Print account logs
                    accounts.get(currentAccount).printHistory();
                    break;
                case "logout":
                    //Logs out the user
                    System.out.println("Logging out...");
                    //Sets user role to none
                    role = LoginEnums.NONE;
                    currentAccount = -1;
                    return ControlFlow.MAIN_MENU;
                case "change password":
                    //Call edit method
                    Account newAcc = AccountLogic.editPassword(accounts.get(currentAccount));
                    if (newAcc != null) {
                        //Check if the account isn't null
                        accounts.set(currentAccount, newAcc);
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
                int accountIndex = -1;
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
                        admin = admins.get(currentAdmin);
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
                        int adminIndex = -1;
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
            String tempCurrentPassword = ProjectUtils.getValidString("Please enter current owner password.");
            String currentPassword = ProjectUtils.hashPassword(tempCurrentPassword);
            if (currentPassword.equals(owner.getPassword())) {
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
                        owner.setPassword(ProjectUtils.getValidPassword("Enter new password:"));
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
                    LogManager.printLogs();
                } else if (option.equalsIgnoreCase("clear all logs")) {
                    SaveData.clearLogs();
                } else if (option.equalsIgnoreCase("quit managing")) {
                    return;
                } else {
                    System.out.println("Invalid option. Please try again.");
                }
            }
            catch (Exception e) {
                System.out.printf("An unexcepted error occurred: %s%n", e.getMessage());
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
                        admins = AdminLogic.addAdmins(admins);
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
            Admin admin = null;
            if (role == LoginEnums.ADMIN) {
                admin = admins.get(currentAdmin);
            }
            switch (option.toLowerCase()) {
                case "add accounts":
                    //Calls addAccount method
                    accounts = AccountLogic.createAccount(accounts, admin);
                    break;
                case "delete accounts":
                    //Calls deleteAccount method
                    ArrayList <Account> tempAccount = AccountLogic.deleteAccounts(accounts, admin);
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
                    currentAdmin = -1;
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
        //Loads data
        accounts = SaveData.loadAccountData();
        admins = SaveData.loadAdminData();
        owner = SaveData.loadOwnerData();
        LogManager.loadLogs(SaveData.loadAuditData());
    }
    //Main method
    public static void main(String[] args) {
        //Calls loadData method
        loadData();
        //Adds a shutdown hook to save data
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(() -> {
            if (killswitch) return;
            SaveData.saveData(admins, accounts, owner, LogManager.getLogs());
            ProjectUtils.closeReader();
        }));
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
