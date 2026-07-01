package com.unilabs.service;

import com.unilabs.config.RabbitMQConfig;
import com.unilabs.domain.NotificationJob;
import com.unilabs.dto.ChannelType;
import com.unilabs.dto.NotificationRequest;
import com.unilabs.dto.NotificationStatusResponse;
import com.unilabs.repository.NotificationJobRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationJobRepository jobRepository;
    private final NotificationTemplateService templateService;
    private final RabbitTemplate rabbitTemplate;

    public NotificationService(NotificationJobRepository jobRepository,
                               NotificationTemplateService templateService,
                               RabbitTemplate rabbitTemplate) {
        this.jobRepository = jobRepository;
        this.templateService = templateService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public UUID queueNotification(NotificationRequest request) {
        validateRequest(request);

        if (request.getTemplateName() != null && !request.getTemplateName().isBlank()) {
            templateService.render(request.getTemplateName(), request.getChannelType(), request.getParameters());
        }

        NotificationJob job = new NotificationJob();
        UUID jobId = UUID.randomUUID();
        job.setId(jobId);
        job.setClientId(request.getClientId());
        job.setChannelType(request.getChannelType().name());
        job.setRecipient(request.getRecipient());
        job.setTemplateName(request.getTemplateName());
        job.setParameters(request.getParameters());
        job.setCallbackUrl(request.getCallbackUrl());
        job.setStatus("PENDING");
        job.setRetryCount(0);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());

        jobRepository.save(job);
        request.setJobId(jobId);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    resolveRoutingKey(request.getChannelType()),
                    request
            );
        } catch (Exception e) {
            job.setStatus("FAILED");
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
            throw new RuntimeException("Erro ao enviar mensagem para a fila do RabbitMQ", e);
        }

        return jobId;
    }

    private void validateRequest(NotificationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("O pedido de notificacao nao pode ser nulo.");
        }
        if (request.getClientId() == null || request.getClientId().isBlank()) {
            throw new IllegalArgumentException("O campo 'clientId' e obrigatorio.");
        }
        if (request.getChannelType() == null) {
            throw new IllegalArgumentException("O campo 'channelType' e obrigatorio.");
        }
        if (request.getRecipient() == null || request.getRecipient().isBlank()) {
            throw new IllegalArgumentException("O campo 'recipient' e obrigatorio.");
        }
        validateRecipientFormat(request.getChannelType(), request.getRecipient());
    }

    private void validateRecipientFormat(ChannelType channelType, String recipient) {
        switch (channelType) {
            case EMAIL -> {
                if (!recipient.contains("@") || !recipient.contains(".")) {
                    throw new IllegalArgumentException("Destinatario de e-mail invalido.");
                }
            }
            case SMS -> {
                if (!recipient.matches("\\+[1-9]\\d{7,14}")) {
                    throw new IllegalArgumentException("Numero de telefone invalido. Use formato E.164, ex: +351912345678");
                }
            }
            case PUSH -> {
                if (recipient.length() < 10) {
                    throw new IllegalArgumentException("Token push invalido ou demasiado curto.");
                }
            }
        }
    }

    public NotificationStatusResponse getNotificationStatus(UUID jobId) {
        NotificationJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job com o ID fornecido nao foi encontrado."));

        return new NotificationStatusResponse(
                job.getId(),
                job.getStatus(),
                job.getChannelType(),
                job.getRecipient(),
                job.getTemplateName(),
                job.getRetryCount(),
                job.getUpdatedAt()
        );
    }

    private String resolveRoutingKey(ChannelType channelType) {
        return switch (channelType) {
            case EMAIL -> RabbitMQConfig.ROUTING_KEY_EMAIL;
            case SMS -> RabbitMQConfig.ROUTING_KEY_SMS;
            case PUSH -> RabbitMQConfig.ROUTING_KEY_PUSH;
        };
    }
}
