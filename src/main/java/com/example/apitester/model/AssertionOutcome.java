package com.example.apitester.model;

public record AssertionOutcome(boolean passed, String message) {

    public static AssertionOutcome pass(String message) {
        return new AssertionOutcome(true, message);
    }

    public static AssertionOutcome fail(String message) {
        return new AssertionOutcome(false, message);
    }
}