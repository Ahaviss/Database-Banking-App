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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

//Log class
public class Log {
    @JsonIgnore
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LocalDateTime timeStamp;
    private final Action action;
    private final User user;
    private final String source;
    private String destination;
    private final String before;
    private final String after;
    public LocalDateTime getTimeStamp() {return timeStamp;}
    public Action getAction() {return action;}
    public User getUser() {return user;}
    public String getSource() {return source;}
    public String getDestination() {return destination;}
    public String getBefore() {return before;}
    public String getAfter() {return after;}
    public Log (Action action, User user, String source, String before, String after) {
        timeStamp = LocalDateTime.now();
        this.action = action;
        this.user = user;
        this.source = source;
        this.before = before;
        this.after = after;
    }
    public Log (Action action, User user, String source, String destination, String before, String after) {
        timeStamp = LocalDateTime.now();
        this.action = action;
        this.user = user;
        this.source = source;
        this.destination = destination;
        this.before = before;
        this.after = after;
    }
    @JsonCreator
    Log (@JsonProperty("timeStamp") LocalDateTime timeStamp, @JsonProperty("action") Action action, @JsonProperty("user") User user, @JsonProperty("source") String source, @JsonProperty("destination") String destination, @JsonProperty("before") String before, @JsonProperty("after") String after) {
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
        if (destination == null) {
            return String.format ("[AUDIT %s]: %s action | (%s) | %s | %s -> %s", timeStamp.format(formatter), user.getValue(), source, action.getAction(), before, after);
        } else {
            return String.format ("[AUDIT %s]: %s action | (Admin) %s edited (User) %s | %s | %s -> %s", timeStamp.format(formatter), user.getValue(), source, destination, action.getAction(), before, after);
        }
    }
}
