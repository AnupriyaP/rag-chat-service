-- V1__Create_chat_messages_table.sql

CREATE TABLE chat_messages (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Message Details
    sender VARCHAR(255),
    content VARCHAR(2000), -- Matches @Column(length = 2000)
    context VARCHAR(5000), -- Matches @Column(length = 5000)

    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, -- Matches Instant and nullable=false
    updated_at TIMESTAMP WITH TIME ZONE,          -- Matches Instant

    -- Foreign Key to ChatSession (Assumes a 'chat_sessions' table exists with a BIGSERIAL PK)
    session_id BIGINT,

    -- Define the foreign key constraint
    CONSTRAINT fk_chat_session
        FOREIGN KEY (session_id)
        REFERENCES chat_sessions (id)
        ON DELETE CASCADE -- Optional: Deleting a session deletes all associated messages
);

-- Optional: Add indexes for performance on frequently queried columns
CREATE INDEX idx_chat_messages_session_id ON chat_messages (session_id);

-- Optional: Add an index on createdAt for chronological queries
CREATE INDEX idx_chat_messages_created_at ON chat_messages (created_at);