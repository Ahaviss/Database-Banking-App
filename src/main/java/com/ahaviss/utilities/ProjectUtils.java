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

package com.ahaviss.utilities;
//Java imports
import net.sf.jsqlparser.JSQLParserException;

import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public class ProjectUtils {
    private final BufferedReader br;
    private final SQLExecutor executor;
    public ProjectUtils (BufferedReader br, SQLExecutor executor) {
        this.br = br;
        this.executor = executor;
    }
    public int getValidInt (String prompt) {
        while (true) {
            try {
                //Prints the given prompt
                System.out.println(prompt);
                String tempInput = br.readLine().trim();
                if (tempInput.isBlank()) {
                    System.out.println("Invalid input. Please enter a non-empty number.");
                    continue;
                }
                int input = Integer.parseInt(tempInput);
                //Checks if the input is positive
                if (input < 0) {
                    System.out.println("Invalid input. Please enter a positive integer.");
                    continue;
                }
                //Returns the input
                return input;
            //Catch invalid input
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            } catch (IOException e) {
                System.out.println("Invalid input: " + e.getMessage());
            } catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public boolean idExists (int id, Class<?> clazz) throws SQLException, JSQLParserException {
        return switch (clazz.getSimpleName()) {
            case "Account" -> !executor.executeSQL("SELECT account_id FROM accounts WHERE account_id = ?", List.of(List.of(id))).getFirst().isEmpty();
            case "Admin" -> !executor.executeSQL("SELECT admin_id FROM admins WHERE admin_id = ?", List.of(List.of(id))).getFirst().isEmpty();
            default -> false;
        };
    }
    public int sizeOfTable (Class<?> clazz) throws Exception {
        return switch (clazz.getSimpleName()) {
            case "Account" -> verifyInstanceOf(executor.executeSQL("SELECT COUNT(*) AS number_of_accounts FROM accounts", null).getFirst().getFirst().get("number_of_accounts"), Long.class, () -> new SQLException("Incorrect return type given from database.")).intValue();
            case "Admin" -> verifyInstanceOf(executor.executeSQL("SELECT COUNT(*) AS number_of_admins FROM admins", null).getFirst().getFirst().get("number_of_admins"), Long.class, () -> new SQLException("Incorrect return type given from database")).intValue();
            default -> -1;
        };
    }
    public boolean tableHasContents (Class<?> clazz) throws SQLException, JSQLParserException {
        return switch (clazz.getSimpleName()) {
            case "Account" -> !executor.executeSQL("SELECT account_id FROM accounts LIMIT 1", null).getFirst().isEmpty();
            case "Admin" -> !executor.executeSQL("SELECT admin_id FROM admins LIMIT 1", null).getFirst().isEmpty();
            case "Log" -> !executor.executeSQL("SELECT action FROM audit_logs LIMIT 1", null).getFirst().isEmpty();
            default -> false;
        };
    }
    public <T> T verifyInstanceOf (Object obj, Class<T> returnType, ExceptionSupplier<? extends Exception> exception) throws Exception{
        if (returnType != null && returnType.isInstance(obj)) {
            return returnType.cast(obj);
        }
        else {throw exception.getException();}
    }
    public long getValidLong (String prompt) {
        while (true) {
            try {
                //Prints the given prompt
                System.out.println(prompt);
                String tempInput = br.readLine().trim();
                if (tempInput.isBlank()) {
                    System.out.println("Invalid input. Please enter a non-empty number.");
                    continue;
                }
                long input = Long.parseLong(tempInput);
                //Checks if the input is positive
                if (input < 0) {
                    System.out.println("Invalid input. Please enter a positive integer.");
                    continue;
                }
                //Returns the input
                return input;
                //Catch invalid input
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            } catch (IOException e) {
                System.out.println("Invalid input: " + e.getMessage());
            } catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public boolean askToContinue () {
        while (true) {
            String answer = getValidString("Would you like to continue? (Y/N)");
            if (answer.equalsIgnoreCase("Y")) {
                return true;
            } else if (answer.equalsIgnoreCase("N")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter Y or N.");
            }
        }
    }
    public String getValidString (String prompt) {
        while (true) {
            try {
                //Prints the given prompt
                System.out.println(prompt);
                String input = br.readLine().trim();
                //Checks if the input is empty
                if (input.isBlank()) {
                    System.out.println("Invalid input. Please enter a non-empty sentence/word.");
                    continue;
                }
                //Returns the input
                return input;
            //Catch invalid input
            } catch (IOException e) {
                System.out.println("Invalid input: " + e.getMessage());
            } catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public String getValidUsername (String prompt, int cap) {
        while (true) {
            try {
                //Prints the given prompt
                System.out.println(prompt);
                System.out.println("Max of " + cap + " characters.");
                String input = br.readLine().trim();
                //Checks if the input is empty
                if (input.isBlank()) {
                    System.out.println("Invalid input. Please enter a non-empty sentence/word.");
                    continue;
                }
                if (input.length() > cap) {
                    System.out.println("Number of characters exceeds maximum. Please try again.");
                    continue;
                }
                //Returns the input
                return input;
                //Catch invalid input
            } catch (IOException e) {
                System.out.println("Invalid input: " + e.getMessage());
            } catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public double getValidDouble (String prompt) {
        while (true) {
            try {
                //Prints the given prompt
                System.out.println(prompt);
                String tempInput = br.readLine().trim();
                if (tempInput.isBlank()) {
                    System.out.println("Invalid input. Please enter a non-empty number.");
                    continue;
                }
                double input = Double.parseDouble(tempInput);
                //Checks if the input is positive and hasn't overflowed
                if (input < 0 || Double.isNaN(input) || !Double.isFinite(input)) {
                    System.out.println("Invalid input. Please enter a positive number.");
                    continue;
                }
                //Returns the input
                return input;
            }
            //Catch invalid input
            catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            } catch (IOException e) {
                System.out.println("Invalid input: " + e.getMessage());
            } catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public String getValidPassword (String prompt) {
        while (true) {
            try {
                String password = getValidString(prompt + "\nPassword must be 8 characters long and less than 255 characters and contain at least one uppercase letter,\none lowercase letter and one number");
                //Checks the password length
                if (password.length() < 8) {
                    System.out.println("Invalid password. Password must be 8 characters long.");
                    continue;
                }
                //Converts the password to a character array
                char[] passwordChar = password.toCharArray();
                //Password requirements as booleans
                boolean hasUppercase = false;
                boolean hasLowercase = false;
                boolean hasNumber = false;
                for (char c : passwordChar) {
                    //Checks if the current character is: uppercase, lowercase or number
                    if (Character.isUpperCase(c)) {
                        hasUppercase = true;
                    }
                    if (Character.isLowerCase(c)) {
                        hasLowercase = true;
                    }
                    if (Character.isDigit(c)) {
                        hasNumber = true;
                    }
                }
                //If all requirements have been met
                if (hasUppercase && hasLowercase && hasNumber) {
                    System.out.println("Password accepted.");
                    return password;
                //Otherwise
                } else {
                    System.out.println("Invalid password. Password must contain at least one uppercase letter,\none lowercase letter and one number.");
                }
            }
            //Catch invalid input
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    //To close BufferedReader to save resources
    public void closeReader () {
        try {
            br.close();
        }
        catch (IOException e) {
            System.out.println("Error closing reader: " + e.getMessage());
        }
    }
}
