package com.unilabs.service;

import com.unilabs.config.RabbitMQConfig;
import com.unilabs.domain.NotificationJob;
import com.unilabs.dto.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class NotificationRetryServiceTest {

    @Mock
    private com.unilabs.repository.NotificationJobRepository jobRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationRetryService retryService;

    private UUID jobId;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        jobId = UUID.randomUUID();
    }

    @Test
    public void testRetry_Success() {
        NotificationJob job = failedJob();
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = retryService.retry(jobId);

        assertEquals("PENDING", response.getStatus());
        assertEquals(1, response.getRetryCount());
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.ROUTING_KEY_SMS),
                any(com.unilabs.dto.NotificationRequest.class)
        );
    }

    @Test
    public void testRetry_NotFailedJob() {
        NotificationJob job = failedJob();
        job.setStatus("SUCCESS");
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThrows(IllegalArgumentException.class, () -> retryService.retry(jobId));
    }

    @Test
    public void testRetry_MaxRetriesExceeded() {
        NotificationJob job = failedJob();
        job.setRetryCount(3);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThrows(IllegalArgumentException.class, () -> retryService.retry(jobId));
    }

    private NotificationJob failedJob() {
        NotificationJob job = new NotificationJob();
        job.setId(jobId);
        job.setClientId("portal");
        job.setChannelType(ChannelType.SMS.name());
        job.setRecipient("+351912345678");
        job.setTemplateName("template_lembrete_consulta");
        job.setStatus("FAILED");
        job.setRetryCount(0);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        return job;
    }
}
