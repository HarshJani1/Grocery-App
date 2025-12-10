package com.grocery.service_cart.service;

import com.grocery.service_cart.entity.Cart;
import com.grocery.service_cart.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CartService {
    Cart createCartForUser(String email);
    Optional<Cart> getCartById(Long cartId);
    Optional<Cart> getCartByUserEmail(String email);

    Cart addItemToCart(String email, String productName, int quantity);
    Cart updateItemQuantity(String email,String productName, int quantity);
    Cart removeItemFromCart(String email,String productName);
    Cart clearCart(String email);

//    BigDecimal calculateCartTotal(String email);
    List<CartItem> listItems(String email);
}
