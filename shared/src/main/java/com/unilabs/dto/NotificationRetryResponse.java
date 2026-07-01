package com.unilabs.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resposta de reenvio de job falhado")
public class NotificationRetryResponse {

    @Schema(description = "ID do job reenviado")
    private UUID jobId;

    @Schema(description = "Novo estado", example = "PENDING")
    private String status;

    @Schema(description = "Número de tentativas de reenvio", example = "1")
    private int retryCount;

    public NotificationRetryResponse() {}

    public NotificationRetryResponse(UUID jobId, String status, int retryCount) {
        this.jobId = jobId;
        this.status = status;
        this.retryCount = retryCount;
    }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
}
