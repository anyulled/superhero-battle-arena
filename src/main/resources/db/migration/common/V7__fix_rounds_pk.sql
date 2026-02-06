-- Add round_id UUID column
ALTER TABLE rounds ADD COLUMN round_id UUID;

-- Since this is H2 memory DB for now or we assume no data that violates, we can just set random UUIDs
-- However, for H2 compatibility we might need a java function or valid SQL. 
-- RANDOM_UUID() is available in H2.
UPDATE rounds SET round_id = RANDOM_UUID() WHERE round_id IS NULL;

ALTER TABLE rounds ALTER COLUMN round_id SET NOT NULL;

-- Drop old PK
ALTER TABLE rounds DROP PRIMARY KEY;

-- Set new PK
ALTER TABLE rounds ADD PRIMARY KEY (round_id);

-- Add Unique Constraint
ALTER TABLE rounds ADD CONSTRAINT uq_rounds_session_round UNIQUE (session_id, round_no);
