package com.alper.worldcup.service;

import com.alper.worldcup.dao.FinalPredictionRepository;
import com.alper.worldcup.dao.FinalResultRepository;
import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.TeamRepository;
import com.alper.worldcup.entity.FinalPrediction;
import com.alper.worldcup.entity.FinalResult;
import com.alper.worldcup.entity.PredictionAuditAction;
import com.alper.worldcup.entity.Team;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinalPredictionService {

    public static final int CHAMPION_POINTS = 10;
    public static final int RUNNER_UP_POINTS = 5;
    public static final int WRONG_POSITION_POINTS = 3;

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final FinalPredictionRepository predictionRepository;
    private final FinalResultRepository finalResultRepository;
    private final PredictionAuditService predictionAuditService;

    public FinalPredictionService(TeamRepository teamRepository,
                                  MatchRepository matchRepository,
                                  FinalPredictionRepository predictionRepository,
                                  FinalResultRepository finalResultRepository,
                                  PredictionAuditService predictionAuditService) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.finalResultRepository = finalResultRepository;
        this.predictionAuditService = predictionAuditService;
    }

    @Transactional(readOnly = true)
    public List<Team> getAllTeams() {
        return teamRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Optional<FinalResult> getFinalResult() {
        return finalResultRepository.findByIdWithTeams(FinalResult.SINGLETON_ID);
    }

    @Transactional(readOnly = true)
    public Map<String, Boolean> getChampionCorrectByUsername() {
        Optional<FinalResult> result = getFinalResult();
        if (result.isEmpty()) {
            return Map.of();
        }
        Integer championId = result.get().getChampionTeam().getId();
        Map<String, Boolean> byUsername = new HashMap<>();
        for (FinalPrediction prediction : predictionRepository.findAllWithTeams()) {
            byUsername.put(
                    prediction.getUsername(),
                    championId.equals(prediction.getChampionTeam().getId()));
        }
        return byUsername;
    }

    @Transactional(readOnly = true)
    public Optional<FinalPrediction> getPredictionForUser(String username) {
        return predictionRepository.findByUsernameWithTeams(username);
    }

    @Transactional(readOnly = true)
    public Optional<Instant> getTournamentStartKickoff() {
        return matchRepository.findTournamentStartKickoff();
    }

    @Transactional(readOnly = true)
    public boolean isEditable() {
        return getTournamentStartKickoff()
                .map(kickoff -> kickoff.isAfter(Instant.now()))
                .orElse(true);
    }

    @Transactional
    public void savePrediction(String username, Integer championTeamId, Integer runnerUpTeamId) {
        if (!isEditable()) {
            throw new IllegalStateException("Final prediction is locked — the tournament has started.");
        }

        Team championTeam = loadTeam(championTeamId);
        Team runnerUpTeam = loadTeam(runnerUpTeamId);
        if (championTeam.getId().equals(runnerUpTeam.getId())) {
            throw new IllegalArgumentException("Champion and runner-up must be different teams.");
        }

        FinalPrediction prediction = predictionRepository.findByUsername(username)
                .orElse(new FinalPrediction());

        boolean isUpdate = prediction.getId() != null;
        String previousChampionName = isUpdate ? prediction.getChampionTeam().getName() : null;
        String previousRunnerUpName = isUpdate ? prediction.getRunnerUpTeam().getName() : null;

        if (isUpdate
                && championTeam.getId().equals(prediction.getChampionTeam().getId())
                && runnerUpTeam.getId().equals(prediction.getRunnerUpTeam().getId())) {
            return;
        }

        prediction.setUsername(username);
        prediction.setChampionTeam(championTeam);
        prediction.setRunnerUpTeam(runnerUpTeam);
        prediction.setUpdatedAt(Instant.now());

        FinalResult result = finalResultRepository.findById(FinalResult.SINGLETON_ID).orElse(null);
        if (result != null) {
            prediction.setPoints(calculatePoints(prediction, result));
        } else {
            prediction.setPoints(null);
        }

        predictionRepository.save(prediction);

        predictionAuditService.recordFinalPredictionChange(
                username,
                championTeam.getName(),
                runnerUpTeam.getName(),
                isUpdate ? PredictionAuditAction.UPDATED : PredictionAuditAction.CREATED,
                previousChampionName,
                previousRunnerUpName);
    }

    @Transactional
    public void saveFinalResult(Integer championTeamId, Integer runnerUpTeamId) {
        Team championTeam = loadTeam(championTeamId);
        Team runnerUpTeam = loadTeam(runnerUpTeamId);
        if (championTeam.getId().equals(runnerUpTeam.getId())) {
            throw new IllegalArgumentException("Champion and runner-up must be different teams.");
        }

        FinalResult result = finalResultRepository.findById(FinalResult.SINGLETON_ID)
                .orElse(new FinalResult(championTeam, runnerUpTeam));
        result.setChampionTeam(championTeam);
        result.setRunnerUpTeam(runnerUpTeam);
        finalResultRepository.save(result);

        for (FinalPrediction prediction : predictionRepository.findAllWithTeams()) {
            prediction.setPoints(calculatePoints(prediction, result));
            predictionRepository.save(prediction);
        }
    }

    public String statusLabel(FinalPrediction prediction) {
        if (prediction == null) {
            return isEditable() ? "Open" : "Missed";
        }
        if (prediction.getPoints() != null) {
            return "Scored";
        }
        return isEditable() ? "Saved" : "Locked";
    }

    int calculatePoints(FinalPrediction prediction, FinalResult result) {
        int points = 0;

        Integer predictedChampionId = prediction.getChampionTeam().getId();
        Integer predictedRunnerUpId = prediction.getRunnerUpTeam().getId();
        Integer actualChampionId = result.getChampionTeam().getId();
        Integer actualRunnerUpId = result.getRunnerUpTeam().getId();

        if (predictedChampionId.equals(actualChampionId)) {
            points += CHAMPION_POINTS;
        } else if (predictedChampionId.equals(actualRunnerUpId)) {
            points += WRONG_POSITION_POINTS;
        }

        if (predictedRunnerUpId.equals(actualRunnerUpId)) {
            points += RUNNER_UP_POINTS;
        } else if (predictedRunnerUpId.equals(actualChampionId)) {
            points += WRONG_POSITION_POINTS;
        }

        return points;
    }

    private Team loadTeam(Integer teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
    }
}
