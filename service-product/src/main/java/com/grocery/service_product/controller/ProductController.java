package com.grocery.service_product.controller;

import com.grocery.service_product.entity.Product;
import com.grocery.service_product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    private Map<String, Object> buildResponse(String status, String message, Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("message", message);
        body.put("data", data);
        body.put("timestamp", java.time.LocalDateTime.now());
        return body;
    }

    // ✅ CREATE PRODUCT
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Product product) {
        try {
            Product savedProduct = productService.addProduct(product);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(buildResponse("success", "Product created successfully", savedProduct));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Failed to create product: " + e.getMessage(), null));
        }
    }

    // ✅ GET ALL PRODUCTS
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts() {
        try {
            List<Product> products = productService.getProducts();
            return ResponseEntity
                    .ok(buildResponse("success", "Products fetched successfully", products));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Failed to fetch products: " + e.getMessage(), null));
        }
    }

    // ✅ GET PRODUCT BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable long id) {
        try {
            Product product = productService.getProduct(id);
            if (product == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(buildResponse("error", "Product not found", null));
            }
            return ResponseEntity
                    .ok(buildResponse("success", "Product fetched successfully", product));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Error fetching product: " + e.getMessage(), null));
        }
    }

    // ✅ UPDATE PRODUCT
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateProduct(@RequestBody Product product) {
        try {
            Product updated = productService.updateProduct(product);
            return ResponseEntity
                    .ok(buildResponse("success", "Product updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(buildResponse("error", "Product not found", null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(buildResponse("error", "Failed to update product: " + e.getMessage(), null));
        }
    }

    // ✅ DELETE PRODUCT
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity
                    .ok(buildResponse("success", "Product deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(buildResponse("error", "Product not found", null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Failed to delete product: " + e.getMessage(), null));
        }
    }
}
