CREATE TABLE users (
                       id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid(),
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(100) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE media_entries (
                               id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid(),
                               title VARCHAR(255) NOT NULL,
                               description TEXT,
                               media_type VARCHAR(20) NOT NULL,
                               release_year INTEGER,
                               genres VARCHAR(255),
                               age_restriction INTEGER,
                               creator_id VARCHAR(36) REFERENCES users(id),
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ratings (
                         id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid(),
                         media_id VARCHAR(36) REFERENCES media_entries(id),
                         user_id VARCHAR(36) REFERENCES users(id),
                         score INTEGER CHECK (score >= 1 AND score <= 5),
                         comment TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         UNIQUE(media_id, user_id)
);
