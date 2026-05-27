/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.database;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Withdraw implements Printable {
    //Private fields
    private final double amountWithdrawn;
    private final int accountId;
    //Constructor for object creation
    @JsonCreator
    public Withdraw(@JsonProperty("amountWithdrawn") double amountWithdrawn, @JsonProperty("accountId") int accountId) {
        this.amountWithdrawn = amountWithdrawn;
        this.accountId = accountId;
    }
    //Prints the withdrawal information
    @Override
    public void printInfo () {
        System.out.println("Account ID: " + accountId);
        System.out.println("Amount Withdrawn: " + amountWithdrawn);
        System.out.println();
    }
    public double getAmountWithdrawn() {return amountWithdrawn;}
    public int getAccountId() {return accountId;}
}
