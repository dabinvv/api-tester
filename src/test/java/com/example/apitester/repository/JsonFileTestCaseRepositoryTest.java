package com.example.apitester.repository;

import com.example.apitester.exception.TestDefinitionException;
import com.example.apitester.model.TestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonFileTestCaseRepositoryTest {

    @TempDir
    Path dir;

    private Path write(String content) {
        try {
            return Files.writeString(dir.resolve("tests.json"), content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<TestCase> load(String json) {
        return new JsonFileTestCaseRepository(write(json)).loadAll();
    }

    private void assertInvalid(String json, String expectedFragment) {
        TestDefinitionException e = assertThrows(TestDefinitionException.class, () -> load(json));
        assertTrue(e.getMessage().contains(expectedFragment), "Message was: " + e.getMessage());
    }

    /** Wraps one assertion in an otherwise valid file. */
    private static String withAssertion(String assertionJson) {
        return """
                { "tests": [ {
                    "name": "t",
                    "request": { "path": "/x" },
                    "assertions": [ %s ]
                } ] }
                """.formatted(assertionJson);
    }

    @Test
    void loadsAValidFileAndAppliesDefaults() {
        List<TestCase> cases = load("""
                {
                  "baseUrl": "http://localhost:8080",
                  "tests": [ {
                    "name": "Get user",
                    "request": { "path": "/users/1" },
                    "assertions": [
                      { "type": "STATUS", "expected": 200 },
                      { "type": "JSON_PATH", "path": "$.name", "expected": "Leanne" }
                    ]
                  } ]
                }
                """);

        assertEquals(1, cases.size());
        TestCase tc = cases.get(0);
        assertEquals("GET", tc.request().method());                         // default
        assertEquals("http://localhost:8080/users/1", tc.request().url());  // baseUrl + path
        assertEquals(2, tc.assertions().size());
    }

    @Test
    void objectBodyIsSentAsJsonWithContentType() {
        TestCase tc = load("""
                { "tests": [ {
                    "name": "create",
                    "request": { "method": "post", "path": "/posts", "body": { "title": "hello" } },
                    "assertions": [ { "type": "STATUS", "expected": 201 } ]
                } ] }
                """).get(0);

        assertEquals("POST", tc.request().method());                        // upper-cased
        assertEquals("{\"title\":\"hello\"}", tc.request().body());
        assertEquals("application/json", tc.request().headers().get("Content-Type"));
    }

    @Test
    void absolutePathIgnoresBaseUrl() {
        TestCase tc = load("""
                { "baseUrl": "http://a", "tests": [ {
                    "name": "t",
                    "request": { "path": "http://other/x" },
                    "assertions": [ { "type": "STATUS", "expected": 200 } ]
                } ] }
                """).get(0);

        assertEquals("http://other/x", tc.request().url());
    }

    @Test
    void invalidJsonIsReported() {
        assertInvalid("{ not json", "Invalid JSON");
    }

    @Test
    void missingFileIsReported() {
        var repo = new JsonFileTestCaseRepository(dir.resolve("missing.json"));
        TestDefinitionException e = assertThrows(TestDefinitionException.class, repo::loadAll);
        assertTrue(e.getMessage().contains("Cannot read file"));
    }

    @Test
    void emptyTestsArrayIsRejected() {
        assertInvalid("{ \"tests\": [] }", "non-empty array");
    }

    @Test
    void unknownAssertionTypeIsRejectedWithValidTypesListed() {
        assertInvalid(withAssertion("{ \"type\": \"STATU\", \"expected\": 200 }"), "unknown type 'STATU'");
    }

    @Test
    void nonNumericExpectedStatusIsRejected() {
        assertInvalid(withAssertion("{ \"type\": \"STATUS\", \"expected\": \"abc\" }"), "whole number");
    }

    @Test
    void jsonPathWithoutPathIsRejected() {
        assertInvalid(withAssertion("{ \"type\": \"JSON_PATH\", \"expected\": \"x\" }"),
                "missing required field 'path'");
    }

    @Test
    void unsupportedMethodIsRejected() {
        assertInvalid("""
                { "tests": [ {
                    "name": "t",
                    "request": { "method": "FETCH", "path": "/x" },
                    "assertions": [ { "type": "STATUS", "expected": 200 } ]
                } ] }
                """, "unsupported method");
    }
}
