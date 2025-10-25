CREATE TABLE IF NOT EXISTS chat_sessions (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(512),
  favorite BOOLEAN DEFAULT FALSE,
  owner VARCHAR(255),
  created_at TIMESTAMPTZ DEFAULT now()
);
CREATE TABLE IF NOT EXISTS chat_messages (
  id BIGSERIAL PRIMARY KEY,
  session_id BIGINT REFERENCES chat_sessions(id) ON DELETE CASCADE,
  sender VARCHAR(64),
  content TEXT,
  context_json TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);
