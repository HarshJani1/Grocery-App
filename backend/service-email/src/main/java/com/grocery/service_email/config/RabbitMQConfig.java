package com.grocery.service_email.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Exchange ────────────────────────────────────────────────
    public static final String EXCHANGE = "grocery.email.exchange";

    // ── Queues ──────────────────────────────────────────────────
    public static final String WELCOME_QUEUE        = "grocery.welcome.queue";
    public static final String CHECKOUT_QUEUE       = "grocery.checkout.queue";

    // ── Routing Keys ────────────────────────────────────────────
    public static final String WELCOME_ROUTING_KEY  = "email.welcome";
    public static final String CHECKOUT_ROUTING_KEY = "email.checkout";

    // ── Exchange Bean ────────────────────────────────────────────
    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    // ── Queue Beans ──────────────────────────────────────────────
    @Bean
    public Queue welcomeQueue() {
        return QueueBuilder.durable(WELCOME_QUEUE).build();
    }

    @Bean
    public Queue checkoutQueue() {
        return QueueBuilder.durable(CHECKOUT_QUEUE).build();
    }

    // ── Bindings ─────────────────────────────────────────────────
    @Bean
    public Binding welcomeBinding(Queue welcomeQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(welcomeQueue).to(emailExchange).with(WELCOME_ROUTING_KEY);
    }

    @Bean
    public Binding checkoutBinding(Queue checkoutQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(checkoutQueue).to(emailExchange).with(CHECKOUT_ROUTING_KEY);
    }

    // ── JSON Message Converter ────────────────────────────────────
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
