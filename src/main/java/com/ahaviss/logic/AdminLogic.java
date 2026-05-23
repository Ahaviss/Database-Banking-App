package com.ahaviss.logic;
//Local imports
import com.ahaviss.database.Admin;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.utilities.SecurityUtils;
//Java imports
import java.util.Map;
import java.util.Random;
public class AdminLogic {
    public static Map<Integer, Admin> deleteAdmins (Map<Integer, Admin> admins) {
        //Checks if the admins list is empty
        if (!ProjectUtils.checkMap(admins)) {
            System.out.println("No admins available. Please create an admin.");
            return null;
        }
        while (true) {
            try {
                //Asks the number of admins to delete
                int amountOfPeople = ProjectUtils.getValidInt(String.format("Enter the amount of admins you want to delete (%d total admins): ", admins.size()));
                //Validates input
                if (amountOfPeople > admins.size()) {
                    System.out.println("Invalid amount of admins.");
                    continue;
                } else if (amountOfPeople == 0) {
                    System.out.println("No admins deleted.");
                    return admins;
                }
                for (int i = 0; i < amountOfPeople; i++) {
                    while (true) {
                        //Asks for the ID of the admin to delete
                        int adminId = ProjectUtils.getValidInt("Enter the ID of the admin you want to delete: ");
                        //Checks if the admin is found
                        if (!admins.containsKey(adminId)) {
                            System.out.printf("Admin ID %d not found.%n", adminId);
                            continue;
                        }
                        //Removes the admin
                        admins.remove(adminId);
                        System.out.println("Admin deleted successfully!");
                        break;
                    }
                }
                //Returns the updated admins list
                return admins;
            }
            //Catch invalid input
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public static void editPassword (Admin admin) {
        //Gets a valid password, sets it to the admin and returns it
        String tempNewPassword = ProjectUtils.getValidPassword("Enter the new password: ");
        String newPassword = SecurityUtils.hashPassword(tempNewPassword);
        admin.setAdminPassword(newPassword);
    }
    public static void editAdminName (Admin admin) {
        //Gets a valid name, sets it to the admin and returns it
        String newName = ProjectUtils.getValidString("Enter the new admin name: ");
        admin.setAdminName(newName);
    }
    //RNG for admin ID
    private static final Random random = new Random();
    public static void addAdmins (Map<Integer, Admin> admins) {
        //Asks the number of admins to add
        int amountOfAdmins = ProjectUtils.getValidInt("Enter the amount of admins you want to add: ");
        //Validates input
        if (amountOfAdmins == 0) {
            System.out.println("No admins added.");
            return;
        }
        for (int i = 0; i < amountOfAdmins; i++) {
            //Gets the admin's name and password
            String adminName = ProjectUtils.getValidString("Enter admin name:");
            String tempAdminPassword = ProjectUtils.getValidPassword("Enter admin password:");
            String adminPassword = SecurityUtils.hashPassword(tempAdminPassword);
            //Generates a random admin ID
            int adminId = random.nextInt(9999999 - 1000000 + 1) + 1000000;
            //Checks if the ID is already taken
            while (admins.containsKey(adminId)) {
                //Increments the ID and repeats the check
                adminId++;
                if (adminId > 9999999) {
                    adminId = 1000000;
                }
            }
            //Prints the admin ID
            System.out.println("Admin ID: " + adminId);
            //Adds the admin to the admins list
            admins.put(adminId, new Admin(adminId, adminName, adminPassword));
        }
    }
}
