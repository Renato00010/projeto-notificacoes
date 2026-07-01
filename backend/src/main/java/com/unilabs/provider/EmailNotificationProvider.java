package com.unilabs.provider;

import com.unilabs.dto.ChannelType;
import com.unilabs.dto.NotificationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

@Component
public class EmailNotificationProvider implements NotificationProvider {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from}")
    private String fromAddress;

    public EmailNotificationProvider(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public String providerName() {
        return "Gmail SMTP";
    }

    @Override
    public ChannelType channelType() {
        return ChannelType.EMAIL;
    }

    @Override
    public void deliver(NotificationRequest request, String renderedSubject, String renderedBody)
            throws NotificationDeliveryException {

        String recipient = request.getRecipient();

        if (recipient == null || !recipient.contains("@")) {
            throw new NotificationDeliveryException("Destinatário de e-mail inválido: " + recipient);
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(recipient);
            helper.setSubject(renderedSubject != null ? renderedSubject : "(sem assunto)");
            helper.setText(renderedBody != null ? renderedBody : "", false);

            mailSender.send(message);

        } catch (Exception e) {
            throw new NotificationDeliveryException("Falha ao enviar e-mail para " + recipient + ": " + e.getMessage(), e);
        }
    }
}
