package com.example.apitester.service;

import com.example.apitester.assertion.AssertionEvaluator;
import com.example.apitester.client.HttpExecutor;
import com.example.apitester.model.ApiResponse;
import com.example.apitester.model.Assertion;
import com.example.apitester.model.AssertionOutcome;
import com.example.apitester.model.TestCase;
import com.example.apitester.model.TestResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestRunnerService {

    private final HttpExecutor executor;
    private final List<AssertionEvaluator> evaluators;

    // Dependencies are passed in, not created here (manual dependency injection)
    public TestRunnerService(HttpExecutor executor, List<AssertionEvaluator> evaluators) {
        this.executor = executor;
        this.evaluators = evaluators;
    }

    public List<TestResult> run(List<TestCase> testCases) {
        List<TestResult> results = new ArrayList<>();
        for (TestCase testCase : testCases) {
            results.add(runOne(testCase));
        }
        return results;
    }

    private TestResult runOne(TestCase testCase) {
        ApiResponse response;
        try {
            response = executor.send(testCase.request());
        } catch (IOException e) {
            // Covers timeouts, connection refused, unknown host
            return TestResult.errored(testCase.name(), "Request failed: " + e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // restore the flag, never swallow it
            return TestResult.errored(testCase.name(), "Interrupted");
        } catch (IllegalArgumentException e) {
            // Thrown by URI.create / HttpRequest for a malformed URL or method
            return TestResult.errored(testCase.name(), "Invalid request: " + e.getMessage());
        }

        List<AssertionOutcome> outcomes = new ArrayList<>();
        for (Assertion assertion : testCase.assertions()) {
            outcomes.add(evaluate(assertion, response));
        }
        return TestResult.completed(testCase.name(), outcomes);
    }

    private AssertionOutcome evaluate(Assertion assertion, ApiResponse response) {
        return evaluators.stream()
                .filter(e -> e.supports(assertion.type()))
                .findFirst()
                .map(e -> e.evaluate(assertion, response))
                .orElse(AssertionOutcome.fail("No evaluator for " + assertion.type()));
    }
}