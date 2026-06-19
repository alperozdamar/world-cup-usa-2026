package com.alper.worldcup.service;

import java.util.List;

public record GroupStandingsView(String groupName, List<GroupTeamStanding> rows) {
}
