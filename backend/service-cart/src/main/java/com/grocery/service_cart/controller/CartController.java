package com.grocery.service_cart.controller;

import com.grocery.service_cart.DTO.AddItemRequest;
import com.grocery.service_cart.DTO.DeleteItemRequest;
import com.grocery.service_cart.entity.CartItem;
import com.grocery.service_cart.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

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
        try{

            List<CartItem> items = cartService.listItems(email);
            return ResponseEntity
                    .ok(buildResponse(HttpStatus.OK, "cart fetched", items));

        }catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(buildResponse(HttpStatus.NOT_FOUND, "User not found", null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete user: " + e.getMessage(), null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addItem(@RequestBody AddItemRequest addItemDTO,@RequestHeader String email) {
             cartService.addItemToCart(email,addItemDTO.getProductName(),addItemDTO.getQuantity());
         return ResponseEntity
                 .ok(buildResponse(HttpStatus.OK, "item added to cart", null));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String,Object>> deleteItem(@RequestBody DeleteItemRequest deleteItemRequest,@RequestHeader String email){
        cartService.removeItemFromCart(email, deleteItemRequest.getProductName());
        return ResponseEntity
                .ok(buildResponse(HttpStatus.OK, "item deleted successfully", null));

    }


    @PostMapping("/clear")
    public ResponseEntity<Map<String,Object>> clearCart(@RequestHeader String email){
        cartService.clearCart(email);
        return ResponseEntity.ok(buildResponse(HttpStatus.OK, "cart cleared", null));
    }





}
