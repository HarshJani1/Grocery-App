package service;

import entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    List<Product> products = new ArrayList<>(List.of(
            new Product(0, "Product A", "Desc A", 10, 2, 100),
            new Product(1, "Product B", "Desc B", 5, 1, 200)
    ));;


    public List<Product> getProducts() {
        return products;
    }

    public Product getProduct(int id) {
        return products.stream().filter(product -> product.getId() == id).findFirst().orElse(null);
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public void updateProduct(int id, Product product) {
        products.set(id, product);
    }

    public void deleteProduct(int id) {
        products.remove(id);
    }
}
