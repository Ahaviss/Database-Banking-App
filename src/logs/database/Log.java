package logs.database;
//Local imports
import logs.enums.*;
//Java imports
import java.io.Serial;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.Serializable;
//Log class
public class Log implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LocalDateTime timeStamp;
    private final Action action;
    private final User user;
    private final String source;
    private String destination;
    private final String before;
    private final String after;
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
    @Override
    public String toString () {
        if (destination == null) {
            return String.format ("[AUDIT %s]: %s action | (%s) | %s | %s -> %s", timeStamp.format(formatter), user.getValue(), source, action.getAction(), before, after);
        } else {
            return String.format ("[AUDIT %s]: %s action | (Admin) %s edited (User) %s | %s | %s -> %s", timeStamp.format(formatter), user.getValue(), source, destination, action.getAction(), before, after);
        }
    }
}
