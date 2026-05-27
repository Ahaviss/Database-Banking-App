/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.database;
//Java imports
import java.util.ArrayList;
//Local imports
import com.ahaviss.enums.AccountStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.LocalDateTime;
import java.util.List;

public class Account implements Printable {
    //Private fields
    private LocalDateTime accountLockedTime;
    private int amountOfTimesLocked;
    private int durationLocked;
    private final int accountId;
    private String accountHolder;
    private double balance;
    private String accountPassword;
    private AccountStatus accountStatus;
    private int creditScore;
    //ArrayLists for deposit, withdraw, and transfer history
    private final List<Deposit> deposits = new ArrayList<>();
    private final List<Withdraw> withdraws = new ArrayList<>();
    private final List<Transfer> transfers = new ArrayList<>();
    //Constructor for object creation
    @JsonCreator
    public Account(@JsonProperty("accountId") int accountId, @JsonProperty("accountHolder") String accountHolder, @JsonProperty("balance") double balance, @JsonProperty("accountPassword") String accountPassword, @JsonProperty("accountStatus") AccountStatus accountStatus, @JsonProperty("creditScore") int creditScore) {
        this.accountId = accountId;
        this.accountHolder = accountHolder;
        this.balance = balance;
        this.accountPassword = accountPassword;
        this.accountStatus = accountStatus;
        this.creditScore = creditScore;
    }
    //Getters and setters
    public int getCreditScore() {return creditScore;}
    public void setCreditScore(int creditScore) {this.creditScore = creditScore;}
    public AccountStatus getAccountStatus() {return accountStatus;}
    public void setAccountStatus(AccountStatus accountStatus) {this.accountStatus = accountStatus;}
    public String getAccountPassword() {return accountPassword;}
    public void setAccountPassword(String accountPassword) {this.accountPassword = accountPassword;}
    public void addDeposit(Deposit deposit) {deposits.add(deposit);}
    public void addWithdraw(Withdraw withdraw) {withdraws.add(withdraw);}
    public void addTransfer(Transfer transfer) {transfers.add(transfer);}
    public int getAccountId() {return accountId;}
    public String getAccountHolder() {return accountHolder;}
    public double getBalance() {return balance;}
    public void setBalance(double balance) {this.balance = balance;}
    public void setAccountHolder(String accountHolder) {this.accountHolder = accountHolder;}
    public void setAccountLockedTime(LocalDateTime accountLockedTime) {this.accountLockedTime = accountLockedTime;}
    public LocalDateTime getAccountLockedTime() {return accountLockedTime;}
    public void setAmountOfTimesLocked (int amountOfTimesLocked) {this.amountOfTimesLocked = amountOfTimesLocked;}
    public int getAmountOfTimesLocked() {return amountOfTimesLocked;}
    public void setDurationLocked(int durationLocked) {this.durationLocked = durationLocked;}
    public int getDurationLocked() {return durationLocked;}
    //Prints the account logs/history
    public void printHistory () {
        System.out.println("Account History:");
        System.out.println("Account ID: " + accountId);
        System.out.println("Deposits:");
        deposits.forEach(Deposit::printInfo);
        System.out.println("Withdraws:");
        withdraws.forEach(Withdraw::printInfo);
        System.out.println("Transfers:");
        transfers.forEach(Transfer::printInfo);
    }
    //Prints the account information
    @Override
    public void printInfo() {
        System.out.println("Account ID: " + accountId);
        System.out.println("Account Holder: " + accountHolder);
        System.out.println("Account Status: " + accountStatus);
        System.out.println("Credit Score: " + creditScore);
        System.out.println("Balance: " + balance);
    }
}
