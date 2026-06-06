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
import java.time.Instant;

@Entity
@Table(name = "final_predictions")
public class FinalPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champion_team_id", nullable = false)
    private Team championTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runner_up_team_id", nullable = false)
    private Team runnerUpTeam;

    private Integer points;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public FinalPrediction() {
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

    public Team getChampionTeam() {
        return championTeam;
    }

    public void setChampionTeam(Team championTeam) {
        this.championTeam = championTeam;
    }

    public Team getRunnerUpTeam() {
        return runnerUpTeam;
    }

    public void setRunnerUpTeam(Team runnerUpTeam) {
        this.runnerUpTeam = runnerUpTeam;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
