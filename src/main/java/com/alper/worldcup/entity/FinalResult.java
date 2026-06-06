package com.alper.worldcup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "final_results")
public class FinalResult {

    public static final String SINGLETON_ID = "FINAL";

    @Id
    @Column(length = 10)
    private String id = SINGLETON_ID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champion_team_id", nullable = false)
    private Team championTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runner_up_team_id", nullable = false)
    private Team runnerUpTeam;

    public FinalResult() {
    }

    public FinalResult(Team championTeam, Team runnerUpTeam) {
        this.id = SINGLETON_ID;
        this.championTeam = championTeam;
        this.runnerUpTeam = runnerUpTeam;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
