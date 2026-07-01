package com.unilabs.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Template de notificação disponível")
public class NotificationTemplateResponse {

    @Schema(example = "template_resultados_exame")
    private String name;

    @Schema(example = "EMAIL")
    private String channelType;

    @Schema(example = "Resultados de exame disponíveis")
    private String subject;

    @Schema(example = "Olá {{nome_paciente}}, os resultados de {{data_exame}} estão disponíveis.")
    private String bodyPreview;

    public NotificationTemplateResponse() {}

    public NotificationTemplateResponse(String name, String channelType, String subject, String bodyPreview) {
        this.name = name;
        this.channelType = channelType;
        this.subject = subject;
        this.bodyPreview = bodyPreview;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getChannelType() { return channelType; }
    public void setChannelType(String channelType) { this.channelType = channelType; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBodyPreview() { return bodyPreview; }
    public void setBodyPreview(String bodyPreview) { this.bodyPreview = bodyPreview; }
}
