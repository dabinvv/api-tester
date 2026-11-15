package com.example.apitester;

import com.example.apitester.client.HttpExecutor;
import com.example.apitester.model.ApiRequest;
import com.example.apitester.model.ApiResponse;

public class ApiTesterApplication {

    public static void main(String[] args) throws Exception {
        HttpExecutor executor = new HttpExecutor();
        ApiRequest request = ApiRequest.get("https://jsonplaceholder.typicode.com/users/1");

        ApiResponse response = executor.send(request);

        System.out.println("Status:   " + response.statusCode());
        System.out.println("Duration: " + response.durationMs() + " ms");
        System.out.println("Body:     " + response.body());
    }
}