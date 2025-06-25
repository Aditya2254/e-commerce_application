package com.aditya2254.ecommerceapp.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for user profile responses.
 * 
 * This class is used to transfer user profile data from the server to the client
 * when a client requests information about a user. It contains only the necessary
 * user information that is safe to expose to clients.
 * 
 * Note that sensitive information like passwords is not included in this DTO.
 * This is an example of how DTOs help maintain security by controlling what data
 * is exposed to clients.
 * 
 * Lombok annotations:
 * - @Data: Generates getters, setters, equals, hashCode, and toString methods
 * - @Builder: Implements the Builder pattern for creating UserProfileResponse objects
 * - @AllArgsConstructor: Generates a constructor with all parameters
 * - @NoArgsConstructor: Generates a constructor with no parameters (required for JSON serialization)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    /**
     * The unique identifier of the user.
     */
    private Long id;

    /**
     * The username of the user.
     * This is used for authentication and is displayed in the user interface.
     */
    private String username;

    /**
     * The email address of the user.
     * This may be used for communication or account recovery.
     */
    private String email;

    /**
     * The role of the user in the system (e.g., "ROLE_USER", "ROLE_ADMIN").
     * This determines what actions the user is allowed to perform.
     */
    private String role;
}
