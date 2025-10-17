package com.grocery.service_product.service;

import com.grocery.service_product.entity.Product;
import org.springframework.stereotype.Service;
import com.grocery.service_product.repository.ProductRepository;

import java.util.List;

@Service
public class ProductService implements ProService {

    private final ProductRepository productRepository;
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> getProducts() {

        return productRepository.findAll();
    }

    @Override
    public Product updateProduct(Product product) {
        Product oldProduct = getProduct(product.getId());
        oldProduct.setName(product.getName());
        oldProduct.setPrice(product.getPrice());
        oldProduct.setDescription(product.getDescription());
        oldProduct.setDislike(product.getDislike());
        oldProduct.setLike(product.getLike());

        return productRepository.save(oldProduct);
    }

    @Override
    public void deleteProduct(long id) {
        productRepository.delete(getProduct(id));
    }

    @Override
    public Product getProduct(long id) {
        return productRepository.findById(id).orElse(null);
    }
}
