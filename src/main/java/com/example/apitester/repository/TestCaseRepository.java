package com.example.apitester.repository;

import com.example.apitester.model.TestCase;

import java.util.List;

public interface TestCaseRepository {

    /** @throws com.example.apitester.exception.TestDefinitionException if the source is invalid */
    List<TestCase> loadAll();
}