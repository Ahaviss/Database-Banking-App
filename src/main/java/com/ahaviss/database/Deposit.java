package com.ahaviss.database;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Deposit {
    //Private fields
    private final double amountDeposited;
    private final int accountId;
    //Constructor for object creation
    @JsonCreator
    public Deposit(@JsonProperty("amountDeposited") double amountDeposited, @JsonProperty("accountId") int accountId) {
        this.amountDeposited = amountDeposited;
        this.accountId = accountId;
    }
    //Prints the deposit information
    public void printInfo () {
        System.out.println("Account ID: " + accountId);
        System.out.println("Amount Deposited: " + amountDeposited);
        System.out.println();
    }
    public double getAmountDeposited() {return amountDeposited;}
    public int getAccountId() {return accountId;}
}
