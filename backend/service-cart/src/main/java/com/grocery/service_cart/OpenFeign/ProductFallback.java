package com.grocery.service_cart.OpenFeign;

import com.grocery.service_cart.DTO.ApiResponse;
import com.grocery.service_cart.DTO.Product;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ProductFallback implements ProductController {
    @Override
    public ApiResponse<List<Product>> getAllProducts() {
        return new ApiResponse<>(
            503,
            "Product service is temporarily unavailable. Please try again later.",
            Collections.emptyList()
        );
    }
}
