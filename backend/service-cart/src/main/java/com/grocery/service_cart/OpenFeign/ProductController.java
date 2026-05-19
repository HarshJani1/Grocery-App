package com.grocery.service_cart.OpenFeign;

import com.grocery.service_cart.DTO.ApiResponse;
import com.grocery.service_cart.DTO.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("service-product/products")
public interface ProductController {
    @GetMapping
    ApiResponse<List<Product>> getAllProducts();
}
