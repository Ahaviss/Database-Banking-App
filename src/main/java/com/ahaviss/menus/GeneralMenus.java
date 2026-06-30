/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.menus;

import com.ahaviss.logs.manager.LogManager;
import com.ahaviss.utilities.ProjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class GeneralMenus {
    private final ProjectUtils projectUtils;
    private final LogManager logManager;
    public GeneralMenus (ProjectUtils projectUtils, LogManager logManager) {
        this.projectUtils = projectUtils;
        this.logManager = logManager;
    }
    public void manageLogs () {
        while (true) {
            try {
                String option = projectUtils.getValidString("Print all logs, Print recent logs, Print logs within timeframe, Clear all logs, Quit managing.");
                if (option.equalsIgnoreCase("print all logs")) {
                    //Prints logs
                    logManager.printAllLogs();
                } else if (option.equalsIgnoreCase("print recent logs")) {
                    logManager.printRecentLogs(projectUtils.getValidInt("Limit:"));
                } else if (option.equalsIgnoreCase("clear all logs")) {
                    //Clears from database
                    logManager.clearLogs();
                } else if (option.equalsIgnoreCase("print logs within timeframe")) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime start = LocalDateTime.parse(projectUtils.getValidString("Start (24H): (yyyy-MM-dd HH:mm:ss) e.g. 2026-06-29 09:30:00"), formatter);
                        LocalDateTime end = LocalDateTime.parse(projectUtils.getValidString("End (24H): (yyyy-MM-dd HH:mm:ss) e.g. 2026-06-29 14:00:00"), formatter);
                        logManager.printAllLogsWithinTimeFrame(start, end);
                    }
                    catch (DateTimeParseException e) {
                        System.out.println("Invalid time format: " + e.getMessage());
                    }
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
