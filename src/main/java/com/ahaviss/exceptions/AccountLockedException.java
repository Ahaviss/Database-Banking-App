package com.ahaviss.exceptions;
import com.ahaviss.database.Account;

//To handle newly locked or previously locked accounts
public class AccountLockedException extends Exception {
    private Account account = null;
    public AccountLockedException(int accountID, int timeLeft) {
        String message;
        if (timeLeft == Integer.MAX_VALUE) {
            message = String.format("Account is locked permanently. Please contact the bank for assistance.%nCause: %d is locked", accountID);
        } else {
            message = String.format("Account is locked for %d more minutes. Please contact the bank for assistance.%nCause: %d is locked", timeLeft, accountID);
        }
        super(message);
    }
    public AccountLockedException (Account account) {
        super(String.format("Account has been locked. Please contact the bank for assistance.%nCause: Repeated attempts on: %d", account.getAccountId()));
        this.account = account;
    }

    public Account traceAccount () {return account;}
}
