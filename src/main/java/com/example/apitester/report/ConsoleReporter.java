package com.example.apitester.report;

import com.example.apitester.model.AssertionOutcome;
import com.example.apitester.model.TestResult;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

public class ConsoleReporter {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String DIM = "\u001B[2m";
    private static final String BOLD = "\u001B[1m";

    private final PrintStream out;
    private final boolean color;

    public ConsoleReporter(PrintStream out, boolean color) {
        this.out = out;
        this.color = color;
    }

    public void print(List<TestResult> results, long elapsedMs) {
        for (TestResult result : results) {
            printResult(result);
        }
        printSummary(results, elapsedMs);
    }

    private void printResult(TestResult result) {
        String badge = result.passed() ? paint(GREEN, "[PASS]") : paint(RED, "[FAIL]");
        out.println(badge + " " + result.testName());

        if (result.error() != null) {
            out.println("       " + paint(RED, "! " + result.error()));
        }
        for (AssertionOutcome outcome : result.outcomes()) {
            String line = outcome.passed()
                    ? paint(DIM, "✓ " + outcome.message())
                    : paint(RED, "✗ " + outcome.message());
            out.println("       " + line);
        }
    }

    private void printSummary(List<TestResult> results, long elapsedMs) {
        List<TestResult> failed = results.stream().filter(r -> !r.passed()).toList();
        int passedCount = results.size() - failed.size();

        out.println();
        if (!failed.isEmpty()) {
            out.println(paint(BOLD, "Failed tests:"));
            failed.forEach(r -> out.println("  - " + r.testName()));
            out.println();
        }

        String counts = String.format(Locale.ROOT, "Passed: %d   Failed: %d   Total: %d   Time: %.2f s",
                passedCount, failed.size(), results.size(), elapsedMs / 1000.0);
        out.println(failed.isEmpty() ? paint(GREEN, counts) : paint(RED, counts));
    }

    private String paint(String ansiCode, String text) {
        return color ? ansiCode + text + RESET : text;
    }
}
