package com.aditya2254.ecommerceapp.cloudgateway.service;

import com.aditya2254.ecommerceapp.cloudgateway.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class TokenValidationService {

    private final WebClient webClient;

    private static final Logger log = LoggerFactory.getLogger(TokenValidationService.class);

    public TokenValidationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("lb://user-service").build();
    }

    public Mono<UserDTO> validateToken(String token) {
        return webClient.get()
                .uri("/api/users/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    log.error("Token validation failed: {}", response.statusCode());
                    return Mono.error(new RuntimeException("Invalid token"));
                })
                .onStatus(status -> status.is5xxServerError(), response -> {
                    log.error("User service error: {}", response.statusCode());
                    return Mono.error(new RuntimeException("Authentication service unavailable"));
                })
                .bodyToMono(UserDTO.class)
                .doOnNext(userDTO -> log.debug("Token validation successful for user: {}", userDTO.getUsername()));
    }
}