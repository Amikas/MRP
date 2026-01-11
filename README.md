# Media Ratings Platform (MRP)

A RESTful HTTP server that acts as an API for managing media content (movies, series, and games), allowing users to rate, comment, and receive recommendations.

## Features

- User registration and login with unique credentials
- Profile management with personal statistics
- Create, update, and delete media entries
- Rate media entries from 1-5 stars with optional comments
- Like other users' ratings
- Mark media entries as favorites
- View rating history and favorite lists
- Receive personalized recommendations based on genre similarity and user behavior
- Public leaderboard of most active users
- Comment moderation system (comments require author confirmation)

## Technology Stack

- Java 24
- Maven for dependency management
- PostgreSQL for data persistence
- Jackson for JSON processing
- com.sun.net.httpserver for HTTP handling (as required by specifications)
- JUnit 5 for testing
- Mockito for mocking
- UUID Creator for generating UUID v7 identifiers

## Architecture

The application follows a clean, layered architecture:

### Layers:
- **Presentation Layer**: HTTP handlers manage incoming requests and responses
- **Application Layer**: Services contain business logic and coordinate operations
- **Infrastructure Layer**: Repositories handle database interactions
- **Domain Layer**: Models represent the core data structures

### Components:
- **Handlers**: UserHandler, MediaHandler, RatingHandler, FavoriteHandler, RecommendationHandler
- **Services**: UserService, MediaService, RatingService, FavoriteService, RecommendationService
- **Repositories**: UserRepository, MediaRepository, RatingRepository, FavoriteRepository
- **Models**: User, MediaEntry, Rating, Favorite, RatingLike

## Prerequisites

- Java 24 or higher
- Maven 3.6.0 or higher
- PostgreSQL database
- Docker (for easy PostgreSQL setup)

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd MRP
```

### 2. Database Setup

#### Option A: Using Docker (Recommended)

Start PostgreSQL using the provided docker-compose file:

```bash
docker-compose up -d
```

This will start a PostgreSQL instance on port 5432 with:
- Database: `mrp`
- Username: `postgres`
- Password: `password`

#### Option B: Manual PostgreSQL Setup

1. Install PostgreSQL
2. Create a database named `mrp`
3. Create a user with appropriate permissions
4. Update `src/main/java/mrp/database/DatabaseConnection.java` with your credentials

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn exec:java -Dexec.mainClass="mrp.Main"
```

The server will start on `http://localhost:8080`

## API Endpoints

### User Management

- `POST /api/users/register` - Register a new user
- `POST /api/users/login` - Login and get token
- `GET /api/users/{username}/profile` - Get user profile
- `PATCH /api/users/{username}/profile` - Update user profile
- `GET /api/users/leaderboard` - Get top-rated users

### Media Management

- `POST /api/media` - Create a new media entry
- `GET /api/media` - Get all media entries
- `GET /api/media/{id}` - Get specific media entry
- `PATCH /api/media/{id}` - Update media entry (creator only)
- `DELETE /api/media/{id}` - Delete media entry (creator only)
- `GET /api/media/search` - Search media with filters (by title, genre, type, year, rating)

### Rating System

- `POST /api/ratings` - Create a rating
- `GET /api/ratings?mediaId={id}` - Get ratings for a media
- `GET /api/ratings/my` - Get user's own ratings
- `GET /api/ratings/stats?mediaId={id}` - Get rating statistics
- `PATCH /api/ratings/{id}` - Update rating (owner only)
- `DELETE /api/ratings/{id}` - Delete rating (owner only)
- `POST /api/ratings/{id}/like` - Like a rating
- `DELETE /api/ratings/{id}/like` - Unlike a rating
- `PATCH /api/ratings/{id}/confirm-comment` - Confirm comment visibility

### Favorites

- `POST /api/favorites/{mediaId}` - Add media to favorites
- `DELETE /api/favorites/{mediaId}` - Remove media from favorites
- `GET /api/favorites` - Get user's favorite media
- `GET /api/favorites/{mediaId}/check` - Check if media is favorite
- `GET /api/media/{mediaId}/favorite-count` - Get favorite count

### Recommendations

- `GET /api/recommendations` - Get personalized recommendations
- `GET /api/media/{id}/similar` - Get similar media entries

## Authentication

All endpoints (except registration and login) require token-based authentication using the Bearer scheme:

```
Authorization: Bearer {token}
```

Tokens are obtained during login and should be included in all subsequent requests.

## Testing

Run the unit tests:

```bash
mvn test
```

The project includes over 20 unit tests covering:
- Service layer functionality
- Repository operations
- Utility classes
- Model validation
- Business logic
- Authentication and authorization

## API Testing

Import the provided Postman collection (`MRP Postman Collection.json`) to test all API endpoints.

## Database Schema

The application uses the following tables:

- `users`: Stores user information (id, username, password hash, token)
- `media_entries`: Stores media content (id, title, description, type, year, genres, age restriction, creator)
- `ratings`: Stores user ratings (id, media_id, user_id, score, comment, visibility status, timestamps)
- `rating_likes`: Stores likes on ratings (id, user_id, rating_id, timestamp)
- `favorites`: Stores user favorites (id, user_id, media_id, timestamp)

## Development Notes

- Passwords are securely hashed using SHA-256 with salt
- Tokens are UUID v7 based for ordering capability
- All sensitive operations require proper authentication and authorization
- Comments require confirmation by the author before becoming publicly visible
- Media entries can only be modified by their creators
- The recommendation system uses collaborative filtering combined with content-based filtering

## Project Structure

```
src/
├── main/
│   ├── java/mrp/
│   │   ├── database/          # Database connection utilities
│   │   ├── di/               # Dependency injection container
│   │   ├── handler/          # HTTP request handlers
│   │   ├── model/            # Data models
│   │   ├── repository/       # Database access layer
│   │   ├── server/           # HTTP server setup
│   │   ├── service/          # Business logic layer
│   │   └── util/             # Utility classes
│   └── resources/
│       └── schema.sql        # Database schema
└── test/
    └── java/mrp/             # Unit tests
```

## License

This project is licensed under the terms specified in the LICENSE file.

## Additional Resources

- [Architecture Guide](architecture.md) - Detailed architecture documentation
- [Development Protocol](protocol.md) - Development process and decisions documentation