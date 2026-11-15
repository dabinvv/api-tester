package com.example.apitester.assertion;

import com.example.apitester.model.ApiResponse;
import com.example.apitester.model.Assertion;
import com.example.apitester.model.AssertionType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTimeEvaluatorTest {

    private final ResponseTimeEvaluator evaluator = new ResponseTimeEvaluator();

    private static ApiResponse responseTakingMs(long ms) {
        return new ApiResponse(200, Map.of(), "", ms);
    }

    private static Assertion maxMs(String limit) {
        return Assertion.of(AssertionType.MAX_TIME_MS, limit);
    }

    @Test
    void passesWhenFasterThanLimit() {
        assertTrue(evaluator.evaluate(maxMs("500"), responseTakingMs(100)).passed());
    }

    @Test
    void passesExactlyAtTheLimit() {          // boundary case
        assertTrue(evaluator.evaluate(maxMs("500"), responseTakingMs(500)).passed());
    }

    @Test
    void failsWhenSlowerThanLimit() {
        var outcome = evaluator.evaluate(maxMs("500"), responseTakingMs(501));

        assertFalse(outcome.passed());
        assertEquals("Expected at most 500 ms but took 501 ms", outcome.message());
    }
}
