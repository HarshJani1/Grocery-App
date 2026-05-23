package com.grocery.service_cart.service;

import com.grocery.service_cart.DTO.ApiResponse;
import com.grocery.service_cart.DTO.Product;
import com.grocery.service_cart.OpenFeign.ProductController;
import com.grocery.service_cart.entity.CartItem;
import com.grocery.service_cart.repository.CartItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemServiceImpl implements CartItemService {

    private static final Logger log = LoggerFactory.getLogger(CartItemServiceImpl.class);

    private final CartItemRepository cartItemRepository;
    private final ProductController productController;

    public CartItemServiceImpl(CartItemRepository cartItemRepository,
            ProductController productController) {
        this.cartItemRepository = cartItemRepository;
        this.productController = productController;
    }

    @Override
    public CartItem createCartItem(String productName, int quantity) {
        log.info("Creating cart item | product={} | quantity={}", productName, quantity);
        try {
            ApiResponse<List<Product>> response = productController.getAllProducts();

            if (response == null || response.getData() == null) {
                log.error("Product service returned empty response while creating cart item | product={}", productName);
                throw new IllegalStateException("Product service returned empty response");
            }

            Product prod = response.getData().stream()
                    .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(productName))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.warn("Product not found in product service | product={}", productName);
                        return new IllegalArgumentException("Product not found: " + productName);
                    });

            CartItem item = new CartItem();
            item.setName(prod.getName());
            item.setDescription(prod.getDescription());
            item.setPrice(prod.getPrice());
            item.setQuantity(quantity);

            log.info("Cart item created successfully | product={} | price={}", prod.getName(), prod.getPrice());
            // DO NOT save here. Return transient CartItem and let Cart save cascade persist
            // it.
            return item;
        } catch (Exception e) {
            log.error("Failed to create cart item | product={} | error={}", productName, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<CartItem> findByProductName(String productName) {
        log.debug("Finding cart item by product name | product={}", productName);
        // return repository result if you want to look up persisted items (but careful
        // — these items likely already belong to a cart)
        return cartItemRepository.findCartItemByName(productName);
    }

    @Override
    public CartItem updateQuantity(String productName, int quantity) {
        log.info("Updating cart item quantity | product={} | quantity={}", productName, quantity);
        try {
            CartItem item = cartItemRepository.findCartItemByName(productName)
                    .orElseThrow(() -> {
                        log.warn("CartItem not found for update | product={}", productName);
                        return new IllegalArgumentException("CartItem not found: " + productName);
                    });

            if (quantity <= 0) {
                cartItemRepository.delete(item);
                log.info("Cart item deleted (quantity <= 0) | product={}", productName);
                throw new IllegalArgumentException(
                        "Quantity must be > 0, item deleted: " + productName);
            }

            item.setQuantity(quantity);
            CartItem saved = cartItemRepository.save(item);
            log.info("Cart item quantity updated successfully | product={} | newQuantity={}", productName, quantity);
            return saved;
        } catch (Exception e) {
            log.error("Failed to update cart item quantity | product={} | error={}", productName, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void deleteById(String productName) {
        log.info("Deleting cart item | product={}", productName);
        try {
            cartItemRepository.deleteByName(productName);
            log.info("Cart item deleted successfully | product={}", productName);
        } catch (Exception e) {
            log.error("Failed to delete cart item | product={} | error={}", productName, e.getMessage(), e);
            throw e;
        }
    }
}
