package com.grocery.service_cart.service;

import com.grocery.service_cart.DTO.CheckoutEvent;
import com.grocery.service_cart.entity.Cart;
import com.grocery.service_cart.entity.CartItem;
import com.grocery.service_cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemService cartItemService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        cart = new Cart(1L, "user@grocery.com", new ArrayList<>());
        cartItem = new CartItem(10L, "Apple", "Fresh apple", BigDecimal.valueOf(1.50), 2, cart);
        cart.getItems().add(cartItem);
    }

    // ── createCartForUser ────────────────────────────────────────

    @Test
    @DisplayName("createCartForUser - creates and saves new cart")
    void createCartForUser_success_createsCart() {
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.createCartForUser("new@grocery.com");

        assertNotNull(result);
        assertEquals("new@grocery.com", result.getEmail());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    // ── getCartById ──────────────────────────────────────────────

    @Test
    @DisplayName("getCartById - returns optional cart")
    void getCartById_returnsCartOptional() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        Optional<Cart> result = cartService.getCartById(1L);

        assertTrue(result.isPresent());
        assertEquals("user@grocery.com", result.get().getEmail());
    }

    // ── getCartByUserEmail ───────────────────────────────────────

    @Test
    @DisplayName("getCartByUserEmail - returns optional cart by email")
    void getCartByUserEmail_returnsCartOptional() {
        when(cartRepository.findByEmail("user@grocery.com")).thenReturn(Optional.of(cart));

        Optional<Cart> result = cartService.getCartByUserEmail("user@grocery.com");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    // ── addItemToCart ────────────────────────────────────────────

    @Test
    @DisplayName("addItemToCart - item exists in cart increments quantity")
    void addItemToCart_itemExists_incrementsQuantity() {
        when(cartRepository.findByEmail("user@grocery.com")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.addItemToCart("user@grocery.com", "Apple", 3);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(5, result.getItems().get(0).getQuantity());
        verify(cartRepository, times(1)).save(cart);
        verifyNoInteractions(cartItemService);
    }

    @Test
    @DisplayName("addItemToCart - item does not exist creates new item and adds it")
    void addItemToCart_itemDoesNotExist_createsAndAdds() {
        when(cartRepository.findByEmail("user@grocery.com")).thenReturn(Optional.of(cart));
        CartItem newTransientItem = new CartItem(null, "Orange", "Fresh orange", BigDecimal.valueOf(2.0), 0, null);
        when(cartItemService.createCartItem("Orange", 4)).thenReturn(newTransientItem);
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.addItemToCart("user@grocery.com", "Orange", 4);

        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        CartItem addedItem = result.getItems().stream()
                .filter(i -> i.getName().equals("Orange"))
                .findFirst().orElse(null);
        assertNotNull(addedItem);
        assertEquals(4, addedItem.getQuantity());
        assertEquals(cart, addedItem.getCart());
        verify(cartItemService, times(1)).createCartItem("Orange", 4);
        verify(cartRepository, times(1)).save(cart);
    }

    // ── updateItemQuantity ───────────────────────────────────────

    @Test
    @DisplayName("updateItemQuantity - positive quantity updates quantity")
    void updateItemQuantity_positive_updates() {
        when(cartRepository.findByEmail("user@grocery.com")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.updateItemQuantity("user@grocery.com", "Apple", 5);

        assertNotNull(result);
        assertEquals(5, cartItem.getQuantity());
    }

    @Test
    @DisplayName("updateItemQuantity - zero/negative quantity removes item from cart")
    void updateItemQuantity_zero_removesItem() {
        when(cartRepository.findByEmail("user@grocery.com")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.updateItemQuantity("user@grocery.com", "Apple", 0);

        assertNotNull(result);
        assertEquals(0, result.getItems().size());
    }

    @Test
    @DisplayName("updateItemQuantity - cart not found throws IllegalArgumentException")
    void updateItemQuantity_cartNotFound_throwsException() {
        when(cartRepository.findByEmail("missing@grocery.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            cartService.updateItemQuantity("missing@grocery.com", "Apple", 3);
        });
    }

    // ── removeItemFromCart ───────────────────────────────────────

    @Test
    @DisplayName("removeItemFromCart - removes item from cart")
    void removeItemFromCart_removesItem() {
        when(cartRepository.findByEmail("user@grocery.com")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.removeItemFromCart("user@grocery.com", "Apple");

        assertNotNull(result);
        assertEquals(0, result.getItems().size());
        assertNull(cartItem.getCart());
    }

    // ── clearCart ────────────────────────────────────────────────

    @Test
    @DisplayName("clearCart - clears cart items, calculates bill, and publishes RabbitMQ CheckoutEvent")
    void clearCart_clearsAndPublishesEvent() {
        when(cartRepository.findByEmail("user@grocery.com")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.clearCart("user@grocery.com");

        assertNotNull(result);
        assertEquals(0, result.getItems().size());
        assertNull(cartItem.getCart());

        ArgumentCaptor<CheckoutEvent> eventCaptor = ArgumentCaptor.forClass(CheckoutEvent.class);
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("grocery.email.exchange"),
                eq("email.checkout"),
                eventCaptor.capture()
        );

        CheckoutEvent publishedEvent = eventCaptor.getValue();
        assertEquals("user@grocery.com", publishedEvent.getEmail());
        assertEquals(1, publishedEvent.getItems().size());
        assertEquals("Apple", publishedEvent.getItems().get(0).getName());
        assertEquals(BigDecimal.valueOf(3.00), publishedEvent.getTotalAmount()); // 1.50 * 2
    }

    @Test
    @DisplayName("clearCart - empty cart does not publish CheckoutEvent")
    void clearCart_emptyCart_noPublish() {
        cart.getItems().clear();
        when(cartRepository.findByEmail("user@grocery.com")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.clearCart("user@grocery.com");

        assertNotNull(result);
        verifyNoInteractions(rabbitTemplate);
    }

    // ── listItems ────────────────────────────────────────────────

    @Test
    @DisplayName("listItems - returns cart items list")
    void listItems_returnsList() {
        when(cartRepository.findByEmail("user@grocery.com")).thenReturn(Optional.of(cart));

        java.util.List<CartItem> result = cartService.listItems("user@grocery.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Apple", result.get(0).getName());
    }
}
