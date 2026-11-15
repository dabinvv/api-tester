package com.example.apitester.assertion;

import com.example.apitester.model.ApiResponse;
import com.example.apitester.model.Assertion;
import com.example.apitester.model.AssertionOutcome;
import com.example.apitester.model.AssertionType;

public interface AssertionEvaluator {

    /** Which assertion type this evaluator handles. */
    boolean supports(AssertionType type);

    AssertionOutcome evaluate(Assertion assertion, ApiResponse response);
}