package com.aditya2254.ecommerceapp.userservice.controller;

import com.aditya2254.ecommerceapp.userservice.dto.UserDTO;
import com.aditya2254.ecommerceapp.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling user-related HTTP requests.
 * 
 * This controller provides endpoints for retrieving user information.
 * It delegates the actual user data retrieval to the UserService.
 * 
 * The @RestController annotation marks this class as a controller where every method
 * returns a domain object instead of a view. It's shorthand for @Controller and
 * @ResponseBody combined.
 * 
 * The @RequestMapping annotation maps HTTP requests to handler methods of the controller.
 * In this case, all endpoints in this controller will be prefixed with "/api/users".
 * 
 * The @RequiredArgsConstructor is a Lombok annotation that generates a constructor
 * with required arguments for all final fields, which enables constructor-based
 * dependency injection.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    /**
     * Logger for logging user-related operations.
     */
    Logger log = LoggerFactory.getLogger(UserController.class);
    /**
     * Service for handling user-related operations.
     */
    private final UserService userService;

    /**
     * Endpoint for retrieving the authenticated user's profile.
     * 
     * This endpoint accepts a GET request and returns the profile information of the
     * currently authenticated user. It uses Spring Security's SecurityContextHolder
     * to get the current user's authentication details.
     * 
     * This endpoint is protected and can only be accessed by authenticated users.
     * The authentication is handled by Spring Security and the JwtAuthFilter.
     * 
     * URL: GET /api/users/profile
     * 
     * @return a ResponseEntity containing the user's profile information
     */
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile() {

        log.info("Retrieving user profile");
        // Get the current authentication from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication.isAuthenticated() == false || authentication.getPrincipal() == null) {
            throw new IllegalStateException("User is not authenticated");
        }

        // Extract the username from the authentication object
        String username = authentication.getName();

        // Delegate to the UserService to retrieve the user profile
        return ResponseEntity.ok(userService.getUserProfile(username));
    }
}
