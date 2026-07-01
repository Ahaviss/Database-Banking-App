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

public class Deposit implements Printable{
    //Private fields
    private final double amountDeposited;
    private final int accountId;
    //Constructor for object creation
    public Deposit(double amountDeposited, int accountId) {
        this.amountDeposited = amountDeposited;
        this.accountId = accountId;
    }
    //Prints the deposit information
    @Override
    public void printInfo () {
        System.out.println("Account ID: " + accountId);
        System.out.println("Amount Deposited: " + amountDeposited);
        System.out.println();
    }
    public double getAmountDeposited() {return amountDeposited;}
    public int getAccountId() {return accountId;}
}
