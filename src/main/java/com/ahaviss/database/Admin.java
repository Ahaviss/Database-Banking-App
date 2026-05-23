package com.ahaviss.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

public class Admin implements Printable{
    //Private fields
    private final int adminId;
    private String adminName;
    private String adminPassword;
    //Constructor for object creation
    @JsonCreator
    public Admin(@JsonProperty("adminId") int adminId, @JsonProperty("adminName") String adminName, @JsonProperty("adminPassword") String adminPassword) {
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
