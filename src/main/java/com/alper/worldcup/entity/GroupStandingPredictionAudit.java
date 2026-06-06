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
@Table(name = "group_standing_prediction_audits")
public class GroupStandingPredictionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "group_name", nullable = false, length = 1)
    private String groupName;

    @Column(name = "first_team_name", nullable = false, length = 100)
    private String firstTeamName;

    @Column(name = "second_team_name", nullable = false, length = 100)
    private String secondTeamName;

    @Column(name = "previous_first_team_name", length = 100)
    private String previousFirstTeamName;

    @Column(name = "previous_second_team_name", length = 100)
    private String previousSecondTeamName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PredictionAuditAction action;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    public GroupStandingPredictionAudit() {
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getFirstTeamName() {
        return firstTeamName;
    }

    public void setFirstTeamName(String firstTeamName) {
        this.firstTeamName = firstTeamName;
    }

    public String getSecondTeamName() {
        return secondTeamName;
    }

    public void setSecondTeamName(String secondTeamName) {
        this.secondTeamName = secondTeamName;
    }

    public String getPreviousFirstTeamName() {
        return previousFirstTeamName;
    }

    public void setPreviousFirstTeamName(String previousFirstTeamName) {
        this.previousFirstTeamName = previousFirstTeamName;
    }

    public String getPreviousSecondTeamName() {
        return previousSecondTeamName;
    }

    public void setPreviousSecondTeamName(String previousSecondTeamName) {
        this.previousSecondTeamName = previousSecondTeamName;
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

    public String getGroupLabel() {
        return "Group " + groupName;
    }

    public String getPickLabel() {
        return firstTeamName + " (1st), " + secondTeamName + " (2nd)";
    }

    public String getChangeLabel() {
        if (action != PredictionAuditAction.UPDATED
                || previousFirstTeamName == null
                || previousSecondTeamName == null) {
            return getPickLabel();
        }
        return previousFirstTeamName + " (1st), " + previousSecondTeamName + " (2nd)"
                + " → " + getPickLabel();
    }
}
