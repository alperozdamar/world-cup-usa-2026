package com.alper.worldcup.service;

import com.alper.worldcup.dao.FinalPredictionRepository;
import com.alper.worldcup.dao.GroupStandingPredictionRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.entity.UserProfile;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    private LeaderboardService service;

    @BeforeEach
    void setUp() {
        service = new LeaderboardService(
                predictionRepository,
                groupStandingPredictionRepository,
                finalPredictionRepository,
                userProfileService,
                new PoolMemberRegistry("default"));
    }

    @Test
    void includesPoolMembersWithZeroPoints() {
        when(predictionRepository.findLeaderboardTotals()).thenReturn(List.of(
                new Object[]{"gonenc", 5L},
                new Object[]{"alper", 2L}));
        when(groupStandingPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(finalPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(userProfileService.getPoolProfiles()).thenReturn(List.of(
                profile("gonenc", "Gonenc Gorgulu"),
                profile("kubilay", "Kubilay Kahraman"),
                profile("alper", "Alper Ozdamar")));

        List<Object[]> leaderboard = service.getLeaderboard();

        assertEquals(3, leaderboard.size());
        assertEquals("gonenc", leaderboard.get(0)[0]);
        assertEquals(5L, leaderboard.get(0)[1]);
        assertEquals("alper", leaderboard.get(1)[0]);
        assertEquals(2L, leaderboard.get(1)[1]);
        assertEquals("kubilay", leaderboard.get(2)[0]);
        assertEquals(0L, leaderboard.get(2)[1]);
    }

    @Test
    void ignoresNonPoolUsersWithPoints() {
        when(predictionRepository.findLeaderboardTotals()).thenReturn(List.of(
                new Object[]{"susan", 99L},
                new Object[]{"gonenc", 5L}));
        when(groupStandingPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(finalPredictionRepository.findLeaderboardTotals()).thenReturn(List.of());
        when(userProfileService.getPoolProfiles()).thenReturn(List.of(profile("gonenc", "Gonenc Gorgulu")));

        List<Object[]> leaderboard = service.getLeaderboard();

        assertEquals(1, leaderboard.size());
        assertEquals("gonenc", leaderboard.get(0)[0]);
        assertTrue(leaderboard.stream().noneMatch(row -> "susan".equals(row[0])));
    }

    private static UserProfile profile(String username, String displayName) {
        return new UserProfile(username, "America/New_York", displayName);
    }
}
