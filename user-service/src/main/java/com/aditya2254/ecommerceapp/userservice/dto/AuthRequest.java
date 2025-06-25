package com.aditya2254.ecommerceapp.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for authentication requests.
 * 
 * This class is used to transfer authentication data from the client to the server.
 * It contains the username and password that a user submits when trying to log in.
 * 
 * DTOs are used to decouple the client-facing API from the internal domain model.
 * This separation helps maintain clean architecture and prevents exposing sensitive
 * internal details to the client.
 * 
 * Lombok annotations:
 * - @Data: Generates getters, setters, equals, hashCode, and toString methods
 * - @Builder: Implements the Builder pattern for creating AuthRequest objects
 * - @AllArgsConstructor: Generates a constructor with all parameters
 * - @NoArgsConstructor: Generates a constructor with no parameters (required for JSON deserialization)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    /**
     * The username provided by the user for authentication.
     * This will be matched against the username stored in the database.
     */
    private String username;

    /**
     * The password provided by the user for authentication.
     * This will be matched against the encrypted password stored in the database.
     */
    private String password;
}
