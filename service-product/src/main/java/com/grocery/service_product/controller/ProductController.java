package com.grocery.service_product.controller;

import com.grocery.service_product.entity.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.grocery.service_product.service.ProductService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ProductController {


    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    private Map<String, Object> buildResponse(String message, Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "success");
        body.put("message", message);
        body.put("data", data);
        body.put("timestamp", java.time.LocalDateTime.now());
        return body;
    }

    @PostMapping("/createProduct")
    public ResponseEntity<Map<String, Object>> create( @RequestBody Product product) {
        Product savedBook = productService.addProduct(product);
        return ResponseEntity.ok(buildResponse("Book created successfully", savedBook));
    }

    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getAllProducts() {
        List<Product> products = productService.getProducts();
        return ResponseEntity.ok(buildResponse("Books fetched successfully", products));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Map<String, Object>> getProductByName(@PathVariable long id) {
        Product product = productService.getProduct(id);
        return ResponseEntity.ok(buildResponse("Book fetched successfully", product));
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateProduct( @RequestBody Product product) {
        return ResponseEntity.ok(buildResponse("Product updated successfully", productService.updateProduct(product)));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(buildResponse("Product deleted successfully",null));
    }
}
