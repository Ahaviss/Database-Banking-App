/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.exceptions;
import com.ahaviss.database.Account;

//To handle newly locked or previously locked accounts
public class AccountLockedException extends Exception {
    private Account account = null;
    public AccountLockedException(int accountID, int timeLeft) {
        super(timeLeft == Integer.MAX_VALUE ? String.format("Account is locked permanently. Please contact the bank for assistance.%nCause: %d is locked", accountID) : String.format("Account is locked for %d more minutes. Please contact the bank for assistance.%nCause: %d is locked", timeLeft, accountID));
    }
    public AccountLockedException (Account account) {
        super(String.format("Account has been locked. Please contact the bank for assistance.%nCause: Repeated attempts on: %d", account.getAccountId()));
        this.account = account;
    }

    public Account traceAccount () {return account;}
}
