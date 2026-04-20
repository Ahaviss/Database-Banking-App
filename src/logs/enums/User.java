package logs.enums;

public enum User {
    //User
    ADMIN("Admin"),
    USER("User");
    private final String value;
    User (String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
