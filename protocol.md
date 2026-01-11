# MRP - Intermediate Hand-in Protocol

## Project Overview
Media Ratings Platform intermediate submission - REST server with user auth and media CRUD.

## Technical Stack
- Java with built-in HttpServer
- PostgreSQL in Docker
- Maven build
- Jackson for JSON

## Architecture
Modular structure:
- models (User, MediaEntry)
- server (MRPServer)
- handlers (routing)
- services (business logic)
- database (PostgreSQL connection)

## Key Decisions
1. Used Java HttpServer instead of frameworks (requirement)
2. Simple token auth: "username-mrpToken"
3. Two main tables: users and media_entries
4. Basic error handling with HTTP status codes

## Problems & Solutions
1. Foreign key error - fixed by using real user IDs instead
2. Slow responses - fixed OutputStream handling with try-with-resources
3. Naming conflict - renamed HttpServer to MRPServer

## Implementation Steps
1. Setup project and database (1.5 hours)
2. HTTP server and basic routing (1 hour)
3. User registration and login (3 hours)
4. Media CRUD operations (4 hours)
5. Testing and fixes (2 hours)

## Testing
- Postman collection with 7 endpoints
- Manual testing of all operations
- Error scenario testing

## Time Spent
Total: ~12 hours

## Git
https://github.com/Amikas/MRP.git
