package com.ahaviss.save;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ahaviss.database.Account;
import com.ahaviss.database.Admin;
import com.ahaviss.database.Owner;
import com.ahaviss.logs.database.Log;
import com.ahaviss.session.Session;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.utilities.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class SaveData {
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    private static final String fileSeparator = java.io.File.separator;
    private static final String ACCOUNTS_FILE = "src" + fileSeparator + "main" + fileSeparator + "resources" + fileSeparator + "accounts.enc";
    private static final String ADMINS_FILE   = "src" + fileSeparator + "main" + fileSeparator + "resources" + fileSeparator + "admins.enc";
    private static final String OWNER_FILE    = "src" + fileSeparator + "main" + fileSeparator + "resources" + fileSeparator + "owner.enc";
    private static final String AUDIT_FILE    = "src" + fileSeparator + "main" + fileSeparator + "resources" + fileSeparator + "auditLogs.enc";
    private static final String BACKUP_ACCOUNTS_FILE = "src" + fileSeparator + "main" + fileSeparator + "backup-resources" + fileSeparator + "accounts.enc";
    private static final String BACKUP_ADMINS_FILE   = "src" + fileSeparator + "main" + fileSeparator + "backup-resources" + fileSeparator + "admins.enc";
    private static final String BACKUP_OWNER_FILE    = "src" + fileSeparator + "main" + fileSeparator + "backup-resources" + fileSeparator + "owner.enc";
    private static final String BACKUP_AUDIT_FILE    = "src" + fileSeparator + "main" + fileSeparator + "backup-resources" + fileSeparator + "auditLogs.enc";
    //Loads account data
    public static Map<Integer, Account> loadAccountData (String password) throws Exception {
        if (!new File(ACCOUNTS_FILE).exists() && new File(BACKUP_ACCOUNTS_FILE).exists()) {
            System.out.println("Account file removed after save or doesn't exist\nLoading backup...");
            return loadMap(BACKUP_ACCOUNTS_FILE, password, Account.class);
        }
        return loadMap(ACCOUNTS_FILE, password, Account.class);
    }
    //Loads admin data
    public static Map<Integer, Admin> loadAdminData (String password) throws Exception {
        if (!new File(ADMINS_FILE).exists() && new File(BACKUP_ADMINS_FILE).exists()) {
            System.out.println("Admin file removed after save or doesn't exist\nLoading backup...");
            return loadMap(BACKUP_ADMINS_FILE, password, Admin.class);
        }
        return loadMap(ADMINS_FILE, password, Admin.class);
    }
    //Loads owner data
    public static Owner loadOwnerData (String password) throws Exception {
        File file = new File(OWNER_FILE);
        if (!file.exists() && new File(BACKUP_OWNER_FILE).exists()) {
            System.out.println("Owner file removed after save or doesn't exist\nLoading backup...");
            file = new File(BACKUP_OWNER_FILE);
        }
        else if (!file.exists()) return new Owner();
        String encrypted = Files.readString(file.toPath());
        String jsonString = SecurityUtils.decrypt(encrypted, password);
        Owner owner = mapper.readValue(jsonString, Owner.class);
        if (owner == null)  return new Owner();
        return owner;
    }
    //Loads audit log data
    public static ArrayList<Log> loadAuditData (String password) throws Exception {
        File file = new File(AUDIT_FILE);
        if (!file.exists() && new File(BACKUP_AUDIT_FILE).exists()) {
            System.out.println("Audit file removed after save or doesn't exist\nLoading backup...");
            file = new File(BACKUP_AUDIT_FILE);
        }
        else if (!file.exists()) return new ArrayList<>();
        String encrypted = Files.readString(file.toPath());
        String jsonString = SecurityUtils.decrypt(encrypted, password);
        ArrayList<Log> list = mapper.readValue(jsonString, mapper.getTypeFactory().constructCollectionType(ArrayList.class, Log.class));
        if (list == null) return new ArrayList<>();
        return list;
    }
    private static <T> Map<Integer, T> loadMap(String filename, String password, Class <T> type) throws Exception {
        File file = new File(filename);
        if (!file.exists()) return new HashMap<>();
        String encrypted = Files.readString(file.toPath());
        String jsonString = SecurityUtils.decrypt(encrypted, password);
        Map<Integer, T> list = mapper.readValue(jsonString, mapper.getTypeFactory().constructMapType(HashMap.class, Integer.class, type));
        if (list == null) return new HashMap<>();
        return list;
    }
    //Saves all data
    public static void saveData(Map<Integer, Admin> admins, Map<Integer, Account> accounts, Owner owner, ArrayList<Log> logs) {
        File file1 = new File(ADMINS_FILE);
        File file2 = new File(OWNER_FILE);
        File file3 = new File(ACCOUNTS_FILE);
        File file4 = new File(AUDIT_FILE);
        File backupFile1 = new File(BACKUP_ADMINS_FILE);
        File backupFile2 = new File(BACKUP_OWNER_FILE);
        File backupFile3 = new File(BACKUP_ACCOUNTS_FILE);
        File backupFile4 = new File(BACKUP_AUDIT_FILE);
        String password = Session.getMasterPassword();
        try {
            // Step 1: Convert list to JSON string (same as before, just not to file yet)
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(admins);
            // Step 2: Encrypt that string
            String encrypted = SecurityUtils.encrypt(jsonString, password);
            // Step 3: Write the encrypted string to the file
            Files.writeString(file1.toPath(), encrypted);
            Files.writeString(backupFile1.toPath(), encrypted);
        }
        catch (Exception e) {
            System.out.println("Error saving admin data: " +  e.getMessage());
        }
        try {
            // Step 1: Convert list to JSON string (same as before, just not to file yet)
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(owner);
            // Step 2: Encrypt that string
            String encrypted = SecurityUtils.encrypt(jsonString, password);
            // Step 3: Write the encrypted string to the file
            Files.writeString(file2.toPath(), encrypted);
            Files.writeString(backupFile2.toPath(), encrypted);
        }
        catch (Exception e) {
            System.out.println("Error saving owner data: " + e.getMessage());
        }
        try {
            // Step 1: Convert list to JSON string (same as before, just not to file yet)
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(accounts);
            // Step 2: Encrypt that string
            String encrypted = SecurityUtils.encrypt(jsonString, password);
            // Step 3: Write the encrypted string to the file
            Files.writeString(file3.toPath(), encrypted);
            Files.writeString(backupFile3.toPath(), encrypted);
        }
        catch (Exception e) {
            System.out.println("Error saving accounts data: " + e.getMessage());
        }
        try {
            // Step 1: Convert list to JSON string (same as before, just not to file yet)
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(logs);
            // Step 2: Encrypt that string
            String encrypted = SecurityUtils.encrypt(jsonString, password);
            // Step 3: Write the encrypted string to the file
            Files.writeString(file4.toPath(), encrypted);
            Files.writeString(backupFile4.toPath(), encrypted);
        }
        catch (Exception e) {
            System.out.println("Error saving audit data: " + e.getMessage());
        }
    }
    //To delete logs file
    public static void clearLogs () {
        try {
            File file = new File (AUDIT_FILE);
            File file2 = new File (BACKUP_AUDIT_FILE);
            if (file.exists()) {
                boolean del = file.delete();
                boolean del2 = file2.delete();
                if (!del || !del2) {System.out.println("Error deleting logs.");}
            }
        }
        catch (Exception e) {
            System.err.println("Error clearing logs: " + e.getMessage());
        }
    }

    //Killswitch for owner
    public static boolean killswitch () {
        while (true) {
            try {
                //Confirms with user
                System.out.println("Are you sure you want to turn on the killswitch?");
                System.out.println("This will delete all data and terminate the program.");
                String option = ProjectUtils.getValidString("Y/N");
                if (option.equalsIgnoreCase("y")) {
                    File delete1 = new File(ACCOUNTS_FILE);
                    File delete2 = new File(ADMINS_FILE);
                    File delete3 = new File(OWNER_FILE);
                    File delete4 = new File(AUDIT_FILE);
                    File backupDelete1 = new File(BACKUP_ACCOUNTS_FILE);
                    File backupDelete2 = new File(BACKUP_ADMINS_FILE);
                    File backupDelete3 = new File(BACKUP_OWNER_FILE);
                    File backupDelete4 = new File(BACKUP_AUDIT_FILE);
                    //Deletes all files
                    if (delete1.exists()) {
                        boolean del = delete1.delete();
                        if (!del) System.out.println("Delete failed for account data.");
                    }
                    if (delete2.exists()) {
                        boolean del = delete2.delete();
                        if (!del) System.out.println("Delete failed for admin data.");
                    }
                    if (delete3.exists()) {
                        boolean del = delete3.delete();
                        if (!del) System.out.println("Delete failed for owner data.");
                    }
                    if (delete4.exists()) {
                        boolean del = delete4.delete();
                        if (!del) System.out.println("Delete failed for audit logs data.");
                    }
                    if (backupDelete1.exists()) {
                        boolean del = backupDelete1.delete();
                        if (!del) System.out.println("Delete failed for backup accounts data.");
                    }
                    if (backupDelete2.exists()) {
                        boolean del = backupDelete2.delete();
                        if (!del) System.out.println("Delete failed for backup admins data.");
                    }
                    if (backupDelete3.exists()) {
                        boolean del = backupDelete3.delete();
                        if (!del) System.out.println("Delete failed for backup owner data.");
                    }
                    if (backupDelete4.exists()) {
                        boolean del = backupDelete4.delete();
                        if (!del) System.out.println("Delete failed for backup audit logs data.");
                    }
                    return true;
                }
                else if (option.equalsIgnoreCase("n")) return false;
                //Invalid input
                else {
                    System.out.println("Invalid input. Please try again.");
                }
            }
            catch (Exception e) {
                System.out.println("Error implementing killswitch: " + e.getMessage());
            }
        }
    }
}
