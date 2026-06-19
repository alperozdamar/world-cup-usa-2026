package com.alper.worldcup.config;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.entity.Match;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

@Configuration
@Profile("local")
public class KnockoutDevBootstrapConfig {

    private static final Logger log = LoggerFactory.getLogger(KnockoutDevBootstrapConfig.class);

    @Bean
    @Order(100)
    CommandLineRunner bootstrapKnockoutForLocalDev(MatchRepository matchRepository,
                                                   @Value("${app.knockout.dev-bootstrap:true}") boolean enabled) {
        return args -> {
            if (!enabled) {
                return;
            }
            List<Match> knockoutMatches = matchRepository.findKnockoutMatchesWithTeams();
            if (knockoutMatches.isEmpty()) {
                return;
            }

            int updated = 0;
            for (Match match : knockoutMatches) {
                if (!match.isPredictionsEnabled()) {
                    match.setPredictionsEnabled(true);
                    matchRepository.save(match);
                    updated++;
                }
            }
            if (updated > 0) {
                log.info("Knockout dev bootstrap: enabled predictions on {} matches (display uses standings slots)",
                        updated);
            }
        };
    }
}
