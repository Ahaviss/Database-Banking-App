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

module Banking.App {
    //Data saving
    requires net.sf.jsqlparser;
    requires com.zaxxer.hikari;
    //Basic
    requires java.base;
    requires java.sql;
    //Hashing
    requires org.bouncycastle.provider;
    //Reflection
    opens com.ahaviss.database to org.junit.jupiter;
    opens com.ahaviss.logs.database to org.junit.jupiter;
}