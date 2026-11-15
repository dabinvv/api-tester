package com.example.apitester.report;

import com.example.apitester.model.AssertionOutcome;
import com.example.apitester.model.TestResult;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleReporterTest {

    private final List<TestResult> results = List.of(
            TestResult.completed("Good test", List.of(AssertionOutcome.pass("Status is 200"))),
            TestResult.completed("Bad test", List.of(AssertionOutcome.fail("Expected status 201 but got 200"))),
            TestResult.errored("Broken test", "Request failed: boom"));

    private String render(boolean color, long elapsedMs) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        new ConsoleReporter(out, color).print(results, elapsedMs);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void printsPassAndFailLinesWithDetails() {
        String output = render(false, 1500);

        assertTrue(output.contains("[PASS] Good test"));
        assertTrue(output.contains("[FAIL] Bad test"));
        assertTrue(output.contains("Expected status 201 but got 200"));
        assertTrue(output.contains("Request failed: boom"));
    }

    @Test
    void summaryHasCountsAndTime() {
        String output = render(false, 1500);

        assertTrue(output.contains("Passed: 1   Failed: 2   Total: 3   Time: 1.50 s"));
        assertTrue(output.contains("Failed tests:"));
    }

    @Test
    void noEscapeCodesWhenColorIsOff() {
        assertFalse(render(false, 10).contains("\u001B["));
    }

    @Test
    void escapeCodesPresentWhenColorIsOn() {
        assertTrue(render(true, 10).contains("\u001B["));
    }
}
