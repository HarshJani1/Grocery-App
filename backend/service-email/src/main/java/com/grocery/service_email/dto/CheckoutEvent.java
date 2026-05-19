package com.grocery.service_email.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Event published by service-cart when clearCart() is called.
 * Contains the complete bill snapshot (items + total) before the cart is cleared.
 * Consumed by service-email to send a bill email.
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
