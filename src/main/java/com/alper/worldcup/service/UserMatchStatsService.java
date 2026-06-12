package com.alper.worldcup.service;

import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.Prediction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserMatchStatsService {

    private final PredictionRepository predictionRepository;
    private final PointsServiceImpl pointsService;

    public UserMatchStatsService(PredictionRepository predictionRepository,
                                 PointsServiceImpl pointsService) {
        this.predictionRepository = predictionRepository;
        this.pointsService = pointsService;
    }

    @Transactional(readOnly = true)
    public UserMatchStats getStats(String username) {
        int exactScores = 0;
        int correctOutcomes = 0;

        for (Prediction prediction : predictionRepository.findScoredByUsernameWithMatch(username)) {
            Match match = prediction.getMatch();
            if (isExactScore(prediction, match)) {
                exactScores++;
            }
            if (hasCorrectOutcome(prediction, match)) {
                correctOutcomes++;
            }
        }

        return new UserMatchStats(exactScores, correctOutcomes);
    }

    private boolean isExactScore(Prediction prediction, Match match) {
        return prediction.getHomeScoreGuess().equals(match.getHomeScoreActual())
                && prediction.getAwayScoreGuess().equals(match.getAwayScoreActual());
    }

    private boolean hasCorrectOutcome(Prediction prediction, Match match) {
        return pointsService.calculateBasePoints(
                prediction.getHomeScoreGuess(),
                prediction.getAwayScoreGuess(),
                match.getHomeScoreActual(),
                match.getAwayScoreActual()) > 0;
    }
}
