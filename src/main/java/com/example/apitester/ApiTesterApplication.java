package com.example.apitester;

import com.example.apitester.assertion.AssertionEvaluator;
import com.example.apitester.assertion.JsonPathEvaluator;
import com.example.apitester.assertion.ResponseTimeEvaluator;
import com.example.apitester.assertion.StatusCodeEvaluator;
import com.example.apitester.client.HttpExecutor;
import com.example.apitester.exception.TestDefinitionException;
import com.example.apitester.model.AssertionOutcome;
import com.example.apitester.model.TestCase;
import com.example.apitester.model.TestResult;
import com.example.apitester.repository.JsonFileTestCaseRepository;
import com.example.apitester.repository.TestCaseRepository;
import com.example.apitester.service.TestRunnerService;

import java.nio.file.Path;
import java.util.List;

public class ApiTesterApplication {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar api-tester.jar <tests.json>");
            System.exit(2);
        }

        // 1. Wire dependencies by hand
        TestCaseRepository repository = new JsonFileTestCaseRepository(Path.of(args[0]));
        HttpExecutor executor = new HttpExecutor();
        List<AssertionEvaluator> evaluators = List.of(
                new StatusCodeEvaluator(),
                new ResponseTimeEvaluator(),
                new JsonPathEvaluator());
        TestRunnerService service = new TestRunnerService(executor, evaluators);

        // 2. Load
        List<TestCase> testCases;
        try {
            testCases = repository.loadAll();
        } catch (TestDefinitionException e) {
            System.err.println("Invalid test file: " + e.getMessage());
            System.exit(2);
            return;
        }

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