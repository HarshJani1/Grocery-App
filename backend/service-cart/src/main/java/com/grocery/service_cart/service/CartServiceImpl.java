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

    private final CartRepository cartRepository;
    private final CartItemService cartItemService;

    public CartServiceImpl(CartRepository cartRepository, CartItemService cartItemService) {
        this.cartRepository = cartRepository;
        this.cartItemService = cartItemService;
    }

    @Override
    public Cart createCartForUser(String email) {
        Cart cart = new Cart();
        cart.setEmail(email);
        return cartRepository.save(cart);
    }

    @Override
    public Optional<Cart> getCartById(Long cartId) {
        return cartRepository.findById(cartId);
    }

    @Override
    public Optional<Cart> getCartByUserEmail(String email) {
        return cartRepository.findByEmail(email);
    }

    @Override
    public Cart addItemToCart(String email, String productName, int quantity) {
        Cart cart = getCartByUserEmail(email)
                .orElseGet(() -> createCartForUser(email));

        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getName().equalsIgnoreCase(productName))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            CartItem newItem = cartItemService.createCartItem(productName, quantity);
            newItem.setQuantity(quantity);
            cart.addItem(newItem); // addItem will set cart on item
        }

        return cartRepository.save(cart);
    }


    @Override
    public Cart updateItemQuantity(String email, String productName, int quantity) {
        Cart cart = getCartByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + email));

        CartItem target = cart.getItems().stream()
                .filter(i -> i.getName().equalsIgnoreCase(productName))
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
    public Cart removeItemFromCart(String email, String productName) {
        Cart cart = getCartByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + email));

        CartItem target = cart.getItems().stream()
                .filter(i -> i.getName().equalsIgnoreCase(productName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("CartItem not found: " + productName));

        cart.removeItem(target);
        return cartRepository.save(cart);
    }

    @Override
    public Cart clearCart(String email) {
        Cart cart = getCartByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + email));

        cart.getItems().clear();
        return cartRepository.save(cart);
    }

    @Override
    public List<CartItem> listItems(String email) {
        Cart cart = getCartByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + email));

        return cart.getItems();
    }
}
