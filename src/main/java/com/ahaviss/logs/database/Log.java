/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.logs.database;
//Local imports
import com.ahaviss.logs.enums.*;
//Java imports
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Log class
public class Log {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LocalDateTime timeStamp;
    private final Action action;
    private final User user;
    private final String source;
    private final String destination;
    private final String before;
    private final String after;
    public Log (LocalDateTime timeStamp, Action action, User user, String source, String destination, String before, String after) {
        this.timeStamp = timeStamp;
        this.action = action;
        this.user = user;
        this.source = source;
        this.destination = destination;
        this.before = before;
        this.after = after;
    }
    @Override
    public String toString () {
        return String.format ("[AUDIT %s]: %s action | (Admin) %s edited (User) %s | %s | %s -> %s", timeStamp.format(formatter), user.getValue(), source, destination, action.getAction(), before, after);
    }
}
