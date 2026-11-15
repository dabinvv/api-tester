package com.example.apitester.model;

import java.util.List;

/**
 * Result of running one TestCase.
 * Either the request completed (outcomes filled, error null)
 * or it never got a response (outcomes empty, error set).
 */
public record TestResult(String testName, List<AssertionOutcome> outcomes, String error) {

    public static TestResult completed(String testName, List<AssertionOutcome> outcomes) {
        return new TestResult(testName, outcomes, null);
    }

    public static TestResult errored(String testName, String error) {
        return new TestResult(testName, List.of(), error);
    }

    public boolean passed() {
        return error == null && outcomes.stream().allMatch(AssertionOutcome::passed);
    }
}