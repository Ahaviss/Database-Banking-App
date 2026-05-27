/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.database;
import com.ahaviss.session.Session;
import com.ahaviss.utilities.SecurityUtils;

public class Owner {
    //Private fields
    private String username;
    private String password;
    public Owner () {
        username = "tempUsername@123";
        password = SecurityUtils.hashPassword("tempPassword@123");
        Session.setMasterPassword("tempPassword@123");
    }
    public void setPassword (String password) {this.password = password;}
    public void setPasswordFromUser (String password) {Session.setMasterPassword(password); this.password = SecurityUtils.hashPassword(password);}
    public String getPassword () {return password;}
    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}
}
