package com.grocery.service_cart.controller;

import com.grocery.service_cart.DTO.AddItemRequest;
import com.grocery.service_cart.DTO.DeleteItemRequest;
import com.grocery.service_cart.entity.CartItem;
import com.grocery.service_cart.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        cartItem = new CartItem(10L, "Apple", "Fresh apple", BigDecimal.valueOf(1.50), 2, null);
    }

    // ── getUserCart ──────────────────────────────────────────────

    @Test
    @DisplayName("getUserCart - success returns 200 with list of items")
    void getUserCart_success_returns200() {
        when(cartService.listItems("user@grocery.com")).thenReturn(Arrays.asList(cartItem));

        ResponseEntity<Map<String, Object>> response = cartController.getUserCart("user@grocery.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getBody().get("status"));
        assertEquals("cart fetched", response.getBody().get("message"));
        assertEquals(Arrays.asList(cartItem), response.getBody().get("data"));
    }

    @Test
    @DisplayName("getUserCart - user not found returns 404")
    void getUserCart_notFound_returns404() {
        when(cartService.listItems("missing@grocery.com")).thenThrow(new IllegalArgumentException("User not found"));

        ResponseEntity<Map<String, Object>> response = cartController.getUserCart("missing@grocery.com");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getBody().get("status"));
        assertEquals("User not found", response.getBody().get("message"));
    }

    @Test
    @DisplayName("getUserCart - general exception returns 500")
    void getUserCart_exception_returns500() {
        when(cartService.listItems("user@grocery.com")).thenThrow(new RuntimeException("Server error"));

        ResponseEntity<Map<String, Object>> response = cartController.getUserCart("user@grocery.com");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getBody().get("status"));
    }

    // ── addItem ──────────────────────────────────────────────────

    @Test
    @DisplayName("addItem - success returns 200")
    void addItem_success_returns200() {
        AddItemRequest request = new AddItemRequest();
        request.setProductName("Apple");
        request.setQuantity(3);
        when(cartService.addItemToCart("user@grocery.com", "Apple", 3)).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = cartController.addItem(request, "user@grocery.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getBody().get("status"));
        assertEquals("item added to cart", response.getBody().get("message"));
    }

    @Test
    @DisplayName("addItem - general exception returns 500")
    void addItem_exception_returns500() {
        AddItemRequest request = new AddItemRequest();
        request.setProductName("Apple");
        request.setQuantity(3);
        when(cartService.addItemToCart("user@grocery.com", "Apple", 3)).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<Map<String, Object>> response = cartController.addItem(request, "user@grocery.com");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getBody().get("status"));
    }

    // ── deleteItem ───────────────────────────────────────────────

    @Test
    @DisplayName("deleteItem - success returns 200")
    void deleteItem_success_returns200() {
        DeleteItemRequest request = new DeleteItemRequest();
        request.setProductName("Apple");
        when(cartService.removeItemFromCart("user@grocery.com", "Apple")).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = cartController.deleteItem(request, "user@grocery.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getBody().get("status"));
        assertEquals("item deleted successfully", response.getBody().get("message"));
    }

    @Test
    @DisplayName("deleteItem - item not found returns 404")
    void deleteItem_notFound_returns404() {
        DeleteItemRequest request = new DeleteItemRequest();
        request.setProductName("Apple");
        when(cartService.removeItemFromCart("user@grocery.com", "Apple")).thenThrow(new IllegalArgumentException("Item not found"));

        ResponseEntity<Map<String, Object>> response = cartController.deleteItem(request, "user@grocery.com");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getBody().get("status"));
        assertTrue(response.getBody().get("message").toString().contains("Item not found"));
    }

    @Test
    @DisplayName("deleteItem - general exception returns 500")
    void deleteItem_exception_returns500() {
        DeleteItemRequest request = new DeleteItemRequest();
        request.setProductName("Apple");
        when(cartService.removeItemFromCart("user@grocery.com", "Apple")).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<Map<String, Object>> response = cartController.deleteItem(request, "user@grocery.com");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getBody().get("status"));
    }

    // ── clearCart ────────────────────────────────────────────────

    @Test
    @DisplayName("clearCart - success returns 200")
    void clearCart_success_returns200() {
        when(cartService.clearCart("user@grocery.com")).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = cartController.clearCart("user@grocery.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getBody().get("status"));
        assertEquals("cart cleared", response.getBody().get("message"));
    }

    @Test
    @DisplayName("clearCart - cart not found returns 404")
    void clearCart_notFound_returns404() {
        when(cartService.clearCart("missing@grocery.com")).thenThrow(new IllegalArgumentException("Cart not found"));

        ResponseEntity<Map<String, Object>> response = cartController.clearCart("missing@grocery.com");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getBody().get("status"));
    }

    @Test
    @DisplayName("clearCart - general exception returns 500")
    void clearCart_exception_returns500() {
        when(cartService.clearCart("user@grocery.com")).thenThrow(new RuntimeException("Queue error"));

        ResponseEntity<Map<String, Object>> response = cartController.clearCart("user@grocery.com");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getBody().get("status"));
    }
}
