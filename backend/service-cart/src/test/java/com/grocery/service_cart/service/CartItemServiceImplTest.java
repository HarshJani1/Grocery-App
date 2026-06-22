package com.grocery.service_cart.service;

import com.grocery.service_cart.DTO.ApiResponse;
import com.grocery.service_cart.DTO.Product;
import com.grocery.service_cart.OpenFeign.ProductController;
import com.grocery.service_cart.entity.CartItem;
import com.grocery.service_cart.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartItemServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductController productController;

    @InjectMocks
    private CartItemServiceImpl cartItemService;

    private Product product;
    private ApiResponse<java.util.List<Product>> apiResponse;

    @BeforeEach
    void setUp() {
        product = new Product(1L, "Apple", "Fresh apple", 0L, 0L, BigDecimal.valueOf(1.50), null, null);
        apiResponse = new ApiResponse<>();
        apiResponse.setStatus("success");
        apiResponse.setMessage("Products fetched");
        apiResponse.setData(Arrays.asList(product));
    }

    // ── createCartItem ───────────────────────────────────────────

    @Test
    @DisplayName("createCartItem - success returns transient CartItem with product details")
    void createCartItem_success_returnsTransientCartItem() {
        when(productController.getAllProducts()).thenReturn(apiResponse);

        CartItem result = cartItemService.createCartItem("Apple", 3);

        assertNotNull(result);
        assertEquals("Apple", result.getName());
        assertEquals("Fresh apple", result.getDescription());
        assertEquals(BigDecimal.valueOf(1.50), result.getPrice());
        assertEquals(3, result.getQuantity());
        assertNull(result.getId()); // verify transient
        verify(productController, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("createCartItem - empty Feign response throws IllegalStateException")
    void createCartItem_nullResponse_throwsIllegalStateException() {
        when(productController.getAllProducts()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> {
            cartItemService.createCartItem("Apple", 3);
        });
    }

    @Test
    @DisplayName("createCartItem - null data in response throws IllegalStateException")
    void createCartItem_nullData_throwsIllegalStateException() {
        apiResponse.setData(null);
        when(productController.getAllProducts()).thenReturn(apiResponse);

        assertThrows(IllegalStateException.class, () -> {
            cartItemService.createCartItem("Apple", 3);
        });
    }

    @Test
    @DisplayName("createCartItem - product name mismatch throws IllegalArgumentException")
    void createCartItem_productNotFound_throwsIllegalArgumentException() {
        when(productController.getAllProducts()).thenReturn(apiResponse);

        assertThrows(IllegalArgumentException.class, () -> {
            cartItemService.createCartItem("Banana", 2);
        });
    }

    // ── findByProductName ────────────────────────────────────────

    @Test
    @DisplayName("findByProductName - returns optional with CartItem")
    void findByProductName_returnsOptionalCartItem() {
        CartItem cartItem = new CartItem(1L, "Apple", "Fresh apple", BigDecimal.valueOf(1.5), 3, null);
        when(cartItemRepository.findCartItemByName("Apple")).thenReturn(Optional.of(cartItem));

        Optional<CartItem> result = cartItemService.findByProductName("Apple");

        assertTrue(result.isPresent());
        assertEquals("Apple", result.get().getName());
        verify(cartItemRepository, times(1)).findCartItemByName("Apple");
    }

    // ── updateQuantity ───────────────────────────────────────────

    @Test
    @DisplayName("updateQuantity - valid positive quantity updates and saves")
    void updateQuantity_validQuantity_updatesAndSaves() {
        CartItem cartItem = new CartItem(1L, "Apple", "Fresh apple", BigDecimal.valueOf(1.5), 3, null);
        when(cartItemRepository.findCartItemByName("Apple")).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItem result = cartItemService.updateQuantity("Apple", 5);

        assertNotNull(result);
        assertEquals(5, result.getQuantity());
        verify(cartItemRepository, times(1)).findCartItemByName("Apple");
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    @Test
    @DisplayName("updateQuantity - zero or negative quantity deletes item and throws exception")
    void updateQuantity_negativeQuantity_deletesAndThrows() {
        CartItem cartItem = new CartItem(1L, "Apple", "Fresh apple", BigDecimal.valueOf(1.5), 3, null);
        when(cartItemRepository.findCartItemByName("Apple")).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).delete(cartItem);

        assertThrows(IllegalArgumentException.class, () -> {
            cartItemService.updateQuantity("Apple", 0);
        });

        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateQuantity - non-existent item throws IllegalArgumentException")
    void updateQuantity_nonExistentItem_throwsIllegalArgumentException() {
        when(cartItemRepository.findCartItemByName("Ghost")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            cartItemService.updateQuantity("Ghost", 3);
        });
    }

    // ── deleteById ───────────────────────────────────────────────

    @Test
    @DisplayName("deleteById - deletes cart item by product name")
    void deleteById_deletesItem() {
        doNothing().when(cartItemRepository).deleteByName("Apple");

        assertDoesNotThrow(() -> cartItemService.deleteById("Apple"));
        verify(cartItemRepository, times(1)).deleteByName("Apple");
    }

    @Test
    @DisplayName("deleteById - exception propagates")
    void deleteById_exception_propagates() {
        doThrow(new RuntimeException("DB error")).when(cartItemRepository).deleteByName("Apple");

        assertThrows(RuntimeException.class, () -> cartItemService.deleteById("Apple"));
    }
}
