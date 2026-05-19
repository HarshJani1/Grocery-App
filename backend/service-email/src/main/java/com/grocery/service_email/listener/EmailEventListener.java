package com.grocery.service_email.listener;

import com.grocery.service_email.config.RabbitMQConfig;
import com.grocery.service_email.dto.CheckoutEvent;
import com.grocery.service_email.dto.WelcomeEvent;
import com.grocery.service_email.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailEventListener {

    private static final Logger log = LoggerFactory.getLogger(EmailEventListener.class);

    private final EmailService emailService;

    public EmailEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Listens for signup events from service-auth.
     * Sends a Welcome email to the newly registered user.
     */
    @RabbitListener(queues = RabbitMQConfig.WELCOME_QUEUE)
    public void handleWelcomeEvent(WelcomeEvent event) {
        log.info("Received WelcomeEvent | email={}", event.getEmail());
        try {
            emailService.sendWelcomeEmail(event);
        } catch (Exception e) {
            log.error("Error processing WelcomeEvent | email={} | error={}", event.getEmail(), e.getMessage(), e);
            // Exception propagated → RabbitMQ will nack and retry (or route to DLQ if configured)
            throw e;
        }
    }

    /**
     * Listens for checkout events from service-cart (triggered by clearCart()).
     * Sends an itemized bill email to the user.
     */
    @RabbitListener(queues = RabbitMQConfig.CHECKOUT_QUEUE)
    public void handleCheckoutEvent(CheckoutEvent event) {
        log.info("Received CheckoutEvent | email={} | items={} | total={}",
                event.getEmail(), event.getItems().size(), event.getTotalAmount());
        try {
            emailService.sendBillEmail(event);
        } catch (Exception e) {
            log.error("Error processing CheckoutEvent | email={} | error={}", event.getEmail(), e.getMessage(), e);
            throw e;
        }
    }
}
