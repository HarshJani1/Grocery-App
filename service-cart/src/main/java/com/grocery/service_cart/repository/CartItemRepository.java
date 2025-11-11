package com.grocery.service_cart.repository;

import com.grocery.service_cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Integer> {
    Optional<CartItem> findCartItemByProductName(String productName);
    void deleteByProductName(String productName);
}
