/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.session;

import com.ahaviss.enums.LoginEnums;


public class Session {
    //Current account index
    private static int currentAccount = -1;
    private static int currentAdmin = -1;
    //Current role
    private static LoginEnums role = LoginEnums.NONE;
    //Getters and setters
    public static int getCurrentAccount () {return currentAccount;}
    public static void setCurrentAccount (int account) {currentAccount = account;}
    public static int getCurrentAdmin () {return currentAdmin;}
    public static void setCurrentAdmin (int admin) {currentAdmin = admin;}
    public static LoginEnums getRole () {return role;}
    public static void setRole (LoginEnums role) {Session.role = role;}
}
