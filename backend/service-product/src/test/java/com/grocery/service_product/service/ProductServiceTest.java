package com.grocery.service_product.service;

import com.grocery.service_product.entity.Product;
import com.grocery.service_product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product(1L, "Apple", "Fresh red apple", 10L, 2L, BigDecimal.valueOf(1.50), null, null);
    }

    @Test
    @DisplayName("addProduct - saves product and returns saved instance")
    void addProduct_success_savesProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.addProduct(product);

        assertNotNull(result);
        assertEquals("Apple", result.getName());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("addProduct - repository failure throws exception")
    void addProduct_failure_throwsException() {
        when(productRepository.save(any(Product.class))).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> productService.addProduct(product));
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("getProductById - existing ID returns optional containing product")
    void getProductById_exists_returnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> result = productService.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("Apple", result.get().getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getProductById - non-existing ID returns empty optional")
    void getProductById_notExists_returnsEmpty() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(99L);

        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("getProducts - returns all products from repository")
    void getProducts_returnsAll() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));

        List<Product> result = productService.getProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Apple", result.get(0).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("updateProduct - product exists updates and saves product")
    void updateProduct_exists_updatesAndSaves() {
        when(productRepository.findByName("Apple")).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.updateProduct("Apple", "2.99", "New description");

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(2.99), result.getPrice());
        assertEquals("New description", result.getDescription());
        verify(productRepository, times(1)).findByName("Apple");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("updateProduct - invalid price format uses default value of 0.0")
    void updateProduct_invalidPriceFormat_defaultsToZero() {
        when(productRepository.findByName("Apple")).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.updateProduct("Apple", "invalidPrice", "Desc");

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(0.0), result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("updateProduct - exception is propagated")
    void updateProduct_exception_propagated() {
        when(productRepository.findByName("Apple")).thenThrow(new RuntimeException("Lookup failure"));

        assertThrows(RuntimeException.class, () -> productService.updateProduct("Apple", "1.50", "Desc"));
    }

    @Test
    @DisplayName("deleteProduct - product exists deletes product")
    void deleteProduct_exists_deletes() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(any(Product.class));

        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("deleteProduct - repository failure propagates exception")
    void deleteProduct_failure_propagates() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doThrow(new RuntimeException("Delete error")).when(productRepository).delete(any(Product.class));

        assertThrows(RuntimeException.class, () -> productService.deleteProduct(1L));
    }

    @Test
    @DisplayName("getProductByName - returns matching product")
    void getProductByName_returnsProduct() {
        when(productRepository.findByName("Apple")).thenReturn(product);

        Product result = productService.getProductByName("Apple");

        assertNotNull(result);
        assertEquals("Apple", result.getName());
        verify(productRepository, times(1)).findByName("Apple");
    }

    @Test
    @DisplayName("getProduct - returns product directly or null")
    void getProduct_returnsProductOrNull() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findById(2L)).thenReturn(Optional.empty());

        Product found = productService.getProduct(1L);
        Product notFound = productService.getProduct(2L);

        assertNotNull(found);
        assertEquals("Apple", found.getName());
        assertNull(notFound);
    }
}
