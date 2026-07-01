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
import com.ahaviss.exceptions.*;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.database.Account;
import com.ahaviss.utilities.SQLExecutor;
import com.ahaviss.utilities.SecurityUtils;
import net.sf.jsqlparser.JSQLParserException;
//Java imports
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class LoginSystem {
    //Total tries
    private final ProjectUtils projectUtils;
    private final SQLExecutor executor;
    public LoginSystem (ProjectUtils projectUtils, SQLExecutor executor) {
        this.projectUtils = projectUtils;
        this.executor = executor;
    }
    public int accountLogin () throws Exception {
        //Arrays to track if the same username is targeted multiple times
        int[] foundUsernames = new int[3];
        for (int i = 0; i < 3; i++) {
            System.out.printf("Login attempt %d/3%n", i + 1);
            //Gets the account ID and password
            String tempAccountId = projectUtils.getValidString("Enter your account ID: ");
            String accountPassword = projectUtils.getValidString("Enter your account password: ");
            //Checks if the ID is a number
            boolean isNumber = tempAccountId.matches("\\d+");
            if (!isNumber) {
                System.out.println("Invalid admin ID. Please enter a valid admin ID.");
                continue;
            }
            //Parses the ID to an integer
            int accountId = Integer.parseInt(tempAccountId);
            if (projectUtils.idExists(accountId, Account.class)) {
                Map<String, Object> info  = executor.executeSQL("SELECT account_password, account_status, locked_time, duration_locked FROM accounts WHERE account_id = ?", List.of(List.of(accountId))).getFirst().getFirst();
                if (SecurityUtils.verifyPassword(accountPassword, info.get("account_password").toString())) {
                    //Checks if the account is locked if the above is true
                    if (info.get("account_status").toString().equalsIgnoreCase("LOCKED")) {
                        LocalDateTime lockedTime = projectUtils.verifyInstanceOf(info.get("locked_time"), LocalDateTime.class, () -> new SQLException("Incorrect return type given from database"));
                        int durationLocked = projectUtils.verifyInstanceOf(info.get("duration_locked"), Integer.class, () -> new SQLException("Incorrect return type given from database"));
                        if (lockedTime == null && durationLocked == Integer.MAX_VALUE)
                            throw new AccountLockedException(accountId, Integer.MAX_VALUE);
                        else if (lockedTime == null)
                            throw new IllegalStateException("Locked time is in an illegal state. Duration locked is not Integer.MAX_VAUE and locked time is null.");
                        Duration duration = Duration.between(lockedTime, LocalDateTime.now());
                        if (durationLocked == Integer.MAX_VALUE)
                            throw new AccountLockedException(accountId, Integer.MAX_VALUE);
                        if (duration.toMinutes() >= durationLocked) {
                            executor.executeSQL("""
                                UPDATE accounts SET account_status = ?,
                                duration_locked = ?,
                                locked_time = NULL,
                                times_locked = ?
                                WHERE account_id = ?
                            """, List.of(List.of("ACTIVE", 0, 0, accountId)));
                            return accountId;
                        }
                        throw new AccountLockedException(accountId, durationLocked - (int) duration.toMinutes());
                    }
                    //Otherwise, return the account
                    return accountId;
                }
                foundUsernames[i] = accountId;
            } else foundUsernames[i] = -1;
            System.out.println("Invalid account ID or password. Please try again.");
        }
        //If attempts are exceeded
        System.out.println("Unauthorised access. Please try again.");
        int amountOfTimes = 0;
        //Checks if the same ID was targeted multiple times
        int repeatedUsername = foundUsernames[0];
        if (repeatedUsername == -1) throw new LoginFailedException();
        for (int foundUsername : foundUsernames) {
            if (foundUsername == repeatedUsername) {
                amountOfTimes++;
            }
        }
        //Returns the value to indicate the account should be locked
        if (amountOfTimes >= 3) {
            throw new AccountLockedException(repeatedUsername);
        }
        //Otherwise
        throw new LoginFailedException();
    }
    public int adminLogin () throws SQLException, JSQLParserException {
        for (int i = 0; i < 3; i++) {
            System.out.printf("Login attempt %d/3%n", i + 1);
            //Gets the admin ID and password
            String adminId = projectUtils.getValidString("Enter your admin ID: ");
            String adminPassword = projectUtils.getValidString("Enter your admin password: ");
            //Checks if the ID and password match the owner
            if (adminId.equals(executor.executeSQL("SELECT username FROM owner WHERE id = 1", null).getFirst().getFirst().get("username").toString()) && SecurityUtils.verifyPassword(adminPassword, executor.executeSQL("SELECT owner_password FROM owner WHERE id = 1", null).getFirst().getFirst().get("owner_password").toString())) {
                return Integer.MIN_VALUE;
            }
            //Checks if the ID is a number
            boolean isNumber = adminId.matches("\\d+");
            if (!isNumber) {
                System.out.println("Invalid admin ID. Please enter a valid admin ID.");
                continue;
            }
            //Checks if the input matches the current admin being checked
            int adminIdInt = Integer.parseInt(adminId);
            if (projectUtils.idExists(adminIdInt, Admin.class) && SecurityUtils.verifyPassword(adminPassword, executor.executeSQL("SELECT admin_password FROM admins WHERE admin_id = ?", List.of(List.of(adminIdInt))).getFirst().getFirst().get("admin_password").toString()))
                return adminIdInt;
            System.out.println("Invalid admin ID or password. Please try again.");
        }
        //If attempts are exceeded
        System.out.println("Unauthorised access. Defaulting...");
        //Terminates the JVM
        System.exit(0);
        return -1;
    }
}
