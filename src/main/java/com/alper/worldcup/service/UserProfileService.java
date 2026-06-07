package com.alper.worldcup.service;

import com.alper.worldcup.dao.UserProfileRepository;
import com.alper.worldcup.entity.UserProfile;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final String defaultTimezone;

    public UserProfileService(UserProfileRepository userProfileRepository,
                              @Value("${app.fixture-timezone:America/New_York}") String defaultTimezone) {
        this.userProfileRepository = userProfileRepository;
        this.defaultTimezone = defaultTimezone;
    }

    @Transactional(readOnly = true)
    public ZoneId getUserZoneId(String username) {
        return userProfileRepository.findById(username)
                .map(profile -> ZoneId.of(profile.getTimezoneId()))
                .orElse(ZoneId.of(defaultTimezone));
    }

    @Transactional(readOnly = true)
    public String getDisplayName(String username) {
        return userProfileRepository.findById(username)
                .map(UserProfile::getDisplayName)
                .filter(name -> name != null && !name.isBlank())
                .orElse(username);
    }

    @Transactional(readOnly = true)
    public Map<String, String> getDisplayNamesForUsernames(List<String> usernames) {
        Map<String, String> names = new HashMap<>();
        for (String username : usernames) {
            names.put(username, getDisplayName(username));
        }
        return names;
    }

    @Transactional(readOnly = true)
    public List<UserProfile> getAllProfiles() {
        return userProfileRepository.findAll().stream()
                .sorted(Comparator.comparing(profile ->
                        profile.getDisplayName() != null ? profile.getDisplayName() : profile.getUsername()))
                .toList();
    }

    @Transactional(readOnly = true)
    public String getEmail(String username) {
        return userProfileRepository.findById(username)
                .map(UserProfile::getEmail)
                .orElse(null);
    }

    @Transactional
    public void saveProfileSettings(String username, String timezoneId, String email) {
        ZoneId.of(timezoneId);
        String normalizedEmail = normalizeEmail(email);

        UserProfile profile = userProfileRepository.findById(username)
                .orElse(new UserProfile(username, timezoneId, null));
        profile.setTimezoneId(timezoneId);
        profile.setEmail(normalizedEmail);
        userProfileRepository.save(profile);
    }

    @Transactional
    public void saveTimezone(String username, String timezoneId) {
        saveProfileSettings(username, timezoneId, getEmail(username));
    }

    @Transactional
    public void ensureProfile(String username, String displayName, String timezoneId) {
        UserProfile profile = userProfileRepository.findById(username)
                .orElse(new UserProfile(username, timezoneId, displayName));
        profile.setDisplayName(displayName);
        if (profile.getTimezoneId() == null) {
            profile.setTimezoneId(timezoneId);
        }
        userProfileRepository.save(profile);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim();
        if (!trimmed.contains("@") || trimmed.startsWith("@") || trimmed.endsWith("@")) {
            throw new IllegalArgumentException("Enter a valid email address, or leave blank to remove it.");
        }
        return trimmed;
    }

    public List<String> getCommonTimezones() {
        return Arrays.asList(
                "America/New_York",
                "America/Chicago",
                "America/Denver",
                "America/Los_Angeles",
                "America/Toronto",
                "America/Mexico_City",
                "Europe/London",
                "Europe/Istanbul",
                "Europe/Berlin",
                "Asia/Tokyo",
                "Australia/Sydney",
                "UTC"
        );
    }
}
