/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.logs.manager;
import com.ahaviss.logs.database.Log;
import com.ahaviss.logs.enums.Action;
import com.ahaviss.logs.enums.User;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.utilities.SQLExecutor;
import net.sf.jsqlparser.JSQLParserException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class LogManager {
    //To print all logs
    private final ProjectUtils projectUtils;
    private final SQLExecutor executor;
    public LogManager(ProjectUtils projectUtils, SQLExecutor executor) {
        this.projectUtils = projectUtils;
        this.executor = executor;
    }
    public void printAllLogs() throws Exception {
        if (!projectUtils.tableHasContents(Log.class)) {
            System.out.println("Logs are empty.");
            return;
        }
        printCommonLogs(executor.executeSQL("SELECT * FROM audit_logs", null).getFirst());
    }
    public void printRecentLogs(int limit) throws Exception {
        if (!projectUtils.tableHasContents(Log.class)) {
            System.out.println("Logs are empty.");
            return;
        }
        printCommonLogs(executor.executeSQL("SELECT * FROM audit_logs LIMIT ?", List.of(List.of(limit))).getFirst());
    }
    public void printAllLogsWithinTimeFrame(LocalDateTime start, LocalDateTime end) throws Exception {
        if (!projectUtils.tableHasContents(Log.class)) {
            System.out.println("Logs are empty.");
            return;
        }
        printCommonLogs(executor.executeSQL("SELECT * FROM audit_logs WHERE timestamp BETWEEN ? AND ?", List.of(List.of(start, end))).getFirst());
    }
    private void printCommonLogs (List<Map<String, Object>> logs) throws Exception {
        System.out.println("---[AUDIT LOGS START]---");
        try {
            logs.forEach(map -> {
                LocalDateTime timestamp;
                try {
                    timestamp = projectUtils.verifyInstanceOf(map.get("timestamp"), LocalDateTime.class, () -> new SQLException("Incorrect return type given from database"));
                } catch (Exception e) {throw new RuntimeException(e);}
                Action action = Action.fromValue(map.get("action").toString());
                User user = User.fromValue(map.get("performed_by").toString());
                String source = map.get("source").toString();
                String target = map.get("target").toString();
                String beforeValue = map.get("before_value").toString();
                String afterValue = map.get("after_value").toString();
                Log log = new Log(timestamp, action, user, source, target, beforeValue, afterValue);
                System.out.println(log);
            });
        } catch (RuntimeException e) {throw new Exception(e);}
        System.out.println("---[AUDIT LOGS ENDS]---");
    }
    //To clear all logs
    public void clearLogs() throws SQLException, JSQLParserException {
        executor.executeSQL("TRUNCATE TABLE audit_logs", null);
    }
}
