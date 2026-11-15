package com.example.apitester;

import com.example.apitester.assertion.AssertionEvaluator;
import com.example.apitester.assertion.ResponseTimeEvaluator;
import com.example.apitester.assertion.StatusCodeEvaluator;
import com.example.apitester.client.HttpExecutor;
import com.example.apitester.model.ApiRequest;
import com.example.apitester.model.ApiResponse;
import com.example.apitester.model.Assertion;
import com.example.apitester.model.AssertionOutcome;
import com.example.apitester.model.AssertionType;

import java.util.List;

public class ApiTesterApplication {

    public static void main(String[] args) throws Exception {
        HttpExecutor executor = new HttpExecutor();
        List<AssertionEvaluator> evaluators = List.of(
                new StatusCodeEvaluator(),
                new ResponseTimeEvaluator());

        ApiRequest request = ApiRequest.get("https://jsonplaceholder.typicode.com/users/1");
        List<Assertion> assertions = List.of(
                Assertion.of(AssertionType.STATUS, "200"),
                Assertion.of(AssertionType.MAX_TIME_MS, "1000"));

        ApiResponse response = executor.send(request);

        for (Assertion assertion : assertions) {
            AssertionEvaluator evaluator = evaluators.stream()
                    .filter(e -> e.supports(assertion.type()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No evaluator for " + assertion.type()));

            AssertionOutcome outcome = evaluator.evaluate(assertion, response);
            System.out.println((outcome.passed() ? "PASS  " : "FAIL  ") + outcome.message());
        }
    }
}