# MRP Development Protocol

## Technical Steps and Architecture Decisions

### Project Structure
The application follows a layered architecture with clear separation of concerns:

- **Model Layer**: Contains data transfer objects (User, MediaEntry, Rating)
- **Repository Layer**: Handles database operations with SQL queries
- **Service Layer**: Implements business logic and orchestrates operations
- **Handler Layer**: Manages HTTP request/response handling
- **Database Layer**: Manages database connections
- **Utility Layer**: Provides helper functions (JSON, hashing, tokens)

### HTTP Server Implementation
Chose `com.sun.net.httpserver.HttpServer` as it meets the requirement of using a pure HTTP protocol stack without heavy frameworks like Spring. This lightweight server handles routing and request processing efficiently.

### Database Design
Implemented a normalized PostgreSQL schema with the following key tables:
- `users`: Stores user credentials and tokens
- `media_entries`: Contains media metadata
- `ratings`: Links users to media with scores and comments
- `rating_likes`: Tracks likes on ratings
- `favorites`: Manages user's favorite media

### Authentication System
Implemented token-based authentication using UUID v7 for ordered tokens. The system stores tokens in the database and validates them on protected endpoints.

### Security Measures
- Passwords are hashed using SHA-256 with random salt
- Input validation on all endpoints
- Proper authorization checks (users can only modify their own content)
- Comment moderation system (comments hidden until confirmed)

## Unit Test Coverage

### Test Strategy
Implemented comprehensive unit tests focusing on:
- Business logic validation
- Input validation
- Authentication and authorization
- Database operations
- Utility functions

### Test Coverage Details
Created 22 unit tests across multiple categories:
- **Service Layer (8 tests)**: User, Media, Rating, Favorite, Recommendation services
- **Repository Layer (5 tests)**: User, Media, Rating, Favorite repositories
- **Utility Classes (3 tests)**: JSON, PasswordHasher, TokenUtil utilities
- **Model Classes (2 tests)**: Data model validation
- **Integration Points (4 tests)**: Main class and server startup

### Why Specific Logic Was Tested
- **Authentication**: Critical for security
- **Authorization**: Ensures users can only access authorized resources
- **Input Validation**: Prevents injection attacks and data corruption
- **Business Rules**: Ensures application behaves as specified
- **Data Integrity**: Maintains consistency across operations

## Problems Encountered and Solutions

### Problem 1: Token-Based Authentication
**Issue**: Implementing secure token-based authentication without sessions
**Solution**: Used UUID v7 tokens stored in database with proper validation middleware

### Problem 2: Database Connection Management
**Issue**: Managing PostgreSQL connections efficiently
**Solution**: Implemented connection pooling and proper exception handling

### Problem 3: Comment Moderation System
**Issue**: Implementing comment approval workflow
**Solution**: Added `is_comment_public` flag with confirmation endpoint

### Problem 4: Media Search and Filtering
**Issue**: Complex search with multiple criteria and sorting
**Solution**: Built dynamic SQL queries with parameterized inputs

### Problem 5: Recommendation Algorithm
**Issue**: Creating meaningful recommendations based on user behavior
**Solution**: Implemented collaborative filtering based on genre similarity and rating patterns

## Time Tracking

### Phase 1: Project Setup and Architecture (8 hours)
- Setting up Maven project
- Configuring dependencies
- Designing database schema
- Creating project structure

### Phase 2: Core Implementation (25 hours)
- Implementing HTTP server
- Building repository layer
- Developing service layer
- Creating handler layer
- Implementing authentication

### Phase 3: Feature Implementation (20 hours)
- User management features
- Media CRUD operations
- Rating system
- Favorites functionality
- Recommendation engine

### Phase 4: Testing and Documentation (12 hours)
- Writing unit tests
- Creating Postman collection
- Updating documentation
- Bug fixes and refinements

### Total Development Time: ~65 hours

## Key Design Patterns Used

### Repository Pattern
Encapsulates database operations and provides clean interface to services.

### Service Layer Pattern
Orchestrates business logic and coordinates between repositories.

### MVC-like Architecture
Clear separation between data models, business logic, and HTTP handling.

## Performance Considerations

- Implemented database indexing for frequently queried columns
- Used prepared statements to prevent SQL injection
- Optimized queries with appropriate JOINs and WHERE clauses
- Implemented pagination for large result sets (planned)

## Security Features

- Password hashing with salt
- Input validation and sanitization
- Proper authentication for all sensitive endpoints
- Authorization checks to prevent unauthorized access
- Comment moderation system

## Scalability Considerations

- Modular architecture allows for easy extension
- Database normalization supports growth
- Separation of concerns enables parallel development
- Comprehensive testing ensures maintainability