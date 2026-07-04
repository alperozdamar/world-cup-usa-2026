package com.alper.worldcup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "predictions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username", "match_id"})
})
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "home_score_guess", nullable = false)
    private Integer homeScoreGuess;

    @Column(name = "away_score_guess", nullable = false)
    private Integer awayScoreGuess;

    @Column(name = "penalty_shootout_guess")
    private Boolean penaltyShootoutGuess;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advancing_team_id")
    private Team advancingTeamGuess;

    @Column
    private Double points;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Prediction() {
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

    public Boolean getPenaltyShootoutGuess() {
        return penaltyShootoutGuess;
    }

    public void setPenaltyShootoutGuess(Boolean penaltyShootoutGuess) {
        this.penaltyShootoutGuess = penaltyShootoutGuess;
    }

    public Team getAdvancingTeamGuess() {
        return advancingTeamGuess;
    }

    public void setAdvancingTeamGuess(Team advancingTeamGuess) {
        this.advancingTeamGuess = advancingTeamGuess;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
