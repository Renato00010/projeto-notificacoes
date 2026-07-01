package com.unilabs.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Notificação encontrada nos logs de auditoria")
public class NotificationFilterResponse {

    @Schema(description = "ID do log de auditoria")
    private Long logId;

    @Schema(description = "ID do job associado")
    private UUID jobId;

    @Schema(description = "Canal de envio", example = "SMS")
    private String channelType;

    @Schema(description = "Destinatário (e-mail, telefone ou token push)")
    private String recipient;

    @Schema(description = "Sistema de origem do pedido")
    private String clientId;

    @Schema(description = "Estado do job", example = "SUCCESS")
    private String status;

    @Schema(description = "Fornecedor utilizado no envio", example = "Gmail SMTP")
    private String provider;

    @Schema(description = "Parâmetros enviados no payload")
    private Map<String, Object> parameters;

    @Schema(description = "Mensagem de erro, se existir")
    private String errorMessage;

    @Schema(description = "Data de criação do log")
    private LocalDateTime createdAt;

    public NotificationFilterResponse() {}

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public String getChannelType() { return channelType; }
    public void setChannelType(String channelType) { this.channelType = channelType; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
