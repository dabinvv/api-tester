package com.example.apitester.assertion;

import com.example.apitester.model.ApiResponse;
import com.example.apitester.model.Assertion;
import com.example.apitester.model.AssertionOutcome;
import com.example.apitester.model.AssertionType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StatusCodeEvaluatorTest {

    private final StatusCodeEvaluator evaluator = new StatusCodeEvaluator();

    private static ApiResponse responseWithStatus(int status) {
        return new ApiResponse(status, Map.of(), "", 10);
    }

    @Test
    void supportsOnlyStatusAssertions() {
        assertTrue(evaluator.supports(AssertionType.STATUS));
        assertFalse(evaluator.supports(AssertionType.MAX_TIME_MS));
        assertFalse(evaluator.supports(AssertionType.JSON_PATH));
    }

    @Test
    void passesWhenStatusMatches() {
        AssertionOutcome outcome =
                evaluator.evaluate(Assertion.of(AssertionType.STATUS, "200"), responseWithStatus(200));

        assertTrue(outcome.passed());
    }

    @Test
    void failsWithClearMessageWhenStatusDiffers() {
        AssertionOutcome outcome =
                evaluator.evaluate(Assertion.of(AssertionType.STATUS, "201"), responseWithStatus(200));

        assertFalse(outcome.passed());
        assertEquals("Expected status 201 but got 200", outcome.message());
    }
}
