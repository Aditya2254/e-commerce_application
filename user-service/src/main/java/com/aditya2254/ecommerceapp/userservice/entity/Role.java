package com.aditya2254.ecommerceapp.userservice.entity;

/**
 * Enum representing the possible roles a user can have in the e-commerce application.
 * 
 * In Spring Security, roles are used to determine what actions a user is allowed to perform.
 * The naming convention with the "ROLE_" prefix is important for Spring Security's role-based
 * authorization to work correctly with annotations like @PreAuthorize("hasRole('ROLE_ADMIN')").
 * 
 * Available roles:
 * - ROLE_USER: Regular user with basic permissions (e.g., browse products, place orders)
 * - ROLE_ADMIN: Administrator with full access to all features
 * - ROLE_MODERATOR: Moderator with elevated permissions but less than an admin
 */
public enum Role {
    /**
     * Regular user role with basic permissions
     */
    ROLE_USER,

    /**
     * Administrator role with full access to all features
     */
    ROLE_ADMIN,

    /**
     * Moderator role with elevated permissions but less than an admin
     */
    ROLE_MODERATOR
}
