package com.ahaviss.database;
import com.ahaviss.enums.TransferDirection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Transfer implements Printable {
    //Private fields
    private final double amountTransferred;
    private final int targetAccountId;
    private final int sourceAccountId;
    private final TransferDirection direction;
    //Constructor for object creation
    @JsonCreator
    public Transfer(@JsonProperty("amountTransferred") double amountTransferred, @JsonProperty("targetAccountId") int targetAccountId, @JsonProperty("sourceAccountId") int sourceAccountId, @JsonProperty("direction") TransferDirection direction) {
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
    public double getAmountTransferred() {return amountTransferred;}
    public int getTargetAccountId() {return targetAccountId;}
    public int getSourceAccountId() {return sourceAccountId;}
}
