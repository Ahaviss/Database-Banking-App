/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.menus;

import com.ahaviss.database.Admin;
import com.ahaviss.enums.ControlFlow;
import com.ahaviss.enums.LoginEnums;
import com.ahaviss.logic.AdminLogic;
import com.ahaviss.session.Session;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.utilities.SQLExecutor;
import com.ahaviss.utilities.SecurityUtils;
import net.sf.jsqlparser.JSQLParserException;

import java.sql.SQLException;
import java.util.List;


public class OwnerMenus {
    private final ProjectUtils projectUtils;
    private final AdminLogic adminLogic;
    private final GeneralMenus generalMenus;
    private final SQLExecutor executor;
    public OwnerMenus(ProjectUtils projectUtils, AdminLogic adminLogic, GeneralMenus generalMenus, SQLExecutor executor) {
        this.projectUtils = projectUtils;
        this.adminLogic = adminLogic;
        this.generalMenus = generalMenus;
        this.executor = executor;
    }
    public void editOwner () throws SQLException, JSQLParserException {
        //Asks for and validates the current password
        boolean validated = false;
        for (int i = 0; i < 3; i++) {
            String currentPassword = projectUtils.getValidString("Please enter current owner password.");
            if (SecurityUtils.verifyPassword(currentPassword, executor.executeSQL("SELECT owner_password FROM owner WHERE id = 1", null).getFirst().getFirst().get("owner_password").toString())) {
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
                String option = projectUtils.getValidString("Edit Username, Edit Password, Quit editing");
                switch (option.toLowerCase()) {
                    case "edit username":
                        //Sets username
                        executor.executeSQL("UPDATE owner SET username = ? WHERE id = 1", List.of(List.of(projectUtils.getValidString("Enter new username:"))));
                        break;
                    case "edit password":
                        //Sets password
                        executor.executeSQL("UPDATE owner SET owner_password = ? WHERE id = 1", List.of(List.of(SecurityUtils.hashPassword(projectUtils.getValidPassword("Enter new password:")))));
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
    public void editAdmins() {
        //Owner option to edit admins
        while (true) {
            try {
                while (true) {
                    //Checks admin list
                    if (!projectUtils.tableHasContents(Admin.class)) {
                        System.out.println("No admins available. Please create an admin.");
                        return;
                    }
                    int totalAdmins = projectUtils.sizeOfTable(Admin.class);
                    int amountOfAdminsToEdit = projectUtils.getValidInt(String.format("Enter the amount of the admins you want to edit (%d total admins): ", totalAdmins));
                    //Gets valid input
                    if (amountOfAdminsToEdit > totalAdmins) {
                        System.out.println("Invalid input. Please enter a number less than or equal to the number of admins.");
                        continue;
                    } else if (amountOfAdminsToEdit == 0) {
                        System.out.println("No admins edited.");
                        return;
                    }
                    for (int i = 0; i < amountOfAdminsToEdit; i++) {
                        int admin;
                        while (true) {
                            //Gets the ID of the admin to edit
                            admin = projectUtils.getValidInt("Enter the ID of the admin you want to edit: ");
                            //Checks if admin is found
                            if (!projectUtils.idExists(admin, Admin.class)) {
                                System.out.printf("Admin ID %d not found", admin);
                                continue;
                            }
                            break;
                        }

                        while (true) {
                            //Admin editing options
                            String option = projectUtils.getValidString("Edit Name, Edit Password, Quit editing");
                            switch (option.toLowerCase()) {
                                case "edit name":
                                    //Calls editAdminName method
                                    adminLogic.editAdminName(admin);
                                    break;
                                case "edit password":
                                    //Calls editPassword method
                                    adminLogic.editPassword(admin);
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
                            if (!projectUtils.askToContinue()) {
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
    public ControlFlow ownerPanel () {
        //Owner panel options
        while (true) {
            try {
                String option = projectUtils.getValidString("Add Admins, Delete Admins, Edit Admins, Logout, Quit Owner Panel, Killswitch\nEdit Owner Account, Manage Logs, Quit Program");
                switch (option.toLowerCase()) {
                    case "add admins":
                        //Calls addAdmin method
                        adminLogic.addAdmins();
                        break;
                    case "delete admins":
                        //Calls deleteAdmin method
                        adminLogic.deleteAdmins();
                        break;
                    case "edit admins":
                        //Calls editAdmin method
                        editAdmins();
                        break;
                    case "logout":
                        //Logs out the user
                        System.out.println("Logging out...");
                        //Sets user role to none
                        Session.setRole(LoginEnums.NONE);
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
                        if (projectUtils.getValidString("Confirm? Y/N").equalsIgnoreCase("Y")) return ControlFlow.QUIT;
                        break;
                    case "edit owner account":
                        //Calls editOwner method
                        editOwner();
                        break;
                    case "manage logs":
                        //Calls manageLogs method
                        generalMenus.manageLogs();
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
}
