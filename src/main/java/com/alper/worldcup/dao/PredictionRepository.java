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
}
