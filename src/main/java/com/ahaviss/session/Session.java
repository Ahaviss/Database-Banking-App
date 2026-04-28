package com.ahaviss.session;

import com.ahaviss.database.Account;
import com.ahaviss.database.Admin;
import com.ahaviss.database.Owner;
import com.ahaviss.enums.LoginEnums;

import java.util.ArrayList;

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
    private static ArrayList<Account> accounts = new ArrayList<>();
    private static ArrayList<Admin> admins = new ArrayList<>();
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
    public static ArrayList<Account> getAccounts () {return accounts;}
    public static void setAccounts (ArrayList<Account> accounts) {Session.accounts = accounts;}
    public static ArrayList<Admin> getAdmins () {return admins;}
    public static void setAdmins (ArrayList<Admin> admins) {Session.admins = admins;}
    public static boolean getKillswitch () {return killswitch;}
    public static void setKillswitch (boolean killswitch) {Session.killswitch = killswitch;}
}
