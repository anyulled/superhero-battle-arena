-- Add round_id UUID column
ALTER TABLE rounds ADD COLUMN round_id UUID;

-- Use gen_random_uuid() for Postgres compatibility (requires Postgres 13+)
UPDATE rounds SET round_id = gen_random_uuid() WHERE round_id IS NULL;

ALTER TABLE rounds ALTER COLUMN round_id SET NOT NULL;

-- Drop old PK (Postgres requires dropping by constraint name)
ALTER TABLE rounds DROP CONSTRAINT rounds_pkey;

-- Set new PK
ALTER TABLE rounds ADD PRIMARY KEY (round_id);

-- Add Unique Constraint
ALTER TABLE rounds ADD CONSTRAINT uq_rounds_session_round UNIQUE (session_id, round_no);
