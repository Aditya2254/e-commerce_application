package com.aditya2254.ecommerceapp.userservice.controller;

import com.aditya2254.ecommerceapp.userservice.dto.AuthRequest;
import com.aditya2254.ecommerceapp.userservice.dto.AuthResponse;
import com.aditya2254.ecommerceapp.userservice.dto.RefreshTokenRequest;
import com.aditya2254.ecommerceapp.userservice.dto.RegisterRequest;
import com.aditya2254.ecommerceapp.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<AuthResponse> authenticate(
            @RequestBody AuthRequest request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
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
}
