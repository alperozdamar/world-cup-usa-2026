package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Prediction;
import com.alper.worldcup.entity.Team;
import com.alper.worldcup.entity.PredictionAuditAction;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PredictionService {

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final PointsService pointsService;
    private final PredictionAuditService predictionAuditService;
    private final KnockoutAssignmentService knockoutAssignmentService;

    public PredictionService(MatchRepository matchRepository,
                             PredictionRepository predictionRepository,
                             PointsService pointsService,
                             PredictionAuditService predictionAuditService,
                             KnockoutAssignmentService knockoutAssignmentService) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.pointsService = pointsService;
        this.predictionAuditService = predictionAuditService;
        this.knockoutAssignmentService = knockoutAssignmentService;
    }

    @Transactional(readOnly = true)
    public List<Match> getGroupStageMatches() {
        return sortGroupStageMatchesForList(
                matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE),
                Instant.now());
    }

    static List<Match> sortGroupStageMatchesForList(List<Match> matches, Instant now) {
        return matches.stream()
                .sorted(Comparator
                        .comparing((Match match) -> match.hasStarted(now))
                        .thenComparing(Match::getKickoffUtc))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<Integer, Prediction> getPredictionsForUser(String username) {
        Map<Integer, Prediction> byMatchId = new HashMap<>();
        for (Prediction prediction : predictionRepository.findByUsername(username)) {
            byMatchId.put(prediction.getMatch().getId(), prediction);
        }
        return byMatchId;
    }

    @Transactional
    public void savePrediction(String username, Integer matchId, Integer homeGuess, Integer awayGuess) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        if (match.getStage() != MatchStage.GROUP_STAGE) {
            throw new IllegalStateException("Predictions are only open for group stage matches");
        }
        if (!match.isPredictionsEnabled()) {
            throw new IllegalStateException("Predictions are not enabled for this match yet");
        }
        if (match.hasStarted(Instant.now())) {
            throw new IllegalStateException("Kickoff has passed; predictions are locked");
        }
        validateScore(homeGuess);
        validateScore(awayGuess);

        Prediction prediction = predictionRepository.findByUsernameAndMatchId(username, matchId)
                .orElse(new Prediction());

        boolean isUpdate = prediction.getId() != null;
        Integer previousHome = isUpdate ? prediction.getHomeScoreGuess() : null;
        Integer previousAway = isUpdate ? prediction.getAwayScoreGuess() : null;

        if (isUpdate && homeGuess.equals(previousHome) && awayGuess.equals(previousAway)) {
            return;
        }

        prediction.setUsername(username);
        prediction.setMatch(match);
        prediction.setHomeScoreGuess(homeGuess);
        prediction.setAwayScoreGuess(awayGuess);
        prediction.setUpdatedAt(Instant.now());

        if (match.isScoreEntered()) {
            prediction.setPoints(calculateMatchPoints(match, prediction));
        }

        predictionRepository.save(prediction);

        predictionAuditService.recordPredictionChange(
                username,
                match,
                homeGuess,
                awayGuess,
                isUpdate ? PredictionAuditAction.UPDATED : PredictionAuditAction.CREATED,
                previousHome,
                previousAway);
    }

    @Transactional
    public SaveScoreResult saveActualScore(Integer matchId,
                                           Integer homeScore,
                                           Integer awayScore,
                                           Integer advancingTeamId,
                                           Boolean penaltyShootout) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));
        validateScore(homeScore);
        validateScore(awayScore);

        match.setHomeScoreActual(homeScore);
        match.setAwayScoreActual(awayScore);
        if (KnockoutStageLabels.isKnockout(match)) {
            if (homeScore.equals(awayScore)) {
                match.setAdvancingTeamActual(requireAdvancingTeam(match, advancingTeamId));
                if (penaltyShootout == null) {
                    throw new IllegalArgumentException(
                            "Pick yes or no for penalty shootout when the score is level at 90′.");
                }
                match.setPenaltyShootoutActual(penaltyShootout);
            } else {
                match.setAdvancingTeamActual(null);
                match.setPenaltyShootoutActual(null);
            }
        } else {
            match.setPenaltyShootoutActual(null);
        }
        matchRepository.save(match);

        for (Prediction prediction : predictionRepository.findByMatchId(matchId)) {
            prediction.setPoints(calculateMatchPoints(match, prediction));
            predictionRepository.save(prediction);
        }

        KnockoutSyncResult sync = null;
        if (KnockoutStageLabels.isKnockout(match)) {
            sync = knockoutAssignmentService.syncBracketFromStandings(Instant.now());
        }
        return new SaveScoreResult(sync);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getLeaderboard() {
        return predictionRepository.findLeaderboardTotals();
    }

    private double calculateMatchPoints(Match match, Prediction prediction) {
        if (KnockoutStageLabels.isKnockout(match)) {
            return pointsService.calculateKnockoutPoints(
                    prediction.getHomeScoreGuess(),
                    prediction.getAwayScoreGuess(),
                    match.getHomeScoreActual(),
                    match.getAwayScoreActual(),
                    match.getStage(),
                    prediction.getPenaltyShootoutGuess(),
                    match.getPenaltyShootoutActual(),
                    prediction.getAdvancingTeamGuess(),
                    resolveActualAdvancer(match));
        }
        return pointsService.calculatePoints(
                prediction.getHomeScoreGuess(),
                prediction.getAwayScoreGuess(),
                match.getHomeScoreActual(),
                match.getAwayScoreActual(),
                match.getStage());
    }

    static Team resolveActualAdvancer(Match match) {
        if (!match.isScoreEntered()) {
            return null;
        }
        int homeScore = match.getHomeScoreActual();
        int awayScore = match.getAwayScoreActual();
        if (homeScore > awayScore) {
            return match.getHomeTeam();
        }
        if (awayScore > homeScore) {
            return match.getAwayTeam();
        }
        return match.getAdvancingTeamActual();
    }

    private void validateScore(Integer score) {
        if (score == null || score < 0 || score > 20) {
            throw new IllegalArgumentException("Score must be between 0 and 20");
        }
    }

    private Team requireAdvancingTeam(Match match, Integer advancingTeamId) {
        if (advancingTeamId == null) {
            throw new IllegalArgumentException(
                    "Select which team advances when the score is level at 90′.");
        }
        if (match.getHomeTeam() != null && advancingTeamId.equals(match.getHomeTeam().getId())) {
            return match.getHomeTeam();
        }
        if (match.getAwayTeam() != null && advancingTeamId.equals(match.getAwayTeam().getId())) {
            return match.getAwayTeam();
        }
        throw new IllegalArgumentException("Advancing team must be one of the two sides in this match.");
    }
}
