package com.grocery.service_cart.OpenFeign;

import com.grocery.service_cart.DTO.ApiResponse;
import com.grocery.service_cart.DTO.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.InterfaceAddress;
import java.util.List;
import java.util.Map;

@FeignClient("service-product/products")
public interface ProductController {
    @GetMapping
    ApiResponse<List<Product>> getAllProducts();
}
