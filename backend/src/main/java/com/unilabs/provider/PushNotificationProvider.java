package com.unilabs.provider;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.unilabs.dto.ChannelType;
import com.unilabs.dto.NotificationRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationProvider implements NotificationProvider {

    @Value("${firebase.service-account:firebase-service-account.json}")
    private String serviceAccountPath;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                GoogleCredentials credentials = GoogleCredentials
                        .fromStream(new ClassPathResource(serviceAccountPath).getInputStream());

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inicializar Firebase: " + e.getMessage(), e);
        }
    }

    @Override
    public String providerName() {
        return "Firebase FCM";
    }

    @Override
    public ChannelType channelType() {
        return ChannelType.PUSH;
    }

    @Override
    public void deliver(NotificationRequest request, String renderedSubject, String renderedBody)
            throws NotificationDeliveryException {

        String token = request.getRecipient();

        if (token == null || token.length() < 10) {
            throw new NotificationDeliveryException("Token push inválido ou demasiado curto.");
        }

        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(renderedSubject != null ? renderedSubject : "Notificação")
                            .setBody(renderedBody != null ? renderedBody : "")
                            .build())
                    .build();

            FirebaseMessaging.getInstance().send(message);

        } catch (Exception e) {
            throw new NotificationDeliveryException(
                    "Falha ao enviar push para token " + token.substring(0, 10) + "...: " + e.getMessage(), e);
        }
    }
}
