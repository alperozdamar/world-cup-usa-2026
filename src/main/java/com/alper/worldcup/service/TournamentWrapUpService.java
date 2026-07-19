package com.alper.worldcup.service;

import com.alper.worldcup.dao.FinalPredictionAuditRepository;
import com.alper.worldcup.dao.GroupStandingPredictionAuditRepository;
import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.PredictionAuditRepository;
import com.alper.worldcup.dao.PredictionRepository;
import com.alper.worldcup.dao.TeamRepository;
import com.alper.worldcup.dao.UserCommentRepository;
import com.alper.worldcup.entity.PredictionAuditAction;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TournamentWrapUpService {

    private static final Pattern WC_ID = Pattern.compile("WC-\\d+");
    private static final Pattern FIX_LINE = Pattern.compile("(?i)WC-\\d+.*\\bfix\\b");

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final PredictionAuditRepository predictionAuditRepository;
    private final GroupStandingPredictionAuditRepository groupAuditRepository;
    private final FinalPredictionAuditRepository finalAuditRepository;
    private final TeamRepository teamRepository;
    private final UserCommentRepository userCommentRepository;
    private final PoolMemberRegistry poolMemberRegistry;
    private final ResourceLoader resourceLoader;
    private final int configuredGitCommits;
    private final LocalDate projectStartDate;

    public TournamentWrapUpService(MatchRepository matchRepository,
                                   PredictionRepository predictionRepository,
                                   PredictionAuditRepository predictionAuditRepository,
                                   GroupStandingPredictionAuditRepository groupAuditRepository,
                                   FinalPredictionAuditRepository finalAuditRepository,
                                   TeamRepository teamRepository,
                                   UserCommentRepository userCommentRepository,
                                   PoolMemberRegistry poolMemberRegistry,
                                   ResourceLoader resourceLoader,
                                   @Value("${app.wrapup.git-commits:95}") int configuredGitCommits,
                                   @Value("${app.wrapup.project-start-date:2026-05-31}") String projectStartDate) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.predictionAuditRepository = predictionAuditRepository;
        this.groupAuditRepository = groupAuditRepository;
        this.finalAuditRepository = finalAuditRepository;
        this.teamRepository = teamRepository;
        this.userCommentRepository = userCommentRepository;
        this.poolMemberRegistry = poolMemberRegistry;
        this.resourceLoader = resourceLoader;
        this.configuredGitCommits = configuredGitCommits;
        this.projectStartDate = LocalDate.parse(projectStartDate);
    }

    @Transactional(readOnly = true)
    public List<WrapUpStat> getStats() {
        Instant now = Instant.now();
        long totalMatches = matchRepository.count();
        long scoredMatches = matchRepository.countScoredMatches();
        long goals = matchRepository.sumGoalsScored();
        long penalties = matchRepository.countPenaltyShootouts();
        long predictions = predictionRepository.count();
        long exactScores = predictionRepository.countExactScorePredictions();

        long created = predictionAuditRepository.countByAction(PredictionAuditAction.CREATED)
                + groupAuditRepository.countByAction(PredictionAuditAction.CREATED)
                + finalAuditRepository.countByAction(PredictionAuditAction.CREATED);
        long updated = predictionAuditRepository.countByAction(PredictionAuditAction.UPDATED)
                + groupAuditRepository.countByAction(PredictionAuditAction.UPDATED)
                + finalAuditRepository.countByAction(PredictionAuditAction.UPDATED);

        long teams = teamRepository.count();
        long players = poolMemberRegistry.getMembers().size();
        long feedback = userCommentRepository.count();

        long daysBuilding = ChronoUnit.DAYS.between(projectStartDate, LocalDate.now(ZoneOffset.UTC)) + 1;
        long daysTournament = matchRepository.findTournamentStartKickoff()
                .map(start -> Math.max(1, ChronoUnit.DAYS.between(start, now) + 1))
                .orElse(daysBuilding);

        ChangelogCounts changelog = parseChangelog();
        int commits = resolveGitCommits();

        List<WrapUpStat> stats = new ArrayList<>();
        stats.add(new WrapUpStat(
                format(daysTournament),
                "Days of football",
                "From kickoff to almost the final whistle."));
        stats.add(new WrapUpStat(
                format(daysBuilding),
                "Days of coding chaos",
                "First commit → this recap. Worth every late night."));
        stats.add(new WrapUpStat(
                scoredMatches + " / " + totalMatches,
                "Matches scored",
                "Admin thumbs, meet scoreboard."));
        stats.add(new WrapUpStat(
                format(goals),
                "Goals that happened",
                "Real ones. Not the ones we typed at 2am."));
        stats.add(new WrapUpStat(
                format(penalties),
                "Penalty shootouts",
                "Nerves of steel. Or not."));
        stats.add(new WrapUpStat(
                format(predictions),
                "Predictions saved",
                "Someone believed in every one of these."));
        stats.add(new WrapUpStat(
                format(created),
                "First-time entries",
                "The brave first click."));
        stats.add(new WrapUpStat(
                format(updated),
                "Mind-changes",
                "\"Wait… let me edit that.\""));
        stats.add(new WrapUpStat(
                format(exactScores),
                "Exact scores nailed",
                "Crystal ball moments across the pool."));
        stats.add(new WrapUpStat(
                format(players),
                "Pool rivals",
                "Friends. Enemies. Sometimes both."));
        stats.add(new WrapUpStat(
                format(teams),
                "National teams",
                "Flags everywhere."));
        stats.add(new WrapUpStat(
                format(changelog.features()),
                "Features shipped",
                "WC tickets closed — including this recap."));
        stats.add(new WrapUpStat(
                format(changelog.fixes()),
                "Bugs wrestled",
                "They fought. We won. Mostly."));
        stats.add(new WrapUpStat(
                format(commits),
                "Git commits",
                "Yes, counting the one that added this page."));
        stats.add(new WrapUpStat(
                format(feedback),
                "Feedback notes",
                "The peanut gallery, documented."));
        return stats;
    }

    int resolveGitCommits() {
        try {
            Process process = new ProcessBuilder("git", "rev-list", "--count", "HEAD")
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(2, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return configuredGitCommits;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                if (line != null && !line.isBlank()) {
                    int counted = Integer.parseInt(line.trim());
                    return Math.max(counted, configuredGitCommits);
                }
            }
        } catch (Exception ignored) {
            // Production images often have no .git — fall back to configured count.
        }
        return configuredGitCommits;
    }

    ChangelogCounts parseChangelog() {
        try {
            Resource resource = resourceLoader.getResource("classpath:Changelog.md");
            if (!resource.exists()) {
                return new ChangelogCounts(0, 0);
            }
            int features = 0;
            int fixes = 0;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher wc = WC_ID.matcher(line);
                    if (wc.find()) {
                        features++;
                        if (FIX_LINE.matcher(line).find()) {
                            fixes++;
                        }
                    }
                }
            }
            return new ChangelogCounts(features, fixes);
        } catch (Exception ex) {
            return new ChangelogCounts(0, 0);
        }
    }

    private static String format(long value) {
        return String.format("%,d", value);
    }

    record ChangelogCounts(int features, int fixes) {
    }
}
