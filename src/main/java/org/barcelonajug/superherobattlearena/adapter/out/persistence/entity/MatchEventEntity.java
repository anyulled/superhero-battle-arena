package org.barcelonajug.superherobattlearena.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import org.barcelonajug.superherobattlearena.domain.json.MatchEventSnapshot;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "match_events")
@IdClass(MatchEventEntity.MatchEventId.class)
public class MatchEventEntity {

  @Id
  @Column(name = "match_id")
  @SuppressWarnings("NullAway.Init")
  private UUID matchId;

  @Id
  @SuppressWarnings("NullAway.Init")
  private Integer seq;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "event_json")
  @SuppressWarnings("NullAway.Init")
  private MatchEventSnapshot eventJson;

  public static class MatchEventId implements Serializable {
    @SuppressWarnings("NullAway.Init")
    private UUID matchId;

    @SuppressWarnings("NullAway.Init")
    private Integer seq;

    public MatchEventId() {}

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof MatchEventId that)) return false;
      return Objects.equals(matchId, that.matchId) && Objects.equals(seq, that.seq);
    }

    @Override
    public int hashCode() {
      return Objects.hash(matchId, seq);
    }
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

  public MatchEventSnapshot getEventJson() {
    return eventJson;
  }

  public void setEventJson(MatchEventSnapshot eventJson) {
    this.eventJson = eventJson;
  }
}
