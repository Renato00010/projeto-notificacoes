package com.unilabs.consumer;

import com.unilabs.dto.ChannelType;
import com.unilabs.dto.NotificationRequest;
import com.unilabs.service.NotificationProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.mockito.Mockito.verify;

public class NotificationConsumerTest {

    @Mock
    private NotificationProcessorService processorService;

    @InjectMocks
    private NotificationConsumer consumer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConsumeEmailNotification_DelegatesToProcessor() {
        NotificationRequest request = buildRequest(ChannelType.EMAIL);
        consumer.consumeEmailNotification(request);
        verify(processorService).process(request);
    }

    @Test
    public void testConsumeSmsNotification_DelegatesToProcessor() {
        NotificationRequest request = buildRequest(ChannelType.SMS);
        consumer.consumeSmsNotification(request);
        verify(processorService).process(request);
    }

    private NotificationRequest buildRequest(ChannelType channelType) {
        NotificationRequest request = new NotificationRequest();
        request.setJobId(UUID.randomUUID());
        request.setClientId("test-client");
        request.setChannelType(channelType);
        request.setRecipient("dest@example.com");
        return request;
    }
}
