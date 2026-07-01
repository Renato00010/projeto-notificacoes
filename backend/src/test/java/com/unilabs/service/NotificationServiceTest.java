package com.unilabs.service;

import com.unilabs.config.RabbitMQConfig;
import com.unilabs.domain.NotificationJob;
import com.unilabs.dto.ChannelType;
import com.unilabs.dto.NotificationRequest;
import com.unilabs.repository.NotificationJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    @Mock
    private NotificationJobRepository jobRepository;

    @Mock
    private NotificationTemplateService templateService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testQueueNotification_Success() {
        NotificationRequest request = new NotificationRequest();
        request.setClientId("test-client");
        request.setChannelType(ChannelType.EMAIL);
        request.setRecipient("dest@example.com");
        request.setTemplateName("template_resultados_exame");
        request.setCallbackUrl("https://httpbin.org/post");
        request.setParameters(new HashMap<>());

        when(templateService.render(any(), any(), any()))
                .thenReturn(new NotificationTemplateService.RenderedNotification("Subject", "Body"));
        when(jobRepository.save(any(NotificationJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID jobId = notificationService.queueNotification(request);

        assertNotNull(jobId);
        verify(jobRepository, atLeastOnce()).save(any(NotificationJob.class));
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.ROUTING_KEY_EMAIL),
                eq(request)
        );
    }

    @Test
    public void testQueueNotification_InvalidRequest() {
        NotificationRequest request = new NotificationRequest();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.queueNotification(request);
        });

        assertTrue(exception.getMessage().contains("clientId"));
        verify(jobRepository, never()).save(any(NotificationJob.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    public void testQueueNotification_WithoutCallbackUrl() {
        NotificationRequest request = new NotificationRequest();
        request.setClientId("test-client");
        request.setChannelType(ChannelType.EMAIL);
        request.setRecipient("dest@example.com");

        when(jobRepository.save(any(NotificationJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID jobId = notificationService.queueNotification(request);

        assertNotNull(jobId);
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), eq(request));
    }

    @Test
    public void testQueueNotification_InvalidPhone() {
        NotificationRequest request = new NotificationRequest();
        request.setClientId("test-client");
        request.setChannelType(ChannelType.SMS);
        request.setRecipient("912345678");

        assertThrows(IllegalArgumentException.class, () -> notificationService.queueNotification(request));
    }
}
