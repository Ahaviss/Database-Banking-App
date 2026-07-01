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
    public static Action fromValue(String value) {
        for (Action action : Action.values()) {
            if (action.getAction().equalsIgnoreCase(value)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
