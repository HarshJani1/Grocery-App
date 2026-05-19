package com.grocery.service_cart.DTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Event published to RabbitMQ when a user's cart is cleared (checkout).
 * Contains a full bill snapshot: email, items, and pre-computed grand total.
 * Consumed by service-email to send an invoice email.
 */
public class CheckoutEvent implements Serializable {

    private String email;
    private List<CheckoutItemDTO> items;
    private BigDecimal totalAmount;

    public CheckoutEvent() {}

    public CheckoutEvent(String email, List<CheckoutItemDTO> items, BigDecimal totalAmount) {
        this.email = email;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<CheckoutItemDTO> getItems() { return items; }
    public void setItems(List<CheckoutItemDTO> items) { this.items = items; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
