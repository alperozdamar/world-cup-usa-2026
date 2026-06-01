package com.alper.worldcup.dao;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MatchRepository extends JpaRepository<Match, Integer> {

    long countByStage(MatchStage stage);

    @Query("SELECT m FROM Match m JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam "
            + "WHERE m.stage = :stage ORDER BY m.kickoffUtc")
    List<Match> findByStageWithTeams(MatchStage stage);

    @Query("SELECT m FROM Match m LEFT JOIN FETCH m.homeTeam LEFT JOIN FETCH m.awayTeam "
            + "ORDER BY m.kickoffUtc")
    List<Match> findAllWithTeams();
}
