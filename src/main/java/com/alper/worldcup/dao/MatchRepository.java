package com.alper.worldcup.dao;

import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MatchRepository extends JpaRepository<Match, Integer> {

    long countByStage(MatchStage stage);

    @Query("SELECT COUNT(m) FROM Match m "
            + "WHERE m.homeScoreActual IS NOT NULL AND m.awayScoreActual IS NOT NULL")
    long countScoredMatches();

    @Query("SELECT COALESCE(SUM(m.homeScoreActual + m.awayScoreActual), 0) FROM Match m "
            + "WHERE m.homeScoreActual IS NOT NULL AND m.awayScoreActual IS NOT NULL")
    long sumGoalsScored();

    @Query("SELECT COUNT(m) FROM Match m WHERE m.penaltyShootoutActual = TRUE")
    long countPenaltyShootouts();

    @Query("SELECT m FROM Match m JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam "
            + "WHERE m.stage = :stage ORDER BY m.kickoffUtc")
    List<Match> findByStageWithTeams(MatchStage stage);

    @Query("SELECT m FROM Match m LEFT JOIN FETCH m.homeTeam LEFT JOIN FETCH m.awayTeam "
            + "ORDER BY m.kickoffUtc")
    List<Match> findAllWithTeams();

    @Query("SELECT MIN(m.kickoffUtc) FROM Match m "
            + "WHERE m.groupName = :groupName AND m.stage = com.alper.worldcup.entity.MatchStage.GROUP_STAGE")
    Optional<Instant> findEarliestGroupStageKickoff(String groupName);

    @Query("SELECT DISTINCT m.groupName FROM Match m "
            + "WHERE m.stage = com.alper.worldcup.entity.MatchStage.GROUP_STAGE AND m.groupName IS NOT NULL "
            + "ORDER BY m.groupName")
    List<String> findDistinctGroupStageGroupNames();

    @Query("SELECT m FROM Match m JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam "
            + "WHERE m.stage = com.alper.worldcup.entity.MatchStage.GROUP_STAGE AND m.groupName = :groupName")
    List<Match> findGroupStageMatchesByGroup(String groupName);

    @Query("SELECT MIN(m.kickoffUtc) FROM Match m "
            + "WHERE m.stage = com.alper.worldcup.entity.MatchStage.GROUP_STAGE")
    Optional<Instant> findTournamentStartKickoff();

    @Query("SELECT MAX(m.kickoffUtc) FROM Match m "
            + "WHERE m.stage = com.alper.worldcup.entity.MatchStage.GROUP_STAGE "
            + "AND m.groupName = :groupName")
    Optional<Instant> findLatestGroupMatchKickoff(String groupName);

    @Query("SELECT MIN(m.kickoffUtc) FROM Match m "
            + "WHERE m.stage = com.alper.worldcup.entity.MatchStage.FINAL")
    Optional<Instant> findFinalMatchKickoff();

    Optional<Match> findFirstByStageAndPredictionsEnabledTrueAndKickoffUtcAfterOrderByKickoffUtcAsc(
            MatchStage stage, Instant instant);

    @Query("SELECT m FROM Match m "
            + "WHERE m.predictionsEnabled = true "
            + "AND m.kickoffUtc > :now "
            + "AND m.homeTeam IS NOT NULL AND m.awayTeam IS NOT NULL "
            + "ORDER BY m.kickoffUtc ASC "
            + "LIMIT 1")
    Optional<Match> findNextPredictableMatch(Instant now);

    @Query("SELECT m FROM Match m JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam "
            + "WHERE m.stage = com.alper.worldcup.entity.MatchStage.GROUP_STAGE "
            + "AND m.predictionsEnabled = true "
            + "AND m.kickoffUtc > :now AND m.kickoffUtc <= :cutoff "
            + "ORDER BY m.kickoffUtc")
    List<Match> findOpenGroupStageMatchesKickingOffBetween(Instant now, Instant cutoff);

    @Query("SELECT m FROM Match m JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam "
            + "WHERE m.stage = com.alper.worldcup.entity.MatchStage.GROUP_STAGE "
            + "AND m.homeScoreActual IS NOT NULL AND m.awayScoreActual IS NOT NULL "
            + "ORDER BY m.kickoffUtc")
    List<Match> findScoredGroupStageMatchesWithTeams();

    @Query("SELECT m FROM Match m LEFT JOIN FETCH m.homeTeam LEFT JOIN FETCH m.awayTeam "
            + "WHERE m.stage <> com.alper.worldcup.entity.MatchStage.GROUP_STAGE "
            + "ORDER BY m.kickoffUtc")
    List<Match> findKnockoutMatchesWithTeams();
}
