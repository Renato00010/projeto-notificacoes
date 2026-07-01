package com.unilabs.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Canal de entrega da notificação",
        example = "EMAIL",
        allowableValues = {"EMAIL", "SMS", "PUSH"}
)
public enum ChannelType {

    @Schema(description = "Notificação por e-mail (Gmail SMTP)")
    EMAIL,

    @Schema(description = "Notificação por SMS (Vonage)")
    SMS,

    @Schema(description = "Notificação push mobile (Firebase FCM)")
    PUSH;

    @JsonCreator
    public static ChannelType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if ("PUSH_NOTIFICATION".equals(normalized)) {
            return PUSH;
        }
        try {
            return ChannelType.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("O campo 'channelType' deve ser EMAIL, SMS ou PUSH.");
        }
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
