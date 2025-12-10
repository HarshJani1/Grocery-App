package com.grocery.service_product.controller;

import com.grocery.service_product.DTO.ApiResponse;
import com.grocery.service_product.entity.Product;
import com.grocery.service_product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    private <T> ApiResponse<T> buildResponse(String status, String message, T data) {
        ApiResponse<T> body = new ApiResponse<>();
        body.setStatus(status);
        body.setMessage(message);
        body.setData(data);
        return body;
    }

    // ✅ CREATE PRODUCT
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Product>> create(
            @RequestParam("name") String name,
            @RequestParam("price") String priceStr,
            @RequestParam("description") String description,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException nfe) {
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
                    .body(buildResponse("success", "Product created successfully", savedProduct));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Failed to create product: " + e.getMessage(), null));
        }
    }

    // ✅ GET ALL PRODUCTS
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        try {
            List<Product> products = productService.getProducts();
            return ResponseEntity.ok(buildResponse("success", "Products fetched successfully", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Failed to fetch products: " + e.getMessage(), null));
        }
    }

    // ✅ GET PRODUCT BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable long id) {
        try {
            Product product = productService.getProduct(id);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(buildResponse("error", "Product not found", null));
            }
            return ResponseEntity.ok(buildResponse("success", "Product fetched successfully", product));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Error fetching product: " + e.getMessage(), null));
        }
    }

    // ✅ UPDATE PRODUCT
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Product>> updateProduct(@RequestBody Product product) {
        try {
            Product updated = productService.updateProduct(product);
            return ResponseEntity.ok(buildResponse("success", "Product updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildResponse("error", "Product not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildResponse("error", "Failed to update product: " + e.getMessage(), null));
        }
    }

    // ✅ DELETE PRODUCT
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Object>> deleteProduct(@RequestBody long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(buildResponse("success", "Product deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildResponse("error", "Product not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
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
