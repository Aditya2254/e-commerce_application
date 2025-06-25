package com.aditya2254.ecommerceapp.userservice.service;

import com.aditya2254.ecommerceapp.userservice.dto.AuthRequest;
import com.aditya2254.ecommerceapp.userservice.dto.AuthResponse;
import com.aditya2254.ecommerceapp.userservice.dto.RegisterRequest;
import com.aditya2254.ecommerceapp.userservice.entity.Role;
import com.aditya2254.ecommerceapp.userservice.entity.User;
import com.aditya2254.ecommerceapp.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for handling authentication operations.
 * 
 * This service is responsible for user registration, authentication, and token refresh.
 * It works with the UserRepository to store and retrieve user data, the PasswordEncoder
 * to securely hash passwords, the JwtService to generate and validate tokens, and the
 * AuthenticationManager to authenticate users.
 * 
 * The @Service annotation marks this class as a Spring service component, making it
 * eligible for dependency injection.
 * 
 * The @RequiredArgsConstructor is a Lombok annotation that generates a constructor
 * with required arguments for all final fields, which enables constructor-based
 * dependency injection.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /**
     * Repository for accessing user data in the database.
     */
    private final UserRepository userRepository;

    /**
     * Encoder for hashing passwords before storing them in the database.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Service for generating and validating JWT tokens.
     */
    private final JwtService jwtService;

    /**
     * Spring Security's authentication manager for authenticating users.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user in the system.
     * 
     * This method:
     * 1. Creates a new User entity from the registration request
     * 2. Encodes the password for secure storage
     * 3. Assigns the ROLE_USER role to the new user
     * 4. Saves the user to the database
     * 5. Generates access and refresh tokens for the new user
     * 
     * @param request the registration request containing username, email, and password
     * @return an AuthResponse containing the access and refresh tokens
     */
    public AuthResponse register(RegisterRequest request) {
        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER) // Default role for new users
                .build();
        userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Authenticates a user with their username and password.
     * 
     * This method:
     * 1. Uses Spring Security's AuthenticationManager to verify the credentials
     * 2. Retrieves the user from the database if authentication is successful
     * 3. Generates access and refresh tokens for the authenticated user
     * 
     * If authentication fails, the AuthenticationManager will throw an exception.
     * 
     * @param request the authentication request containing username and password
     * @return an AuthResponse containing the access and refresh tokens
     * @throws org.springframework.security.core.AuthenticationException if authentication fails
     */
    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Refreshes an access token using a valid refresh token.
     * 
     * This method:
     * 1. Extracts the username from the refresh token
     * 2. Retrieves the user from the database
     * 3. Validates the refresh token
     * 4. If valid, generates a new access token
     * 
     * @param refreshToken the refresh token to use
     * @return an AuthResponse containing the new access token and the same refresh token
     * @throws RuntimeException if the refresh token is invalid
     */
    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtService.getUsernameFromToken(refreshToken);
        var user = userRepository.findByUsername(username)
                .orElseThrow();

        if (jwtService.isTokenValid(refreshToken, user)) {
            var newAccessToken = jwtService.generateToken(user);
            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken) // Same refresh token
                    .build();
        }
        throw new RuntimeException("Invalid refresh token");
    }
}
