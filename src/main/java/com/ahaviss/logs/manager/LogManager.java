/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.logs.manager;
import com.ahaviss.logs.enums.*;
import com.ahaviss.logs.database.Log;
import com.ahaviss.save.SaveData;
import com.ahaviss.utilities.ProjectUtils;

import java.util.ArrayList;
public class LogManager {
    //ArrayList to store logs
    private static volatile ArrayList<Log> logs = new ArrayList<>();
    //To add a log
    public static void addLog (Action action, User user, String source, String destination, String before, String after) {
        SaveData.locks.writeLock().lock();
        try {
            if (user == User.USER) {
                logs.add(new Log(action, user, source, before, after));
            } else {
                logs.add(new Log(action, user, source, destination, before, after));
            }
        }
        finally {SaveData.locks.writeLock().unlock();}
    }
    //To pull all logs
    public static ArrayList<Log> getLogs() {
        SaveData.locks.readLock().lock();
        try {
            return logs;
        }
        finally {SaveData.locks.readLock().unlock();}
    }
    //To pull all logs from file and load into JVM
    public static void loadLogs (ArrayList<Log> logs) {
        SaveData.locks.writeLock().lock();
        try {
            LogManager.logs = logs;
        }
        finally {SaveData.locks.writeLock().unlock();}
    }
    //To print all logs
    public static void printLogs () {
        SaveData.locks.readLock().lock();
        try {
            if (!ProjectUtils.checkArrayList(logs)) {
                System.out.println("Logs are empty.");
                return;
            }
            System.out.println("---[AUDIT LOGS START]---");
            for (Log log : logs) {
                System.out.println(log);
            }
            System.out.println("---[AUDIT LOGS ENDS]---");
        }
        finally {SaveData.locks.readLock().unlock();}
    }
    //To clear all logs
    public static void clearLogs() {
        SaveData.locks.writeLock().lock();
        try {
            logs.clear();
        }
        finally {SaveData.locks.writeLock().unlock();}
    }
}
