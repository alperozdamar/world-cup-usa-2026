package com.alper.worldcup.dao;

import com.alper.worldcup.entity.Prediction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PredictionRepository extends JpaRepository<Prediction, Integer> {

    Optional<Prediction> findByUsernameAndMatchId(String username, Integer matchId);

    List<Prediction> findByUsername(String username);

    List<Prediction> findByMatchId(Integer matchId);

    @Query("SELECT p.username, COALESCE(SUM(p.points), 0) FROM Prediction p "
            + "WHERE p.points IS NOT NULL GROUP BY p.username ORDER BY COALESCE(SUM(p.points), 0) DESC")
    List<Object[]> findLeaderboardTotals();

    @Query("SELECT p.username, COALESCE(SUM(p.points), 0) FROM Prediction p JOIN p.match m "
            + "WHERE p.points IS NOT NULL AND m.stage = com.alper.worldcup.entity.MatchStage.GROUP_STAGE "
            + "GROUP BY p.username")
    List<Object[]> findGroupStageLeaderboardTotals();

    @Query("SELECT p.username, COALESCE(SUM(p.points), 0) FROM Prediction p JOIN p.match m "
            + "WHERE p.points IS NOT NULL AND m.stage <> com.alper.worldcup.entity.MatchStage.GROUP_STAGE "
            + "GROUP BY p.username")
    List<Object[]> findKnockoutLeaderboardTotals();

    @Query("SELECT p.username, COALESCE(SUM(COALESCE(p.points, 0)), 0) FROM Prediction p JOIN p.match m "
            + "WHERE m.stage <> com.alper.worldcup.entity.MatchStage.GROUP_STAGE "
            + "GROUP BY p.username")
    List<Object[]> findKnockoutPointsTotalsByUser();

    @Query("SELECT p FROM Prediction p JOIN FETCH p.match m "
            + "WHERE m.homeScoreActual IS NOT NULL AND m.awayScoreActual IS NOT NULL")
    List<Prediction> findAllScoredWithMatch();

    @Query("SELECT p FROM Prediction p JOIN FETCH p.match m "
            + "WHERE p.username = :username "
            + "AND m.homeScoreActual IS NOT NULL AND m.awayScoreActual IS NOT NULL")
    List<Prediction> findScoredByUsernameWithMatch(String username);
}
