package com.alper.worldcup.service;

import com.alper.worldcup.entity.GroupStandingPrediction;
import com.alper.worldcup.entity.Team;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionReminderServiceTest {

    @Mock
    private GroupStandingPredictionService groupStandingPredictionService;

    @Mock
    private FinalPredictionService finalPredictionService;

    @Mock
    private UserProfileService userProfileService;

    @Test
    void includesMissingFinalAndGroupsWhileStillEditable() {
        when(finalPredictionService.isEditable()).thenReturn(true);
        when(finalPredictionService.getPredictionForUser("gonenc")).thenReturn(Optional.empty());
        when(groupStandingPredictionService.getGroupNames()).thenReturn(List.of("A", "B", "C"));
        when(groupStandingPredictionService.isGroupEditable("A")).thenReturn(true);
        when(groupStandingPredictionService.isGroupEditable("B")).thenReturn(true);
        when(groupStandingPredictionService.isGroupEditable("C")).thenReturn(false);
        when(groupStandingPredictionService.getPredictionsForUser("gonenc"))
                .thenReturn(Map.of("B", prediction("B")));
        when(userProfileService.getDisplayName("gonenc")).thenReturn("Gonenc");

        PredictionReminderService service = new PredictionReminderService(
                null, groupStandingPredictionService, finalPredictionService, userProfileService,
                new PoolMemberRegistry("default"));

        PredictionReminderContent content = service.buildReminderForUser("gonenc", "gonenc@example.com");

        assertTrue(content.missingFinal());
        assertEquals(List.of("A"), content.missingGroupNames());
        assertTrue(content.hasReminders());
    }

    @Test
    void skipsLockedSections() {
        when(finalPredictionService.isEditable()).thenReturn(false);
        when(groupStandingPredictionService.getGroupNames()).thenReturn(List.of("A"));
        when(groupStandingPredictionService.isGroupEditable("A")).thenReturn(false);
        when(groupStandingPredictionService.getPredictionsForUser("gonenc")).thenReturn(Map.of());
        when(userProfileService.getDisplayName("gonenc")).thenReturn("Gonenc");

        PredictionReminderService service = new PredictionReminderService(
                null, groupStandingPredictionService, finalPredictionService, userProfileService,
                new PoolMemberRegistry("default"));

        PredictionReminderContent content = service.buildReminderForUser("gonenc", "gonenc@example.com");

        assertFalse(content.hasReminders());
    }

    private GroupStandingPrediction prediction(String groupName) {
        GroupStandingPrediction prediction = new GroupStandingPrediction();
        prediction.setGroupName(groupName);
        Team first = new Team("Team A", groupName);
        first.setId(1);
        Team second = new Team("Team B", groupName);
        second.setId(2);
        prediction.setFirstPlaceTeam(first);
        prediction.setSecondPlaceTeam(second);
        return prediction;
    }
}
