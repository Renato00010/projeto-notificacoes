package com.unilabs.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

@Schema(description = "Pedido de envio de notificação")
public class NotificationRequest {

    @Schema(description = "ID do job (atribuído pelo servidor antes de publicar no RabbitMQ)", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID jobId;

    @Schema(description = "Identificador do sistema de origem", example = "portal-do-paciente", requiredMode = Schema.RequiredMode.REQUIRED)
    private String clientId;

    @Schema(description = "Canal de entrega: EMAIL, SMS ou PUSH", example = "EMAIL", requiredMode = Schema.RequiredMode.REQUIRED)
    private ChannelType channelType;

    @Schema(
            description = "Destinatário conforme o canal: e-mail, número de telefone (E.164) ou token push",
            example = "paciente@exemplo.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String recipient;

    @Schema(description = "Nome do template a utilizar", example = "template_resultados_exame")
    private String templateName;

    @Schema(description = "Parâmetros dinâmicos do template")
    private Map<String, Object> parameters;

    @Schema(description = "URL para callback HTTP após processamento (opcional)", example = "https://meu-sistema.com/webhooks/notificacoes")
    private String callbackUrl;

    public NotificationRequest() {}

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public ChannelType getChannelType() { return channelType; }
    public void setChannelType(ChannelType channelType) { this.channelType = channelType; }

    public void setChannelType(String channelType) {
        this.channelType = ChannelType.fromValue(channelType);
    }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public String getCallbackUrl() { return callbackUrl; }
    public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }
}
