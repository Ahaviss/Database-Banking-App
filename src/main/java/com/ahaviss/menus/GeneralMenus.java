/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.menus;

import com.ahaviss.logs.manager.LogManager;
import com.ahaviss.save.SaveData;
import com.ahaviss.utilities.ProjectUtils;

public class GeneralMenus {
    private final ProjectUtils projectUtils;
    public GeneralMenus (ProjectUtils projectUtils) {this.projectUtils = projectUtils;}
    public void manageLogs () {
        while (true) {
            try {
                String option = projectUtils.getValidString("Print logs, Clear all logs, Quit managing.");
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
}
