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
import com.ahaviss.logic.AccountLogic;
import com.ahaviss.logic.AdminLogic;
import com.ahaviss.save.SaveData;
import com.ahaviss.session.Session;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.utilities.SecurityUtils;

import java.util.Map;

public class OwnerMenus {
    private final ProjectUtils projectUtils;
    private final AdminLogic adminLogic;
    public OwnerMenus(ProjectUtils projectUtils, AdminLogic adminLogic) {this.projectUtils = projectUtils; this.adminLogic = adminLogic;}
    public void editOwner () {
        //Asks for and validates the current password
        boolean validated = false;
        for (int i = 0; i < 3; i++) {
            String currentPassword = projectUtils.getValidString("Please enter current owner password.");
            if (SecurityUtils.verifyPassword(currentPassword, Session.getOwner().getPassword())) {
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
                        Session.getOwner().setUsername(projectUtils.getValidString("Enter new username:"));
                        break;
                    case "edit password":
                        //Sets password
                        Session.getOwner().setPasswordFromUser(projectUtils.getValidPassword("Enter new password:"));
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
    public ControlFlow ownerPanel () {
        //Owner panel options
        while (true) {
            try {
                String option = projectUtils.getValidString("Add Admins, Delete Admins, Edit Admins, Logout, Quit Owner Panel, Killswitch\nEdit Owner Account, Manage Logs, Quit Program");
                switch (option.toLowerCase()) {
                    case "add admins":
                        //Calls addAdmin method
                        adminLogic.addAdmins(Session.getAdmins());
                        break;
                    case "delete admins":
                        //Calls deleteAdmin method
                        Map<Integer, Admin> tempAdmins = adminLogic.deleteAdmins(Session.getAdmins());
                        if (tempAdmins != null) {
                            //Edits the admin list only if tempAdmins is not null
                            Session.setAdmins(tempAdmins);
                        }
                        break;
                    case "edit admins":
                        //Calls editAdmin method
                        new AdminMenus(adminLogic, new AccountLogic(projectUtils), projectUtils).editAdmin();
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
                        //Assigns killswitch to true
                        if (new SaveData(projectUtils).killswitch()) {
                            Session.setKillswitch(true);
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
                        new GeneralMenus(projectUtils).manageLogs();
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
