package com.ahaviss.utilities;
@FunctionalInterface
public interface ExceptionSupplier<E extends Exception> {
    E getException() throws Exception;
}
