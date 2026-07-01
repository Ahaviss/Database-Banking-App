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

package com.ahaviss.testutilities;

import com.ahaviss.utilities.ExceptionSupplier;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.utilities.SQLExecutor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TestUtils {
    private static final SQLExecutor executor;
    static {
        executor = new SQLExecutor("jdbc:h2:mem:banking_test;DATABASE_TO_UPPER=FALSE;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        try {
            initSchema();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    static void initSchema () throws Exception {
        try (Connection connection = executor.getConnection(); Statement stmt = connection.createStatement()) {
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS accounts (
                account_id        INT PRIMARY KEY,
                account_holder    VARCHAR(100) NOT NULL,
                balance           DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
                account_password  VARCHAR(255) NOT NULL,
                account_status    VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
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
                                id              INT DEFAULT 1 PRIMARY KEY,
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
                                performed_by  VARCHAR(10) NOT NULL,
                                source        VARCHAR(100) NOT NULL,
                                target        VARCHAR(100) NULL DEFAULT 'N/A',
                                before_value  VARCHAR(255) NULL DEFAULT 'N/A',
                                after_value   VARCHAR(255) NULL DEFAULT 'N/A',
                                timestamp     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                            )
            """);
        }
    }
    public static ProjectUtils mockInput (String input) {
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(bais));
        return new ProjectUtils(br, executor);
    }
    public static SQLExecutor executor() {return executor;}
    public static ExceptionSupplier<SQLException> exception() {return () -> new SQLException("Incorrect return type given from database");}
    public static boolean clearDirectory (String folderPath) {
        File file = new File(folderPath);
        if (!file.exists() || !file.isDirectory()) {
            System.out.println(folderPath + " doesn't exist or is not a directory");
            return false;
        }
        File[] files = file.listFiles();
        if (files == null) {
            System.out.println("Restricted access to " + folderPath +", or directory has no files.");
            return false;
        }
        boolean successful = true;
        for (File f : files) {
            if (f.isDirectory()) {if (!clearDirectory(f.getAbsolutePath())) return false;}
            boolean del = f.delete();
            if (!del) {
                System.out.println("Unable to delete file: " + f.getAbsolutePath());
                successful = false;
            }
        }
        return successful;
    }
}
