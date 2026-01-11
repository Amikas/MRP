-- Create users table
CREATE TABLE IF NOT EXISTS users (
                                     id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    token VARCHAR(255)
    );

-- Create media_entries table
CREATE TABLE IF NOT EXISTS media_entries (
                                             id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    media_type VARCHAR(50) NOT NULL,
    release_year INTEGER,
    genres VARCHAR(255),
    age_restriction INTEGER DEFAULT 0,
    creator_id VARCHAR(255) NOT NULL
    );

-- Create ratings table
CREATE TABLE IF NOT EXISTS ratings (
                                       id VARCHAR(255) PRIMARY KEY,
    media_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    score INTEGER NOT NULL CHECK (score >= 1 AND score <= 5),
    comment TEXT,
    is_comment_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Create rating_likes table
CREATE TABLE IF NOT EXISTS rating_likes (
                                            id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    rating_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS favorites (
                                         id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    media_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, media_id)
    );


-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_ratings_user_id ON ratings(user_id);
CREATE INDEX IF NOT EXISTS idx_ratings_media_id ON ratings(media_id);
CREATE INDEX IF NOT EXISTS idx_media_creator_id ON media_entries(creator_id);
CREATE INDEX IF NOT EXISTS idx_rating_likes_rating_id ON rating_likes(rating_id);
CREATE INDEX IF NOT EXISTS idx_users_token ON users(token);
CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_favorites_media_id ON favorites(media_id);