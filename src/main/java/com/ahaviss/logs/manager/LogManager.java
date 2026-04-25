package com.ahaviss.logs.manager;
import com.ahaviss.logs.enums.*;
import com.ahaviss.logs.database.Log;
import com.ahaviss.utilities.ProjectUtils;

import java.util.ArrayList;
public class LogManager {
    //ArrayList to store logs
    private static ArrayList<Log> logs = new ArrayList<>();
    //To add a log
    public static void addLog (Action action, User user, String source, String destination, String before, String after) {
        if (user == User.USER) {
            logs.add(new Log(action, user, source, before, after));
        }
        else {
            logs.add(new Log(action, user, source, destination, before, after));
        }
    }
    //To pull all logs
    public static ArrayList<Log> getLogs() {return logs;}
    //To pull all logs from file and load into JVM
    public static void loadLogs (ArrayList<Log> logs) {LogManager.logs = logs;}
    //To print all logs
    public static void printLogs () {
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
    //To clear all logs
    public static void clearLogs() {logs.clear();}
}
