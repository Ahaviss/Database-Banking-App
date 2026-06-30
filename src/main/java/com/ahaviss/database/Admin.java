/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.database;

public class Admin implements Printable{
    //Private fields
    private final int adminId;
    private String adminName;
    private String adminPassword;
    //Constructor for object creation
    public Admin(int adminId, String adminName, String adminPassword) {
        this.adminId = adminId;
        this.adminName = adminName;
        this.adminPassword = adminPassword;
    }
    //Getters and setters
    public int getAdminId() {return adminId;}
    public String getAdminName() {return adminName;}
    public String getAdminPassword() {return adminPassword;}
    public void setAdminPassword(String adminPassword) {this.adminPassword = adminPassword;}
    public void setAdminName(String adminName) {this.adminName = adminName;}
    //Prints the admin information
    @Override
    public void printInfo () {
        System.out.println("Admin ID: " + adminId);
        System.out.println("Admin Name: " + adminName);
    }
}
