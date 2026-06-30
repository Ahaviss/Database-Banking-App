
package com.ahaviss.logs.enums;

public enum User {
    //User
    ADMIN("ADMIN"),
    USER("USER");
    private final String value;
    User (String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
    public static User fromValue(String value) {
        for (User user : User.values()) {
            if (user.getValue().equalsIgnoreCase(value)) {
                return user;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
