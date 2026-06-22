package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnockoutAdminService {

    private final MatchRepository matchRepository;
    private final KnockoutBracketResolver knockoutBracketResolver;
    private final boolean teamSyncEnabled;

    public KnockoutAdminService(MatchRepository matchRepository,
                                KnockoutBracketResolver knockoutBracketResolver,
                                @Value("${app.knockout.team-sync.enabled:false}") boolean teamSyncEnabled) {
        this.matchRepository = matchRepository;
        this.knockoutBracketResolver = knockoutBracketResolver;
        this.teamSyncEnabled = teamSyncEnabled;
    }

    public boolean isTeamSyncEnabled() {
        return teamSyncEnabled;
    }

    @Transactional(readOnly = true)
    public List<KnockoutStageStatusView> getStageStatuses() {
        List<KnockoutStageStatusView> statuses = new ArrayList<>();
        for (MatchStage stage : List.of(
                MatchStage.ROUND_OF_32,
                MatchStage.ROUND_OF_16,
                MatchStage.QUARTER_FINAL,
                MatchStage.SEMI_FINAL,
                MatchStage.THIRD_PLACE,
                MatchStage.FINAL)) {
            List<Match> matches = matchesForStage(stage);
            if (matches.isEmpty()) {
                continue;
            }
            int withTeams = 0;
            int predictionsOpen = 0;
            for (Match match : matches) {
                if (match.getHomeTeam() != null && match.getAwayTeam() != null) {
                    withTeams++;
                }
                if (match.isPredictionsEnabled()) {
                    predictionsOpen++;
                }
            }
            statuses.add(new KnockoutStageStatusView(
                    stage,
                    KnockoutStageLabels.label(stage),
                    matches.size(),
                    withTeams,
                    predictionsOpen));
        }
        return statuses;
    }

    @Transactional
    public TeamSyncResult syncTeamsForStage(MatchStage stage) {
        if (!teamSyncEnabled) {
            throw new IllegalStateException("Knockout team sync is disabled on this server");
        }
        if (stage == MatchStage.GROUP_STAGE || stage == MatchStage.UNKNOWN) {
            throw new IllegalArgumentException("Cannot sync teams for stage: " + stage);
        }

        int updated = 0;
        int unresolved = 0;
        List<String> warnings = new ArrayList<>();

        for (Match match : matchesForStage(stage)) {
            Optional<Team> homeTeam = knockoutBracketResolver.resolveTeamForSide(match, true);
            Optional<Team> awayTeam = knockoutBracketResolver.resolveTeamForSide(match, false);

            if (homeTeam.isEmpty() || awayTeam.isEmpty()) {
                unresolved++;
                warnings.add("Match " + match.getId() + ": could not resolve "
                        + (homeTeam.isEmpty() ? "home" : "away") + " side");
                continue;
            }

            boolean changed = !sameTeam(homeTeam.get(), match.getHomeTeam())
                    || !sameTeam(awayTeam.get(), match.getAwayTeam());
            match.setHomeTeam(homeTeam.get());
            match.setAwayTeam(awayTeam.get());
            matchRepository.save(match);
            if (changed) {
                updated++;
            }
        }

        return new TeamSyncResult(updated, unresolved, warnings);
    }

    @Transactional
    public OpenPredictionsResult openPredictionsForStage(MatchStage stage) {
        if (stage == MatchStage.GROUP_STAGE || stage == MatchStage.UNKNOWN) {
            throw new IllegalArgumentException("Cannot open predictions for stage: " + stage);
        }

        int opened = 0;
        int alreadyOpen = 0;
        int missingTeams = 0;

        for (Match match : matchesForStage(stage)) {
            if (match.getHomeTeam() == null || match.getAwayTeam() == null) {
                missingTeams++;
                continue;
            }
            if (match.isPredictionsEnabled()) {
                alreadyOpen++;
                continue;
            }
            match.setPredictionsEnabled(true);
            matchRepository.save(match);
            opened++;
        }

        return new OpenPredictionsResult(opened, alreadyOpen, missingTeams);
    }

    private List<Match> matchesForStage(MatchStage stage) {
        return matchRepository.findKnockoutMatchesWithTeams().stream()
                .filter(match -> match.getStage() == stage)
                .toList();
    }

    private static boolean sameTeam(Team left, Team right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return Objects.equals(left.getId(), right.getId());
    }

    public record TeamSyncResult(int matchesUpdated, int unresolvedSides, List<String> warnings) {
    }

    public record OpenPredictionsResult(int opened, int alreadyOpen, int missingTeams) {
    }

    public record KnockoutStageStatusView(
            MatchStage stage,
            String label,
            int matchCount,
            int matchesWithTeams,
            int matchesWithPredictionsOpen) {
    }
}
