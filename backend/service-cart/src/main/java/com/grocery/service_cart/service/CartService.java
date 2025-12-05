package com.grocery.service_cart.service;

import com.grocery.service_cart.entity.Cart;
import com.grocery.service_cart.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CartService {
    Cart createCartForUser(Long userId);
    Optional<Cart> getCartById(Integer cartId);
    Optional<Cart> getCartByUserId(Long userId);

    Cart addItemToCart(Long userId, String productName, int quantity);
    Cart updateItemQuantity(Long userId,String productName, int quantity);
    Cart removeItemFromCart(Long userId,String productName);
    Cart clearCart(Long userId);

//    BigDecimal calculateCartTotal(Long userId);
    List<CartItem> listItems(Long userId);
}
