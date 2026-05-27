/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.utilities;
//Java imports
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class ProjectUtils {
    private final BufferedReader br;
    public ProjectUtils (BufferedReader br) {this.br = br;}
    public int getValidInt (String prompt) {
        while (true) {
            try {
                //Prints the given prompt
                System.out.println(prompt);
                String tempInput = br.readLine().trim();
                if (tempInput == null || tempInput.isBlank()) {
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
                if (input == null || input.isBlank()) {
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
    public double getValidDouble (String prompt) {
        while (true) {
            try {
                //Prints the given prompt
                System.out.println(prompt);
                String tempInput = br.readLine().trim();
                if (tempInput == null || tempInput.isBlank()) {
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
                //Prints the given prompt
                System.out.println(prompt);
                //Tells the user the password requirements
                String password = getValidString("Password must be 8 characters long and contain at least one uppercase letter,\none lowercase letter and one number");
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
    //Generic method to check if an arraylist is empty
    public static <T> boolean checkArrayList (ArrayList<T> arrayList) {return !arrayList.isEmpty();}
    public static <T> boolean checkMap (Map<Integer, T> map) {return !map.isEmpty();}
}
