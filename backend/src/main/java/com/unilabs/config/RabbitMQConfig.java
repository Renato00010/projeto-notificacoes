package com.unilabs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "notification.exchange";
    public static final String DLX_NAME = "notification.dlx";
    public static final String DLQ_NAME = "notification.queue.dlq";
    public static final String DLQ_ROUTING_KEY = "notification.routing.dlq";

    public static final String QUEUE_EMAIL = "notification.queue.email";
    public static final String QUEUE_SMS = "notification.queue.sms";
    public static final String QUEUE_PUSH = "notification.queue.push";

    public static final String ROUTING_KEY_EMAIL = "notification.routing.email";
    public static final String ROUTING_KEY_SMS = "notification.routing.sms";
    public static final String ROUTING_KEY_PUSH = "notification.routing.push";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_NAME);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DLQ_ROUTING_KEY);
    }

    @Bean
    public Queue emailQueue() {
        return queueWithDeadLetter(QUEUE_EMAIL);
    }

    @Bean
    public Queue smsQueue() {
        return queueWithDeadLetter(QUEUE_SMS);
    }

    @Bean
    public Queue pushQueue() {
        return queueWithDeadLetter(QUEUE_PUSH);
    }

    private Queue queueWithDeadLetter(String queueName) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding bindingEmail(Queue emailQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(emailQueue).to(notificationExchange).with(ROUTING_KEY_EMAIL);
    }

    @Bean
    public Binding bindingSms(Queue smsQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(smsQueue).to(notificationExchange).with(ROUTING_KEY_SMS);
    }

    @Bean
    public Binding bindingPush(Queue pushQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(pushQueue).to(notificationExchange).with(ROUTING_KEY_PUSH);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
