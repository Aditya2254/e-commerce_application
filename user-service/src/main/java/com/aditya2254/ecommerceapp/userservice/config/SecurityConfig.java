package com.aditya2254.ecommerceapp.userservice.config;

import com.aditya2254.ecommerceapp.userservice.filter.JwtAuthFilter;
import com.aditya2254.ecommerceapp.userservice.util.CustomAccessDeniedHandler;
import com.aditya2254.ecommerceapp.userservice.util.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration class for Spring Security.
 * 
 * This class defines how authentication and authorization are handled in the application.
 * It configures a stateless security filter chain with JWT-based authentication.
 * 
 * The @Configuration annotation marks this class as a source of bean definitions.
 * 
 * The @EnableWebSecurity annotation enables Spring Security's web security support.
 * 
 * The @EnableMethodSecurity annotation enables method-level security using annotations
 * like @PreAuthorize and @PostAuthorize.
 * 
 * The @RequiredArgsConstructor is a Lombok annotation that generates a constructor
 * with required arguments for all final fields, which enables constructor-based
 * dependency injection.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Custom JWT authentication filter that validates JWT tokens in HTTP requests.
     */
    private final JwtAuthFilter authFilter;

    /**
     * Service for loading user details from the database.
     * This is used by the authentication provider to authenticate users.
     */
    private final UserDetailsService userDetailsService;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    /**
     * Configures the security filter chain.
     * 
     * This method defines:
     * 1. Which endpoints are secured and which are public
     * 2. How authentication is handled
     * 3. Session management policy
     * 4. The order of security filters
     * 
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Disable CSRF protection as we're using stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)
                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to authentication endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/users/**").permitAll()
                        // Require authentication for all other endpoints
                        .anyRequest().authenticated()
                )
                // Configure session management to be stateless (no session)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Set the authentication provider
                .authenticationProvider(authenticationProvider())
                // Add the JWT filter before the standard authentication filter
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .build();
    }

    /**
     * Creates a password encoder bean for securely hashing passwords.
     * 
     * BCrypt is a strong hashing function designed for passwords.
     * It automatically handles salt generation and verification.
     * 
     * @return a BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates an authentication provider bean.
     * 
     * The DaoAuthenticationProvider is a standard authentication provider that
     * uses a UserDetailsService to load user details and a PasswordEncoder to
     * verify passwords.
     * 
     * @return a configured DaoAuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Creates an authentication manager bean.
     * 
     * The authentication manager is used to authenticate users during the login process.
     * It delegates to the authentication provider configured above.
     * 
     * @param config the authentication configuration
     * @return the authentication manager
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
