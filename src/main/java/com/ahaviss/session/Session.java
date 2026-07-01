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

package com.ahaviss.session;

import com.ahaviss.enums.LoginEnums;


public class Session {
    //Current account index
    private static int currentAccount = -1;
    private static int currentAdmin = -1;
    //Current role
    private static LoginEnums role = LoginEnums.NONE;
    //Getters and setters
    public static int getCurrentAccount () {return currentAccount;}
    public static void setCurrentAccount (int account) {currentAccount = account;}
    public static int getCurrentAdmin () {return currentAdmin;}
    public static void setCurrentAdmin (int admin) {currentAdmin = admin;}
    public static LoginEnums getRole () {return role;}
    public static void setRole (LoginEnums role) {Session.role = role;}
}
