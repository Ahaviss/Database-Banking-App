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
