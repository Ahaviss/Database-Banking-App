/*
 * Copyright (c) 2026 Ahaviss. All Rights Reserved.
 *
 * This file is part of the Banking-App project.
 * Unauthorized copying, modification, or distribution of this file,
 * via any medium is strictly prohibited.
 */

module Banking.App {
    //Data saving
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.bouncycastle.provider;
    //Basic
    requires java.base;
    //Allowing reflection
    exports com.ahaviss.enums to com.fasterxml.jackson.databind;
    exports com.ahaviss.logs.enums to com.fasterxml.jackson.databind;
    opens com.ahaviss.database to com.fasterxml.jackson.core, com.fasterxml.jackson.databind, com.fasterxml.jackson.annotation, org.junit.jupiter;
    opens com.ahaviss.logs.database to com.fasterxml.jackson.core, com.fasterxml.jackson.databind, com.fasterxml.jackson.annotation, org.junit.jupiter;
}