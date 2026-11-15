package com.example.apitester.model;

import java.util.Map;

public record ApiRequest(
        String method,
        String url,
        Map<String, String> headers,
        String body) {

    /** Convenience factory for a simple GET. */
    public static ApiRequest get(String url) {
        return new ApiRequest("GET", url, Map.of(), null);
    }
}