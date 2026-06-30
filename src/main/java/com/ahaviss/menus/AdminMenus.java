/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.menus;

import com.ahaviss.enums.ControlFlow;
import com.ahaviss.enums.LoginEnums;
import com.ahaviss.logic.AccountLogic;
import com.ahaviss.session.Session;
import com.ahaviss.utilities.ProjectUtils;

public class AdminMenus {
    private final AccountLogic accountLogic;
    private final ProjectUtils projectUtils;
    private final AccountMenus accountMenus;
    public AdminMenus(AccountLogic accountLogic, ProjectUtils projectUtils, AccountMenus accountMenus) {
        this.accountLogic = accountLogic;
        this.projectUtils = projectUtils;
        this.accountMenus = accountMenus;
    }
    public ControlFlow adminPanel () throws Exception {
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
                option = projectUtils.getValidString("Add Accounts, Delete Accounts, Edit accounts, Logout, Owner Panel, Quit program");
            }
            //General admin panel option
            else {
                option = projectUtils.getValidString("Add Accounts, Delete Accounts, Edit accounts, Logout, Quit program");
            }
            switch (option.toLowerCase()) {
                case "add accounts":
                    //Calls addAccount method
                    accountLogic.createAccount(Session.getCurrentAdmin());
                    break;
                case "delete accounts":
                    //Calls deleteAccount method
                    accountLogic.deleteAccounts(Session.getCurrentAdmin());
                    break;
                case "edit accounts":
                    //Calls editAccount method
                    accountMenus.editAccount();
                    break;
                case "logout":
                    //Logs out the user
                    System.out.println("Logging out...");
                    //Sets user role to none
                    Session.setRole(LoginEnums.NONE);
                    Session.setCurrentAdmin(-1);
                    return ControlFlow.MAIN_MENU;
                case "quit program":
                    System.out.println("Terminating program...");
                    //Send a quit message
                    return ControlFlow.QUIT;
                default:
                    //Access the owner panel option only if the role is the owner
                    if (option.equalsIgnoreCase("owner panel") && Session.getRole() == LoginEnums.OWNER) {
                        return ControlFlow.OWNER_PANEL;
                    }
                    //General admin panel error message
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
