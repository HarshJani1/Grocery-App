package com.grocery.service_product.service;

import com.grocery.service_product.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ProService {

    Product addProduct(Product product);

    Optional<Product> getProductById(Long id);

    List<Product> getProducts();
    Product getProduct(long id);
    Product updateProduct(
                           String name,
                           String priceStr,
                           String description);
    void deleteProduct(long id);
    Product getProductByName(String name);
//    Optional<Product> getProductById(long id);
}
