package com.grocery.service_cart.controller;

import com.grocery.service_cart.DTO.AddItemRequest;
import com.grocery.service_cart.DTO.DeleteItemRequest;
import com.grocery.service_cart.entity.CartItem;
import com.grocery.service_cart.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private Map<String, Object> buildResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("message", message);
        body.put("data", data);
        body.put("timestamp", java.time.LocalDateTime.now());
        return body;
    }

    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getUserCart(@RequestHeader String email) {
        log.info("GET /cart/get - Fetching cart | email={}", email);
        try{

            List<CartItem> items = cartService.listItems(email);
            MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
            log.info("Cart fetched successfully | email={} | itemCount={}", email, items.size());
            return ResponseEntity
                    .ok(buildResponse(HttpStatus.OK, "cart fetched", items));

        }catch (IllegalArgumentException e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.NOT_FOUND.value()));
            log.warn("Cart not found | email={} | error={}", email, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(buildResponse(HttpStatus.NOT_FOUND, "User not found", null));
        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            log.error("Failed to fetch cart | email={} | error={}", email, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete user: " + e.getMessage(), null));
        } finally {
            MDC.clear();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addItem(@RequestBody AddItemRequest addItemDTO,@RequestHeader String email) {
        log.info("POST /cart/add - Adding item | email={} | product={} | quantity={}", email, addItemDTO.getProductName(), addItemDTO.getQuantity());
        try {
            cartService.addItemToCart(email, addItemDTO.getProductName(), addItemDTO.getQuantity());
            MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
            log.info("Item added to cart successfully | email={} | product={}", email, addItemDTO.getProductName());
            return ResponseEntity
                    .ok(buildResponse(HttpStatus.OK, "item added to cart", null));
        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            log.error("Failed to add item to cart | email={} | product={} | error={}", email, addItemDTO.getProductName(), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add item: " + e.getMessage(), null));
        } finally {
            MDC.clear();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String,Object>> deleteItem(@RequestBody DeleteItemRequest deleteItemRequest,@RequestHeader String email){
        log.info("DELETE /cart/delete - Removing item | email={} | product={}", email, deleteItemRequest.getProductName());
        try {
            cartService.removeItemFromCart(email, deleteItemRequest.getProductName());
            MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
            log.info("Item removed from cart successfully | email={} | product={}", email, deleteItemRequest.getProductName());
            return ResponseEntity
                    .ok(buildResponse(HttpStatus.OK, "item deleted successfully", null));
        } catch (IllegalArgumentException e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.NOT_FOUND.value()));
            log.warn("Item not found for deletion | email={} | product={} | error={}", email, deleteItemRequest.getProductName(), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(buildResponse(HttpStatus.NOT_FOUND, "Item not found: " + e.getMessage(), null));
        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            log.error("Failed to delete item from cart | email={} | product={} | error={}", email, deleteItemRequest.getProductName(), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete item: " + e.getMessage(), null));
        } finally {
            MDC.clear();
        }
    }


    @PostMapping("/clear")
    public ResponseEntity<Map<String,Object>> clearCart(@RequestHeader String email){
        log.info("POST /cart/clear - Clearing cart | email={}", email);
        try {
            cartService.clearCart(email);
            MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
            log.info("Cart cleared successfully | email={}", email);
            return ResponseEntity.ok(buildResponse(HttpStatus.OK, "cart cleared", null));
        } catch (IllegalArgumentException e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.NOT_FOUND.value()));
            log.warn("Cart not found for clearing | email={} | error={}", email, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(buildResponse(HttpStatus.NOT_FOUND, "Cart not found: " + e.getMessage(), null));
        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            log.error("Failed to clear cart | email={} | error={}", email, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to clear cart: " + e.getMessage(), null));
        } finally {
            MDC.clear();
        }
    }




}
