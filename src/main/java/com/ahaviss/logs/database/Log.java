/*
 * Copyright [2026] [Ahaviss]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
