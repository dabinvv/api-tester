package com.example.apitester.model;

import java.util.List;

public record TestCase(String name, ApiRequest request, List<Assertion> assertions) {
}