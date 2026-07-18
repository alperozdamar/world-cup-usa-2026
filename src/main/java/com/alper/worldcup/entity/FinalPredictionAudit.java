package com.alper.worldcup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "final_prediction_audits")
public class FinalPredictionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "champion_team_name", nullable = false, length = 100)
    private String championTeamName;

    @Column(name = "runner_up_team_name", nullable = false, length = 100)
    private String runnerUpTeamName;

    @Column(name = "previous_champion_team_name", length = 100)
    private String previousChampionTeamName;

    @Column(name = "previous_runner_up_team_name", length = 100)
    private String previousRunnerUpTeamName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PredictionAuditAction action;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    public FinalPredictionAudit() {
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

    public String getChampionTeamName() {
        return championTeamName;
    }

    public void setChampionTeamName(String championTeamName) {
        this.championTeamName = championTeamName;
    }

    public String getRunnerUpTeamName() {
        return runnerUpTeamName;
    }

    public void setRunnerUpTeamName(String runnerUpTeamName) {
        this.runnerUpTeamName = runnerUpTeamName;
    }

    public String getPreviousChampionTeamName() {
        return previousChampionTeamName;
    }

    public void setPreviousChampionTeamName(String previousChampionTeamName) {
        this.previousChampionTeamName = previousChampionTeamName;
    }

    public String getPreviousRunnerUpTeamName() {
        return previousRunnerUpTeamName;
    }

    public void setPreviousRunnerUpTeamName(String previousRunnerUpTeamName) {
        this.previousRunnerUpTeamName = previousRunnerUpTeamName;
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

    public String getSubjectLabel() {
        return "Champion & Runner-up";
    }

    public String getPickLabel() {
        return championTeamName + " (champion), " + runnerUpTeamName + " (runner-up)";
    }

    public String getChangeLabel() {
        if (action != PredictionAuditAction.UPDATED
                || previousChampionTeamName == null
                || previousRunnerUpTeamName == null) {
            return getPickLabel();
        }
        return previousChampionTeamName + " (champion), " + previousRunnerUpTeamName + " (runner-up)"
                + " → " + getPickLabel();
    }
}
