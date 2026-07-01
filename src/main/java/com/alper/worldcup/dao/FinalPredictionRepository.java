package com.alper.worldcup.dao;

import com.alper.worldcup.entity.FinalPrediction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FinalPredictionRepository extends JpaRepository<FinalPrediction, Integer> {

    Optional<FinalPrediction> findByUsername(String username);

    @Query("SELECT f FROM FinalPrediction f "
            + "JOIN FETCH f.championTeam JOIN FETCH f.runnerUpTeam "
            + "WHERE f.username = :username")
    Optional<FinalPrediction> findByUsernameWithTeams(String username);

    @Query("SELECT f FROM FinalPrediction f "
            + "JOIN FETCH f.championTeam JOIN FETCH f.runnerUpTeam")
    List<FinalPrediction> findAllWithTeams();

    @Query("SELECT f FROM FinalPrediction f "
            + "JOIN FETCH f.championTeam JOIN FETCH f.runnerUpTeam ORDER BY f.username")
    List<FinalPrediction> findAllWithTeamsOrderByUsername();

    @Query("SELECT f FROM FinalPrediction f WHERE f.points IS NOT NULL")
    List<FinalPrediction> findAllScored();

    @Query("SELECT f.username, COALESCE(SUM(f.points), 0) FROM FinalPrediction f "
            + "WHERE f.points IS NOT NULL GROUP BY f.username")
    List<Object[]> findLeaderboardTotals();
}
