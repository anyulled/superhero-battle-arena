package com.superherobattle.superherobattlearena.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    private UUID teamId;

    private String name;

    private OffsetDateTime createdAt;

    public Team() {
    }

    public Team(UUID teamId, String name, OffsetDateTime createdAt) {
        this.teamId = teamId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
