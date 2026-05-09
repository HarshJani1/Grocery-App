package com.grocery.service_product.service;

import com.grocery.service_product.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.grocery.service_product.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService implements ProService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product addProduct(Product product) {
        log.info("Adding product | name={}", product.getName());
        try {
            Product saved = productRepository.save(product);
            log.info("Product added successfully | name={} | id={}", saved.getName(), saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to add product | name={} | error={}", product.getName(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        log.debug("Fetching product by ID | id={}", id);
        return this.productRepository.findById(id);
    }

    @Override
    public List<Product> getProducts() {
        log.debug("Fetching all products");
        List<Product> products = productRepository.findAll();
        log.debug("Products fetched | count={}", products.size());
        return products;
    }

    @Override
    public Product updateProduct(
                                 String name,
                                 String priceStr,
                                 String description) {
        log.info("Updating product | name={} | price={}", name, priceStr);
        try {
            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException nfe) {
                log.warn("Invalid price format, defaulting to 0.0 | name={} | priceStr={}", name, priceStr);
                price = 0.0;
            }
            Product p = getProductByName(name);
            p.setName(name);
            p.setPrice(BigDecimal.valueOf(price));
            p.setDescription(description);

            Product saved = productRepository.save(p);
            log.info("Product updated successfully | name={} | id={}", saved.getName(), saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to update product | name={} | error={}", name, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void deleteProduct(long id) {
        log.info("Deleting product | id={}", id);
        try {
            productRepository.delete(getProduct(id));
            log.info("Product deleted successfully | id={}", id);
        } catch (Exception e) {
            log.error("Failed to delete product | id={} | error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Product getProductByName(String name) {
        log.debug("Fetching product by name | name={}", name);
        Product product = productRepository.findByName(name);
        if (product != null) {
            log.debug("Product found | name={} | id={}", name, product.getId());
        } else {
            log.warn("Product not found | name={}", name);
        }
        return product;
    }

    @Override
    public Product getProduct(long id) {
        log.debug("Fetching product | id={}", id);
        return productRepository.findById(id).orElse(null);
    }
}
