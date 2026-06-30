/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

package com.ahaviss.database;
import com.ahaviss.enums.TransferDirection;

public class Transfer implements Printable {
    //Private fields
    private final double amountTransferred;
    private final int targetAccountId;
    private final int sourceAccountId;
    private final TransferDirection direction;
    //Constructor for object creation
    public Transfer(double amountTransferred, int targetAccountId, int sourceAccountId, TransferDirection direction) {
        this.amountTransferred = amountTransferred;
        this.targetAccountId = targetAccountId;
        this.sourceAccountId = sourceAccountId;
        this.direction = direction;
    }
    //Prints the transfer information
    @Override
    public void printInfo () {
        System.out.printf("Target Account ID: %d%nSource Account ID: %d%nAmount Transferred: %.2f%n%s%n", targetAccountId, sourceAccountId, amountTransferred, direction);
    }
    public TransferDirection getDirection() {return direction;}
    public double getAmountTransferred() {return amountTransferred;}
    public int getTargetAccountId() {return targetAccountId;}
    public int getSourceAccountId() {return sourceAccountId;}
}
