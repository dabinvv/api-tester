package com.example.apitester.repository;

import com.example.apitester.exception.TestDefinitionException;
import com.example.apitester.model.ApiRequest;
import com.example.apitester.model.Assertion;
import com.example.apitester.model.AssertionType;
import com.example.apitester.model.TestCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonFileTestCaseRepository implements TestCaseRepository {

    private static final Set<String> METHODS =
            Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");

    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonFileTestCaseRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<TestCase> loadAll() {
        JsonNode root = readTree();

        String baseUrl = root.hasNonNull("baseUrl") ? root.get("baseUrl").asText() : "";
        JsonNode tests = root.get("tests");
        if (tests == null || !tests.isArray() || tests.isEmpty()) {
            throw new TestDefinitionException("'tests' must be a non-empty array");
        }

        List<TestCase> result = new ArrayList<>();
        for (int i = 0; i < tests.size(); i++) {
            result.add(parseTest(tests.get(i), baseUrl, "tests[" + i + "]"));
        }
        return result;
    }

    private JsonNode readTree() {
        try {
            return mapper.readTree(file.toFile());
        } catch (JsonProcessingException e) {   // must come before IOException (it is a subclass)
            throw new TestDefinitionException(
                    "Invalid JSON in " + file + ": " + e.getOriginalMessage()
                            + " (line " + e.getLocation().getLineNr() + ")", e);
        } catch (IOException e) {
            throw new TestDefinitionException("Cannot read file " + file + ": " + e.getMessage(), e);
        }
    }

    private TestCase parseTest(JsonNode node, String baseUrl, String where) {
        requireObject(node, where);
        String name = requireText(node, "name", where);

        JsonNode req = node.get("request");
        requireObject(req, where + ".request");

        String method = req.hasNonNull("method") ? req.get("method").asText().toUpperCase() : "GET";
        if (!METHODS.contains(method)) {
            throw new TestDefinitionException(where + ".request.method: unsupported method '" + method + "'");
        }

        String path = requireText(req, "path", where + ".request");
        String url = path.startsWith("http") ? path : baseUrl + path;

        Map<String, String> headers = new LinkedHashMap<>();
        if (req.hasNonNull("headers")) {
            req.get("headers").fields().forEachRemaining(e -> headers.put(e.getKey(), e.getValue().asText()));
        }

        String body = null;
        if (req.hasNonNull("body")) {
            JsonNode b = req.get("body");
            if (b.isContainerNode()) {                 // object or array: send as JSON
                body = b.toString();
                headers.putIfAbsent("Content-Type", "application/json");
            } else {
                body = b.asText();
            }
        }

        JsonNode assertionNodes = node.get("assertions");
        if (assertionNodes == null || !assertionNodes.isArray() || assertionNodes.isEmpty()) {
            throw new TestDefinitionException(where + ".assertions must be a non-empty array");
        }
        List<Assertion> assertions = new ArrayList<>();
        for (int i = 0; i < assertionNodes.size(); i++) {
            assertions.add(parseAssertion(assertionNodes.get(i), where + ".assertions[" + i + "]"));
        }

        return new TestCase(name, new ApiRequest(method, url, headers, body), assertions);
    }

    private Assertion parseAssertion(JsonNode node, String where) {
        requireObject(node, where);

        String typeText = requireText(node, "type", where);
        AssertionType type;
        try {
            type = AssertionType.valueOf(typeText);
        } catch (IllegalArgumentException e) {
            throw new TestDefinitionException(where + ".type: unknown type '" + typeText
                    + "'. Valid types: " + Arrays.toString(AssertionType.values()));
        }

        String expected = requireText(node, "expected", where);

        switch (type) {
            case STATUS, MAX_TIME_MS -> {
                try {
                    Long.parseLong(expected);
                } catch (NumberFormatException e) {
                    throw new TestDefinitionException(
                            where + ".expected: must be a whole number for " + type + ", got '" + expected + "'");
                }
                return Assertion.of(type, expected);
            }
            case JSON_PATH -> {
                String path = requireText(node, "path", where);
                try {
                    JsonPath.compile(path);            // fail early on a malformed path
                } catch (InvalidPathException e) {
                    throw new TestDefinitionException(where + ".path: invalid JSON path '" + path + "'");
                }
                return new Assertion(type, path, expected);
            }
            default -> throw new IllegalStateException("Unhandled type " + type);
        }
    }

    // ---- small validation helpers ----

    private static void requireObject(JsonNode node, String where) {
        if (node == null || !node.isObject()) {
            throw new TestDefinitionException(where + " must be an object");
        }
    }

    private static String requireText(JsonNode node, String field, String where) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || !value.isValueNode()) {
            throw new TestDefinitionException(where + ": missing required field '" + field + "'");
        }
        return value.asText();
    }
}