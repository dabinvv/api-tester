package com.example.apitester.assertion;

import com.example.apitester.model.ApiResponse;
import com.example.apitester.model.Assertion;
import com.example.apitester.model.AssertionOutcome;
import com.example.apitester.model.AssertionType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonPathEvaluatorTest {

    private final JsonPathEvaluator evaluator = new JsonPathEvaluator();

    private static ApiResponse body(String json) {
        return new ApiResponse(200, Map.of(), json, 10);
    }

    private static Assertion at(String path, String expected) {
        return new Assertion(AssertionType.JSON_PATH, path, expected);
    }

    @Test
    void passesWhenStringValueMatches() {
        var outcome = evaluator.evaluate(at("$.name", "Leanne"), body("{\"name\":\"Leanne\"}"));
        assertTrue(outcome.passed());
    }

    @Test
    void numbersAreComparedAsText() {
        assertTrue(evaluator.evaluate(at("$.id", "1"), body("{\"id\":1}")).passed());
    }

    @Test
    void readsNestedFieldsAndArrays() {
        String json = "{\"address\":{\"city\":\"Oslo\"},\"items\":[{\"id\":7}]}";

        assertTrue(evaluator.evaluate(at("$.address.city", "Oslo"), body(json)).passed());
        assertTrue(evaluator.evaluate(at("$.items[0].id", "7"), body(json)).passed());
    }

    @Test
    void failsWhenValueDiffers() {
        var outcome = evaluator.evaluate(at("$.name", "Bob"), body("{\"name\":\"Leanne\"}"));

        assertFalse(outcome.passed());
        assertEquals("Expected $.name to be 'Bob' but got 'Leanne'", outcome.message());
    }

    @Test
    void failsCleanlyWhenPathIsMissing() {
        var outcome = evaluator.evaluate(at("$.nope", "x"), body("{\"name\":\"Leanne\"}"));

        assertFalse(outcome.passed());
        assertTrue(outcome.message().contains("not found"));
    }

    @Test
    void failsCleanlyWhenBodyIsNotJson() {
        AssertionOutcome outcome = evaluator.evaluate(at("$.name", "x"), body("<html>oops</html>"));

        assertFalse(outcome.passed());       // must not throw
    }
}
