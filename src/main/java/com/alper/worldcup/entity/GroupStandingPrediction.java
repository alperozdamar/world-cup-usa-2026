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
@Table(name = "group_standing_predictions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username", "group_name"}))
public class GroupStandingPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "group_name", nullable = false, length = 1)
    private String groupName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_place_team_id", nullable = false)
    private Team firstPlaceTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_place_team_id", nullable = false)
    private Team secondPlaceTeam;

    private Integer points;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public GroupStandingPrediction() {
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

    public Team getFirstPlaceTeam() {
        return firstPlaceTeam;
    }

    public void setFirstPlaceTeam(Team firstPlaceTeam) {
        this.firstPlaceTeam = firstPlaceTeam;
    }

    public Team getSecondPlaceTeam() {
        return secondPlaceTeam;
    }

    public void setSecondPlaceTeam(Team secondPlaceTeam) {
        this.secondPlaceTeam = secondPlaceTeam;
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
