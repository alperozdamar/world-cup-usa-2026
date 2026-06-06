package com.alper.worldcup.service;

import com.alper.worldcup.dao.GroupResultRepository;
import com.alper.worldcup.dao.GroupStandingPredictionRepository;
import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.TeamRepository;
import com.alper.worldcup.entity.GroupResult;
import com.alper.worldcup.entity.GroupStandingPrediction;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.PredictionAuditAction;
import com.alper.worldcup.entity.Team;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupStandingPredictionService {

    public static final int FIRST_PLACE_POINTS = 3;
    public static final int SECOND_PLACE_POINTS = 2;
    public static final int WRONG_POSITION_POINTS = 1;

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final GroupStandingPredictionRepository predictionRepository;
    private final GroupResultRepository groupResultRepository;
    private final PredictionAuditService predictionAuditService;

    public GroupStandingPredictionService(TeamRepository teamRepository,
                                            MatchRepository matchRepository,
                                            GroupStandingPredictionRepository predictionRepository,
                                            GroupResultRepository groupResultRepository,
                                            PredictionAuditService predictionAuditService) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.groupResultRepository = groupResultRepository;
        this.predictionAuditService = predictionAuditService;
    }

    @Transactional(readOnly = true)
    public List<String> getGroupNames() {
        List<String> fromMatches = matchRepository.findDistinctGroupStageGroupNames();
        if (!fromMatches.isEmpty()) {
            return fromMatches;
        }
        return teamRepository.findDistinctGroupNames();
    }

    @Transactional(readOnly = true)
    public Map<String, List<Team>> getTeamsByGroup() {
        Map<String, List<Team>> teamsByGroup = new LinkedHashMap<>();
        for (String groupName : getGroupNames()) {
            teamsByGroup.put(groupName, getTeamsForGroup(groupName));
        }
        return teamsByGroup;
    }

    @Transactional(readOnly = true)
    public Map<String, GroupStandingPrediction> getPredictionsForUser(String username) {
        Map<String, GroupStandingPrediction> byGroup = new HashMap<>();
        for (GroupStandingPrediction prediction : predictionRepository.findByUsernameWithTeams(username)) {
            byGroup.put(prediction.getGroupName(), prediction);
        }
        return byGroup;
    }

    @Transactional(readOnly = true)
    public Map<String, GroupResult> getGroupResults() {
        Map<String, GroupResult> results = new HashMap<>();
        for (GroupResult result : groupResultRepository.findAllWithTeams()) {
            results.put(result.getGroupName(), result);
        }
        return results;
    }

    @Transactional(readOnly = true)
    public Optional<Instant> getFirstKickoffForGroup(String groupName) {
        return matchRepository.findEarliestGroupStageKickoff(groupName);
    }

    @Transactional(readOnly = true)
    public boolean isGroupEditable(String groupName) {
        return getFirstKickoffForGroup(groupName)
                .map(kickoff -> kickoff.isAfter(Instant.now()))
                .orElse(false);
    }

    @Transactional
    public void savePrediction(String username, String groupName, Integer firstTeamId, Integer secondTeamId) {
        if (!isGroupEditable(groupName)) {
            throw new IllegalStateException("Group " + groupName + " is locked — its first match has started.");
        }

        Team firstTeam = loadTeamInGroup(firstTeamId, groupName);
        Team secondTeam = loadTeamInGroup(secondTeamId, groupName);
        if (firstTeam.getId().equals(secondTeam.getId())) {
            throw new IllegalArgumentException("1st and 2nd place must be different teams.");
        }

        GroupStandingPrediction prediction = predictionRepository
                .findByUsernameAndGroupName(username, groupName)
                .orElse(new GroupStandingPrediction());

        boolean isUpdate = prediction.getId() != null;
        String previousFirstTeamName = isUpdate ? prediction.getFirstPlaceTeam().getName() : null;
        String previousSecondTeamName = isUpdate ? prediction.getSecondPlaceTeam().getName() : null;

        if (isUpdate
                && firstTeam.getId().equals(prediction.getFirstPlaceTeam().getId())
                && secondTeam.getId().equals(prediction.getSecondPlaceTeam().getId())) {
            return;
        }

        prediction.setUsername(username);
        prediction.setGroupName(groupName);
        prediction.setFirstPlaceTeam(firstTeam);
        prediction.setSecondPlaceTeam(secondTeam);
        prediction.setUpdatedAt(Instant.now());

        GroupResult result = groupResultRepository.findById(groupName).orElse(null);
        if (result != null) {
            prediction.setPoints(calculatePoints(prediction, result));
        } else {
            prediction.setPoints(null);
        }

        predictionRepository.save(prediction);

        predictionAuditService.recordGroupStandingChange(
                username,
                groupName,
                firstTeam.getName(),
                secondTeam.getName(),
                isUpdate ? PredictionAuditAction.UPDATED : PredictionAuditAction.CREATED,
                previousFirstTeamName,
                previousSecondTeamName);
    }

    @Transactional
    public void saveGroupResult(String groupName, Integer firstTeamId, Integer secondTeamId) {
        Team firstTeam = loadTeamInGroup(firstTeamId, groupName);
        Team secondTeam = loadTeamInGroup(secondTeamId, groupName);
        if (firstTeam.getId().equals(secondTeam.getId())) {
            throw new IllegalArgumentException("1st and 2nd place must be different teams.");
        }

        GroupResult result = groupResultRepository.findById(groupName)
                .orElse(new GroupResult(groupName, firstTeam, secondTeam));
        result.setFirstPlaceTeam(firstTeam);
        result.setSecondPlaceTeam(secondTeam);
        groupResultRepository.save(result);

        for (GroupStandingPrediction prediction : predictionRepository.findByGroupName(groupName)) {
            prediction.setPoints(calculatePoints(prediction, result));
            predictionRepository.save(prediction);
        }
    }

    public String statusLabel(String groupName, GroupStandingPrediction prediction) {
        if (prediction == null) {
            return isGroupEditable(groupName) ? "Open" : "Missed";
        }
        if (prediction.getPoints() != null) {
            return "Scored";
        }
        return isGroupEditable(groupName) ? "Saved" : "Locked";
    }

    int calculatePoints(GroupStandingPrediction prediction, GroupResult result) {
        int points = 0;

        Integer predictedFirstId = prediction.getFirstPlaceTeam().getId();
        Integer predictedSecondId = prediction.getSecondPlaceTeam().getId();
        Integer actualFirstId = result.getFirstPlaceTeam().getId();
        Integer actualSecondId = result.getSecondPlaceTeam().getId();

        if (predictedFirstId.equals(actualFirstId)) {
            points += FIRST_PLACE_POINTS;
        } else if (predictedFirstId.equals(actualSecondId)) {
            points += WRONG_POSITION_POINTS;
        }

        if (predictedSecondId.equals(actualSecondId)) {
            points += SECOND_PLACE_POINTS;
        } else if (predictedSecondId.equals(actualFirstId)) {
            points += WRONG_POSITION_POINTS;
        }

        return points;
    }

    private List<Team> getTeamsForGroup(String groupName) {
        Map<Integer, Team> teams = new TreeMap<>();
        for (Match match : matchRepository.findGroupStageMatchesByGroup(groupName)) {
            if (match.getHomeTeam() != null) {
                teams.put(match.getHomeTeam().getId(), match.getHomeTeam());
            }
            if (match.getAwayTeam() != null) {
                teams.put(match.getAwayTeam().getId(), match.getAwayTeam());
            }
        }
        return teams.values().stream()
                .sorted(Comparator.comparing(Team::getName))
                .toList();
    }

    private Team loadTeamInGroup(Integer teamId, String groupName) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
        boolean inGroup = getTeamsForGroup(groupName).stream()
                .anyMatch(groupTeam -> groupTeam.getId().equals(teamId));
        if (!inGroup) {
            throw new IllegalArgumentException(team.getName() + " is not in Group " + groupName);
        }
        return team;
    }
}
