package com.aditya2254.ecommerceapp.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the User Service application.
 * 
 * This class starts the Spring Boot application and serves as the main configuration point.
 * 
 * The @SpringBootApplication annotation is a convenience annotation that combines:
 * - @Configuration: Tags the class as a source of bean definitions for the application context
 * - @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings
 * - @ComponentScan: Tells Spring to look for other components, configurations, and services
 *   in the com.aditya2254.ecommerceapp.userservice package
 * 
 * In a microservice architecture, this service handles user-related operations such as:
 * - User registration and authentication
 * - User profile management
 * - Role-based authorization
 */
@SpringBootApplication
public class UserServiceApplication {

    /**
     * The main method that serves as the entry point for the application.
     * 
     * This method uses SpringApplication.run() to bootstrap the application,
     * creating the Spring application context and starting the embedded web server.
     * 
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
