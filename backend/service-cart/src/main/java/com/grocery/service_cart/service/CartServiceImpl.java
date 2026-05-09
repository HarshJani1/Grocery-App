package com.grocery.service_cart.service;

import com.grocery.service_cart.entity.Cart;
import com.grocery.service_cart.entity.CartItem;
import com.grocery.service_cart.repository.CartRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemService cartItemService;

    public CartServiceImpl(CartRepository cartRepository, CartItemService cartItemService) {
        this.cartRepository = cartRepository;
        this.cartItemService = cartItemService;
    }

    @Override
    public Cart createCartForUser(String email) {
        log.info("Creating new cart | email={}", email);
        Cart cart = new Cart();
        cart.setEmail(email);
        Cart saved = cartRepository.save(cart);
        log.info("Cart created successfully | email={} | cartId={}", email, saved.getId());
        return saved;
    }

    @Override
    public Optional<Cart> getCartById(Long cartId) {
        log.debug("Fetching cart by ID | cartId={}", cartId);
        return cartRepository.findById(cartId);
    }

    @Override
    public Optional<Cart> getCartByUserEmail(String email) {
        log.debug("Fetching cart by email | email={}", email);
        return cartRepository.findByEmail(email);
    }

    @Override
    public Cart addItemToCart(String email, String productName, int quantity) {
        log.info("Adding item to cart | email={} | product={} | quantity={}", email, productName, quantity);
        try {
            Cart cart = getCartByUserEmail(email)
                    .orElseGet(() -> createCartForUser(email));

            CartItem existing = cart.getItems().stream()
                    .filter(i -> i.getName().equalsIgnoreCase(productName))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                int oldQty = existing.getQuantity();
                existing.setQuantity(oldQty + quantity);
                log.info("Updated existing cart item quantity | email={} | product={} | oldQty={} | newQty={}", email, productName, oldQty, existing.getQuantity());
            } else {
                CartItem newItem = cartItemService.createCartItem(productName, quantity);
                newItem.setQuantity(quantity);
                cart.addItem(newItem); // addItem will set cart on item
                log.info("Added new item to cart | email={} | product={} | quantity={}", email, productName, quantity);
            }

            Cart saved = cartRepository.save(cart);
            log.info("Cart saved successfully | email={} | totalItems={}", email, saved.getItems().size());
            return saved;
        } catch (Exception e) {
            log.error("Failed to add item to cart | email={} | product={} | error={}", email, productName, e.getMessage(), e);
            throw e;
        }
    }


    @Override
    public Cart updateItemQuantity(String email, String productName, int quantity) {
        log.info("Updating item quantity | email={} | product={} | newQuantity={}", email, productName, quantity);
        try {
            Cart cart = getCartByUserEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + email));

            CartItem target = cart.getItems().stream()
                    .filter(i -> i.getName().equalsIgnoreCase(productName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("CartItem not found: " + productName));

            if (quantity <= 0) {
                cart.removeItem(target);
                log.info("Removed item (quantity <= 0) | email={} | product={}", email, productName);
            } else {
                target.setQuantity(quantity);
                log.info("Item quantity updated | email={} | product={} | quantity={}", email, productName, quantity);
            }

            return cartRepository.save(cart);
        } catch (Exception e) {
            log.error("Failed to update item quantity | email={} | product={} | error={}", email, productName, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public Cart removeItemFromCart(String email, String productName) {
        log.info("Removing item from cart | email={} | product={}", email, productName);
        try {
            Cart cart = getCartByUserEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + email));

            CartItem target = cart.getItems().stream()
                    .filter(i -> i.getName().equalsIgnoreCase(productName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("CartItem not found: " + productName));

            cart.removeItem(target);
            Cart saved = cartRepository.save(cart);
            log.info("Item removed from cart successfully | email={} | product={}", email, productName);
            return saved;
        } catch (Exception e) {
            log.error("Failed to remove item from cart | email={} | product={} | error={}", email, productName, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Cart clearCart(String email) {
        log.info("Clearing cart | email={}", email);
        try {
            Cart cart = getCartByUserEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + email));

            int itemCount = cart.getItems().size();
            cart.getItems().forEach(item -> item.setCart(null));
            cart.getItems().clear();

            Cart saved = cartRepository.save(cart);
            log.info("Cart cleared successfully | email={} | removedItems={}", email, itemCount);
            return saved;
        } catch (Exception e) {
            log.error("Failed to clear cart | email={} | error={}", email, e.getMessage(), e);
            throw e;
        }
    }


    @Override
    public List<CartItem> listItems(String email) {
        log.debug("Listing cart items | email={}", email);
        Cart cart = getCartByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + email));

        log.debug("Cart items retrieved | email={} | itemCount={}", email, cart.getItems().size());
        return cart.getItems();
    }
}
