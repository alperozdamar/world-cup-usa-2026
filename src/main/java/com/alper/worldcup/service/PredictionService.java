package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Prediction;
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

    public PredictionService(MatchRepository matchRepository,
                             PredictionRepository predictionRepository,
                             PointsService pointsService,
                             PredictionAuditService predictionAuditService) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.pointsService = pointsService;
        this.predictionAuditService = predictionAuditService;
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
            prediction.setPoints(calculateMatchPoints(match, homeGuess, awayGuess));
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
    public void saveActualScore(Integer matchId, Integer homeScore, Integer awayScore) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));
        validateScore(homeScore);
        validateScore(awayScore);

        match.setHomeScoreActual(homeScore);
        match.setAwayScoreActual(awayScore);
        matchRepository.save(match);

        for (Prediction prediction : predictionRepository.findByMatchId(matchId)) {
            prediction.setPoints(calculateMatchPoints(match,
                    prediction.getHomeScoreGuess(),
                    prediction.getAwayScoreGuess()));
            predictionRepository.save(prediction);
        }
    }

    @Transactional(readOnly = true)
    public List<Object[]> getLeaderboard() {
        return predictionRepository.findLeaderboardTotals();
    }

    private int calculateMatchPoints(Match match, int homeGuess, int awayGuess) {
        return pointsService.calculatePoints(
                homeGuess,
                awayGuess,
                match.getHomeScoreActual(),
                match.getAwayScoreActual(),
                match.getStage());
    }

    private void validateScore(Integer score) {
        if (score == null || score < 0 || score > 20) {
            throw new IllegalArgumentException("Score must be between 0 and 20");
        }
    }
}
