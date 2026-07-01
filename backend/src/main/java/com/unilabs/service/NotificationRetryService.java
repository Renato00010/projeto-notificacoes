package com.unilabs.service;

import com.unilabs.config.RabbitMQConfig;
import com.unilabs.domain.NotificationJob;
import com.unilabs.dto.ChannelType;
import com.unilabs.dto.NotificationRequest;
import com.unilabs.dto.NotificationRetryResponse;
import com.unilabs.repository.NotificationJobRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotificationRetryService {

    private static final int MAX_RETRIES = 3;

    private final NotificationJobRepository jobRepository;
    private final RabbitTemplate rabbitTemplate;

    public NotificationRetryService(NotificationJobRepository jobRepository,
                                    RabbitTemplate rabbitTemplate) {
        this.jobRepository = jobRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public NotificationRetryResponse retry(UUID jobId) {
        NotificationJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job com o ID fornecido nao foi encontrado."));

        if (!"FAILED".equals(job.getStatus())) {
            throw new IllegalArgumentException("Apenas jobs com estado FAILED podem ser reenviados.");
        }
        if (job.getRetryCount() >= MAX_RETRIES) {
            throw new IllegalArgumentException("Limite maximo de " + MAX_RETRIES + " reenvios atingido.");
        }
        if (job.getChannelType() == null || job.getRecipient() == null) {
            throw new IllegalArgumentException("Job nao possui dados suficientes para reenvio.");
        }

        NotificationRequest request = buildRequestFromJob(job);
        job.setStatus("PENDING");
        job.setRetryCount(job.getRetryCount() + 1);
        job.setUpdatedAt(LocalDateTime.now());
        job.setWebhookStatus("NONE");
        job.setWebhookResponse(null);
        jobRepository.save(job);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                resolveRoutingKey(job.getChannelType()),
                request
        );

        return new NotificationRetryResponse(jobId, "PENDING", job.getRetryCount());
    }

    private NotificationRequest buildRequestFromJob(NotificationJob job) {
        NotificationRequest request = new NotificationRequest();
        request.setJobId(job.getId());
        request.setClientId(job.getClientId());
        request.setChannelType(ChannelType.fromValue(job.getChannelType()));
        request.setRecipient(job.getRecipient());
        request.setTemplateName(job.getTemplateName());
        request.setParameters(job.getParameters());
        request.setCallbackUrl(job.getCallbackUrl());
        return request;
    }

    private String resolveRoutingKey(String channelType) {
        return switch (ChannelType.fromValue(channelType)) {
            case EMAIL -> RabbitMQConfig.ROUTING_KEY_EMAIL;
            case SMS -> RabbitMQConfig.ROUTING_KEY_SMS;
            case PUSH -> RabbitMQConfig.ROUTING_KEY_PUSH;
        };
    }
}
