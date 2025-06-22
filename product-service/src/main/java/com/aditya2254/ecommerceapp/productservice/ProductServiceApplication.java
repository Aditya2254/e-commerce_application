package com.aditya2254.ecommerceapp.productservice;

import com.aditya2254.ecommerceapp.productservice.entity.Product;
import com.aditya2254.ecommerceapp.productservice.repositories.ProductRepository;
import jakarta.persistence.Id;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProductServiceApplication {

	@Autowired
	private ProductRepository productRepository;

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}



}
