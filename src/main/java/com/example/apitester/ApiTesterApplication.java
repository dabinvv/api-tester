package com.example.apitester;

import com.example.apitester.assertion.AssertionEvaluator;
import com.example.apitester.assertion.ResponseTimeEvaluator;
import com.example.apitester.assertion.StatusCodeEvaluator;
import com.example.apitester.client.HttpExecutor;
import com.example.apitester.model.ApiRequest;
import com.example.apitester.model.Assertion;
import com.example.apitester.model.AssertionOutcome;
import com.example.apitester.model.AssertionType;
import com.example.apitester.model.TestCase;
import com.example.apitester.model.TestResult;
import com.example.apitester.service.TestRunnerService;

import java.util.List;

public class ApiTesterApplication {

    public static void main(String[] args) {
        // 1. Wire dependencies by hand
        HttpExecutor executor = new HttpExecutor();
        List<AssertionEvaluator> evaluators = List.of(
                new StatusCodeEvaluator(),
                new ResponseTimeEvaluator());
        TestRunnerService service = new TestRunnerService(executor, evaluators);

        // 2. Hardcoded test data (replaced by a JSON file in Phase 4)
        String base = "https://jsonplaceholder.typicode.com";
        List<TestCase> testCases = List.of(
                new TestCase("Get user 1 (should pass)",
                        ApiRequest.get(base + "/users/1"),
                        List.of(Assertion.of(AssertionType.STATUS, "200"),
                                Assertion.of(AssertionType.MAX_TIME_MS, "3000"))),

                new TestCase("Missing user returns 404 (should pass)",
                        ApiRequest.get(base + "/users/9999"),
                        List.of(Assertion.of(AssertionType.STATUS, "404"))),

                new TestCase("Wrong expectation (should fail)",
                        ApiRequest.get(base + "/users/1"),
                        List.of(Assertion.of(AssertionType.STATUS, "201"))),

                new TestCase("Unreachable host (should error)",
                        ApiRequest.get("https://nonexistent.invalid/users/1"),
                        List.of(Assertion.of(AssertionType.STATUS, "200"))),

                new TestCase("Malformed URL (should error)",
                        ApiRequest.get("not a url"),
                        List.of(Assertion.of(AssertionType.STATUS, "200")))
        );

        // 3. Run and print (ConsoleReporter replaces this in Phase 5)
        List<TestResult> results = service.run(testCases);
        for (TestResult result : results) {
            System.out.println((result.passed() ? "[PASS] " : "[FAIL] ") + result.testName());
            if (result.error() != null) {
                System.out.println("       ! " + result.error());
            }
            for (AssertionOutcome outcome : result.outcomes()) {
                System.out.println("       " + (outcome.passed() ? "✓ " : "✗ ") + outcome.message());
            }
        }

        long passed = results.stream().filter(TestResult::passed).count();
        System.out.println("\n" + passed + "/" + results.size() + " tests passed");
    }
}