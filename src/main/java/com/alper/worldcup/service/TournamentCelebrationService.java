package com.alper.worldcup.service;

import com.alper.worldcup.dao.AppSettingRepository;
import com.alper.worldcup.dao.FinalResultRepository;
import com.alper.worldcup.entity.AppSetting;
import com.alper.worldcup.entity.FinalResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TournamentCelebrationService {

    static final String KEY_TOURNAMENT_ENDED = "tournament_ended";
    static final String KEY_TOURNAMENT_ENDED_AT = "tournament_ended_at";

    private final AppSettingRepository appSettingRepository;
    private final FinalResultRepository finalResultRepository;
    private final LeaderboardService leaderboardService;
    private final UserProfileService userProfileService;
    private final UserMatchStatsService userMatchStatsService;
    private final PoolMemberRegistry poolMemberRegistry;

    public TournamentCelebrationService(AppSettingRepository appSettingRepository,
                                        FinalResultRepository finalResultRepository,
                                        LeaderboardService leaderboardService,
                                        UserProfileService userProfileService,
                                        UserMatchStatsService userMatchStatsService,
                                        PoolMemberRegistry poolMemberRegistry) {
        this.appSettingRepository = appSettingRepository;
        this.finalResultRepository = finalResultRepository;
        this.leaderboardService = leaderboardService;
        this.userProfileService = userProfileService;
        this.userMatchStatsService = userMatchStatsService;
        this.poolMemberRegistry = poolMemberRegistry;
    }

    @Transactional(readOnly = true)
    public boolean isTournamentEnded() {
        return appSettingRepository.findById(KEY_TOURNAMENT_ENDED)
                .map(s -> "true".equals(s.getValue()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isFinalMatchScored() {
        return finalResultRepository.findByIdWithTeams(FinalResult.SINGLETON_ID).isPresent();
    }

    @Transactional
    public void endTournament() {
        appSettingRepository.save(new AppSetting(KEY_TOURNAMENT_ENDED, "true"));
        appSettingRepository.save(new AppSetting(KEY_TOURNAMENT_ENDED_AT, Instant.now().toString()));
    }

    @Transactional
    public void resetTournamentEnd() {
        appSettingRepository.deleteById(KEY_TOURNAMENT_ENDED);
        appSettingRepository.deleteById(KEY_TOURNAMENT_ENDED_AT);
    }

    @Transactional(readOnly = true)
    public Optional<CelebrationData> getCelebrationData() {
        if (!isTournamentEnded()) {
            return Optional.empty();
        }

        List<LeaderboardRowView> rows = leaderboardService.getLeaderboardRows();
        if (rows.isEmpty()) {
            return Optional.empty();
        }

        LeaderboardRowView winner = rows.get(0);
        String displayName = userProfileService.getDisplayName(winner.username());
        UserMatchStats stats = userMatchStatsService.getStatsForPoolMembers()
                .getOrDefault(winner.username(), new UserMatchStats(0, 0, 0));

        String championTeamName = finalResultRepository.findByIdWithTeams(FinalResult.SINGLETON_ID)
                .map(fr -> fr.getChampionTeam().getName())
                .orElse(null);

        List<RunnerUpEntry> topThree = rows.stream()
                .limit(3)
                .map(row -> new RunnerUpEntry(
                        row.username(),
                        userProfileService.getDisplayName(row.username()),
                        row.totalPoints()))
                .toList();

        return Optional.of(new CelebrationData(
                winner.username(),
                displayName,
                winner.totalPoints(),
                stats,
                championTeamName,
                topThree));
    }

    public record CelebrationData(
            String winnerUsername,
            String winnerDisplayName,
            double totalPoints,
            UserMatchStats stats,
            String championTeamName,
            List<RunnerUpEntry> topThree) {
    }

    public record RunnerUpEntry(
            String username,
            String displayName,
            double totalPoints) {
    }
}
