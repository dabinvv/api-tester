package com.example.apitester;

import com.example.apitester.assertion.AssertionEvaluator;
import com.example.apitester.assertion.JsonPathEvaluator;
import com.example.apitester.assertion.ResponseTimeEvaluator;
import com.example.apitester.assertion.StatusCodeEvaluator;
import com.example.apitester.cli.CliHandler;
import com.example.apitester.cli.CliOptions;
import com.example.apitester.client.HttpExecutor;
import com.example.apitester.exception.CliException;
import com.example.apitester.exception.TestDefinitionException;
import com.example.apitester.model.TestCase;
import com.example.apitester.model.TestResult;
import com.example.apitester.report.ConsoleReporter;
import com.example.apitester.repository.JsonFileTestCaseRepository;
import com.example.apitester.repository.TestCaseRepository;
import com.example.apitester.service.TestRunnerService;

import java.util.List;

public class ApiTesterApplication {

    static final int EXIT_OK = 0;
    static final int EXIT_TESTS_FAILED = 1;
    static final int EXIT_USAGE = 2;

    public static void main(String[] args) {
        System.exit(run(args));   // the only System.exit in the program
    }

    static int run(String[] args) {
        // 1. Parse arguments
        CliOptions options;
        try {
            options = CliHandler.parse(args);
        } catch (CliException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            System.err.println(CliHandler.USAGE);
            return EXIT_USAGE;
        }

        if (options.helpRequested()) {
            System.out.println(CliHandler.USAGE);
            return EXIT_OK;
        }

        // 2. Wire dependencies by hand
        TestCaseRepository repository = new JsonFileTestCaseRepository(options.file());
        HttpExecutor executor = new HttpExecutor();
        List<AssertionEvaluator> evaluators = List.of(
                new StatusCodeEvaluator(),
                new ResponseTimeEvaluator(),
                new JsonPathEvaluator());
        TestRunnerService service = new TestRunnerService(executor, evaluators);
        ConsoleReporter reporter = new ConsoleReporter(System.out, options.color());

        // 3. Load
        List<TestCase> testCases;
        try {
            testCases = repository.loadAll();
        } catch (TestDefinitionException e) {
            System.err.println("Invalid test file: " + e.getMessage());
            return EXIT_USAGE;
        }

        // 4. Run, report, and translate the outcome into an exit code
        long start = System.nanoTime();
        List<TestResult> results = service.run(testCases);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        reporter.print(results, elapsedMs);

        boolean allPassed = results.stream().allMatch(TestResult::passed);
        return allPassed ? EXIT_OK : EXIT_TESTS_FAILED;
    }
}
