package com.ahaviss.menus;

import com.ahaviss.logs.manager.LogManager;
import com.ahaviss.save.SaveData;
import com.ahaviss.utilities.ProjectUtils;

public class GeneralMenus {
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
}
