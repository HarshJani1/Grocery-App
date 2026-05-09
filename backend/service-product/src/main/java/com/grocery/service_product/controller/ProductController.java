package com.grocery.service_product.controller;

import com.grocery.service_product.DTO.ApiResponse;
import com.grocery.service_product.entity.Product;
import com.grocery.service_product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

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
        log.info("POST /products/create - Creating product | name={} | price={}", name, priceStr);
        try {
            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException nfe) {
                MDC.put("statusCode", String.valueOf(HttpStatus.BAD_REQUEST.value()));
                log.warn("Invalid price format | name={} | priceStr={}", name, priceStr);
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
                log.debug("Product image attached | name={} | imageType={} | imageSize={}", name, image.getContentType(), image.getSize());
            }

            Product savedProduct = productService.addProduct(p);

            MDC.put("statusCode", String.valueOf(HttpStatus.CREATED.value()));
            log.info("Product created successfully | name={} | id={}", savedProduct.getName(), savedProduct.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(buildResponse("success", "Product created successfully", savedProduct));

        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            log.error("Failed to create product | name={} | error={}", name, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Failed to create product: " + e.getMessage(), null));
        } finally {
            MDC.clear();
        }
    }

    // ✅ GET ALL PRODUCTS
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        log.info("GET /products - Fetching all products");
        try {
            List<Product> products = productService.getProducts();
            MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
            log.info("Products fetched successfully | count={}", products.size());
            return ResponseEntity.ok(buildResponse("success", "Products fetched successfully", products));
        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            log.error("Failed to fetch products | error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Failed to fetch products: " + e.getMessage(), null));
        } finally {
            MDC.clear();
        }
    }

    // ✅ GET PRODUCT BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable long id) {
        log.info("GET /products/{} - Fetching product by ID", id);
        try {
            Product product = productService.getProduct(id);
            if (product == null) {
                MDC.put("statusCode", String.valueOf(HttpStatus.NOT_FOUND.value()));
                log.warn("Product not found | id={}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(buildResponse("error", "Product not found", null));
            }
            MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
            log.info("Product fetched successfully | id={} | name={}", id, product.getName());
            return ResponseEntity.ok(buildResponse("success", "Product fetched successfully", product));
        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            log.error("Error fetching product | id={} | error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Error fetching product: " + e.getMessage(), null));
        } finally {
            MDC.clear();
        }
    }

    // ✅ UPDATE PRODUCT
    @PutMapping(value="/update" ,consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @RequestParam("name") String name,
            @RequestParam("price") String priceStr,
            @RequestParam("description") String description
    ) {
        log.info("PUT /products/update - Updating product | name={} | price={}", name, priceStr);
        try {

            Product updated = productService.updateProduct(name,priceStr,description);
            MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
            log.info("Product updated successfully | name={}", name);
            return ResponseEntity.ok(buildResponse("success", "Product updated successfully", updated));
        } catch (IllegalArgumentException e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.NOT_FOUND.value()));
            log.warn("Product not found for update | name={} | error={}", name, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildResponse("error", "Product not found", null));
        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.BAD_REQUEST.value()));
            log.error("Failed to update product | name={} | error={}", name, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildResponse("error", "Failed to update product: " + e.getMessage(), null));
        } finally {
            MDC.clear();
        }
    }

    // ✅ DELETE PRODUCT
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Object>> deleteProduct(@RequestBody long id) {
        log.info("DELETE /products/delete - Deleting product | id={}", id);
        try {
            productService.deleteProduct(id);
            MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
            log.info("Product deleted successfully | id={}", id);
            return ResponseEntity.ok(buildResponse("success", "Product deleted successfully", null));
        } catch (IllegalArgumentException e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.NOT_FOUND.value()));
            log.warn("Product not found for deletion | id={} | error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildResponse("error", "Product not found", null));
        } catch (Exception e) {
            MDC.put("statusCode", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            log.error("Failed to delete product | id={} | error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse("error", "Failed to delete product: " + e.getMessage(), null));
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/getImage/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable long id) {
        log.info("GET /products/getImage/{} - Fetching product image", id);
        try {
            Product product = productService.getProduct(id);

            if (product == null || product.getImage() == null) {
                MDC.put("statusCode", String.valueOf(HttpStatus.NOT_FOUND.value()));
                log.warn("Product image not found | id={}", id);
                return ResponseEntity.notFound().build();
            }

            MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
            log.info("Product image fetched successfully | id={} | imageType={}", id, product.getImageType());
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(product.getImageType()))
                    .body(product.getImage());
        } finally {
            MDC.clear();
        }
    }
}
