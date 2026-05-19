package com.grocery.service_cart.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the shared exchange and checkout queue used to publish bill events
 * to service-email. Mirrors the constants in service-email's RabbitMQConfig.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE             = "grocery.email.exchange";
    public static final String CHECKOUT_QUEUE       = "grocery.checkout.queue";
    public static final String CHECKOUT_ROUTING_KEY = "email.checkout";

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue checkoutQueue() {
        return QueueBuilder.durable(CHECKOUT_QUEUE).build();
    }

    @Bean
    public Binding checkoutBinding(Queue checkoutQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(checkoutQueue).to(emailExchange).with(CHECKOUT_ROUTING_KEY);
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
