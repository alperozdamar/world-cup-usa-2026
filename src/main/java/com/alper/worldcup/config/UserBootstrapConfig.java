package com.alper.worldcup.config;

import com.alper.worldcup.service.PoolMember;
import com.alper.worldcup.service.PoolMemberRegistry;
import com.alper.worldcup.service.UserAccountService;
import com.alper.worldcup.service.UserProfileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserBootstrapConfig {

    @Bean
    CommandLineRunner bootstrapUsers(UserAccountService userAccountService,
                                     UserProfileService userProfileService,
                                     PoolMemberRegistry poolMemberRegistry,
                                     @Value("${app.user-bootstrap.enabled:true}") boolean enabled,
                                     @Value("${app.fixture-timezone:America/New_York}") String defaultTimezone) {
        return args -> {
            if (!enabled) {
                return;
            }
            for (PoolMember member : poolMemberRegistry.getMembers()) {
                userAccountService.ensureUser(member.username(), "123", member.admin());
                userProfileService.ensureProfile(member.username(), member.displayName(), defaultTimezone);
            }
        };
    }
}
