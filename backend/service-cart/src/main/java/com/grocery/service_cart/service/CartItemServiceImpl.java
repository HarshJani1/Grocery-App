package com.grocery.service_cart.service;

import com.grocery.service_cart.entity.CartItem;
import com.grocery.service_cart.repository.CartItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CartItemServiceImpl implements CartItemService{
    CartItemRepository cartItemRepository;

    public CartItemServiceImpl(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }


    @Override
    @Transactional
    public CartItem createCartItem( String productName, int quantity) {
        CartItem item = new CartItem();
        item.setProductName(productName);
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Override
    public Optional<CartItem> findByProductName(String productName) {
        return cartItemRepository.findCartItemByProductName(productName);
    }

    @Override
    public CartItem updateQuantity(String productName, int quantity) {
        CartItem item = cartItemRepository.findCartItemByProductName(productName)
                .orElseThrow(() -> new IllegalArgumentException("CartItem not found: " + productName));
        if (quantity <= 0) {
            cartItemRepository.delete(item);
            throw new IllegalArgumentException("Quantity must be > 0; item deleted: " + productName);
        }
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Override
    public void deleteById(String productName) {
        cartItemRepository.deleteByProductName(productName);
    }
}
