package com.aditya2254.ecommerceapp.userservice.service;

import com.aditya2254.ecommerceapp.userservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service class for handling JWT (JSON Web Token) operations.
 * 
 * This service is responsible for generating, validating, and extracting information from JWT tokens.
 * JWT is a compact, URL-safe means of representing claims to be transferred between two parties.
 * 
 * In this application, JWTs are used for:
 * 1. Authentication: Verifying the identity of users
 * 2. Authorization: Determining what resources a user can access
 * 3. Information Exchange: Securely transmitting information between parties
 * 
 * The @Service annotation marks this class as a Spring service component, making it
 * eligible for dependency injection.
 */
@Service
public class JwtService {

    /**
     * Secret key used to sign the JWT tokens.
     * This key is loaded from the application.properties file.
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Expiration time for access tokens in milliseconds.
     * This value is loaded from the application.properties file.
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Expiration time for refresh tokens in milliseconds.
     * This value is loaded from the application.properties file.
     */
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * Extracts the username (subject) from a JWT token.
     * 
     * @param token the JWT token
     * @return the username stored in the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from a JWT token using a claims resolver function.
     * 
     * @param token the JWT token
     * @param claimsResolver a function that extracts a specific claim from the Claims object
     * @return the extracted claim value
     * @param <T> the type of the claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a JWT token for a user with no extra claims.
     * 
     * @param userDetails the user details
     * @return a JWT token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT token for a user with extra claims.
     * 
     * @param extraClaims additional claims to include in the token
     * @param userDetails the user details
     * @return a JWT token
     */
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Generates a refresh token for a user.
     * Refresh tokens have a longer expiration time than regular access tokens.
     * 
     * @param userDetails the user details
     * @return a refresh token
     */
    public String generateRefreshToken(
            UserDetails userDetails
    ) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    /**
     * Builds a JWT token with the specified claims, subject, and expiration time.
     * 
     * @param extraClaims additional claims to include in the token
     * @param userDetails the user details
     * @param expiration the token expiration time in milliseconds
     * @return a JWT token
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates a JWT token for a specific user.
     * A token is valid if:
     * 1. The username in the token matches the provided user details
     * 2. The token has not expired
     * 
     * @param token the JWT token to validate
     * @param userDetails the user details to validate against
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if a JWT token has expired.
     * 
     * @param token the JWT token to check
     * @return true if the token has expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a JWT token.
     * 
     * @param token the JWT token
     * @return the expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts all claims from a JWT token.
     * 
     * @param token the JWT token
     * @return all claims contained in the token
     */
    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Gets the signing key used to verify JWT signatures.
     * The key is derived from the base64-encoded secret key.
     * 
     * @return the signing key
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts the username from a JWT token.
     * This is an alias for extractUsername for backward compatibility.
     * 
     * @param token the JWT token
     * @return the username stored in the token
     */
    public String getUsernameFromToken(String token) {
        return extractUsername(token);
    }
}
