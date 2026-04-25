package com.ahaviss.exceptions;
//Local imports
import com.ahaviss.enums.LoginEnums;
//To handle not found admins or accounts.
public class UserNotFoundException extends Exception {
    public UserNotFoundException(LoginEnums role, String cause) {
        super(String.format("%s not found.%nCause: %s", role == LoginEnums.ADMIN ? "Admin" : "Account", cause));
    }
}
