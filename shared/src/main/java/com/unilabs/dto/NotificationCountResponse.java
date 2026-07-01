package com.unilabs.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Contagem de notificações que correspondem aos filtros aplicados")
public class NotificationCountResponse {

    @Schema(description = "Número total de notificações encontradas", example = "12")
    private long count;

    @Schema(description = "Destinatário filtrado", example = "+351912345678")
    private String recipient;

    @Schema(description = "Canal filtrado", example = "SMS")
    private String channelType;

    public NotificationCountResponse() {}

    public NotificationCountResponse(long count, String recipient, String channelType) {
        this.count = count;
        this.recipient = recipient;
        this.channelType = channelType;
    }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getChannelType() { return channelType; }
    public void setChannelType(String channelType) { this.channelType = channelType; }
}
