package com.aditya2254.ecommerceapp.cloudgateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        return createFallbackResponse("User Service is unavailable");
    }

    @RequestMapping("/fallback/product-service")
    public Mono<ResponseEntity<Map<String, Object>>> productServiceFallback() {
        return createFallbackResponse("Product Service is unavailable");
    }

    @RequestMapping("/fallback/order-service")
    public Mono<ResponseEntity<Map<String, Object>>> orderServiceFallback() {
        return createFallbackResponse("Order Service is unavailable");
    }

    private Mono<ResponseEntity<Map<String, Object>>> createFallbackResponse(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        body.put("error", "Service Unavailable");
        body.put("message", message);

        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(body));
    }
}