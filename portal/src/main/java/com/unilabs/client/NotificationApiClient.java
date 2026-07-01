package com.unilabs.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unilabs.dto.NotificationCountResponse;
import com.unilabs.dto.NotificationFilterResponse;
import com.unilabs.dto.NotificationRequest;
import com.unilabs.dto.NotificationRetryResponse;
import com.unilabs.dto.NotificationStatsResponse;
import com.unilabs.dto.NotificationTemplateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class NotificationApiClient {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final String backendBaseUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;

    public NotificationApiClient(
            @Value("${notification.backend.url:http://localhost:8080}") String backendBaseUrl,
            ObjectMapper objectMapper,
            @Value("${notification.api.key:unilabs-secret-key-2026}") String apiKey) {
        this.backendBaseUrl = backendBaseUrl.replaceAll("/$", "");
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.apiKey = apiKey;
    }

    public UUID createNotification(NotificationRequest request) throws Exception {
        String jsonPayload = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(backendBaseUrl + "/api/v1/notifications"))
                .header("Content-Type", "application/json")
                .header("X-API-Key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 202) {
            Map<String, Object> body = objectMapper.readValue(response.body(), new TypeReference<>() {});
            return UUID.fromString((String) body.get("jobId"));
        }

        throw new RuntimeException(extractErrorMessage(response));
    }

    public NotificationStatsResponse getStats() throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(backendBaseUrl + "/api/v1/notifications/stats"))
                .header("X-API-Key", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), NotificationStatsResponse.class);
        }

        throw new RuntimeException(extractErrorMessage(response));
    }

    public List<NotificationFilterResponse> searchNotifications(String recipient, String channelType,
                                                                String clientId, String status) throws Exception {
        return searchNotifications(recipient, channelType, clientId, status, null, null);
    }

    public List<NotificationFilterResponse> searchNotifications(String recipient, String channelType,
                                                                String clientId, String status,
                                                                LocalDateTime from, LocalDateTime to) throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(backendBaseUrl + "/api/v1/notifications/filter"
                        + buildQuery(recipient, channelType, clientId, status, from, to)))
                .header("X-API-Key", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<>() {});
        }

        throw new RuntimeException(extractErrorMessage(response));
    }

    public NotificationCountResponse countNotifications(String recipient, String channelType,
                                                        String clientId, String status) throws Exception {
        return countNotifications(recipient, channelType, clientId, status, null, null);
    }

    public NotificationCountResponse countNotifications(String recipient, String channelType,
                                                        String clientId, String status,
                                                        LocalDateTime from, LocalDateTime to) throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(backendBaseUrl + "/api/v1/notifications/filter/count"
                        + buildQuery(recipient, channelType, clientId, status, from, to)))
                .header("X-API-Key", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), NotificationCountResponse.class);
        }

        throw new RuntimeException(extractErrorMessage(response));
    }

    public List<NotificationTemplateResponse> listTemplates(String channelType) throws Exception {
        String url = backendBaseUrl + "/api/v1/templates";
        if (channelType != null && !channelType.isBlank()) {
            url += "?channelType=" + encode(channelType.trim());
        }

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-API-Key", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<>() {});
        }

        throw new RuntimeException(extractErrorMessage(response));
    }

    public NotificationRetryResponse retryJob(UUID jobId) throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(backendBaseUrl + "/api/v1/notifications/" + jobId + "/retry"))
                .header("X-API-Key", apiKey)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), NotificationRetryResponse.class);
        }

        throw new RuntimeException(extractErrorMessage(response));
    }

    private String buildQuery(String recipient, String channelType, String clientId, String status,
                               LocalDateTime from, LocalDateTime to) {
        Map<String, String> params = new LinkedHashMap<>();
        if (recipient != null && !recipient.isBlank()) params.put("recipient", recipient.trim());
        if (channelType != null && !channelType.isBlank()) params.put("channelType", channelType.trim());
        if (clientId != null && !clientId.isBlank()) params.put("clientId", clientId.trim());
        if (status != null && !status.isBlank()) params.put("status", status.trim());
        if (from != null) params.put("from", from.format(ISO));
        if (to != null) params.put("to", to.format(ISO));

        if (params.isEmpty()) return "";

        return "?" + params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String extractErrorMessage(HttpResponse<String> response) {
        String fallback = "Erro no servidor (HTTP " + response.statusCode() + ")";
        try {
            Map<String, Object> body = objectMapper.readValue(response.body(), new TypeReference<>() {});
            if (body.containsKey("message")) {
                return (String) body.get("message");
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }
}
