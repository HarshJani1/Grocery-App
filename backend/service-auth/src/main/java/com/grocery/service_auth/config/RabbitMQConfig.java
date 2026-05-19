package com.grocery.service_auth.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the shared exchange and welcome queue used to publish signup events
 * to service-email. Mirrors the constants in service-email's RabbitMQConfig.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE            = "grocery.email.exchange";
    public static final String WELCOME_QUEUE       = "grocery.welcome.queue";
    public static final String WELCOME_ROUTING_KEY = "email.welcome";

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue welcomeQueue() {
        return QueueBuilder.durable(WELCOME_QUEUE).build();
    }

    @Bean
    public Binding welcomeBinding(Queue welcomeQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(welcomeQueue).to(emailExchange).with(WELCOME_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
