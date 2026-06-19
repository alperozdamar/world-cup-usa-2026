package com.alper.worldcup.config;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.TeamRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.Team;
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
                                                   TeamRepository teamRepository,
                                                   @Value("${app.knockout.dev-bootstrap:true}") boolean enabled) {
        return args -> {
            if (!enabled) {
                return;
            }
            List<Match> knockoutMatches = matchRepository.findKnockoutMatchesWithTeams();
            if (knockoutMatches.isEmpty()) {
                return;
            }

            List<Team> teams = teamRepository.findAllByOrderByNameAsc();
            if (teams.size() < 2) {
                log.warn("Knockout dev bootstrap skipped: need at least 2 teams in database");
                return;
            }

            int teamIndex = 0;
            int updated = 0;
            for (Match match : knockoutMatches) {
                boolean changed = false;
                if (match.getHomeTeam() == null) {
                    match.setHomeTeam(teams.get(teamIndex % teams.size()));
                    teamIndex++;
                    match.setHomePlaceholder(null);
                    changed = true;
                }
                if (match.getAwayTeam() == null) {
                    match.setAwayTeam(teams.get(teamIndex % teams.size()));
                    teamIndex++;
                    match.setAwayPlaceholder(null);
                    changed = true;
                }
                if (!match.isPredictionsEnabled()) {
                    match.setPredictionsEnabled(true);
                    changed = true;
                }
                if (changed) {
                    matchRepository.save(match);
                    updated++;
                }
            }
            if (updated > 0) {
                log.info("Knockout dev bootstrap: assigned test teams and enabled predictions on {} matches",
                        updated);
            }
        };
    }
}
