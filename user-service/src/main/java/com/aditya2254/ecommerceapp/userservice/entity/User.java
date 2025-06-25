package com.aditya2254.ecommerceapp.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * User entity class that represents a user in the e-commerce application.
 * 
 * This class implements Spring Security's UserDetails interface, which allows it to be used
 * directly by Spring Security for authentication and authorization purposes.
 * 
 * The @Entity annotation marks this class as a JPA entity, meaning it will be mapped to a database table.
 * The @Table annotation specifies the name of the database table as "users".
 * 
 * Lombok annotations:
 * - @Data: Generates getters, setters, equals, hashCode, and toString methods
 * - @NoArgsConstructor: Generates a constructor with no parameters
 * - @AllArgsConstructor: Generates a constructor with all parameters
 * - @Builder: Implements the Builder pattern for creating User objects
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    /**
     * Unique identifier for the user.
     * @Id marks this field as the primary key
     * @GeneratedValue specifies that the ID should be automatically generated
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username for the user. Must be unique and cannot be null.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Email address for the user. Must be unique and cannot be null.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Password for the user. Cannot be null.
     * This should be stored in encrypted form, not plain text.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Role assigned to the user (e.g., ROLE_USER, ROLE_ADMIN).
     * Stored as a string representation of the Role enum.
     */
    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * Returns the authorities (roles) granted to the user.
     * This method is required by the UserDetails interface.
     * 
     * In this implementation, each user has a single role that is converted to a SimpleGrantedAuthority.
     * Spring Security uses these authorities to determine what the user is allowed to do.
     * 
     * @return a collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Indicates whether the user's account has expired.
     * This method is required by the UserDetails interface.
     * 
     * @return true if the account is valid (not expired), false otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     * This method is required by the UserDetails interface.
     * 
     * @return true if the account is not locked, false otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) has expired.
     * This method is required by the UserDetails interface.
     * 
     * @return true if credentials are valid (not expired), false otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     * This method is required by the UserDetails interface.
     * 
     * @return true if the user is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
