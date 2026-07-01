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

package com.ahaviss.exceptions;

//To handle newly locked or previously locked accounts
public class AccountLockedException extends Exception {
    private int accountID = -1;
    public AccountLockedException(int accountID, int timeLeft) {
        super(timeLeft == Integer.MAX_VALUE ? String.format("Account is locked permanently. Please contact the bank for assistance.%nCause: %d is locked", accountID) : String.format("Account is locked for %d more minutes. Please contact the bank for assistance.%nCause: %d is locked", timeLeft, accountID));
    }
    public AccountLockedException (int accountID) {
        super(String.format("Account has been locked. Please contact the bank for assistance.%nCause: Repeated attempts on: %d", accountID));
        this.accountID = accountID;
    }

    public int traceAccount () {return accountID;}
}
