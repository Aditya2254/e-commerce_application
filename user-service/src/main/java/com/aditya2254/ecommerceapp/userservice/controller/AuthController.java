package com.aditya2254.ecommerceapp.userservice.controller;

import com.aditya2254.ecommerceapp.userservice.dto.*;
import com.aditya2254.ecommerceapp.userservice.entity.User;
import com.aditya2254.ecommerceapp.userservice.exceptions.InvalidTokenException;
import com.aditya2254.ecommerceapp.userservice.exceptions.TokenExpiredException;
import com.aditya2254.ecommerceapp.userservice.exceptions.UserNotFoundException;
import com.aditya2254.ecommerceapp.userservice.repository.UserRepository;
import com.aditya2254.ecommerceapp.userservice.service.AuthService;
import com.aditya2254.ecommerceapp.userservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for handling authentication-related HTTP requests.
 * 
 * This controller provides endpoints for user registration, login, and token refresh.
 * It delegates the actual authentication logic to the AuthService.
 * 
 * The @RestController annotation marks this class as a controller where every method
 * returns a domain object instead of a view. It's shorthand for @Controller and
 * @ResponseBody combined.
 * 
 * The @RequestMapping annotation maps HTTP requests to handler methods of the controller.
 * In this case, all endpoints in this controller will be prefixed with "/api/auth".
 * 
 * The @RequiredArgsConstructor is a Lombok annotation that generates a constructor
 * with required arguments for all final fields, which enables constructor-based
 * dependency injection.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * Service for handling authentication operations.
     */
    private final AuthService authService;

    private final JwtService jwtService;

    private final UserRepository userRepository;

    /**
     * Endpoint for registering a new user.
     * 
     * This endpoint accepts a POST request with a JSON body containing the user's
     * registration information (username, email, password). It delegates to the
     * AuthService to create the user and generate authentication tokens.
     * 
     * URL: POST /api/auth/register
     * 
     * @param request the registration request containing user details
     * @return a ResponseEntity containing the authentication tokens (access and refresh)
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Endpoint for authenticating a user (logging in).
     * 
     * This endpoint accepts a POST request with a JSON body containing the user's
     * credentials (username, password). It delegates to the AuthService to authenticate
     * the user and generate authentication tokens.
     * 
     * URL: POST /api/auth/login
     * 
     * @param request the authentication request containing user credentials
     * @return a ResponseEntity containing the authentication tokens (access and refresh)
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(authService.authenticate(request));
        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication failed");
            error.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * Endpoint for refreshing an access token.
     * 
     * This endpoint accepts a POST request with a JSON body containing a refresh token.
     * It delegates to the AuthService to validate the refresh token and generate a new
     * access token.
     * 
     * URL: POST /api/auth/refresh
     * 
     * @param request the refresh token request
     * @return a ResponseEntity containing the new access token and the same refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    /*@GetMapping("/validate")
    public ResponseEntity<UserDTO> validateToken(@RequestHeader("Authorization") String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Missing or invalid Authorization header");
        }

        String jwt = tokenHeader.replace("Bearer ", "");

        try {
            Claims claims = jwtService.extractAllClaims(jwt);

            // Check expiration
            if (claims.getExpiration().before(new Date())) {
                throw new TokenExpiredException("Token has expired");
            }

            // Get user ID from claims
            Long userId = claims.get("userId", Long.class);
            String roles = claims.get("roles", String.class);

            return ResponseEntity.ok(new UserDTO(userId, List.of(roles)));
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid token: " + e.getMessage());
        }
    }*/
}
