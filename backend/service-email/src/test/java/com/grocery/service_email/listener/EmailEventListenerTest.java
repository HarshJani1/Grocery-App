package com.grocery.service_email.listener;

import com.grocery.service_email.dto.CheckoutEvent;
import com.grocery.service_email.dto.CheckoutItemDTO;
import com.grocery.service_email.dto.WelcomeEvent;
import com.grocery.service_email.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailEventListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailEventListener emailEventListener;

    @Test
    @DisplayName("handleWelcomeEvent - calls sendWelcomeEmail on service")
    void handleWelcomeEvent_success_callsService() {
        WelcomeEvent event = new WelcomeEvent("user@grocery.com", "John");
        doNothing().when(emailService).sendWelcomeEmail(event);

        assertDoesNotThrow(() -> emailEventListener.handleWelcomeEvent(event));

        verify(emailService, times(1)).sendWelcomeEmail(event);
    }

    @Test
    @DisplayName("handleWelcomeEvent - service failure throws exception")
    void handleWelcomeEvent_failure_throwsException() {
        WelcomeEvent event = new WelcomeEvent("user@grocery.com", "John");
        doThrow(new RuntimeException("Mail error")).when(emailService).sendWelcomeEmail(event);

        assertThrows(RuntimeException.class, () -> {
            emailEventListener.handleWelcomeEvent(event);
        });

        verify(emailService, times(1)).sendWelcomeEmail(event);
    }

    @Test
    @DisplayName("handleCheckoutEvent - calls sendBillEmail on service")
    void handleCheckoutEvent_success_callsService() {
        CheckoutItemDTO item = new CheckoutItemDTO("Apple", "Fresh apple", BigDecimal.valueOf(1.50), 2);
        CheckoutEvent event = new CheckoutEvent("user@grocery.com", Arrays.asList(item), BigDecimal.valueOf(3.00));
        doNothing().when(emailService).sendBillEmail(event);

        assertDoesNotThrow(() -> emailEventListener.handleCheckoutEvent(event));

        verify(emailService, times(1)).sendBillEmail(event);
    }

    @Test
    @DisplayName("handleCheckoutEvent - service failure throws exception")
    void handleCheckoutEvent_failure_throwsException() {
        CheckoutItemDTO item = new CheckoutItemDTO("Apple", "Fresh apple", BigDecimal.valueOf(1.50), 2);
        CheckoutEvent event = new CheckoutEvent("user@grocery.com", Arrays.asList(item), BigDecimal.valueOf(3.00));
        doThrow(new RuntimeException("Mail error")).when(emailService).sendBillEmail(event);

        assertThrows(RuntimeException.class, () -> {
            emailEventListener.handleCheckoutEvent(event);
        });

        verify(emailService, times(1)).sendBillEmail(event);
    }
}
