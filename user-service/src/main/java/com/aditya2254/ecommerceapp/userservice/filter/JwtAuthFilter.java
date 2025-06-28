package com.aditya2254.ecommerceapp.userservice.filter;

import com.aditya2254.ecommerceapp.userservice.exceptions.TokenExpiredException;
import com.aditya2254.ecommerceapp.userservice.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter for validating JWT tokens in HTTP requests.
 * 
 * This filter intercepts every HTTP request and checks for a valid JWT token in the
 * Authorization header. If a valid token is found, it sets up the authentication in
 * the Spring Security context, allowing the request to access protected resources.
 * 
 * The filter extends OncePerRequestFilter to ensure it's applied once per request.
 * 
 * The @Component annotation marks this class as a Spring component, making it
 * eligible for dependency injection and automatic registration in the Spring context.
 * 
 * The @RequiredArgsConstructor is a Lombok annotation that generates a constructor
 * with required arguments for all final fields, which enables constructor-based
 * dependency injection.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    /**
     * Service for JWT operations like extracting claims and validating tokens.
     */
    private final JwtService jwtService;

    /**
     * Service for loading user details from the database.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Processes each HTTP request to validate JWT tokens and set up authentication.
     * 
     * This method:
     * 1. Extracts the JWT token from the Authorization header
     * 2. Extracts the username from the token
     * 3. Loads the user details from the database
     * 4. Validates the token
     * 5. Sets up the authentication in the Spring Security context if the token is valid
     * 
     * The @NonNull annotation indicates that the parameters cannot be null.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to continue processing the request
     * @throws ServletException if a servlet exception occurs
     * @throws IOException if an I/O exception occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // Extract the Authorization header from the request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // If the Authorization header is missing or doesn't start with "Bearer ",
        // continue the filter chain without authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the JWT token (remove "Bearer " prefix)
        jwt = authHeader.substring(7);
        // Extract the username from the token
        try {
            username = jwtService.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token has expired");
        }

        // If the username was successfully extracted and the user is not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load the user details from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            // Validate the token
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Create an authentication token with the user details and authorities
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // No credentials needed here as we're authenticating with a token
                        userDetails.getAuthorities()
                );
                // Add request details to the authentication token
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // Set the authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
