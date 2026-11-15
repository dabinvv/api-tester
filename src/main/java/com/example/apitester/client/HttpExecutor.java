package com.example.apitester.client;

import com.example.apitester.model.ApiRequest;
import com.example.apitester.model.ApiResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

public class HttpExecutor {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    // One client reused for all requests (it is thread-safe and pools connections)
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    public ApiResponse send(ApiRequest request) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(request.url()))
                .timeout(TIMEOUT);

        request.headers().forEach(builder::header);

        HttpRequest.BodyPublisher body = request.body() == null
                ? BodyPublishers.noBody()
                : BodyPublishers.ofString(request.body());
        builder.method(request.method(), body);

        long start = System.nanoTime();
        HttpResponse<String> response = client.send(builder.build(), BodyHandlers.ofString());
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        return new ApiResponse(
                response.statusCode(),
                response.headers().map(),
                response.body(),
                durationMs);
    }
}