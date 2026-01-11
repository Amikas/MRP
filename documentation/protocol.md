# Media Ratings Platform (MRP) - Development Protocol

## Project Overview

I built the Media Ratings Platform (MRP) as a REST API in Java that lets users manage and rate media content like movies, series, and games. The app handles user accounts, media entries, ratings, favorites, and recommendations.

**What I implemented:**
- User registration and login with secure tokens
- Full CRUD operations for media content
- Rating system with optional comments
- Ability to like other people's ratings
- Favorites system
- Personalized recommendations
- User leaderboards

## Architecture & Design Choices

### Tech Stack
- Java 24 with Maven for builds
- PostgreSQL for the database
- Jackson for JSON handling
- Built-in `com.sun.net.httpserver` (as required)
- JUnit 5 and Mockito for testing

### How I Structured the Code

I went with a clean, layered architecture:

1. **Handlers** - Deal with HTTP requests and responses
2. **Services** - Contain the business logic
3. **Repositories** - Handle database operations  
4. **Models** - Represent the data objects

I also created a simple DI container to manage dependencies without using Spring (since that wasn't allowed).

### Key Decisions I Made

**Authentication**: Used token-based auth where tokens are stored in the database. Each request (except login/register) checks for a valid token.

**Security**: Passwords are hashed with SHA-256, and I added a comment moderation system where comments need to be confirmed by the author before showing publicly.

**Database**: Designed normalized tables with proper indexes for good performance. Used UUIDs for IDs to avoid conflicts.

## Testing Approach

I wrote over 20 unit tests covering the main functionality:

- User registration and login
- Media CRUD operations
- Rating system
- Favorites and recommendations
- Utility functions like password hashing

Used Mockito to mock dependencies and test individual components in isolation. Had to use reflection to inject mocks into private fields since I didn't want to change the class structure just for testing.

## SOLID Principles in Practice

**Single Responsibility**: Each class does one thing well. For example, `UserService` only handles user-related business logic, while `UserHandler` only deals with HTTP requests.

**Open/Closed**: I used interfaces like `IUserService` so I could swap implementations without changing the code that uses them.

**Dependency Inversion**: Services depend on repository interfaces, not concrete implementations. This makes testing easier and keeps things loosely coupled.

## What I Learned

- Working with the built-in HTTP server was more work than Spring Boot, but I got a better understanding of how HTTP really works
- Database connection management is trickier than I thought - had to add retry logic for flaky connections
- Testing HTTP handlers is hard, so I focused more on service layer testing
- Planning the database schema upfront saved me a lot of headaches later
- Writing tests early helped me think through the design better

## Time Spent

| Task | Time |
|------|------|
| Database design | 3 hours |
| HTTP server basics | 8 hours |
| User auth system | 5 hours |
| Media CRUD features | 6 hours |
| Rating system | 7 hours |
| Favorites & recommendations | 4 hours |
| Writing tests | 3 hours |
| Documentation | 2 hours |
| Bug fixes & polish | 8 hours |
| **Total** | **46 hours** |

## Challenges & Solutions

**Reading JSON from requests**: The built-in HTTP server doesn't make this easy, so I had to write helper methods to read the request body properly.


**Database connections**: Sometimes connections would fail randomly, so I added retry logic to make the app more robust.


**Recommendation algorithm**: Coming up with good recommendations was trickier than expected - I ended up combining user behavior with content similarity (genres, etc.).

**Memory management**: Had to be careful about closing database connections and streams to avoid memory leaks.


[MRP GitHub Repository](https://github.com/Amikas/MRP.git)
