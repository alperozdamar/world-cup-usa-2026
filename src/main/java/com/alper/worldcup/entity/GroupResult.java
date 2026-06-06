package com.alper.worldcup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "group_results")
public class GroupResult {

    @Id
    @Column(name = "group_name", length = 1)
    private String groupName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_place_team_id", nullable = false)
    private Team firstPlaceTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_place_team_id", nullable = false)
    private Team secondPlaceTeam;

    public GroupResult() {
    }

    public GroupResult(String groupName, Team firstPlaceTeam, Team secondPlaceTeam) {
        this.groupName = groupName;
        this.firstPlaceTeam = firstPlaceTeam;
        this.secondPlaceTeam = secondPlaceTeam;
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
}
