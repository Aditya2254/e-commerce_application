package com.aditya2254.ecommerceapp.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for authentication responses.
 * 
 * This class is used to transfer authentication result data from the server to the client
 * after a successful authentication. It contains the JWT tokens that the client will use
 * for subsequent authenticated requests.
 * 
 * In JWT-based authentication:
 * - The access token is used for authorizing requests to protected resources
 * - The refresh token is used to obtain a new access token when the current one expires
 * 
 * Lombok annotations:
 * - @Data: Generates getters, setters, equals, hashCode, and toString methods
 * - @Builder: Implements the Builder pattern for creating AuthResponse objects
 * - @AllArgsConstructor: Generates a constructor with all parameters
 * - @NoArgsConstructor: Generates a constructor with no parameters (required for JSON serialization)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    /**
     * The JWT access token issued to the client after successful authentication.
     * 
     * This token is short-lived (typically 15 minutes) and is used to access protected resources.
     * It should be included in the Authorization header of subsequent HTTP requests.
     */
    private String accessToken;

    /**
     * The JWT refresh token issued to the client after successful authentication.
     * 
     * This token is long-lived (typically 24 hours or more) and is used to obtain a new
     * access token when the current one expires. This prevents the user from having to
     * log in again when their access token expires.
     */
    private String refreshToken;
}
