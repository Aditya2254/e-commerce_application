package com.aditya2254.ecommerceapp.cloudgateway.filter;

import com.aditya2254.ecommerceapp.cloudgateway.service.TokenValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GlobalAuthFilter.class);
    private final TokenValidationService tokenValidationService;
    private static final String[] PUBLIC_PATHS = {
            "/user-service/api/auth/register",
            "/user-service/api/auth/login",
            "/user-service/api/auth/refresh",
            "/user-service/api/auth/validate",
            "/user-service/api/users/profile"
    };

    public GlobalAuthFilter(TokenValidationService tokenValidationService) {
        this.tokenValidationService = tokenValidationService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        log.info("Processing request for path: {}", path);

        // Skip authentication for public endpoints
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                log.debug("Skipping auth for public path: {}", path);
                return chain.filter(exchange);
            }
        }

        // Validate token for all other endpoints
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing Authorization header for path: {}", path);
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header"));
        }

        String token = authHeader.substring(7);
        log.debug("Validating token for path: {}", path);

        return tokenValidationService.validateToken(token)
                .flatMap(userDTO -> {
                    log.debug("Token validated successfully for user: {}", userDTO.getUsername());

                    // Add user details to request headers
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-ID", userDTO.getId().toString())
                            .header("X-User-Name", userDTO.getUsername())
                            .header("X-User-Roles", String.join(",", userDTO.getRoles()))
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .onErrorResume(e -> {
                    log.error("Authentication failed for path: {}", path, e);
                    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed"));
                });
    }

    @Override
    public int getOrder() {
        return -100; // Higher precedence than route filters
    }
}