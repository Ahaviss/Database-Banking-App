package save;
import java.io.*;
import java.util.ArrayList;
import database.Account;
import database.Admin;
import database.Owner;
import logs.database.Log;
import logs.manager.LogManager;
import utilities.ProjectUtils;

public class SaveData {
    //Loads account data
    @SuppressWarnings("unchecked")
    public static ArrayList<Account> loadAccountData () {
        File file = new File("accountMetadata.ser");
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (ArrayList<Account>) ois.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    //Loads admin data
    @SuppressWarnings("unchecked")
    public static ArrayList<Admin> loadAdminData () {
        File file = new File("adminMetadata.ser");
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (ArrayList<Admin>) ois.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    //Loads owner data
    public static Owner loadOwnerData () {
        File file = new File("ownerMetadata.ser");
        if (!file.exists()) return new Owner();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Owner) ois.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
            return new Owner();
        }
    }
    //Loads audit log data
    @SuppressWarnings("unchecked")
    public static ArrayList<Log> loadAuditData () {
        File file = new File("auditMetadata.ser");
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (ArrayList<Log>) ois.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    //Saves all data
    public static void saveData(ArrayList<Admin> admins, ArrayList<Account> accounts, Owner owner, ArrayList<Log> logs) {
        File file1 = new File("adminMetadata.ser");
        File file2 = new File("ownerMetadata.ser");
        File file3 = new File("accountMetadata.ser");
        File file4 = new File("auditMetadata.ser");
        try (ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(file3))) {
            ous.writeObject(accounts);
        }
        catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
        try (ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(file1))) {
            ous.writeObject(admins);
        }
        catch (IOException e) {
            System.err.println("Error saving admin data: " + e.getMessage());
        }
        try (ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(file2))) {
            ous.writeObject(owner);
        }
        catch (IOException e) {
            System.err.println("Error saving owner data: " + e.getMessage());
        }
        try (ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(file4))) {
            ous.writeObject(logs);
        }
        catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    //To delete logs file
    public static void clearLogs () {
        try {
            File file = new File ("auditMetadata.ser");
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
                    File delete1 = new File("accountMetadata.ser");
                    File delete2 = new File("adminMetadata.ser");
                    File delete3 = new File("ownerMetadata.ser");
                    File delete4 = new File("auditMetadata.ser");
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
