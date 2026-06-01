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
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    @Column(name = "home_placeholder", length = 50)
    private String homePlaceholder;

    @Column(name = "away_placeholder", length = 50)
    private String awayPlaceholder;

    @Column(name = "kickoff_utc", nullable = false)
    private Instant kickoffUtc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchStage stage;

    @Column(name = "group_name", length = 1)
    private String groupName;

    @Column(length = 150)
    private String venue;

    @Column(length = 100)
    private String city;

    @Column(name = "home_score_actual")
    private Integer homeScoreActual;

    @Column(name = "away_score_actual")
    private Integer awayScoreActual;

    @Column(name = "predictions_enabled", nullable = false)
    private boolean predictionsEnabled;

    public Match() {
        this.predictionsEnabled = false;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public String getHomePlaceholder() {
        return homePlaceholder;
    }

    public void setHomePlaceholder(String homePlaceholder) {
        this.homePlaceholder = homePlaceholder;
    }

    public String getAwayPlaceholder() {
        return awayPlaceholder;
    }

    public void setAwayPlaceholder(String awayPlaceholder) {
        this.awayPlaceholder = awayPlaceholder;
    }

    public Instant getKickoffUtc() {
        return kickoffUtc;
    }

    public void setKickoffUtc(Instant kickoffUtc) {
        this.kickoffUtc = kickoffUtc;
    }

    public MatchStage getStage() {
        return stage;
    }

    public void setStage(MatchStage stage) {
        this.stage = stage;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getHomeScoreActual() {
        return homeScoreActual;
    }

    public void setHomeScoreActual(Integer homeScoreActual) {
        this.homeScoreActual = homeScoreActual;
    }

    public Integer getAwayScoreActual() {
        return awayScoreActual;
    }

    public void setAwayScoreActual(Integer awayScoreActual) {
        this.awayScoreActual = awayScoreActual;
    }

    public boolean isPredictionsEnabled() {
        return predictionsEnabled;
    }

    public void setPredictionsEnabled(boolean predictionsEnabled) {
        this.predictionsEnabled = predictionsEnabled;
    }

    public boolean hasStarted(Instant now) {
        return !kickoffUtc.isAfter(now);
    }

    public String getHomeDisplayName() {
        return homeTeam != null ? homeTeam.getName() : homePlaceholder;
    }

    public String getAwayDisplayName() {
        return awayTeam != null ? awayTeam.getName() : awayPlaceholder;
    }

    public boolean isScoreEntered() {
        return homeScoreActual != null && awayScoreActual != null;
    }
}
