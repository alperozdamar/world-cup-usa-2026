package com.alper.worldcup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MatchPredictionReminderSentId implements Serializable {

    @Column(length = 50)
    private String username;

    @Column(name = "match_id")
    private Integer matchId;

    @Column(name = "hours_before")
    private int hoursBefore;

    public MatchPredictionReminderSentId() {
    }

    public MatchPredictionReminderSentId(String username, Integer matchId, int hoursBefore) {
        this.username = username;
        this.matchId = matchId;
        this.hoursBefore = hoursBefore;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getMatchId() {
        return matchId;
    }

    public void setMatchId(Integer matchId) {
        this.matchId = matchId;
    }

    public int getHoursBefore() {
        return hoursBefore;
    }

    public void setHoursBefore(int hoursBefore) {
        this.hoursBefore = hoursBefore;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MatchPredictionReminderSentId that)) {
            return false;
        }
        return hoursBefore == that.hoursBefore
                && Objects.equals(username, that.username)
                && Objects.equals(matchId, that.matchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, matchId, hoursBefore);
    }
}
