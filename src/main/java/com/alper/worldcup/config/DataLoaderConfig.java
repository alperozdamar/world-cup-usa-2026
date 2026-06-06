package com.alper.worldcup.config;

import com.alper.worldcup.service.FixtureImportService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoaderConfig {

    @Bean
    CommandLineRunner loadFixtures(FixtureImportService fixtureImportService) {
        return args -> {
            fixtureImportService.importFixturesIfEmpty();
            fixtureImportService.syncTeamGroupNamesFromMatches();
        };
    }
}
