package com.alper.worldcup.service;

import com.alper.worldcup.dao.UserProfileRepository;
import com.alper.worldcup.entity.GroupStandingPrediction;
import com.alper.worldcup.entity.UserProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PredictionReminderService {

    private final UserProfileRepository userProfileRepository;
    private final GroupStandingPredictionService groupStandingPredictionService;
    private final FinalPredictionService finalPredictionService;
    private final UserProfileService userProfileService;

    public PredictionReminderService(UserProfileRepository userProfileRepository,
                                       GroupStandingPredictionService groupStandingPredictionService,
                                       FinalPredictionService finalPredictionService,
                                       UserProfileService userProfileService) {
        this.userProfileRepository = userProfileRepository;
        this.groupStandingPredictionService = groupStandingPredictionService;
        this.finalPredictionService = finalPredictionService;
        this.userProfileService = userProfileService;
    }

    @Transactional(readOnly = true)
    public List<PredictionReminderContent> buildDailyReminders() {
        List<PredictionReminderContent> reminders = new ArrayList<>();

        for (UserProfile profile : userProfileRepository.findAllWithEmail()) {
            PredictionReminderContent content = buildReminderForUser(profile.getUsername(), profile.getEmail());
            if (content.hasReminders()) {
                reminders.add(content);
            }
        }

        return reminders;
    }

    @Transactional(readOnly = true)
    public PredictionReminderContent buildReminderForUser(String username, String email) {
        String displayName = userProfileService.getDisplayName(username);
        boolean missingFinal = isFinalReminderNeeded(username);
        List<String> missingGroups = findMissingGroups(username);

        return new PredictionReminderContent(displayName, email, missingFinal, missingGroups);
    }

    private boolean isFinalReminderNeeded(String username) {
        if (!finalPredictionService.isEditable()) {
            return false;
        }
        return finalPredictionService.getPredictionForUser(username).isEmpty();
    }

    private List<String> findMissingGroups(String username) {
        Map<String, GroupStandingPrediction> predictions =
                groupStandingPredictionService.getPredictionsForUser(username);
        List<String> missing = new ArrayList<>();

        for (String groupName : groupStandingPredictionService.getGroupNames()) {
            if (!groupStandingPredictionService.isGroupEditable(groupName)) {
                continue;
            }
            if (!predictions.containsKey(groupName)) {
                missing.add(groupName);
            }
        }

        return missing;
    }
}
