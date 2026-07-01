package com.unilabs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unilabs.dto.NotificationRequest;
import com.unilabs.repository.NotificationJobRepository;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CallbackService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final NotificationJobRepository jobRepository;

    public CallbackService(ObjectMapper objectMapper, NotificationJobRepository jobRepository) {
        this.objectMapper = objectMapper;
        this.jobRepository = jobRepository;
    }

    public void sendCallback(NotificationRequest request, String status, String errorMessage) {
        if (request.getCallbackUrl() == null || request.getCallbackUrl().isBlank()) {
            updateWebhookStatus(request.getJobId(), "NONE", "Sem URL de callback configurada");
            return;
        }

        updateWebhookStatus(request.getJobId(), "PENDING", "A enviar callback...");

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("jobId", request.getJobId());
            body.put("status", status);
            body.put("channelType", request.getChannelType().name());
            body.put("recipient", request.getRecipient());
            body.put("errorMessage", errorMessage);

            HttpRequest callbackRequest = HttpRequest.newBuilder()
                    .uri(URI.create(request.getCallbackUrl()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            httpClient.sendAsync(callbackRequest, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        int statusCode = response.statusCode();
                        if (statusCode >= 200 && statusCode < 300) {
                            updateWebhookStatus(request.getJobId(), "SUCCESS", "HTTP " + statusCode);
                        } else {
                            String responseBody = response.body();
                            String msg = "HTTP " + statusCode;
                            if (responseBody != null && !responseBody.isBlank()) {
                                msg += " - " + responseBody;
                            }
                            updateWebhookStatus(request.getJobId(), "FAILED", msg);
                        }
                    })
                    .exceptionally(error -> {
                        updateWebhookStatus(request.getJobId(), "FAILED", error.getMessage());
                        System.err.println("Callback failed for job " + request.getJobId() + ": " + error.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            updateWebhookStatus(request.getJobId(), "FAILED", e.getMessage());
            System.err.println("Callback could not be prepared for job " + request.getJobId() + ": " + e.getMessage());
        }
    }

    private void updateWebhookStatus(UUID jobId, String status, String responseMsg) {
        if (jobId == null) {
            return;
        }
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setWebhookStatus(status);
            if (responseMsg != null && responseMsg.length() > 255) {
                job.setWebhookResponse(responseMsg.substring(0, 252) + "...");
            } else {
                job.setWebhookResponse(responseMsg);
            }
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
        });
    }
}
