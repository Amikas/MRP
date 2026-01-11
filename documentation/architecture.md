# MRP Architecture & Schema Guide

## Database Schema

### Tables

#### users
```
id: VARCHAR(255) - Primary Key (UUID)
username: VARCHAR(255) - Unique, Not Null
password: VARCHAR(255) - Hashed, Not Null  
token: VARCHAR(255) - Session token
```

#### media_entries
```
id: VARCHAR(255) - Primary Key (UUID)
title: VARCHAR(255) - Not Null
description: TEXT
media_type: VARCHAR(50) - 'movie', 'series', or 'game'
release_year: INTEGER
genres: VARCHAR(255) - Comma-separated list
age_restriction: INTEGER - Default 0
creator_id: VARCHAR(255) - Foreign Key to users.id
```

#### ratings
```
id: VARCHAR(255) - Primary Key (UUID)
media_id: VARCHAR(255) - Foreign Key to media_entries.id
user_id: VARCHAR(255) - Foreign Key to users.id
score: INTEGER - 1 to 5, Not Null
comment: TEXT
is_comment_public: BOOLEAN - Default FALSE
created_at: TIMESTAMP - Default CURRENT_TIMESTAMP
updated_at: TIMESTAMP - Default CURRENT_TIMESTAMP
```

#### rating_likes
```
id: VARCHAR(255) - Primary Key (UUID)
user_id: VARCHAR(255) - Foreign Key to users.id
rating_id: VARCHAR(255) - Foreign Key to ratings.id
created_at: TIMESTAMP - Default CURRENT_TIMESTAMP
```

#### favorites
```
id: VARCHAR(255) - Primary Key (UUID)
user_id: VARCHAR(255) - Foreign Key to users.id
media_id: VARCHAR(255) - Foreign Key to media_entries.id
created_at: TIMESTAMP - Default CURRENT_TIMESTAMP
```

### Indexes
- idx_ratings_user_id, idx_ratings_media_id
- idx_media_creator_id
- idx_rating_likes_rating_id
- idx_users_token
- idx_favorites_user_id, idx_favorites_media_id

## Application Layers

### 1. Presentation Layer (Handlers)
```
UserHandler -> Handles /api/users/* endpoints
MediaHandler -> Handles /api/media/* endpoints  
RatingHandler -> Handles /api/ratings/* endpoints
FavoriteHandler -> Handles /api/favorites/* endpoints
RecommendationHandler -> Handles /api/recommendations/* endpoints
```

**Responsibilities:**
- Parse HTTP requests and extract parameters
- Validate request format and headers
- Call appropriate service methods
- Format and send HTTP responses
- Handle authentication token validation

### 2. Application Layer (Services)
```
UserService -> User registration, login, profiles, leaderboard
MediaService -> Media CRUD, search, filtering
RatingService -> Rating creation, updates, likes, moderation
FavoriteService -> Favorite management
RecommendationService -> Personalized recommendations
```

**Responsibilities:**
- Implement business logic
- Coordinate between repositories
- Handle authentication and authorization
- Validate business rules
- Format data for presentation

### 3. Infrastructure Layer (Repositories)
```
UserRepository -> User database operations
MediaRepository -> Media database operations
RatingRepository -> Rating database operations
FavoriteRepository -> Favorite database operations
```

**Responsibilities:**
- Execute database queries
- Map database records to objects
- Handle database connections
- Prepare and execute SQL statements

### 4. Domain Layer (Models)
```
User -> Represents user data
MediaEntry -> Represents media content
Rating -> Represents user ratings
RatingLike -> Represents likes on ratings
Favorite -> Represents user favorites
```

**Responsibilities:**
- Define data structures
- Hold business data
- Simple getter/setter methods

## Flow Examples

### User Registration Flow
```
Client POST /api/users/register
    ↓
UserHandler.handle()
    ↓
UserService.register()
    ↓
UserRepository.save()
    ↓
Database INSERT INTO users
    ↓
Response: Success/Failure
```

### Creating a Rating Flow
```
Client POST /api/ratings (with auth token)
    ↓
RatingHandler.handle()
    ↓
Validate token → UserService.validateToken()
    ↓
RatingService.createRating()
    ↓
MediaRepository.findById() + UserRepository.findById()
    ↓
RatingRepository.save()
    ↓
Database INSERT INTO ratings
    ↓
Response: Rating created
```

### Getting Recommendations Flow
```
Client GET /api/recommendations (with auth token)
    ↓
RecommendationHandler.handle()
    ↓
Validate token → UserService.validateToken()
    ↓
RecommendationService.getRecommendations()
    ↓
RatingRepository.getUserRatings() + MediaRepository.findSimilar()
    ↓
Apply recommendation algorithm
    ↓
Response: Array of recommended media
```

## Authentication Flow

1. User registers/login → gets token stored in database
2. Client includes token in Authorization header: `Bearer {token}`
3. Each protected handler validates token against database
4. Token validation happens before business logic
5. Unauthorized requests return 401 status

## Key Design Patterns

- **Dependency Injection**: DIContainer manages object creation
- **Repository Pattern**: Abstracts database access
- **Separation of Concerns**: Each layer has distinct responsibilities
- **Interface Segregation**: Services depend on repository interfaces