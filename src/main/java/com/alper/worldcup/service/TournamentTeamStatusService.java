package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TournamentTeamStatusService {

    private final MatchRepository matchRepository;

    public TournamentTeamStatusService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Transactional(readOnly = true)
    public Set<String> findEliminatedTeamNames() {
        List<Match> knockoutMatches = matchRepository.findKnockoutMatchesWithTeams();
        Set<String> eliminated = new HashSet<>();

        for (Match match : knockoutMatches) {
            if (!match.isScoreEntered() || match.getHomeTeam() == null || match.getAwayTeam() == null) {
                continue;
            }
            Optional<Team> winner = winnerTeam(match);
            if (winner.isEmpty()) {
                continue;
            }
            Team home = match.getHomeTeam();
            Team away = match.getAwayTeam();
            if (!winner.get().getId().equals(home.getId())) {
                eliminated.add(home.getName());
            }
            if (!winner.get().getId().equals(away.getId())) {
                eliminated.add(away.getName());
            }
        }

        addGroupStageEliminated(eliminated, knockoutMatches);
        return Set.copyOf(eliminated);
    }

    private void addGroupStageEliminated(Set<String> eliminated, List<Match> knockoutMatches) {
        boolean roundOf32TeamsAssigned = knockoutMatches.stream()
                .anyMatch(match -> match.getStage() == MatchStage.ROUND_OF_32
                        && match.getHomeTeam() != null
                        && match.getAwayTeam() != null);
        if (!roundOf32TeamsAssigned) {
            return;
        }

        Set<String> inKnockout = new HashSet<>();
        for (Match match : knockoutMatches) {
            if (match.getHomeTeam() != null) {
                inKnockout.add(match.getHomeTeam().getName());
            }
            if (match.getAwayTeam() != null) {
                inKnockout.add(match.getAwayTeam().getName());
            }
        }

        matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE).stream()
                .flatMap(match -> java.util.stream.Stream.of(match.getHomeTeam(), match.getAwayTeam()))
                .filter(Objects::nonNull)
                .map(Team::getName)
                .filter(name -> !inKnockout.contains(name))
                .forEach(eliminated::add);
    }

    private Optional<Team> winnerTeam(Match match) {
        int homeGoals = match.getHomeScoreActual();
        int awayGoals = match.getAwayScoreActual();
        if (homeGoals > awayGoals) {
            return Optional.ofNullable(match.getHomeTeam());
        }
        if (awayGoals > homeGoals) {
            return Optional.ofNullable(match.getAwayTeam());
        }
        return Optional.ofNullable(match.getAdvancingTeamActual());
    }
}
