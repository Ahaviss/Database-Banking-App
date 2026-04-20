package utilities;
//Java imports
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.io.BufferedReader;
import java.io.IOException;
public class ProjectUtils {
    private static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    public static int getValidInt (String prompt) {
        while (true) {
            try {
                //Prints the given prompt
                System.out.println(prompt);
                int input = Integer.parseInt(br.readLine());
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
    public static boolean askToContinue () {
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
    public static String getValidString (String prompt) {
        while (true) {
            try {
                //Prints the given prompt
                System.out.println(prompt);
                String input = br.readLine();
                //Checks if the input is empty
                if (input.isEmpty()) {
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
    public static double getValidDouble (String prompt) {
        while (true) {
            try {
                //Prints the given prompt
                System.out.println(prompt);
                double input = Double.parseDouble(br.readLine());
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
    public static String getValidPassword (String prompt) {
        while (true) {
            try {
                //Prints the given prompt
                System.out.println(prompt);
                //Tells the user the password requirements
                System.out.println("Password must be 8 characters long and contain at least one uppercase letter,\none lowercase letter and one number");
                String password = br.readLine();
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
            catch (IOException e) {
                System.out.println("Invalid input: " + e.getMessage());
            }
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    public static String hashPassword(String password) {
        try {
            // Get an instance of the SHA-256 algorithm
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Convert the password string into bytes and hash it
            byte[] encodedHash = digest.digest(password.getBytes());

            // Convert the byte array into a Base64 string so it can be stored as text
            return Base64.getEncoder().encodeToString(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: Hashing algorithm not found!");
        }
    }
    //To close BufferedReader to save resources
    public static void closeReader () {
        try {
            br.close();
        }
        catch (IOException e) {
            System.out.println("Error closing reader: " + e.getMessage());
        }
    }
    //Generic method to check if an arraylist is empty
    public static <T> boolean checkArrayList (ArrayList<T> arrayList) {return !arrayList.isEmpty();}
}
