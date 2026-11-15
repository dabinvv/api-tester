package com.example.apitester.service;

import com.example.apitester.assertion.StatusCodeEvaluator;
import com.example.apitester.client.HttpExecutor;
import com.example.apitester.model.ApiRequest;
import com.example.apitester.model.ApiResponse;
import com.example.apitester.model.Assertion;
import com.example.apitester.model.AssertionType;
import com.example.apitester.model.TestCase;
import com.example.apitester.model.TestResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TestRunnerServiceTest {

    private final HttpExecutor executor = mock(HttpExecutor.class);
    private final TestRunnerService service =
            new TestRunnerService(executor, List.of(new StatusCodeEvaluator()));

    private static TestCase testCase(String name, String url, int expectedStatus) {
        return new TestCase(name, ApiRequest.get(url),
                List.of(Assertion.of(AssertionType.STATUS, String.valueOf(expectedStatus))));
    }

    private static ApiResponse response(int status) {
        return new ApiResponse(status, Map.of(), "", 5);
    }

    @Test
    void passingTestProducesPassedResult() throws Exception {
        TestCase tc = testCase("ok", "http://x/1", 200);
        when(executor.send(tc.request())).thenReturn(response(200));

        List<TestResult> results = service.run(List.of(tc));

        assertEquals(1, results.size());
        assertTrue(results.get(0).passed());
    }

    @Test
    void wrongStatusProducesFailedResultNotAnError() throws Exception {
        TestCase tc = testCase("wrong", "http://x/1", 201);
        when(executor.send(tc.request())).thenReturn(response(200));

        TestResult result = service.run(List.of(tc)).get(0);

        assertFalse(result.passed());
        assertNull(result.error());                       // a response arrived, so no error
        assertEquals(1, result.outcomes().size());
    }

    @Test
    void networkFailureBecomesErroredResultAndDoesNotStopTheRun() throws Exception {
        TestCase first = testCase("down", "http://x/down", 200);
        TestCase second = testCase("up", "http://x/up", 200);
        when(executor.send(first.request())).thenThrow(new IOException("boom"));
        when(executor.send(second.request())).thenReturn(response(200));

        List<TestResult> results = service.run(List.of(first, second));

        assertFalse(results.get(0).passed());
        assertTrue(results.get(0).error().contains("boom"));
        assertTrue(results.get(1).passed());              // the second test still ran
        verify(executor, times(2)).send(any());
    }

    @Test
    void malformedRequestBecomesErroredResult() throws Exception {
        when(executor.send(any())).thenThrow(new IllegalArgumentException("bad uri"));

        TestResult result = service.run(List.of(testCase("bad", "not a url", 200))).get(0);

        assertTrue(result.error().startsWith("Invalid request"));
    }

    @Test
    void interruptionIsReportedAndTheInterruptFlagIsRestored() throws Exception {
        when(executor.send(any())).thenThrow(new InterruptedException());

        TestResult result = service.run(List.of(testCase("t", "http://x", 200))).get(0);

        assertEquals("Interrupted", result.error());
        assertTrue(Thread.interrupted());    // true, and this call also clears the flag for other tests
    }

    @Test
    void missingEvaluatorFailsTheAssertionInsteadOfCrashing() throws Exception {
        TestRunnerService noEvaluators = new TestRunnerService(executor, List.of());
        TestCase tc = testCase("t", "http://x", 200);
        when(executor.send(tc.request())).thenReturn(response(200));

        TestResult result = noEvaluators.run(List.of(tc)).get(0);

        assertFalse(result.passed());
        assertEquals("No evaluator for STATUS", result.outcomes().get(0).message());
    }
}
