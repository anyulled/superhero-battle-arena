-- Add session_id column to rounds table
ALTER TABLE rounds ADD COLUMN session_id UUID;

-- Add session_id column to matches table
ALTER TABLE matches ADD COLUMN session_id UUID;
