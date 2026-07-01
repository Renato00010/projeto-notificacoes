package com.unilabs.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resposta de criação de notificação (HTTP 202)")
public class NotificationCreateResponse {

    @Schema(description = "Identificador único do job criado", example = "f784e1b8-6a31-4cfa-81a1-cf4939ff7b2b")
    private UUID jobId;

    @Schema(description = "Estado inicial do job", example = "PENDING")
    private String status;

    public NotificationCreateResponse() {}

    public NotificationCreateResponse(UUID jobId, String status) {
        this.jobId = jobId;
        this.status = status;
    }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
