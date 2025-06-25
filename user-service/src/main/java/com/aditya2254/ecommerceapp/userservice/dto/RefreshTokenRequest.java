package com.aditya2254.ecommerceapp.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for refresh token requests.
 * 
 * This class is used when a client wants to obtain a new access token using their refresh token.
 * This typically happens when the access token has expired but the user's session should remain active.
 * 
 * In JWT-based authentication, refresh tokens have a longer lifespan than access tokens.
 * When an access token expires, the client can send the refresh token to get a new access token
 * without requiring the user to log in again.
 * 
 * Lombok annotations:
 * - @Data: Generates getters, setters, equals, hashCode, and toString methods
 * - @Builder: Implements the Builder pattern for creating RefreshTokenRequest objects
 * - @AllArgsConstructor: Generates a constructor with all parameters
 * - @NoArgsConstructor: Generates a constructor with no parameters (required for JSON deserialization)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {
    /**
     * The refresh token that was previously issued during authentication.
     * 
     * This token is sent to the server to request a new access token when the original
     * access token has expired. The server will validate this refresh token and, if valid,
     * issue a new access token to the client.
     */
    private String refreshToken;
}
