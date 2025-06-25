package com.aditya2254.ecommerceapp.userservice.repository;

import com.aditya2254.ecommerceapp.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * 
 * This interface extends Spring Data JPA's JpaRepository, which provides basic CRUD
 * (Create, Read, Update, Delete) operations for the User entity with a Long primary key.
 * 
 * Spring Data JPA automatically implements this interface at runtime, generating the
 * necessary SQL queries based on the method names. This eliminates the need to write
 * boilerplate data access code.
 * 
 * The @Repository annotation marks this interface as a Spring Data repository, which
 * enables exception translation from JPA exceptions to Spring's DataAccessException hierarchy.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username.
     * 
     * This method is used during authentication to look up a user based on the
     * username provided in the login request.
     * 
     * @param username the username to search for
     * @return an Optional containing the user if found, or an empty Optional if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     * 
     * This method can be used for features like password reset where a user is
     * identified by their email address.
     * 
     * @param email the email address to search for
     * @return an Optional containing the user if found, or an empty Optional if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a username already exists in the database.
     * 
     * This method is used during user registration to ensure that usernames are unique.
     * 
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    Boolean existsByUsername(String username);

    /**
     * Checks if an email address already exists in the database.
     * 
     * This method is used during user registration to ensure that email addresses are unique.
     * 
     * @param email the email address to check
     * @return true if the email exists, false otherwise
     */
    Boolean existsByEmail(String email);
}
