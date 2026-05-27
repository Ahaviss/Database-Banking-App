/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.logs.enums;
public enum Action{
    // Account Management
    CREATE_ACCOUNT("Created New Account"),
    DELETE_ACCOUNT("Deleted Account"),
    CHANGE_HOLDER("Changed Holder Name"),
    CHANGE_ACCOUNT_STATUS("Changed Account Status"),
    ACCOUNT_AUTO_LOCKED("Security: Account Auto-Locked"),

    // Financial
    DEPOSIT("Funds Deposited"),
    WITHDRAW("Funds Withdrawn"),
    TRANSFER("Funds Transferred"),
    CHANGE_BALANCE("Manual Balance Adjustment"),
    CHANGE_CREDIT_SCORE("Updated Credit Score"),

    // Security & Admin
    CHANGE_PASSWORD("User Password Reset"),
    CHANGE_ADMIN_PASSWORD("Admin Password Reset");

    private final String action;
    Action(String action) {
        this.action = action;
    }
    public String getAction() {
        return action;
    }
}
