package com.ahaviss.menus;

import com.ahaviss.database.Account;
import com.ahaviss.database.Admin;
import com.ahaviss.enums.ControlFlow;
import com.ahaviss.enums.LoginEnums;
import com.ahaviss.exceptions.UserNotFoundException;
import com.ahaviss.logic.AccountLogic;
import com.ahaviss.logic.AdminLogic;
import com.ahaviss.session.Session;
import com.ahaviss.utilities.ProjectUtils;

import java.util.ArrayList;

public class AdminMenus {
    public static void editAdmin () {
        //Owner option to edit admins
        while (true) {
            try {
                while (true) {
                    //Checks admin list
                    if (!ProjectUtils.checkArrayList(Session.getAdmins())) {
                        System.out.println("No admins available. Please create an admin.");
                        return;
                    }
                    int amountOfAdminsToEdit = ProjectUtils.getValidInt(String.format("Enter the amount of the admins you want to edit (%d total admins): ", Session.getAdmins().size()));
                    //Gets valid input
                    if (amountOfAdminsToEdit > Session.getAdmins().size()) {
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
                                adminIndex = AdminLogic.loopThroughAdmins(Session.getAdmins(), adminId);
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
                                    Admin tempAdmin = AdminLogic.editAdminName(Session.getAdmins().get(adminIndex));
                                    Session.getAdmins().set(adminIndex, tempAdmin);
                                    break;
                                case "edit password":
                                    //Calls editPassword method
                                    Admin tempAdmin2 = AdminLogic.editPassword(Session.getAdmins().get(adminIndex));
                                    Session.getAdmins().set(adminIndex, tempAdmin2);
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
    public static ControlFlow adminPanel () {
        while (true) {
            //If not admin or owner
            if (Session.getRole() != LoginEnums.ADMIN && Session.getRole() != LoginEnums.OWNER) {
                System.out.println("You are not authorized to access this panel.");
                continue;
            }
            //Option declared outside the if-else, allowing both conditions to edit the String option
            String option;
            //Owner panel option for the owner
            if (Session.getRole() == LoginEnums.OWNER) {
                option = ProjectUtils.getValidString("Add Accounts, Delete Accounts, Edit accounts, Logout, Owner Panel, Quit program");
            }
            //General admin panel option
            else {
                option = ProjectUtils.getValidString("Add Accounts, Delete Accounts, Edit accounts, Logout, Quit program");
            }
            switch (option.toLowerCase()) {
                case "add accounts":
                    //Calls addAccount method
                    AccountLogic.createAccount(Session.getAccounts(), Session.getCurrentAdmin());
                    break;
                case "delete accounts":
                    //Calls deleteAccount method
                    ArrayList <Account> tempAccount = AccountLogic.deleteAccounts(Session.getAccounts(), Session.getCurrentAdmin());
                    if (tempAccount != null) {
                        //Edits the accounts list only if tempAccount is not null
                        Session.setAccounts(tempAccount);
                    }
                    break;
                case "edit accounts":
                    //Calls editAccount method
                    AccountMenus.editAccount();
                    break;
                case "logout":
                    //Logs out the user
                    System.out.println("Logging out...");
                    //Sets user role to none
                    Session.setRole(LoginEnums.NONE);
                    Session.setCurrentAdmin(null);
                    return ControlFlow.MAIN_MENU;
                case "quit program":
                    System.out.println("Terminating program...");
                    //Send a quit message
                    return ControlFlow.QUIT;
                default:
                    //Access the owner panel option only if the role is the owner
                    if (option.equalsIgnoreCase("owner panel") && Session.getRole() == LoginEnums.OWNER) {
                        ControlFlow controlFlow = OwnerMenus.ownerPanel();
                        if (controlFlow == ControlFlow.MAIN_MENU) return ControlFlow.MAIN_MENU;
                        if (controlFlow == ControlFlow.BACK) continue;
                        if (controlFlow == ControlFlow.QUIT) return ControlFlow.QUIT;
                    }
                    //General admin panel error message
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
