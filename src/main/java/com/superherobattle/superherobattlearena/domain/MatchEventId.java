package com.superherobattle.superherobattlearena.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class MatchEventId implements Serializable {
    private UUID matchId;
    private Integer seq;

    public MatchEventId() {
    }

    public MatchEventId(UUID matchId, Integer seq) {
        this.matchId = matchId;
        this.seq = seq;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MatchEventId that = (MatchEventId) o;
        return Objects.equals(matchId, that.matchId) && Objects.equals(seq, that.seq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchId, seq);
    }
}
