package com.aditya2254.ecommerceapp.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for user registration requests.
 * 
 * This class is used to transfer user registration data from the client to the server
 * when a new user wants to create an account in the e-commerce application.
 * 
 * The data from this DTO will be used to create a new User entity in the database.
 * Typically, the password will be encrypted before storage, and the user will be
 * assigned a default role (usually ROLE_USER).
 * 
 * Lombok annotations:
 * - @Data: Generates getters, setters, equals, hashCode, and toString methods
 * - @Builder: Implements the Builder pattern for creating RegisterRequest objects
 * - @AllArgsConstructor: Generates a constructor with all parameters
 * - @NoArgsConstructor: Generates a constructor with no parameters (required for JSON deserialization)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    /**
     * The desired username for the new user account.
     * This must be unique in the system as it will be used for authentication.
     */
    private String username;

    /**
     * The email address of the new user.
     * This must be unique in the system and may be used for account verification
     * or password recovery.
     */
    private String email;

    /**
     * The password for the new user account.
     * This will be encrypted before being stored in the database.
     */
    private String password;
}
