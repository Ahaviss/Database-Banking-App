/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.tests;
import com.ahaviss.database.Admin;
import com.ahaviss.database.Owner;
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
import java.util.HashMap;
import java.util.Map;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@Timeout(5)
class AdminTests {
    private Admin admin;
    private static String sharedHash;
    @BeforeAll
    @SuppressWarnings({"unused", "EmptyTryBlock"})
    static void beforeAll() {
        //To connect Mockito before tests
        try (var ignored = Mockito.mockStatic(SecurityUtils.class)) {}
        sharedHash = SecurityUtils.hashPassword("123Password");
        Class<?> warmup = AdminLogic.class;
    }
    @BeforeEach
    void beforeEach() {admin = new Admin(5555555, "John", sharedHash);}
    @AfterEach
    void afterEach() {admin = null;}
    private AdminLogic mockAdminLogic (ProjectUtils projectUtils) {
        return new AdminLogic(projectUtils);
    }
    @Test
    @DisplayName("Test Admin Creation")
    void testAdminCreation() {
        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(() -> SecurityUtils.hashPassword("123Password")).thenReturn(sharedHash);
            mockedStatic.when(() -> SecurityUtils.verifyPassword("123Password", sharedHash)).thenReturn(true);
            assertAll(
                    () -> assertEquals(5555555, admin.getAdminId()),
                    () -> assertEquals("John", admin.getAdminName()),
                    () -> assertTrue(SecurityUtils.verifyPassword("123Password", admin.getAdminPassword()))
            );
        }
    }
    @Test
    @DisplayName("Test Logging In")
    void testLoggingIn () {
        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(() -> SecurityUtils.verifyPassword("123Password", sharedHash)).thenReturn(true);
            String simulatedInput = "5555555\n123Password\n";
            final Map<Integer, Admin> admins = new HashMap<>();
            admins.put(5555555, admin);
            assertDoesNotThrow(() -> new LoginSystem(TestUtils.mockInput(simulatedInput)).adminLogin(admins, new Owner()));
        }
    }
    @ParameterizedTest
    @ValueSource(ints = {1000000, 5000000, 1212121, 9999999})
    @DisplayName("Test Deleting Admins")
    void testDeletingAdmins(int adminId) {
        final Map<Integer, Admin> admins = new HashMap<>();
        admins.put(adminId, new Admin(adminId, "John", sharedHash));
        String simulatedInput = String.format("1\n%s", adminId);
        assertNull(mockAdminLogic(TestUtils.mockInput(simulatedInput)).deleteAdmins(admins).get(adminId));
    }
    @ParameterizedTest
    @CsvSource({
            "1, 123Passwordd",
            "123password, 999Password",
            "Password, 1000Secure",
            "12Pass, 1001SecurePassword"
    })
    @DisplayName("Test Changing Password")
    void testChangingPassword(String newPasswordInvalid, String newPasswordValid) {
        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(() -> SecurityUtils.hashPassword(newPasswordValid))
                    .thenReturn("completely_mocked_hash");
            mockedStatic.when(() -> SecurityUtils.verifyPassword(newPasswordValid, "completely_mocked_hash"))
                    .thenReturn(true);
            String simulatedInput = String.format("%s\n%s\n", newPasswordInvalid, newPasswordValid);
            mockAdminLogic(TestUtils.mockInput(simulatedInput)).editPassword(admin);
            assertTrue(SecurityUtils.verifyPassword(newPasswordValid, admin.getAdminPassword()), "Incorrect password change.");
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
    void testChangingAdminName (String adminNameInvalid, String adminNameValid) {
        String simulatedInput = String.format("%s\n%s\n", adminNameInvalid, adminNameValid);
        mockAdminLogic(TestUtils.mockInput(simulatedInput)).editAdminName(admin);
        assertEquals(adminNameValid, admin.getAdminName());
    }
}
