package com.alper.worldcup.dao;

import com.alper.worldcup.entity.GroupStandingPrediction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GroupStandingPredictionRepository extends JpaRepository<GroupStandingPrediction, Integer> {

    Optional<GroupStandingPrediction> findByUsernameAndGroupName(String username, String groupName);

    List<GroupStandingPrediction> findByUsername(String username);

    List<GroupStandingPrediction> findByGroupName(String groupName);

    @Query("SELECT g FROM GroupStandingPrediction g "
            + "JOIN FETCH g.firstPlaceTeam JOIN FETCH g.secondPlaceTeam "
            + "WHERE g.username = :username ORDER BY g.groupName")
    List<GroupStandingPrediction> findByUsernameWithTeams(String username);

    @Query("SELECT g FROM GroupStandingPrediction g "
            + "JOIN FETCH g.firstPlaceTeam JOIN FETCH g.secondPlaceTeam ORDER BY g.username, g.groupName")
    List<GroupStandingPrediction> findAllWithTeamsOrderByUsernameAndGroup();

    @Query("SELECT g FROM GroupStandingPrediction g WHERE g.points IS NOT NULL")
    List<GroupStandingPrediction> findAllScored();

    @Query("SELECT g.username, COALESCE(SUM(g.points), 0) FROM GroupStandingPrediction g "
            + "WHERE g.points IS NOT NULL GROUP BY g.username")
    List<Object[]> findLeaderboardTotals();
}
