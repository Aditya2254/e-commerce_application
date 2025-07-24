# E-Commerce User Service Development Guidelines

This document provides essential information for developers working on the User Service component of the E-Commerce application.

## Build and Configuration Instructions

### Prerequisites
- Java 17
- Maven
- MySQL 8.x

### Building the Project
The project uses Maven for dependency management and build processes:

```bash
# Clean and build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### Configuration
The application uses the following configuration:

1. **Service Configuration**:
   - Application name: `user-service`
   - Port: 8110
   - Config server: http://localhost:8888
   - Eureka service registry: http://localhost:8761/eureka

2. **Database Configuration**:
   - MySQL database at `jdbc:mysql://localhost:3306/my_db`
   - Hibernate is configured to automatically update the schema (`spring.jpa.hibernate.ddl-auto=update`)
   - MySQL 8 dialect is used

3. **JWT Configuration**:
   - Secret key is defined in application.properties
   - Access token expiration: 15 minutes
   - Refresh token expiration: 24 hours

## Testing Information

### Running Tests
Tests can be run using Maven:

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=UserTest

# Run a specific test method
mvn test -Dtest=UserTest#testUserCreation
```

### Writing Tests
The project uses JUnit 5 for testing. Here's a guide to writing tests:

1. **Unit Tests**:
   - Place unit tests in the `src/test/java` directory
   - Follow the same package structure as the main code
   - Use descriptive test method names that explain what is being tested
   - Use assertions from `org.junit.jupiter.api.Assertions`

2. **Test Example**:
   Here's an example of a test for the User entity:

   ```java
   package com.aditya2254.ecommerceapp.userservice.entity;

   import org.junit.jupiter.api.Test;
   import static org.junit.jupiter.api.Assertions.*;

   class UserTest {

       @Test
       void testUserCreation() {
           // Arrange
           User user = User.builder()
                   .id(1L)
                   .username("testuser")
                   .email("test@example.com")
                   .password("password123")
                   .role(Role.ROLE_USER)
                   .build();

           // Assert
           assertEquals(1L, user.getId());
           assertEquals("testuser", user.getUsername());
           assertEquals("test@example.com", user.getEmail());
           assertEquals("password123", user.getPassword());
           assertEquals(Role.ROLE_USER, user.getRole());
           assertTrue(user.isAccountNonExpired());
           assertTrue(user.isAccountNonLocked());
           assertTrue(user.isCredentialsNonExpired());
           assertTrue(user.isEnabled());
       }
   }
   ```

3. **Integration Tests**:
   - For integration tests that require Spring context, use `@SpringBootTest`
   - For testing REST controllers, use `@WebMvcTest`
   - For testing JPA repositories, use `@DataJpaTest`

## Development Information

### Project Structure
The project follows a standard Spring Boot application structure:

- `com.aditya2254.ecommerceapp.userservice`
  - `.config`: Configuration classes
  - `.controller`: REST controllers
  - `.dto`: Data Transfer Objects
  - `.entity`: JPA entities
  - `.filter`: Security filters
  - `.repository`: Spring Data JPA repositories
  - `.service`: Business logic services
  - `.util`: Utility classes

### Security Implementation
The application uses Spring Security with JWT for authentication:

1. JWT tokens are used for stateless authentication
2. User roles are defined in the `Role` enum
3. The `JwtAuthFilter` intercepts requests to validate JWT tokens
4. The `JwtService` handles token generation and validation

### Microservice Integration
This service is part of a microservice architecture:

1. It registers with Eureka for service discovery
2. It uses OpenFeign for inter-service communication
3. It can be configured via a central config server

### Code Style Guidelines
1. Use Lombok annotations to reduce boilerplate code
2. Follow standard Java naming conventions
3. Use builder pattern for complex object creation
4. Implement proper exception handling and validation