ALTER TABLE matches ADD COLUMN match_type VARCHAR(20) NOT NULL DEFAULT 'KNOCKOUT';
ALTER TABLE matches ADD COLUMN bracket_position INT;

CREATE TABLE eliminated_teams (
    session_id UUID NOT NULL,
    team_id UUID NOT NULL,
    round_no INT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    eliminated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (session_id, team_id)
);
