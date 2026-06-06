package com.alper.worldcup.controller;

import com.alper.worldcup.entity.GroupResult;
import com.alper.worldcup.entity.GroupStandingPrediction;
import com.alper.worldcup.entity.Team;
import java.util.List;

public class GroupPredictionView {

    private final String groupName;
    private final List<Team> teams;
    private final GroupStandingPrediction prediction;
    private final GroupResult groupResult;
    private final String firstKickoffLabel;
    private final boolean editable;

    public GroupPredictionView(String groupName,
                               List<Team> teams,
                               GroupStandingPrediction prediction,
                               GroupResult groupResult,
                               String firstKickoffLabel,
                               boolean editable) {
        this.groupName = groupName;
        this.teams = teams;
        this.prediction = prediction;
        this.groupResult = groupResult;
        this.firstKickoffLabel = firstKickoffLabel;
        this.editable = editable;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public GroupStandingPrediction getPrediction() {
        return prediction;
    }

    public GroupResult getGroupResult() {
        return groupResult;
    }

    public String getFirstKickoffLabel() {
        return firstKickoffLabel;
    }

    public boolean isEditable() {
        return editable;
    }
}
