package com.example.apitester;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
class EndToEndTest {

    @TempDir
    Path dir;

    private record RunOutput(int exitCode, String stdout) {
    }

    /** Runs the real application logic and captures what it prints to stdout. */
    private RunOutput run(String... args) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            int code = ApiTesterApplication.run(args);
            return new RunOutput(code, buffer.toString(StandardCharsets.UTF_8));
        } finally {
            System.setOut(original);
        }
    }

    private Path writeTests(String json) throws IOException {
        return Files.writeString(dir.resolve("tests.json"), json);
    }

    @Test
    void allTestsPassing_exitsWithZero(WireMockRuntimeInfo wm) throws IOException {
        stubFor(get("/users/1").willReturn(okJson("{\"name\":\"Leanne\"}")));
        Path file = writeTests("""
                {
                  "baseUrl": "%s",
                  "tests": [ {
                    "name": "Get user",
                    "request": { "path": "/users/1" },
                    "assertions": [
                      { "type": "STATUS", "expected": 200 },
                      { "type": "JSON_PATH", "path": "$.name", "expected": "Leanne" }
                    ]
                  } ]
                }
                """.formatted(wm.getHttpBaseUrl()));

        RunOutput result = run("--no-color", file.toString());

        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().contains("[PASS] Get user"));
    }

    @Test
    void failingAssertion_exitsWithOne(WireMockRuntimeInfo wm) throws IOException {
        stubFor(get("/users/1").willReturn(okJson("{}")));
        Path file = writeTests("""
                {
                  "baseUrl": "%s",
                  "tests": [ {
                    "name": "Expects wrong status",
                    "request": { "path": "/users/1" },
                    "assertions": [ { "type": "STATUS", "expected": 404 } ]
                  } ]
                }
                """.formatted(wm.getHttpBaseUrl()));

        RunOutput result = run("--no-color", file.toString());

        assertEquals(1, result.exitCode());
        assertTrue(result.stdout().contains("[FAIL] Expects wrong status"));
    }

    @Test
    void postSendsJsonBodyAndContentType(WireMockRuntimeInfo wm) throws IOException {
        stubFor(post("/posts").willReturn(created().withBody("{\"title\":\"hello\"}")));
        Path file = writeTests("""
                {
                  "baseUrl": "%s",
                  "tests": [ {
                    "name": "Create post",
                    "request": { "method": "POST", "path": "/posts", "body": { "title": "hello" } },
                    "assertions": [ { "type": "STATUS", "expected": 201 } ]
                  } ]
                }
                """.formatted(wm.getHttpBaseUrl()));

        RunOutput result = run("--no-color", file.toString());

        assertEquals(0, result.exitCode());
        verify(postRequestedFor(urlEqualTo("/posts"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("{\"title\":\"hello\"}")));
    }

    @Test
    void unreachableServer_isReportedAsFailureNotACrash() throws IOException {
        Path file = writeTests("""
                {
                  "baseUrl": "http://localhost:1",
                  "tests": [ {
                    "name": "Nobody home",
                    "request": { "path": "/x" },
                    "assertions": [ { "type": "STATUS", "expected": 200 } ]
                  } ]
                }
                """);

        RunOutput result = run("--no-color", file.toString());

        assertEquals(1, result.exitCode());
        assertTrue(result.stdout().contains("Request failed"));
    }

    @Test
    void invalidTestFile_exitsWithTwoAndSendsNoRequests() throws IOException {
        Path file = writeTests("{ \"tests\": [] }");

        RunOutput result = run("--no-color", file.toString());

        assertEquals(2, result.exitCode());
        verify(0, anyRequestedFor(anyUrl()));      // validation stopped everything before any HTTP call
    }

    @Test
    void helpExitsWithZero() {
        assertEquals(0, run("--help").exitCode());
    }
}
