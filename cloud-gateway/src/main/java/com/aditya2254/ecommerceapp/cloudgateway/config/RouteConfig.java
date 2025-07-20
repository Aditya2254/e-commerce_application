package com.aditya2254.ecommerceapp.cloudgateway.config;

import com.aditya2254.ecommerceapp.cloudgateway.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder, AuthenticationFilter authenticationFilter) {
        return builder.routes()
                // User Service Routes
                .route("user-service", r -> r.path("/api/auth/**", "/api/users/**")
                        .filters(f -> f.stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("user-service-cb")
                                        .setFallbackUri("forward:/fallback/user-service")))
                        .uri("lb://USER-SERVICE"))

                // Product Service Routes
                .route("product-service", r -> r.path("/api/products/**")
                        .filters(f -> f.stripPrefix(1)
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("product-service-cb")
                                        .setFallbackUri("forward:/fallback/product-service")))
                        .uri("lb://PRODUCT-SERVICE"))

                // Order Service Routes
                .route("order-service", r -> r.path("/api/orders/**")
                        .filters(f -> f.stripPrefix(1)
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("order-service-cb")
                                        .setFallbackUri("forward:/fallback/order-service")))
                        .uri("lb://ORDER-SERVICE"))

                // API Gateway Fallback Routes
                .route("fallback-route", r -> r.path("/fallback/**")
                        .uri("no://op"))
                .build();
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}