package com.aditya2254.ecommerceapp.userservice.service;

import com.aditya2254.ecommerceapp.userservice.dto.UserDTO;
import com.aditya2254.ecommerceapp.userservice.entity.User;
import com.aditya2254.ecommerceapp.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for handling user-related operations.
 * 
 * This service implements Spring Security's UserDetailsService interface, which is
 * used by Spring Security during the authentication process to load user details.
 * 
 * The class also provides methods for retrieving user profile information.
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
public class UserService implements UserDetailsService {

    /**
     * Repository for accessing user data in the database.
     */
    private final UserRepository userRepository;

    /**
     * Loads a user by their username.
     * 
     * This method is required by the UserDetailsService interface and is used by
     * Spring Security during the authentication process. It retrieves a user from
     * the database based on their username.
     * 
     * The User entity implements UserDetails, so it can be returned directly.
     * 
     * @param username the username to search for
     * @return the UserDetails object containing the user's information
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Retrieves a user's profile information.
     * 
     * This method finds a user by their username and converts their information
     * into a UserProfileResponse DTO, which contains only the information that
     * is safe to expose to clients.
     * 
     * @param username the username of the user whose profile to retrieve
     * @return a UserProfileResponse containing the user's profile information
     * @throws UsernameNotFoundException if the user is not found
     */
    public UserDTO getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(List.of(user.getRole().name()))
                .build();
    }
}
