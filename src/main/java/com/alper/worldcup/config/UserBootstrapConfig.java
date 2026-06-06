package com.alper.worldcup.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserBootstrapConfig {

    @Bean
    CommandLineRunner bootstrapUsers(UserAccountService userAccountService,
                                     UserProfileService userProfileService,
                                     @Value("${app.fixture-timezone:America/New_York}") String defaultTimezone) {
        return args -> {
            seedPlayer(userAccountService, userProfileService, defaultTimezone,
                    "alper", "Alper Ozdamar", true);
            seedPlayer(userAccountService, userProfileService, defaultTimezone,
                    "gonenc", "Gonenc Gorgulu", false);
            seedPlayer(userAccountService, userProfileService, defaultTimezone,
                    "tcan", "Tayyip Can", false);
            seedPlayer(userAccountService, userProfileService, defaultTimezone,
                    "kubilay", "Kubilay Kahraman", false);
            seedPlayer(userAccountService, userProfileService, defaultTimezone,
                    "ali", "Ali Sahin", false);
            seedPlayer(userAccountService, userProfileService, defaultTimezone,
                    "sadik", "Sadik Demirdogen", false);
            seedPlayer(userAccountService, userProfileService, defaultTimezone,
                    "adem", "Adem Sari", false);
        };
    }

    private void seedPlayer(UserAccountService userAccountService,
                            UserProfileService userProfileService,
                            String defaultTimezone,
                            String username,
                            String displayName,
                            boolean admin) {
        userAccountService.ensureUser(username, "123", admin);
        userProfileService.ensureProfile(username, displayName, defaultTimezone);
    }
}
