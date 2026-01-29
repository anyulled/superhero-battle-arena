CREATE TABLE sessions (
    session_id UUID PRIMARY KEY,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE teams ADD COLUMN session_id UUID REFERENCES sessions(session_id);
