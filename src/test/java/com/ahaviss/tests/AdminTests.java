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

package com.ahaviss.tests;
import com.ahaviss.database.Admin;
import com.ahaviss.logic.AdminLogic;
import com.ahaviss.logic.LoginSystem;
import com.ahaviss.testutilities.TestUtils;
import com.ahaviss.utilities.ProjectUtils;
import com.ahaviss.utilities.SecurityUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ahaviss.testutilities.TestUtils.exception;
import static com.ahaviss.testutilities.TestUtils.executor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@Timeout(5)
class AdminTests {
    private static String sharedHash;
    private ProjectUtils projectUtils;
    @BeforeAll
    @SuppressWarnings({"unused", "EmptyTryBlock"})
    static void beforeAll() throws Exception{
        //To connect Mockito before tests
        try (var ignored = Mockito.mockStatic(SecurityUtils.class)) {}
        sharedHash = SecurityUtils.hashPassword("123Password");
        Class<?> warmup = AdminLogic.class;
        initOwner();
    }
    static void initOwner() throws Exception {
        executor().executeSQL(
                "INSERT INTO owner (id, username, owner_password) VALUES (1, ?, ?)",
                List.of(List.of("tempUsername@123", "fake_password"))
        );
    }
    @BeforeEach
    void beforeEach() throws Exception {
        executor().executeSQL("INSERT INTO admins (admin_id, admin_name, admin_password) VALUES (?, ?, ?)", List.of(List.of(5555555, "John", sharedHash)));
    }
    @AfterEach
    void afterEach() throws Exception {
        executor().executeSQL("TRUNCATE TABLE admins", null);
        projectUtils = null;
    }
    private AdminLogic mockAdminLogic (ProjectUtils projectUtils) {
        return new AdminLogic(projectUtils, executor());
    }
    @Test
    @DisplayName("Test Admin Creation")
    void testAdminCreation() throws Exception {
        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(() -> SecurityUtils.hashPassword("123Password")).thenReturn(sharedHash);
            mockedStatic.when(() -> SecurityUtils.verifyPassword("123Password", sharedHash)).thenReturn(true);
            Map<String, Object> info = executor().executeSQL("SELECT * FROM admins WHERE admin_id = 5555555", null).getFirst().getFirst();
            projectUtils = TestUtils.mockInput("");
            assertAll(
                    () -> assertEquals(5555555, projectUtils.verifyInstanceOf(info.get("admin_id"), Integer.class, exception())),
                    () -> assertEquals("John", info.get("admin_name").toString()),
                    () -> assertTrue(SecurityUtils.verifyPassword("123Password", info.get("admin_password").toString()))
            );
        }
    }
    @Test
    @DisplayName("Test Logging In")
    void testLoggingIn () {
        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(() -> SecurityUtils.verifyPassword("123Password", sharedHash)).thenReturn(true);
            mockedStatic.when(() -> SecurityUtils.hashPassword("tempPassword@123")).thenReturn(sharedHash);
            String simulatedInput = "5555555\n123Password\n";
            assertDoesNotThrow(() -> new LoginSystem(TestUtils.mockInput(simulatedInput), executor()).adminLogin());
        }
    }
    @ParameterizedTest
    @ValueSource(ints = {1000000, 5000000, 1212121, 9999999})
    @DisplayName("Test Deleting Admins")
    void testDeletingAdmins(int adminId) throws Exception {
        executor().executeSQL("INSERT INTO admins (admin_id, admin_name, admin_password) VALUES (?, ?, ?)", List.of(List.of(adminId, "John", sharedHash)));
        String simulatedInput = String.format("1\n%s", adminId);
        projectUtils = TestUtils.mockInput(simulatedInput);
        mockAdminLogic(projectUtils).deleteAdmins();
        assertFalse(projectUtils.idExists(adminId, Admin.class));
    }
    @ParameterizedTest
    @CsvSource({
            "1, 123Passwordd",
            "123password, 999Password",
            "Password, 1000Secure",
            "12Pass, 1001SecurePassword"
    })
    @DisplayName("Test Changing Password")
    void testChangingPassword(String newPasswordInvalid, String newPasswordValid) throws Exception {
        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(() -> SecurityUtils.hashPassword(newPasswordValid))
                    .thenReturn("completely_mocked_hash");
            mockedStatic.when(() -> SecurityUtils.verifyPassword(eq(newPasswordValid), eq("completely_mocked_hash")))
                    .thenReturn(true);
            mockedStatic.when(() -> SecurityUtils.verifyPassword(eq("completely_mocked_hash"), eq("completely_mocked_hash")))
                    .thenReturn(true);
            String simulatedInput = String.format("%s\n%s\n", newPasswordInvalid, newPasswordValid);
            mockAdminLogic(TestUtils.mockInput(simulatedInput)).editPassword(5555555);
            String password = executor().executeSQL("SELECT admin_password FROM admins WHERE admin_id = 5555555", null).getFirst().getFirst().get("admin_password").toString();
            System.out.println(password);
            assertTrue(SecurityUtils.verifyPassword("completely_mocked_hash", password), "Incorrect password change.");
        }
    }
    @ParameterizedTest
    @CsvSource({
            "'', David",
            "'', Bob",
            "'', Emily",
            "'', Robert"
    })
    @DisplayName("Test Changing Admin Name")
    void testChangingAdminName (String adminNameInvalid, String adminNameValid) throws Exception {
        String simulatedInput = String.format("%s\n%s\n", adminNameInvalid, adminNameValid);
        mockAdminLogic(TestUtils.mockInput(simulatedInput)).editAdminName(5555555);
        String adminName = executor().executeSQL("SELECT admin_name FROM admins WHERE admin_id = 5555555", null).getFirst().getFirst().get("admin_name").toString();
        assertEquals(adminNameValid, adminName);
    }
}
