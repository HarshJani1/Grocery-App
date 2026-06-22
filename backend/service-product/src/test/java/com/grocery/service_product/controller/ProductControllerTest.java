package com.grocery.service_product.controller;

import com.grocery.service_product.DTO.ApiResponse;
import com.grocery.service_product.entity.Product;
import com.grocery.service_product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product(1L, "Apple", "Fresh red apple", 10L, 2L, BigDecimal.valueOf(1.50), "imageData".getBytes(), "image/png");
    }

    // ── create ───────────────────────────────────────────────────

    @Test
    @DisplayName("create - valid parameters returns 201 CREATED")
    void create_validParams_returns201() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "test.png", "image/png", "imageData".getBytes());
        when(productService.addProduct(any(Product.class))).thenReturn(product);

        ResponseEntity<ApiResponse<Product>> response = productController.create("Apple", "1.50", "Fresh red apple", image);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getStatus());
        assertEquals(product, response.getBody().getData());
    }

    @Test
    @DisplayName("create - invalid price format returns 400 BAD_REQUEST")
    void create_invalidPrice_returns400() {
        ResponseEntity<ApiResponse<Product>> response = productController.create("Apple", "invalidPrice", "Fresh red apple", null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
        assertEquals("Invalid price format", response.getBody().getMessage());
    }

    @Test
    @DisplayName("create - general exception returns 500 INTERNAL_SERVER_ERROR")
    void create_exception_returns500() {
        when(productService.addProduct(any(Product.class))).thenThrow(new RuntimeException("Server error"));

        ResponseEntity<ApiResponse<Product>> response = productController.create("Apple", "1.50", "Fresh red apple", null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Failed to create product"));
    }

    // ── getAllProducts ───────────────────────────────────────────

    @Test
    @DisplayName("getAllProducts - success returns 200 with all products")
    void getAllProducts_success_returns200() {
        when(productService.getProducts()).thenReturn(Arrays.asList(product));

        ResponseEntity<ApiResponse<List<Product>>> response = productController.getAllProducts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getStatus());
        assertEquals(Arrays.asList(product), response.getBody().getData());
    }

    @Test
    @DisplayName("getAllProducts - exception returns 500")
    void getAllProducts_exception_returns500() {
        when(productService.getProducts()).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<ApiResponse<List<Product>>> response = productController.getAllProducts();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
    }

    // ── getProductById ───────────────────────────────────────────

    @Test
    @DisplayName("getProductById - product exists returns 200")
    void getProductById_exists_returns200() {
        when(productService.getProduct(1L)).thenReturn(product);

        ResponseEntity<ApiResponse<Product>> response = productController.getProductById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getStatus());
        assertEquals(product, response.getBody().getData());
    }

    @Test
    @DisplayName("getProductById - product not found returns 404")
    void getProductById_notFound_returns404() {
        when(productService.getProduct(99L)).thenReturn(null);

        ResponseEntity<ApiResponse<Product>> response = productController.getProductById(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
        assertEquals("Product not found", response.getBody().getMessage());
    }

    @Test
    @DisplayName("getProductById - exception returns 500")
    void getProductById_exception_returns500() {
        when(productService.getProduct(1L)).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<ApiResponse<Product>> response = productController.getProductById(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
    }

    // ── updateProduct ────────────────────────────────────────────

    @Test
    @DisplayName("updateProduct - success returns 200")
    void updateProduct_success_returns200() {
        when(productService.updateProduct("Apple", "1.99", "Desc")).thenReturn(product);

        ResponseEntity<ApiResponse<Product>> response = productController.updateProduct("Apple", "1.99", "Desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getStatus());
    }

    @Test
    @DisplayName("updateProduct - product not found returns 404")
    void updateProduct_notFound_returns404() {
        when(productService.updateProduct("Ghost", "1.99", "Desc"))
                .thenThrow(new IllegalArgumentException("Product not found"));

        ResponseEntity<ApiResponse<Product>> response = productController.updateProduct("Ghost", "1.99", "Desc");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
    }

    @Test
    @DisplayName("updateProduct - exception returns 400 BAD_REQUEST")
    void updateProduct_exception_returns400() {
        when(productService.updateProduct("Apple", "1.99", "Desc"))
                .thenThrow(new RuntimeException("Invalid params"));

        ResponseEntity<ApiResponse<Product>> response = productController.updateProduct("Apple", "1.99", "Desc");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
    }

    // ── deleteProduct ────────────────────────────────────────────

    @Test
    @DisplayName("deleteProduct - success returns 200")
    void deleteProduct_success_returns200() {
        doNothing().when(productService).deleteProduct(1L);

        ResponseEntity<ApiResponse<Object>> response = productController.deleteProduct(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getStatus());
        assertEquals("Product deleted successfully", response.getBody().getMessage());
    }

    @Test
    @DisplayName("deleteProduct - product not found returns 404")
    void deleteProduct_notFound_returns404() {
        doThrow(new IllegalArgumentException("Not found")).when(productService).deleteProduct(99L);

        ResponseEntity<ApiResponse<Object>> response = productController.deleteProduct(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
    }

    @Test
    @DisplayName("deleteProduct - exception returns 500")
    void deleteProduct_exception_returns500() {
        doThrow(new RuntimeException("Database error")).when(productService).deleteProduct(1L);

        ResponseEntity<ApiResponse<Object>> response = productController.deleteProduct(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
    }

    // ── getImage ─────────────────────────────────────────────────

    @Test
    @DisplayName("getImage - success returns 200 with image bytes")
    void getImage_success_returns200() {
        when(productService.getProduct(1L)).thenReturn(product);

        ResponseEntity<byte[]> response = productController.getImage(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        assertArrayEquals("imageData".getBytes(), response.getBody());
    }

    @Test
    @DisplayName("getImage - product or image null returns 404")
    void getImage_nullImage_returns404() {
        Product noImageProduct = new Product(2L, "Banana", "Desc", 0L, 0L, BigDecimal.ONE, null, null);
        when(productService.getProduct(2L)).thenReturn(noImageProduct);

        ResponseEntity<byte[]> response = productController.getImage(2L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
