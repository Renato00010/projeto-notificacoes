package com.unilabs.provider;

import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;
import com.unilabs.dto.ChannelType;
import com.unilabs.dto.NotificationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmsNotificationProvider implements NotificationProvider {

    private final VonageClient vonageClient;
    private final String fromName;

    public SmsNotificationProvider(
            @Value("${vonage.api-key}") String apiKey,
            @Value("${vonage.api-secret}") String apiSecret,
            @Value("${vonage.from-name:Unilabs}") String fromName) {
        this.vonageClient = VonageClient.builder()
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .build();
        this.fromName = fromName;
    }

    @Override
    public String providerName() {
        return "Vonage";
    }

    @Override
    public ChannelType channelType() {
        return ChannelType.SMS;
    }

    @Override
    public void deliver(NotificationRequest request, String renderedSubject, String renderedBody)
            throws NotificationDeliveryException {

        String recipient = request.getRecipient();

        if (recipient == null || !recipient.matches("\\+[1-9]\\d{7,14}")) {
            throw new NotificationDeliveryException(
                    "Número de telefone inválido. Use formato E.164, ex: +351912345678");
        }

        try {
            // Vonage espera número sem o '+'
            String toNumber = recipient.startsWith("+") ? recipient.substring(1) : recipient;

            TextMessage message = new TextMessage(fromName, toNumber,
                    renderedBody != null ? renderedBody : "");

            SmsSubmissionResponse response = vonageClient.getSmsClient().submitMessage(message);

            if (response.getMessages() == null || response.getMessages().isEmpty()) {
                throw new NotificationDeliveryException("Vonage não retornou resposta.");
            }

            MessageStatus status = response.getMessages().get(0).getStatus();
            if (status != MessageStatus.OK) {
                String errorText = response.getMessages().get(0).getErrorText();
                throw new NotificationDeliveryException(
                        "Vonage retornou erro: " + status + " - " + errorText);
            }

        } catch (NotificationDeliveryException e) {
            throw e;
        } catch (Exception e) {
            throw new NotificationDeliveryException(
                    "Falha ao enviar SMS para " + recipient + ": " + e.getMessage(), e);
        }
    }
}
