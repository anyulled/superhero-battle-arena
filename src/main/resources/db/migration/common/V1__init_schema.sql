CREATE TABLE teams (
    team_id UUID PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE rounds (
    round_no INT PRIMARY KEY,
    spec_json JSONB,
    status VARCHAR(50) NOT NULL,
    submission_deadline TIMESTAMPTZ,
    seed BIGINT
);

CREATE TABLE submissions (
    team_id UUID NOT NULL,
    round_no INT NOT NULL,
    submission_json JSONB,
    accepted BOOLEAN NOT NULL DEFAULT FALSE,
    rejected_reason TEXT,
    submitted_at TIMESTAMPTZ,
    PRIMARY KEY (team_id, round_no)
);

CREATE TABLE hero_usage (
    team_id UUID NOT NULL,
    hero_id INT NOT NULL,
    round_no INT NOT NULL,
    streak INT NOT NULL DEFAULT 0,
    multiplier NUMERIC(4,2) NOT NULL DEFAULT 1.00,
    PRIMARY KEY (team_id, hero_id, round_no)
);

CREATE TABLE matches (
    match_id UUID PRIMARY KEY,
    round_no INT NOT NULL,
    team_a UUID NOT NULL,
    team_b UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    winner_team UUID,
    result_json JSONB
);

CREATE TABLE match_events (
    match_id UUID NOT NULL,
    seq INT NOT NULL,
    event_json JSONB,
    PRIMARY KEY (match_id, seq)
);
