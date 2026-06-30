/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.main;

import com.ahaviss.enums.ControlFlow;
import com.ahaviss.enums.LoginEnums;
import com.ahaviss.logic.AccountLogic;
import com.ahaviss.logic.AdminLogic;
import com.ahaviss.logic.LoginSystem;
import com.ahaviss.logic.Logins;
import com.ahaviss.logs.manager.LogManager;
import com.ahaviss.menus.AccountMenus;
import com.ahaviss.menus.AdminMenus;
import com.ahaviss.menus.GeneralMenus;
import com.ahaviss.menus.OwnerMenus;
import com.ahaviss.session.Session;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.utilities.SQLExecutor;
import com.ahaviss.utilities.SecurityUtils;
import net.sf.jsqlparser.JSQLParserException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

class App {
    private final ProjectUtils projectUtils;
    private final AccountLogic accountLogic;
    private final AccountMenus accountMenus;
    private final GeneralMenus generalMenus;
    private final OwnerMenus ownerMenus;
    private final LoginSystem loginSystem;
    private final LogManager logManager;
    private final AdminLogic adminLogic;
    private final AdminMenus adminMenus;
    private final Logins logins;
    private final SQLExecutor executor;
    App () {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        this.executor = loadExecutor(reader);
        this.projectUtils = new ProjectUtils(reader, executor);
        this.accountLogic = new AccountLogic(projectUtils, executor);
        this.accountMenus = new AccountMenus(accountLogic, projectUtils, executor);
        this.adminLogic = new AdminLogic(projectUtils, executor);
        this.logManager = new LogManager(projectUtils, executor);
        this.generalMenus = new GeneralMenus(projectUtils, logManager);
        this.loginSystem = new LoginSystem(projectUtils, executor);
        this.ownerMenus = new OwnerMenus(projectUtils, adminLogic, generalMenus, executor);
        this.adminMenus = new AdminMenus(accountLogic, projectUtils, accountMenus);
        this.logins = new Logins(loginSystem, projectUtils, executor, accountLogic);
    }
    private SQLExecutor loadExecutor (BufferedReader br) {
        String url = "jdbc:mysql://localhost:3306/banking_app?createDatabaseIfNotExists=true";
        while (true) {
            try {
                System.out.println("Input MySQL Credentials.");
                System.out.println("Username:");
                String username = br.readLine();
                System.out.println("Password:");
                String password = br.readLine();
                initSchema(url, username, password);
                return new SQLExecutor(url, username, password);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    private void initSchema(String url, String username, String password) {
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS accounts (
                account_id        INT PRIMARY KEY,
                account_holder    VARCHAR(100) NOT NULL,
                balance           DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
                account_password  VARCHAR(255) NOT NULL,
                account_status    ENUM('ACTIVE', 'LOCKED') NOT NULL DEFAULT 'ACTIVE',
                credit_score      INT NOT NULL CHECK (credit_score BETWEEN 500 AND 800),
                locked_time       DATETIME NULL,
                times_locked      INT NOT NULL DEFAULT 0,
                duration_locked   INT NOT NULL DEFAULT 0
            )
        """);
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS admins (
                                admin_id        INT PRIMARY KEY,
                                admin_name      VARCHAR(100) NOT NULL,
                                admin_password  VARCHAR(255) NOT NULL
                            )
            """);
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS owner (
                                id              INT PRIMARY KEY DEFAULT 1,
                                username        VARCHAR(100) NOT NULL,
                                owner_password  VARCHAR(255) NOT NULL,
                                CHECK (id = 1)
                            )
            """);
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS deposits (
                                account_id   INT NOT NULL,
                                amount       DECIMAL(15, 2) NOT NULL,
                                FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
                            )
            """);
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS withdrawals (
                                account_id     INT NOT NULL,
                                amount         DECIMAL(15, 2) NOT NULL,
                                FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
                            )
            """);
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS transfers (
                                source_account_id  INT NOT NULL,
                                target_account_id  INT NOT NULL,
                                amount  DECIMAL(15, 2) NOT NULL,
                                FOREIGN KEY (source_account_id) REFERENCES accounts(account_id),
                                FOREIGN KEY (target_account_id) REFERENCES accounts(account_id)
                            )
            """);
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS audit_logs (
                                action        VARCHAR(50) NOT NULL,
                                performed_by  ENUM('ADMIN', 'USER') NOT NULL,
                                source        VARCHAR(100) NOT NULL,
                                target        VARCHAR(100) NULL DEFAULT 'N/A',
                                before_value  VARCHAR(255) NULL DEFAULT 'N/A',
                                after_value   VARCHAR(255) NULL DEFAULT 'N/A',
                                timestamp     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                            )
            """);
        } catch (SQLException e) {
            System.err.println("Critical Error: " + e.getMessage());
            System.exit(1);
        }
    }
    private void initOwner() throws SQLException, JSQLParserException {
        boolean ownerExists = !executor
                .executeSQL("SELECT id FROM owner", null)
                .getFirst().isEmpty();

        if (!ownerExists) {
            String defaultPassword = SecurityUtils.hashPassword("tempPassword@123");
            executor.executeSQL(
                    "INSERT INTO owner (id, username, owner_password) VALUES (1, ?, ?)",
                    List.of(List.of("tempUsername@123", defaultPassword))
            );
            System.out.println("Default owner created. Username: tempUsername@123 | Password: tempPassword@123");
            System.out.println("IMPORTANT: Change these credentials immediately.");
        }
    }
    void start () {
        preStartTasks();
        menu();
    }
    private void menu () {
        while (true) {
            try {
                //If the role is admin or owner, call the adminPanel method
                if (Session.getRole() == LoginEnums.ADMIN || Session.getRole() == LoginEnums.OWNER) {
                    ControlFlow controlFlow = adminMenus.adminPanel();
                    if (controlFlow == ControlFlow.OWNER_PANEL) {
                        ControlFlow controlFlow1 = ownerMenus.ownerPanel();
                        if (controlFlow1 == ControlFlow.QUIT) return;
                        continue;
                    }
                    if (controlFlow == ControlFlow.QUIT) return;
                    else continue;
                    //If the role is user, call the accountPanel method
                } else if (Session.getRole() == LoginEnums.USER) {
                    ControlFlow controlFlow = accountMenus.accountPanel();
                    if (controlFlow == ControlFlow.QUIT) return;
                    else continue;
                }
                //If the role is none
                System.out.println("Welcome to the Banking System!");
                //Ask the user to log in, create or quit
                String answer = projectUtils.getValidString("Would you like to login, create an account, or quit? (login/create/quit)");
                if (answer.equalsIgnoreCase("login")) {
                    String login = projectUtils.getValidString("Would you like to login as an account holder or an admin? (account holder/admin)");
                    if (login.equalsIgnoreCase("account holder")) {
                        //Calls the accountLogin method
                        logins.accountLogin();
                    } else if (login.equalsIgnoreCase("admin")) {
                        //Calls the adminLogin method
                        logins.adminLogin();
                    } else {
                        //Invalid input
                        System.out.println("Invalid input. Please enter 'account holder', 'admin', or 'quit'.");
                    }
                } else if (answer.equalsIgnoreCase("create")) {
                    //Calls the createAccount method
                    accountLogic.createOneAccount();
                } else if (answer.equalsIgnoreCase("quit")) {
                    System.out.println("Terminating program...");
                    //Ends the program
                    return;
                } else {
                    //Invalid input
                    System.out.println("Invalid input. Please enter 'login' or 'create'.");
                }

            }
            //Catch invalid input
            catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
            catch (SQLException e) {
                System.out.println("DB/SQL error: " + e.getMessage());
            }
            catch (JSQLParserException e) {
                System.out.println("Invalid SQL statement: " + e.getMessage());
            }
            catch (Exception e) {
                System.out.printf("An unexpected error occurred: %s%n", e.getMessage());
            }
        }
    }
    private void preStartTasks () {
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(() -> {
            projectUtils.closeReader();
            executor.close();
        }));
        try {
            initOwner();
        } catch (Exception e) {
            System.err.println("Critical Error: Failed to initialize owner: " + e.getMessage());
            System.exit(1);
        }
        int version = Runtime.version().feature();
        System.out.println("Version: JDK " + version);
        if (version < 21) {
            System.out.println("WARNING: This code is recommended for JDK 21 and above.");
        }
    }
}
