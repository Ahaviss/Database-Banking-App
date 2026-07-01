/*
 * Copyright [2026] [Ahaviss]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
