package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Prediction;
import com.alper.worldcup.entity.PredictionAuditAction;
import com.alper.worldcup.entity.Team;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnockoutService {

    private static final List<MatchStage> ROUND_ORDER = List.of(
            MatchStage.ROUND_OF_32,
            MatchStage.ROUND_OF_16,
            MatchStage.QUARTER_FINAL,
            MatchStage.SEMI_FINAL,
            MatchStage.THIRD_PLACE,
            MatchStage.FINAL);

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final PredictionAuditService predictionAuditService;
    private final KnockoutBracketResolver knockoutBracketResolver;

    public KnockoutService(MatchRepository matchRepository,
                           PredictionRepository predictionRepository,
                           PredictionAuditService predictionAuditService,
                           KnockoutBracketResolver knockoutBracketResolver) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.predictionAuditService = predictionAuditService;
        this.knockoutBracketResolver = knockoutBracketResolver;
    }

    @Transactional(readOnly = true)
    public List<KnockoutRoundView> getKnockoutRounds(String username) {
        Instant now = Instant.now();
        List<Match> matches = matchRepository.findKnockoutMatchesWithTeams().stream()
                .sorted(Comparator
                        .comparing((Match match) -> match.hasStarted(now))
                        .thenComparing(Match::getKickoffUtc))
                .toList();

        Map<Integer, Prediction> predictions = new HashMap<>();
        java.util.Set<Integer> knockoutMatchIds = matches.stream()
                .map(Match::getId)
                .collect(java.util.stream.Collectors.toSet());
        for (Prediction prediction : predictionRepository.findByUsername(username)) {
            if (knockoutMatchIds.contains(prediction.getMatch().getId())) {
                predictions.put(prediction.getMatch().getId(), prediction);
            }
        }

        Map<Integer, KnockoutBracketResolver.ResolvedKnockoutSides> resolvedNames =
                knockoutBracketResolver.resolveDisplayNames(matches);

        Map<MatchStage, List<KnockoutMatchView>> byStage = new LinkedHashMap<>();
        for (MatchStage stage : ROUND_ORDER) {
            byStage.put(stage, new ArrayList<>());
        }

        for (Match match : matches) {
            Prediction prediction = predictions.get(match.getId());
            KnockoutBracketResolver.ResolvedKnockoutSides resolved =
                    resolvedNames.getOrDefault(match.getId(),
                            new KnockoutBracketResolver.ResolvedKnockoutSides("TBD", "TBD", null, null));
            byStage.computeIfAbsent(match.getStage(), ignored -> new ArrayList<>())
                    .add(new KnockoutMatchView(
                            match,
                            prediction,
                            isEditable(match, now),
                            statusLabel(match, prediction, now),
                            resolved.homeDisplayName(),
                            resolved.awayDisplayName(),
                            resolved.homeSlotLabel(),
                            resolved.awaySlotLabel()));
        }

        List<KnockoutRoundView> rounds = new ArrayList<>();
        for (MatchStage stage : ROUND_ORDER) {
            List<KnockoutMatchView> stageMatches = byStage.get(stage);
            if (stageMatches == null || stageMatches.isEmpty()) {
                continue;
            }
            rounds.add(new KnockoutRoundView(KnockoutStageLabels.label(stage), stage, stageMatches));
        }
        return rounds;
    }

    @Transactional
    public void saveKnockoutPrediction(String username,
                                         Integer matchId,
                                         Integer homeGuess,
                                         Integer awayGuess,
                                         Boolean penaltyShootoutGuess,
                                         Integer advancingTeamId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        if (!KnockoutStageLabels.isKnockout(match)) {
            throw new IllegalStateException("This page is only for knockout matches");
        }
        if (!match.isPredictionsEnabled()) {
            throw new IllegalStateException("Predictions are not enabled for this match yet");
        }
        if (match.hasStarted(Instant.now())) {
            throw new IllegalStateException("Kickoff has passed; predictions are locked");
        }
        validateScore(homeGuess);
        validateScore(awayGuess);
        validateTeamsResolved(match);

        Team advancingTeam = resolveAdvancingTeam(match, homeGuess, awayGuess, penaltyShootoutGuess, advancingTeamId);
        Boolean storedPenaltyGuess = homeGuess.equals(awayGuess) ? penaltyShootoutGuess : null;

        Prediction prediction = predictionRepository.findByUsernameAndMatchId(username, matchId)
                .orElse(new Prediction());

        boolean isUpdate = prediction.getId() != null;
        Integer previousHome = isUpdate ? prediction.getHomeScoreGuess() : null;
        Integer previousAway = isUpdate ? prediction.getAwayScoreGuess() : null;

        if (isUpdate
                && homeGuess.equals(previousHome)
                && awayGuess.equals(previousAway)
                && java.util.Objects.equals(storedPenaltyGuess, prediction.getPenaltyShootoutGuess())
                && advancingTeam.equals(prediction.getAdvancingTeamGuess())) {
            return;
        }

        prediction.setUsername(username);
        prediction.setMatch(match);
        prediction.setHomeScoreGuess(homeGuess);
        prediction.setAwayScoreGuess(awayGuess);
        prediction.setPenaltyShootoutGuess(storedPenaltyGuess);
        prediction.setAdvancingTeamGuess(advancingTeam);
        prediction.setUpdatedAt(Instant.now());
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

    private Team resolveAdvancingTeam(Match match,
                                      int homeGuess,
                                      int awayGuess,
                                      Boolean penaltyShootoutGuess,
                                      Integer advancingTeamId) {
        if (homeGuess > awayGuess) {
            return requireTeam(match.getHomeTeam(), "Home team");
        }
        if (awayGuess > homeGuess) {
            return requireTeam(match.getAwayTeam(), "Away team");
        }
        if (penaltyShootoutGuess == null) {
            throw new IllegalArgumentException("Pick yes or no for penalty shootout when regular time is a draw");
        }
        if (advancingTeamId == null) {
            throw new IllegalArgumentException("Pick the team to advance");
        }
        Team homeTeam = requireTeam(match.getHomeTeam(), "Home team");
        Team awayTeam = requireTeam(match.getAwayTeam(), "Away team");
        if (advancingTeamId.equals(homeTeam.getId())) {
            return homeTeam;
        }
        if (advancingTeamId.equals(awayTeam.getId())) {
            return awayTeam;
        }
        throw new IllegalArgumentException("Advancing team must be one of the two sides in this match");
    }

    private Team requireTeam(Team team, String label) {
        if (team == null) {
            throw new IllegalStateException(label + " is not set for this match yet");
        }
        return team;
    }

    private void validateTeamsResolved(Match match) {
        if (match.getHomeTeam() == null || match.getAwayTeam() == null) {
            throw new IllegalStateException("Teams are not confirmed for this match yet");
        }
    }

    private boolean isEditable(Match match, Instant now) {
        return match.isPredictionsEnabled()
                && KnockoutStageLabels.isKnockout(match)
                && !match.hasStarted(now)
                && match.getHomeTeam() != null
                && match.getAwayTeam() != null;
    }

    private String statusLabel(Match match, Prediction prediction, Instant now) {
        if (match.getHomeTeam() == null || match.getAwayTeam() == null) {
            return "Teams TBD";
        }
        if (!match.isPredictionsEnabled()) {
            return "Not open yet";
        }
        if (match.hasStarted(now)) {
            return prediction != null ? "Locked" : "Missed";
        }
        return prediction != null ? "Saved" : "Open";
    }

    private void validateScore(Integer score) {
        if (score == null || score < 0 || score > 20) {
            throw new IllegalArgumentException("Score must be between 0 and 20");
        }
    }
}
