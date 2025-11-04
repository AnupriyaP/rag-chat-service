-- V2__Create_chat_sessions_table.sql

CREATE TABLE chat_sessions (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Session Metadata
    title VARCHAR(255),
    owner VARCHAR(255),

    -- Booleans map best to boolean, not NULL means it must be set.
    -- Defaulting to FALSE is a common practice for flags.
    favorite BOOLEAN DEFAULT FALSE,

    -- Timestamps
    -- Matches Instant and nullable=false
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Optional: Add indexes for performance on frequently queried columns
-- Index on owner for quickly retrieving all sessions for a specific user
CREATE INDEX idx_chat_sessions_owner ON chat_sessions (owner);

-- Index on favorite status for quick filtering
CREATE INDEX idx_chat_sessions_favorite ON chat_sessions (favorite);

-- Index on created_at for chronological sorting
CREATE INDEX idx_chat_sessions_created_at ON chat_sessions (created_at);