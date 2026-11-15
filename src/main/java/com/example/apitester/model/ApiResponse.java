package com.example.apitester.model;

import java.util.List;
import java.util.Map;

public record ApiResponse(
        int statusCode,
        Map<String, List<String>> headers,
        String body,
        long durationMs) {
}