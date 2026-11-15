package com.example.apitester.model;

public record Assertion(AssertionType type, String path, String expected) {

    /** For assertions that need no path (STATUS, MAX_TIME_MS). */
    public static Assertion of(AssertionType type, String expected) {
        return new Assertion(type, null, expected);
    }
}