-- Add round_id UUID column
ALTER TABLE rounds ADD COLUMN round_id UUID;

-- Use RANDOM_UUID() for H2
UPDATE rounds SET round_id = RANDOM_UUID() WHERE round_id IS NULL;

ALTER TABLE rounds ALTER COLUMN round_id SET NOT NULL;

-- Drop old PK (Assuming constraint name rounds_pkey from V1)
ALTER TABLE rounds DROP CONSTRAINT rounds_pkey;

-- Set new PK
ALTER TABLE rounds ADD PRIMARY KEY (round_id);

-- Add Unique Constraint
ALTER TABLE rounds ADD CONSTRAINT uq_rounds_session_round UNIQUE (session_id, round_no);
