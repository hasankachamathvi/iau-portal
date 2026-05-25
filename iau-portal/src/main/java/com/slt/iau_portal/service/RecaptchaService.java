package com.slt.iau_portal.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RecaptchaService {

    @Value("${recaptcha.secret:}")
    private String secret;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public boolean verify(String token) {
        if (token == null || token.isBlank() || secret == null || secret.isBlank()) {
            return false;
        }

        try {
            String form = "secret=" + java.net.URLEncoder.encode(secret, java.nio.charset.StandardCharsets.UTF_8)
                    + "&response=" + java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.google.com/recaptcha/api/siteverify"))
                    .timeout(Duration.ofSeconds(6))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return false;
            }

            JsonNode json = mapper.readTree(response.body());
            return json.path("success").asBoolean(false);
        } catch (Exception e) {
            // treat errors as failed verification
            return false;
        }
    }
}
