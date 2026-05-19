package com.grocery.service_email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ServiceEmailApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceEmailApplication.class, args);
	}

}
