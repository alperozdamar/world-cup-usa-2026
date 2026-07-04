package com.alper.worldcup.service;

import com.alper.worldcup.dao.FinalPredictionRepository;
import com.alper.worldcup.dao.GroupStandingPredictionRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.UserProfile;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private PredictionRepository predictionRepository;
    @Mock
    private GroupStandingPredictionRepository groupStandingPredictionRepository;
    @Mock
    private FinalPredictionRepository finalPredictionRepository;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private UserMatchStatsService userMatchStatsService;
    @Mock
    private FinalPredictionService finalPredictionService;
    @Mock
    private PointsTimelineService pointsTimelineService;

    private LeaderboardService service;

    @BeforeEach
    void setUp() {
        service = new LeaderboardService(
                predictionRepository,
                groupStandingPredictionRepository,
                finalPredictionRepository,
                userProfileService,
                new PoolMemberRegistry("default"),
                userMatchStatsService,
                finalPredictionService,
                pointsTimelineService);
        lenient().when(pointsTimelineService.tournamentStartDay(any())).thenReturn(LocalDate.now().plusDays(30));
    }

    @Test
    void includesPoolMembersWithZeroPoints() {
        stubMatchPoints(List.of(
                new Object[]{"gonenc", 5L},
                new Object[]{"alper", 2L}));
        when(groupStandingPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(finalPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(userProfileService.getPoolProfiles()).thenReturn(List.of(
                profile("gonenc", "Gonenc Gorgulu"),
                profile("kubilay", "Kubilay Kahraman"),
                profile("alper", "Alper Ozdamar")));
        when(userMatchStatsService.getStatsForPoolMembers()).thenReturn(Map.of(
                "gonenc", new UserMatchStats(0, 0, 0),
                "kubilay", new UserMatchStats(0, 0, 0),
                "alper", new UserMatchStats(0, 0, 0)));
        when(finalPredictionService.getChampionCorrectByUsername()).thenReturn(Map.of());

        List<Object[]> leaderboard = service.getLeaderboard();

        assertEquals(3, leaderboard.size());
        assertEquals("gonenc", leaderboard.get(0)[0]);
        assertEquals(5.0, leaderboard.get(0)[1]);
        assertEquals("alper", leaderboard.get(1)[0]);
        assertEquals(2.0, leaderboard.get(1)[1]);
        assertEquals("kubilay", leaderboard.get(2)[0]);
        assertEquals(0.0, leaderboard.get(2)[1]);
    }

    @Test
    void ignoresNonPoolUsersWithPoints() {
        stubMatchPoints(List.of(
                new Object[]{"susan", 99L},
                new Object[]{"gonenc", 5L}));
        when(groupStandingPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(finalPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(userProfileService.getPoolProfiles()).thenReturn(List.of(profile("gonenc", "Gonenc Gorgulu")));
        when(userMatchStatsService.getStatsForPoolMembers()).thenReturn(Map.of(
                "gonenc", new UserMatchStats(0, 0, 0)));
        when(finalPredictionService.getChampionCorrectByUsername()).thenReturn(Map.of());

        List<Object[]> leaderboard = service.getLeaderboard();

        assertEquals(1, leaderboard.size());
        assertEquals("gonenc", leaderboard.get(0)[0]);
        assertTrue(leaderboard.stream().noneMatch(row -> "susan".equals(row[0])));
    }

    @Test
    void leaderboardRowsBreakDownPointsByCategory() {
        when(predictionRepository.findGroupStageLeaderboardTotals()).thenReturn(
                List.<Object[]>of(new Object[]{"gonenc", 30L}));
        when(predictionRepository.findKnockoutLeaderboardTotals()).thenReturn(
                List.<Object[]>of(new Object[]{"gonenc", 10L}));
        when(groupStandingPredictionRepository.findLeaderboardTotals()).thenReturn(
                List.<Object[]>of(new Object[]{"gonenc", 12L}));
        when(finalPredictionRepository.findLeaderboardTotals()).thenReturn(
                List.<Object[]>of(new Object[]{"gonenc", 8L}));
        when(userProfileService.getPoolProfiles()).thenReturn(List.of(profile("gonenc", "Gonenc Gorgulu")));
        when(userMatchStatsService.getStatsForPoolMembers()).thenReturn(Map.of(
                "gonenc", new UserMatchStats(0, 0, 0)));
        when(finalPredictionService.getChampionCorrectByUsername()).thenReturn(Map.of());

        List<LeaderboardRowView> rows = service.getLeaderboardRows();

        assertEquals(1, rows.size());
        assertEquals(30.0, rows.get(0).matchPoints());
        assertEquals(10.0, rows.get(0).knockoutPoints());
        assertEquals(12.0, rows.get(0).groupPoints());
        assertEquals(8.0, rows.get(0).finalPoints());
        assertEquals(60.0, rows.get(0).totalPoints());
    }

    @Test
    void tieBreaksOnSuccessRateThenChampionPick() {
        stubMatchPoints(List.of(
                new Object[]{"alper", 10L},
                new Object[]{"gonenc", 10L}));
        when(groupStandingPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(finalPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(userProfileService.getPoolProfiles()).thenReturn(List.of(
                profile("alper", "Alper Ozdamar"),
                profile("gonenc", "Gonenc Gorgulu")));
        when(userMatchStatsService.getStatsForPoolMembers()).thenReturn(Map.of(
                "alper", new UserMatchStats(1, 3, 7),
                "gonenc", new UserMatchStats(2, 5, 5)));
        when(finalPredictionService.getChampionCorrectByUsername()).thenReturn(Map.of());

        List<LeaderboardRowView> rows = service.getLeaderboardRows();

        assertEquals("gonenc", rows.get(0).username());
        assertEquals("alper", rows.get(1).username());
    }

    @Test
    void tieBreaksOnChampionWhenSuccessRateIsEqual() {
        stubMatchPoints(List.of(
                new Object[]{"alper", 10L},
                new Object[]{"gonenc", 10L}));
        when(groupStandingPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(finalPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(userProfileService.getPoolProfiles()).thenReturn(List.of(
                profile("alper", "Alper Ozdamar"),
                profile("gonenc", "Gonenc Gorgulu")));
        when(userMatchStatsService.getStatsForPoolMembers()).thenReturn(Map.of(
                "alper", new UserMatchStats(1, 5, 5),
                "gonenc", new UserMatchStats(1, 5, 5)));
        when(finalPredictionService.getChampionCorrectByUsername()).thenReturn(Map.of(
                "alper", true,
                "gonenc", false));

        List<LeaderboardRowView> rows = service.getLeaderboardRows();

        assertEquals("alper", rows.get(0).username());
        assertEquals("gonenc", rows.get(1).username());
    }

    @Test
    void successRateForSortTreatsMissingStatsAsLowest() {
        assertEquals(-1, LeaderboardService.successRateForSort(null));
        assertEquals(-1, LeaderboardService.successRateForSort(new UserMatchStats(0, 0, 0)));
        assertEquals(50, LeaderboardService.successRateForSort(new UserMatchStats(1, 5, 5)));
    }

    @Test
    void rankMovementComparesAgainstPreviousDay() {
        ZoneId zoneId = ZoneId.of("America/New_York");
        LocalDate yesterday = LocalDate.now(zoneId).minusDays(1);
        when(pointsTimelineService.tournamentStartDay(zoneId)).thenReturn(yesterday.minusDays(10));
        when(pointsTimelineService.cumulativeTotalsThrough(yesterday, zoneId)).thenReturn(Map.of(
                "alper", 8.0,
                "gonenc", 10.0));
        when(userMatchStatsService.getStatsForPoolMembersThrough(yesterday, zoneId)).thenReturn(Map.of(
                "alper", new UserMatchStats(0, 0, 0),
                "gonenc", new UserMatchStats(0, 0, 0)));
        when(finalPredictionService.getChampionCorrectByUsernameThrough(yesterday, zoneId)).thenReturn(Map.of());

        stubMatchPoints(List.of(
                new Object[]{"alper", 12L},
                new Object[]{"gonenc", 10L}));
        when(groupStandingPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(finalPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(userProfileService.getPoolProfiles()).thenReturn(List.of(
                profile("alper", "Alper Ozdamar"),
                profile("gonenc", "Gonenc Gorgulu")));
        when(userMatchStatsService.getStatsForPoolMembers()).thenReturn(Map.of(
                "alper", new UserMatchStats(0, 0, 0),
                "gonenc", new UserMatchStats(0, 0, 0)));
        when(finalPredictionService.getChampionCorrectByUsername()).thenReturn(Map.of());

        List<LeaderboardRowView> rows = service.getLeaderboardRows(zoneId);

        assertEquals("alper", rows.get(0).username());
        assertEquals(RankMovement.UP, rows.get(0).rankMovement());
        assertEquals(2, rows.get(0).previousRank());
        assertEquals("gonenc", rows.get(1).username());
        assertEquals(RankMovement.DOWN, rows.get(1).rankMovement());
        assertEquals(1, rows.get(1).previousRank());
    }

    @Test
    void movementForRanks() {
        assertEquals(RankMovement.UP, LeaderboardService.movementFor(3, 2));
        assertEquals(RankMovement.DOWN, LeaderboardService.movementFor(2, 3));
        assertEquals(RankMovement.STABLE, LeaderboardService.movementFor(2, 2));
        assertEquals(RankMovement.NONE, LeaderboardService.movementFor(null, 1));
    }

    private void stubMatchPoints(List<Object[]> combinedTotals) {
        when(predictionRepository.findGroupStageLeaderboardTotals()).thenReturn(combinedTotals);
        when(predictionRepository.findKnockoutLeaderboardTotals()).thenReturn(List.of());
    }

    private static UserProfile profile(String username, String displayName) {
        return new UserProfile(username, "America/New_York", displayName);
    }
}
