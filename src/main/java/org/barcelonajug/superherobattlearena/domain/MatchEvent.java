package org.barcelonajug.superherobattlearena.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "match_events")
@IdClass(MatchEventId.class)
public class MatchEvent {

    @Id
    @Column(name = "match_id")
    private UUID matchId;

    @Id
    private Integer seq;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_json")
    private org.barcelonajug.superherobattlearena.domain.json.MatchEvent eventJson;

    public MatchEvent() {
    }

    public UUID getMatchId() {
        return matchId;
    }

    public void setMatchId(UUID matchId) {
        this.matchId = matchId;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public org.barcelonajug.superherobattlearena.domain.json.MatchEvent getEventJson() {
        return eventJson;
    }

    public void setEventJson(org.barcelonajug.superherobattlearena.domain.json.MatchEvent eventJson) {
        this.eventJson = eventJson;
    }
}
