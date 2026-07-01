package com.unilabs.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Estado atual de um job de notificação")
public class NotificationStatusResponse {

    @Schema(description = "ID do job", example = "f784e1b8-6a31-4cfa-81a1-cf4939ff7b2b")
    private UUID jobId;

    @Schema(description = "Estado do processamento", example = "SUCCESS")
    private String status;

    @Schema(description = "Canal de envio (disponível após processamento)", example = "SMS")
    private String channelType;

    @Schema(description = "Destinatário (disponível após processamento)", example = "+351912345678")
    private String recipient;

    @Schema(description = "Template utilizado", example = "template_resultados_exame")
    private String templateName;

    @Schema(description = "Tentativas de reenvio realizadas", example = "0")
    private int retryCount;

    @Schema(description = "Última atualização do job")
    private LocalDateTime updatedAt;

    public NotificationStatusResponse() {}

    public NotificationStatusResponse(UUID jobId, String status, LocalDateTime updatedAt) {
        this.jobId = jobId;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public NotificationStatusResponse(UUID jobId, String status, String channelType, String recipient,
                                    String templateName, int retryCount, LocalDateTime updatedAt) {
        this.jobId = jobId;
        this.status = status;
        this.channelType = channelType;
        this.recipient = recipient;
        this.templateName = templateName;
        this.retryCount = retryCount;
        this.updatedAt = updatedAt;
    }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getChannelType() { return channelType; }
    public void setChannelType(String channelType) { this.channelType = channelType; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
