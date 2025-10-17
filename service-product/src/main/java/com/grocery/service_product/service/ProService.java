package com.grocery.service_product.service;

import com.grocery.service_product.entity.Product;

import java.util.List;

public interface ProService {

    Product addProduct(Product product);
    List<Product> getProducts();
    Product getProduct(long id);
    Product updateProduct(Product product);
    void deleteProduct(long id);
}
