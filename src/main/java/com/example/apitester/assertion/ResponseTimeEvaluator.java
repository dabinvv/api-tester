package com.example.apitester.assertion;

import com.example.apitester.model.ApiResponse;
import com.example.apitester.model.Assertion;
import com.example.apitester.model.AssertionOutcome;
import com.example.apitester.model.AssertionType;

public class ResponseTimeEvaluator implements AssertionEvaluator {

    @Override
    public boolean supports(AssertionType type) {
        return type == AssertionType.MAX_TIME_MS;
    }

    @Override
    public AssertionOutcome evaluate(Assertion assertion, ApiResponse response) {
        long max = Long.parseLong(assertion.expected());
        long actual = response.durationMs();

        if (actual <= max) {
            return AssertionOutcome.pass("Responded in " + actual + " ms (limit " + max + ")");
        }
        return AssertionOutcome.fail("Expected at most " + max + " ms but took " + actual + " ms");
    }
}