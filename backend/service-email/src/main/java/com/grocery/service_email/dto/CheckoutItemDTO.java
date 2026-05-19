package com.grocery.service_email.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Represents a single cart item in the bill email.
 * Published by service-cart as part of CheckoutEvent.
 */
public class CheckoutItemDTO implements Serializable {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;

    public CheckoutItemDTO() {}

    public CheckoutItemDTO(String name, String description, BigDecimal price, Integer quantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    /** Convenience: price × quantity */
    public BigDecimal getLineTotal() {
        if (price == null || quantity == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
