package com.grocery.service_product.controller;

import com.grocery.service_product.DTO.ImageResponse;
import com.grocery.service_product.entity.Product;
import com.grocery.service_product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> create(
            @RequestParam("name") String name,
            @RequestParam("price") String priceStr,
            @RequestParam("description") String description,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            double price = 0.0;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException nfe) {
                // handle parse error (return bad request)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(buildResponse("error", "Invalid price format", null));
            }

            Product p = new Product();
            p.setName(name);
            p.setPrice(BigDecimal.valueOf(price));
            p.setDescription(description);
            p.setLike(0L);
            p.setDislike(0L);

            if (image != null && !image.isEmpty()) {
                p.setImageType(image.getContentType());
                p.setImage(image.getBytes());
            }

            Product savedProduct = productService.addProduct(p);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(buildResponse("success", "Product created successfully", "saved it successfully nigga"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
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
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteProduct(@RequestBody long id) {
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

    @GetMapping("/getImage/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable long id) {
        Product product = productService.getProduct(id);

        if (product == null || product.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(product.getImageType()))
                .body(product.getImage());
    }



}
