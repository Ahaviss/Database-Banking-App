module Banking.App {
    //Data saving
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    //Basic
    requires java.base;
    requires org.bouncycastle.provider;
    //Allowing reflection
    exports com.ahaviss.enums to com.fasterxml.jackson.databind;
    exports com.ahaviss.logs.enums to com.fasterxml.jackson.databind;
    opens com.ahaviss.database to com.fasterxml.jackson.core, com.fasterxml.jackson.databind, com.fasterxml.jackson.annotation, org.junit.jupiter;
    opens com.ahaviss.logs.database to com.fasterxml.jackson.core, com.fasterxml.jackson.databind, com.fasterxml.jackson.annotation, org.junit.jupiter;
}