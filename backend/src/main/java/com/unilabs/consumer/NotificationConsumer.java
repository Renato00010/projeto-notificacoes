package com.unilabs.consumer;

import com.unilabs.config.RabbitMQConfig;
import com.unilabs.dto.NotificationRequest;
import com.unilabs.service.NotificationProcessorService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationProcessorService processorService;

    public NotificationConsumer(NotificationProcessorService processorService) {
        this.processorService = processorService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void consumeEmailNotification(NotificationRequest request) {
        processorService.process(request);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SMS)
    public void consumeSmsNotification(NotificationRequest request) {
        processorService.process(request);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PUSH)
    public void consumePushNotification(NotificationRequest request) {
        processorService.process(request);
    }
}
