CREATE TABLE team_members (
    team_id UUID NOT NULL,
    member_name VARCHAR(255) NOT NULL,
    FOREIGN KEY (team_id) REFERENCES teams(team_id)
);
