package com.grocery.service_cart.service;


import com.grocery.service_cart.entity.CartItem;

import java.math.BigDecimal;
import java.util.Optional;

public interface CartItemService {
    CartItem createCartItem( String productName, int quantity);
    Optional<CartItem> findByProductName(String productName);
    CartItem updateQuantity(String productName, int quantity);
    void deleteById(String productName);
}
