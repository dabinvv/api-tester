package com.example.apitester.assertion;

import com.example.apitester.model.ApiResponse;
import com.example.apitester.model.Assertion;
import com.example.apitester.model.AssertionOutcome;
import com.example.apitester.model.AssertionType;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.PathNotFoundException;

public class JsonPathEvaluator implements AssertionEvaluator {

    @Override
    public boolean supports(AssertionType type) {
        return type == AssertionType.JSON_PATH;
    }

    @Override
    public AssertionOutcome evaluate(Assertion assertion, ApiResponse response) {
        String path = assertion.path();
        try {
            Object actual = JsonPath.read(response.body(), path);

            // Compare as text, so 1 matches "1" and true matches "true"
            if (String.valueOf(actual).equals(assertion.expected())) {
                return AssertionOutcome.pass(path + " is " + actual);
            }
            return AssertionOutcome.fail("Expected " + path + " to be '" + assertion.expected()
                    + "' but got '" + actual + "'");

        } catch (PathNotFoundException e) {
            return AssertionOutcome.fail("Path " + path + " not found in response body");
        } catch (JsonPathException e) {
            return AssertionOutcome.fail("Could not evaluate " + path + ": " + e.getMessage());
        }
    }
}