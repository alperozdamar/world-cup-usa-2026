package com.alper.worldcup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "prediction_audits")
public class PredictionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "home_team_name", nullable = false, length = 100)
    private String homeTeamName;

    @Column(name = "away_team_name", nullable = false, length = 100)
    private String awayTeamName;

    @Column(name = "home_score_guess", nullable = false)
    private Integer homeScoreGuess;

    @Column(name = "away_score_guess", nullable = false)
    private Integer awayScoreGuess;

    @Column(name = "previous_home_score_guess")
    private Integer previousHomeScoreGuess;

    @Column(name = "previous_away_score_guess")
    private Integer previousAwayScoreGuess;

    @Column(name = "penalty_shootout_guess")
    private Boolean penaltyShootoutGuess;

    @Column(name = "advancing_team_name", length = 100)
    private String advancingTeamName;

    @Column(name = "previous_penalty_shootout_guess")
    private Boolean previousPenaltyShootoutGuess;

    @Column(name = "previous_advancing_team_name", length = 100)
    private String previousAdvancingTeamName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PredictionAuditAction action;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    public PredictionAudit() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public String getHomeTeamName() {
        return homeTeamName;
    }

    public void setHomeTeamName(String homeTeamName) {
        this.homeTeamName = homeTeamName;
    }

    public String getAwayTeamName() {
        return awayTeamName;
    }

    public void setAwayTeamName(String awayTeamName) {
        this.awayTeamName = awayTeamName;
    }

    public Integer getHomeScoreGuess() {
        return homeScoreGuess;
    }

    public void setHomeScoreGuess(Integer homeScoreGuess) {
        this.homeScoreGuess = homeScoreGuess;
    }

    public Integer getAwayScoreGuess() {
        return awayScoreGuess;
    }

    public void setAwayScoreGuess(Integer awayScoreGuess) {
        this.awayScoreGuess = awayScoreGuess;
    }

    public Integer getPreviousHomeScoreGuess() {
        return previousHomeScoreGuess;
    }

    public void setPreviousHomeScoreGuess(Integer previousHomeScoreGuess) {
        this.previousHomeScoreGuess = previousHomeScoreGuess;
    }

    public Integer getPreviousAwayScoreGuess() {
        return previousAwayScoreGuess;
    }

    public void setPreviousAwayScoreGuess(Integer previousAwayScoreGuess) {
        this.previousAwayScoreGuess = previousAwayScoreGuess;
    }

    public Boolean getPenaltyShootoutGuess() {
        return penaltyShootoutGuess;
    }

    public void setPenaltyShootoutGuess(Boolean penaltyShootoutGuess) {
        this.penaltyShootoutGuess = penaltyShootoutGuess;
    }

    public String getAdvancingTeamName() {
        return advancingTeamName;
    }

    public void setAdvancingTeamName(String advancingTeamName) {
        this.advancingTeamName = advancingTeamName;
    }

    public Boolean getPreviousPenaltyShootoutGuess() {
        return previousPenaltyShootoutGuess;
    }

    public void setPreviousPenaltyShootoutGuess(Boolean previousPenaltyShootoutGuess) {
        this.previousPenaltyShootoutGuess = previousPenaltyShootoutGuess;
    }

    public String getPreviousAdvancingTeamName() {
        return previousAdvancingTeamName;
    }

    public void setPreviousAdvancingTeamName(String previousAdvancingTeamName) {
        this.previousAdvancingTeamName = previousAdvancingTeamName;
    }

    public PredictionAuditAction getAction() {
        return action;
    }

    public void setAction(PredictionAuditAction action) {
        this.action = action;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public String getMatchLabel() {
        return homeTeamName + " vs " + awayTeamName;
    }

    public String getScoreLabel() {
        return formatPredictionLabel(
                homeScoreGuess,
                awayScoreGuess,
                penaltyShootoutGuess,
                advancingTeamName);
    }

    public String getChangeLabel() {
        if (action != PredictionAuditAction.UPDATED
                || previousHomeScoreGuess == null
                || previousAwayScoreGuess == null) {
            return getScoreLabel();
        }
        return formatPredictionLabel(
                previousHomeScoreGuess,
                previousAwayScoreGuess,
                previousPenaltyShootoutGuess,
                previousAdvancingTeamName)
                + " → "
                + getScoreLabel();
    }

    static String formatPredictionLabel(Integer homeGuess,
                                        Integer awayGuess,
                                        Boolean penaltyShootoutGuess,
                                        String advancingTeamName) {
        String label = homeGuess + " - " + awayGuess;
        if (penaltyShootoutGuess != null) {
            label += " · Penalties: " + (penaltyShootoutGuess ? "Yes" : "No");
        }
        if (advancingTeamName != null && !advancingTeamName.isBlank()) {
            label += " · Advances: " + advancingTeamName;
        }
        return label;
    }
}
