package com.alper.worldcup.service;

import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class PointsServiceImpl implements PointsService {

    private static final int EXACT_SCORE_POINTS = 5;
    private static final int CORRECT_OUTCOME_POINTS = 2;
    private static final int GOAL_DIFFERENCE_BONUS = 1;
    private static final int MAX_BASE_POINTS = 6;
    private static final int PENALTY_SHOOTOUT_BONUS = 1;
    private static final int ADVANCING_TEAM_BONUS = 2;
    private static final int ADVANCING_TEAM_SOFT_BONUS = 1;
    private static final int ADVANCING_TEAM_OUTCOME_BONUS = 1;

    @Override
    public int calculatePoints(int guessHome, int guessAway, int actualHome, int actualAway,
                               MatchStage stage) {
        int basePoints = calculateBasePoints(guessHome, guessAway, actualHome, actualAway);
        return (int) Math.round(basePoints * stageMultiplier(stage));
    }

    @Override
    public int calculateKnockoutPoints(int guessHome,
                                       int guessAway,
                                       int actualHome,
                                       int actualAway,
                                       MatchStage stage,
                                       Boolean penaltyGuess,
                                       Boolean penaltyActual,
                                       Team advancingGuess,
                                       Team advancingActual) {
        int basePoints = calculateBasePoints(guessHome, guessAway, actualHome, actualAway);
        int extras = 0;
        boolean guessedDraw = guessHome == guessAway;
        boolean actualDraw = actualHome == actualAway;
        if (actualDraw && guessedDraw
                && penaltyGuess != null
                && penaltyActual != null
                && penaltyGuess.equals(penaltyActual)) {
            extras += PENALTY_SHOOTOUT_BONUS;
        }
        if (guessedDraw
                && advancingGuess != null
                && advancingActual != null
                && Objects.equals(advancingGuess.getId(), advancingActual.getId())) {
            extras += actualDraw ? ADVANCING_TEAM_BONUS : ADVANCING_TEAM_SOFT_BONUS;
        } else if (!guessedDraw
                && basePoints > 0
                && advancingGuess != null
                && advancingActual != null
                && Objects.equals(advancingGuess.getId(), advancingActual.getId())) {
            extras += ADVANCING_TEAM_OUTCOME_BONUS;
        }
        return (int) Math.round((basePoints + extras) * stageMultiplier(stage));
    }

    int calculateBasePoints(int guessHome, int guessAway, int actualHome, int actualAway) {
        if (guessHome == actualHome && guessAway == actualAway) {
            return EXACT_SCORE_POINTS;
        }

        int guessDiff = guessHome - guessAway;
        int actualDiff = actualHome - actualAway;

        if (matchOutcome(guessDiff) != matchOutcome(actualDiff)) {
            return 0;
        }

        int points = CORRECT_OUTCOME_POINTS;
        if (guessDiff == actualDiff) {
            points += GOAL_DIFFERENCE_BONUS;
        }
        return Math.min(points, MAX_BASE_POINTS);
    }

    double stageMultiplier(MatchStage stage) {
        if (stage == null) {
            return 1.0;
        }
        return switch (stage) {
            case GROUP_STAGE, ROUND_OF_32, UNKNOWN -> 1.0;
            case ROUND_OF_16 -> 1.25;
            case QUARTER_FINAL, THIRD_PLACE -> 1.5;
            case SEMI_FINAL -> 1.75;
            case FINAL -> 2.0;
        };
    }

    private int matchOutcome(int goalDifference) {
        return Integer.compare(goalDifference, 0);
    }
}
