package com.grocery.service_cart.service;

import com.grocery.service_cart.entity.Cart;
import com.grocery.service_cart.entity.CartItem;
import com.grocery.service_cart.repository.CartRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    CartRepository cartRepository;
    CartItemService cartItemService;
    public CartServiceImpl(CartRepository cartRepository,CartItemService cartItemService) {
        this.cartRepository = cartRepository;
        this.cartItemService = cartItemService;
    }

    @Override
    public Cart createCartForUser(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }

    @Override
    public Optional<Cart> getCartById(Integer cartId) {
        return cartRepository.findById(cartId);
    }

    @Override
    public Optional<Cart> getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    public Cart addItemToCart(Long userId, String productName, int quantity) {
        Cart cart = getCartByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));

        // check if product already in cart
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProductName().equals(productName))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProductName(productName);
            newItem.setQuantity(quantity);
            cart.addItem(newItem);
            // links both sides
        }

        return cartRepository.save(cart);
    }

    @Override
    public Cart updateItemQuantity(Long userId,String productName, int quantity) {
        Cart cart = getCartByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));

        CartItem target = cart.getItems().stream()
                .filter(i -> i.getProductName().equals(productName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("CartItem not found: " + productName));

        if (quantity <= 0) {
            cart.removeItem(target);
        } else {
            target.setQuantity(quantity);
        }

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart removeItemFromCart(Long userId, String productName) {
        Cart cart = getCartByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));

        CartItem target = cart.getItems().stream()
                .filter(i -> i.getProductName().equals(productName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("CartItem not found: " + productName));

        cart.removeItem(target);
        return cartRepository.save(cart);
    }

    @Override
    public Cart clearCart(Long userId) {
        Cart cart = getCartByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));

        cart.getItems().clear();
        return cartRepository.save(cart);
    }

    @Override
    public List<CartItem> listItems(Long userId) {
        Cart cart = getCartByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));

        return cart.getItems();
    }
}
