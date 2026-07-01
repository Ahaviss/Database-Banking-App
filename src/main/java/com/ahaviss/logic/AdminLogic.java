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

package com.ahaviss.logic;
//Local imports
import com.ahaviss.database.Admin;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.utilities.SQLExecutor;
import com.ahaviss.utilities.SecurityUtils;
import net.sf.jsqlparser.JSQLParserException;
//Java imports
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
public class AdminLogic {
    private final ProjectUtils projectUtils;
    private final SQLExecutor executor;
    public AdminLogic (ProjectUtils projectUtils, SQLExecutor executor) {
        this.projectUtils = projectUtils;
        this.executor = executor;
    }
    public void deleteAdmins () throws SQLException, JSQLParserException {
        //Checks if the admins list is empty
        if (!projectUtils.tableHasContents(Admin.class)) {
            System.out.println("No admins available. Please create an admin.");
            return;
        }
        while (true) {
            try {
                int numberOfAdmins = projectUtils.sizeOfTable(Admin.class);
                //Asks the number of admins to delete
                int amountOfPeople = projectUtils.getValidInt(String.format("Enter the amount of admins you want to delete (%d total admins): ", numberOfAdmins));
                //Validates input
                if (amountOfPeople > numberOfAdmins) {
                    System.out.println("Invalid amount of admins.");
                    continue;
                } else if (amountOfPeople == 0) {
                    System.out.println("No admins deleted.");
                    return;
                }
                for (int i = 0; i < amountOfPeople; i++) {
                    while (true) {
                        //Asks for the ID of the admin to delete
                        int adminId = projectUtils.getValidInt("Enter the ID of the admin you want to delete: ");
                        //Checks if the admin is found
                        if (!projectUtils.idExists(adminId, Admin.class)) {
                            System.out.printf("Admin ID %d not found.%n", adminId);
                            continue;
                        }
                        //Removes the admin
                        executor.executeSQL("DELETE FROM admins WHERE admin_id = ?", List.of(List.of(adminId)));
                        System.out.println("Admin deleted successfully!");
                        break;
                    }
                }
                return;
            }
            //Catch invalid input
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public void editPassword (int adminId) throws SQLException, JSQLParserException{
        //Gets a valid password, sets it to the admin and returns it
        String tempNewPassword = projectUtils.getValidPassword("Enter the new password: ");
        String newPassword = SecurityUtils.hashPassword(tempNewPassword);
        executor.executeSQL("UPDATE admins SET admin_password = ? WHERE admin_id = ?", List.of(List.of(newPassword, adminId)));
    }
    public void editAdminName (int adminId) throws SQLException, JSQLParserException {
        //Gets a valid name, sets it to the admin and returns it
        String newName = projectUtils.getValidUsername("Enter the new admin name: ", 100);
        executor.executeSQL("UPDATE admins SET admin_name = ? WHERE admin_id = ?", List.of(List.of(newName, adminId)));
    }
    //RNG for admin ID
    private final Random random = new Random();
    public void addAdmins () throws SQLException, JSQLParserException {
        //Asks the number of admins to add
        int amountOfAdmins = projectUtils.getValidInt("Enter the amount of admins you want to add: ");
        //Validates input
        if (amountOfAdmins == 0) {
            System.out.println("No admins added.");
            return;
        }
        for (int i = 0; i < amountOfAdmins; i++) {
            //Gets the admin's name and password
            String adminName = projectUtils.getValidUsername("Enter admin name:", 100);
            String tempAdminPassword = projectUtils.getValidPassword("Enter admin password:");
            String adminPassword = SecurityUtils.hashPassword(tempAdminPassword);
            //Generates a random admin ID
            int adminId = random.nextInt(9999999 - 1000000 + 1) + 1000000;
            //Checks if the ID is already taken
            while (projectUtils.idExists(adminId, Admin.class)) {
                //Increments the ID and repeats the check
                adminId++;
                if (adminId > 9999999) {
                    adminId = 1000000;
                }
            }
            //Prints the admin ID
            System.out.println("Admin ID: " + adminId);
            //Adds the admin to the admins list
            executor.executeSQL("INSERT INTO admins (admin_id, admin_name, admin_password) VALUES (?, ?, ?)", List.of(List.of(adminId, adminName, adminPassword)));
        }
    }
}
