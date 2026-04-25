package com.ahaviss.save;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import com.ahaviss.database.Account;
import com.ahaviss.database.Admin;
import com.ahaviss.database.Owner;
import com.ahaviss.logs.database.Log;
import com.ahaviss.logs.manager.LogManager;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.utilities.SecurityUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class SaveData {
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    private static final String ACCOUNTS_FILE = "accounts.ser";
    private static final String ADMINS_FILE   = "admins.ser";
    private static final String OWNER_FILE    = "owner.ser";
    private static final String AUDIT_FILE    = "auditLogs.ser";
    //Loads account data
    public static ArrayList<Account> loadAccountData (String password) {
        return loadList(ACCOUNTS_FILE, password, Account.class);
    }
    //Loads admin data
    public static ArrayList<Admin> loadAdminData (String password) {
        return loadList(ADMINS_FILE, password, Admin.class);
    }
    //Loads owner data
    public static Owner loadOwnerData (String password) {
        try {
            File file = new File(OWNER_FILE);
            if (!file.exists()) return new Owner();
            String encrypted = Files.readString(file.toPath());
            String jsonString = SecurityUtils.decrypt(encrypted, password);
            Owner owner = mapper.readValue(jsonString, Owner.class);
            if (owner == null)  return new Owner();
            return owner;
        }
        catch (Exception e) {
            System.out.println("Error loading data: " + e.getMessage());
            return new Owner();
        }
    }
    //Loads audit log data
    public static ArrayList<Log> loadAuditData (String password) {
        return loadList(AUDIT_FILE, password, Log.class);
    }
    //Generic method to load data
    private static <T> ArrayList<T> loadList(String filename, String password, Class type) {
        try {
            File file = new File(filename);
            if (!file.exists()) return new ArrayList<>();
            String encrypted = Files.readString(file.toPath());
            String jsonString = SecurityUtils.decrypt(encrypted, password);
            ArrayList<T> list = mapper.readValue(jsonString, mapper.getTypeFactory().constructCollectionType(ArrayList.class, type));
            if (list == null) return new ArrayList<>();
            return list;
        }
        catch (Exception e) {
            System.out.println("Error loading data: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    //Saves all data
    public static void saveData(ArrayList<Admin> admins, ArrayList<Account> accounts, Owner owner, ArrayList<Log> logs, String password) {
        File file1 = new File(ADMINS_FILE);
        File file2 = new File(OWNER_FILE);
        File file3 = new File(ACCOUNTS_FILE);
        File file4 = new File(AUDIT_FILE);
        try {
            // Step 1: Convert list to JSON string (same as before, just not to file yet)
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(admins);
            // Step 2: Encrypt that string
            String encrypted = SecurityUtils.encrypt(jsonString, password);
            // Step 3: Write the encrypted string to the file
            Files.writeString(file1.toPath(), encrypted);
        }
        catch (Exception e) {
            System.out.println("Error saving admin data: " + e.getMessage());
        }
        try {
            // Step 1: Convert list to JSON string (same as before, just not to file yet)
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(owner);
            // Step 2: Encrypt that string
            String encrypted = SecurityUtils.encrypt(jsonString, password);
            // Step 3: Write the encrypted string to the file
            Files.writeString(file2.toPath(), encrypted);
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
        }
        catch (Exception e) {
            System.out.println("Error saving audit data: " + e.getMessage());
        }
    }
    //To delete logs file
    public static void clearLogs () {
        try {
            File file = new File (AUDIT_FILE);
            if (file.exists()) {
                boolean del = file.delete();
                if (!del) {
                    System.out.println("Error deleting logs.");
                }
                LogManager.clearLogs();
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
