package com.grocery.service_product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ServiceProductApplication {
	public static void main(String[] args) {
		SpringApplication.run(ServiceProductApplication.class, args);
	}
}
