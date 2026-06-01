package com.alper.worldcup.service;

import com.alper.worldcup.dao.UserProfileRepository;
import com.alper.worldcup.entity.UserProfile;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
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

    @Transactional
    public void saveTimezone(String username, String timezoneId) {
        ZoneId.of(timezoneId);
        UserProfile profile = userProfileRepository.findById(username)
                .orElse(new UserProfile(username, timezoneId));
        profile.setTimezoneId(timezoneId);
        userProfileRepository.save(profile);
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
