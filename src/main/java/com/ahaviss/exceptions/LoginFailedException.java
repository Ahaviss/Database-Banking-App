package com.ahaviss.exceptions;
//To handle failed logins
public class LoginFailedException extends Exception {
    public LoginFailedException() {
        super("Login failed. Attempts exceeded limit.");
    }
}
