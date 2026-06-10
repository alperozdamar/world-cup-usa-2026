package com.alper.worldcup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "match_prediction_reminder_sent")
public class MatchPredictionReminderSent {

    @EmbeddedId
    private MatchPredictionReminderSentId id;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    public MatchPredictionReminderSent() {
    }

    public MatchPredictionReminderSent(MatchPredictionReminderSentId id, Instant sentAt) {
        this.id = id;
        this.sentAt = sentAt;
    }

    public MatchPredictionReminderSentId getId() {
        return id;
    }

    public void setId(MatchPredictionReminderSentId id) {
        this.id = id;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }
}
