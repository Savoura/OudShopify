package com.oud.oudshopify.backend.network;

import java.io.IOException;
import java.net.Authenticator;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpExecutor {
    private final HttpClient httpClient;
    private static HttpExecutor instance;

    private HttpExecutor() {
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public static synchronized HttpExecutor getInstance() {
        if (instance == null)
            instance = new HttpExecutor();
        return instance;
    }

    public HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
