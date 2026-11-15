package com.example.apitester.exception;

/** Thrown when a test definition file is missing, unreadable, or invalid. */
public class TestDefinitionException extends RuntimeException {

    public TestDefinitionException(String message) {
        super(message);
    }

    public TestDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}