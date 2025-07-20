package com.aditya2254.ecommerceapp.cloudgateway.filter;

import com.aditya2254.ecommerceapp.cloudgateway.service.TokenValidationService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final TokenValidationService tokenValidationService;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthenticationFilter.class);

    public AuthenticationFilter(TokenValidationService tokenValidationService) {
        super(Config.class);
        this.tokenValidationService = tokenValidationService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        log.info("-------------- Authentication Filter -------");
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            log.info("Received request for path: " + request.getURI().getPath());
            log.info("Request Path: {}", request.getPath());

            // Skip authentication for auth endpoints
            if (request.getPath().toString().startsWith("/api/auth")) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
            }

            String token = authHeader.substring(7);

            return tokenValidationService.validateToken(token)
                    .flatMap(userDTO -> {
                        // Add user ID to headers
                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header("X-User-ID", userDTO.getId().toString())
                                .header("X-User-Roles", String.join(",", userDTO.getRoles()))
                                .build();

                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    })
                    .onErrorResume(e -> Mono.error(
                            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token", e)
                    ));
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}