package com.grocery.service_cart.service;

import com.grocery.service_cart.DTO.ApiResponse;
import com.grocery.service_cart.DTO.Product;
import com.grocery.service_cart.OpenFeign.ProductController;
import com.grocery.service_cart.entity.CartItem;
import com.grocery.service_cart.repository.CartItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductController productController;

    public CartItemServiceImpl(CartItemRepository cartItemRepository,
                               ProductController productController) {
        this.cartItemRepository = cartItemRepository;
        this.productController = productController;
    }

    @Override
    public CartItem createCartItem(String productName, int quantity) {
        ApiResponse<List<Product>> response = productController.getAllProducts();

        if (response == null || response.getData() == null) {
            throw new IllegalStateException("Product service returned empty response");
        }

        Product prod = response.getData().stream()
                .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(productName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productName));

        CartItem item = new CartItem();
        item.setName(prod.getName());
        item.setDescription(prod.getDescription());
        item.setPrice(prod.getPrice());
        item.setQuantity(quantity);

        // DO NOT save here. Return transient CartItem and let Cart save cascade persist it.
        return item;
    }


    @Override
    public Optional<CartItem> findByProductName(String productName) {
        // return repository result if you want to look up persisted items (but careful — these items likely already belong to a cart)
        return cartItemRepository.findCartItemByName(productName);
    }


    @Override
    public CartItem updateQuantity(String productName, int quantity) {
        CartItem item = cartItemRepository.findCartItemByName(productName)
                .orElseThrow(() ->
                        new IllegalArgumentException("CartItem not found: " + productName)
                );

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            throw new IllegalArgumentException(
                    "Quantity must be > 0, item deleted: " + productName
            );
        }

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Override
    public void deleteById(String productName) {
        cartItemRepository.deleteByName(productName);
    }
}
