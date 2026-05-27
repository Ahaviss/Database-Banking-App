/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.session;

import com.ahaviss.database.Account;
import com.ahaviss.database.Admin;
import com.ahaviss.database.Owner;
import com.ahaviss.enums.LoginEnums;

import java.util.HashMap;
import java.util.Map;

public class Session {
    //Password
    private static String masterPassword;
    //Owner's credentials'
    private static Owner owner = new Owner();
    //Current account index
    private static Account currentAccount;
    private static Admin currentAdmin;
    //Current role
    private static LoginEnums role = LoginEnums.NONE;
    //Account and admin lists
    private static Map<Integer, Account> accounts = new HashMap<>();
    private static Map<Integer, Admin> admins = new HashMap<>();
    //Killswitch boolean
    private static boolean killswitch = false;
    public static String getMasterPassword () {return masterPassword;}
    public static void setMasterPassword (String masterPassword) {Session.masterPassword = masterPassword;}
    public static Owner getOwner () {return owner;}
    public static void setOwner (Owner owner) {Session.owner = owner;}
    public static Account getCurrentAccount () {return currentAccount;}
    public static void setCurrentAccount (Account account) {currentAccount = account;}
    public static Admin getCurrentAdmin () {return currentAdmin;}
    public static void setCurrentAdmin (Admin admin) {currentAdmin = admin;}
    public static LoginEnums getRole () {return role;}
    public static void setRole (LoginEnums role) {Session.role = role;}
    public static Map<Integer, Account> getAccounts () {return accounts;}
    public static void setAccounts (Map<Integer, Account> accounts) {Session.accounts = accounts;}
    public static Map<Integer, Admin> getAdmins () {return admins;}
    public static void setAdmins (Map<Integer, Admin> admins) {Session.admins = admins;}
    public static boolean getKillswitch () {return killswitch;}
    public static void setKillswitch (boolean killswitch) {Session.killswitch = killswitch;}
}
