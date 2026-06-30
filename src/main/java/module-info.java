/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
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