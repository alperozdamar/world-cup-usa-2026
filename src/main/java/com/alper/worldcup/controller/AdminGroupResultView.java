package com.alper.worldcup.controller;

import com.alper.worldcup.entity.GroupResult;
import com.alper.worldcup.entity.Team;
import java.util.List;

public class AdminGroupResultView {

    private final String groupName;
    private final List<Team> teams;
    private final GroupResult groupResult;

    public AdminGroupResultView(String groupName, List<Team> teams, GroupResult groupResult) {
        this.groupName = groupName;
        this.teams = teams;
        this.groupResult = groupResult;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public GroupResult getGroupResult() {
        return groupResult;
    }
}
