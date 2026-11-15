package com.example.apitester.assertion;

import com.example.apitester.model.ApiResponse;
import com.example.apitester.model.Assertion;
import com.example.apitester.model.AssertionOutcome;
import com.example.apitester.model.AssertionType;

public class StatusCodeEvaluator implements AssertionEvaluator {

    @Override
    public boolean supports(AssertionType type) {
        return type == AssertionType.STATUS;
    }

    @Override
    public AssertionOutcome evaluate(Assertion assertion, ApiResponse response) {
        int expected = Integer.parseInt(assertion.expected());
        int actual = response.statusCode();

        if (actual == expected) {
            return AssertionOutcome.pass("Status is " + actual);
        }
        return AssertionOutcome.fail("Expected status " + expected + " but got " + actual);
    }
}