/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.exceptions;
//To handle failed logins
public class LoginFailedException extends Exception {
    public LoginFailedException() {
        super("Login failed. Attempts exceeded limit.");
    }
}
