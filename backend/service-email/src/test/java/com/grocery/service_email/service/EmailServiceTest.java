package com.grocery.service_email.service;

import com.grocery.service_email.dto.CheckoutEvent;
import com.grocery.service_email.dto.CheckoutItemDTO;
import com.grocery.service_email.dto.WelcomeEvent;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        // JavaMail's MimeMessage requires a Session, we can construct one with empty properties or mock it.
        // Direct instantiation with empty Session is clean and avoids mocking protected constructors.
        Session session = Session.getInstance(new Properties());
        mimeMessage = new MimeMessage(session);
    }

    @Test
    @DisplayName("sendWelcomeEmail - success generates and sends email")
    void sendWelcomeEmail_success_sendsEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        WelcomeEvent event = new WelcomeEvent("user@grocery.com", "JohnDoe");

        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(event));

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("sendWelcomeEmail - mail send failure throws RuntimeException")
    void sendWelcomeEmail_failure_throwsRuntimeException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP Server Down")).when(mailSender).send(any(MimeMessage.class));

        WelcomeEvent event = new WelcomeEvent("user@grocery.com", "JohnDoe");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.sendWelcomeEmail(event);
        });

        assertTrue(exception.getMessage().contains("Failed to send welcome email"));
    }

    @Test
    @DisplayName("sendBillEmail - success generates bill HTML and sends email")
    void sendBillEmail_success_sendsEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        CheckoutItemDTO item = new CheckoutItemDTO("Apple", "Fresh apple", BigDecimal.valueOf(1.50), 2);
        CheckoutEvent event = new CheckoutEvent("user@grocery.com", Arrays.asList(item), BigDecimal.valueOf(3.00));

        assertDoesNotThrow(() -> emailService.sendBillEmail(event));

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("sendBillEmail - mail send failure throws RuntimeException")
    void sendBillEmail_failure_throwsRuntimeException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP Server Down")).when(mailSender).send(any(MimeMessage.class));

        CheckoutItemDTO item = new CheckoutItemDTO("Apple", "Fresh apple", BigDecimal.valueOf(1.50), 2);
        CheckoutEvent event = new CheckoutEvent("user@grocery.com", Arrays.asList(item), BigDecimal.valueOf(3.00));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.sendBillEmail(event);
        });

        assertTrue(exception.getMessage().contains("Failed to send bill email"));
    }
}
