package com.alper.worldcup.service;

import com.alper.worldcup.dao.MatchRepository;
import com.alper.worldcup.dao.TeamRepository;
import com.alper.worldcup.entity.Match;
import com.alper.worldcup.entity.MatchStage;
import com.alper.worldcup.entity.Team;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FixtureImportService {

    private static final Logger log = LoggerFactory.getLogger(FixtureImportService.class);

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "^(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday) \\d{1,2} \\w+ \\d{4}$");
    private static final Pattern TIME_PATTERN = Pattern.compile("^\\d{2}:\\d{2}$");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ENGLISH);

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final ZoneId fixtureZone;

    public FixtureImportService(MatchRepository matchRepository,
                                TeamRepository teamRepository,
                                @Value("${app.fixture-timezone:America/New_York}") String fixtureTimezone) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.fixtureZone = ZoneId.of(fixtureTimezone);
    }

    @Transactional
    public void importFixturesIfEmpty() {
        if (matchRepository.count() > 0) {
            log.info("Matches already loaded, skipping fixture import");
            return;
        }

        List<ParsedFixture> fixtures = parseFixtures();
        Map<String, Team> teamsByName = new HashMap<>();

        for (ParsedFixture fixture : fixtures) {
            Match match = new Match();
            match.setKickoffUtc(fixture.kickoffUtc());
            match.setStage(fixture.stage());
            match.setGroupName(fixture.groupName());
            match.setVenue(fixture.venue());
            match.setCity(fixture.city());
            match.setPredictionsEnabled(fixture.stage() == MatchStage.GROUP_STAGE);

            if (fixture.stage() == MatchStage.GROUP_STAGE) {
                Team home = teamsByName.computeIfAbsent(fixture.homeName(),
                        name -> findOrCreateTeam(name, fixture.groupName()));
                Team away = teamsByName.computeIfAbsent(fixture.awayName(),
                        name -> findOrCreateTeam(name, fixture.groupName()));
                match.setHomeTeam(home);
                match.setAwayTeam(away);
            } else {
                match.setHomePlaceholder(fixture.homeName());
                match.setAwayPlaceholder(fixture.awayName());
            }

            matchRepository.save(match);
        }

        log.info("Imported {} matches ({} group stage)", fixtures.size(),
                fixtures.stream().filter(f -> f.stage() == MatchStage.GROUP_STAGE).count());
        syncTeamGroupNamesFromMatches();
    }

    @Transactional
    public void syncTeamGroupNamesFromMatches() {
        for (Match match : matchRepository.findByStageWithTeams(MatchStage.GROUP_STAGE)) {
            if (match.getGroupName() == null) {
                continue;
            }
            updateTeamGroupName(match.getHomeTeam(), match.getGroupName());
            updateTeamGroupName(match.getAwayTeam(), match.getGroupName());
        }
    }

    private void updateTeamGroupName(Team team, String groupName) {
        if (team == null || groupName == null) {
            return;
        }
        if (!groupName.equals(team.getGroupName())) {
            team.setGroupName(groupName);
            teamRepository.save(team);
        }
    }

    private Team findOrCreateTeam(String name, String groupName) {
        return teamRepository.findByName(name)
                .map(existing -> {
                    if (existing.getGroupName() == null && groupName != null) {
                        existing.setGroupName(groupName);
                        return teamRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> teamRepository.save(new Team(name, groupName)));
    }

    List<ParsedFixture> parseFixtures() {
        try {
            ClassPathResource resource = new ClassPathResource("data/fixtures.txt");
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            return parseLines(lines);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse fixtures", ex);
        }
    }

    List<ParsedFixture> parseLines(List<String> lines) {
        List<ParsedFixture> fixtures = new ArrayList<>();
        LocalDate currentDate = null;
        int i = 0;

        while (i < lines.size()) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                i++;
                continue;
            }

            if (DATE_PATTERN.matcher(line).matches()) {
                currentDate = LocalDate.parse(line, DATE_FORMAT);
                i++;
                continue;
            }

            if (currentDate == null || !looksLikeMatchStart(lines, i)) {
                i++;
                continue;
            }

            String home = line;
            i = skipBlankLines(lines, i + 1);
            if (i >= lines.size()) {
                break;
            }

            String timeStr = lines.get(i).trim();
            if (!TIME_PATTERN.matcher(timeStr).matches()) {
                i++;
                continue;
            }
            i++;

            i = skipBlankLines(lines, i);
            if (i >= lines.size()) {
                break;
            }
            String away = lines.get(i).trim();
            i++;

            List<String> metaLines = new ArrayList<>();
            while (i < lines.size()) {
                String metaLine = lines.get(i).trim();
                if (metaLine.isEmpty()) {
                    i++;
                    break;
                }
                if (DATE_PATTERN.matcher(metaLine).matches()) {
                    break;
                }
                if (looksLikeMatchStart(lines, i)) {
                    break;
                }
                metaLines.add(metaLine);
                i++;
            }

            fixtures.add(buildFixture(currentDate, timeStr, home, away, metaLines));
        }

        return fixtures;
    }

    private ParsedFixture buildFixture(LocalDate date, String timeStr, String home, String away,
                                       List<String> metaLines) {
        LocalTime time = LocalTime.parse(timeStr);
        Instant kickoff = LocalDateTime.of(date, time).atZone(fixtureZone).toInstant();

        String stageLine = metaLines.isEmpty() ? "" : metaLines.get(0);
        MatchStage stage = mapStage(stageLine);
        String groupName = extractGroupName(metaLines);
        String venue = extractVenue(metaLines);
        String city = extractCity(metaLines);

        return new ParsedFixture(home, away, kickoff, stage, groupName, venue, city);
    }

    private MatchStage mapStage(String stageLine) {
        if ("First Stage".equalsIgnoreCase(stageLine)) {
            return MatchStage.GROUP_STAGE;
        }
        if (stageLine.startsWith("Round of 32")) {
            return MatchStage.ROUND_OF_32;
        }
        if (stageLine.startsWith("Round of 16")) {
            return MatchStage.ROUND_OF_16;
        }
        if (stageLine.contains("Quarter-final") || stageLine.contains("Quarter final")) {
            return MatchStage.QUARTER_FINAL;
        }
        if (stageLine.contains("Semi-final") || stageLine.contains("Semi final")) {
            return MatchStage.SEMI_FINAL;
        }
        if (stageLine.contains("Third place") || stageLine.contains("Third Place")) {
            return MatchStage.THIRD_PLACE;
        }
        if (stageLine.contains("Final")) {
            return MatchStage.FINAL;
        }
        return MatchStage.UNKNOWN;
    }

    private String extractGroupName(List<String> metaLines) {
        for (String line : metaLines) {
            if (line.startsWith("Group ")) {
                return line.substring("Group ".length()).trim();
            }
        }
        return null;
    }

    private String extractVenue(List<String> metaLines) {
        for (int i = 0; i < metaLines.size(); i++) {
            String line = metaLines.get(i);
            if (line.startsWith("Group ") || line.equals("·") || line.equals("First Stage")
                    || line.startsWith("Round of")) {
                continue;
            }
            if (!line.startsWith("(")) {
                return line;
            }
        }
        return null;
    }

    private String extractCity(List<String> metaLines) {
        for (String line : metaLines) {
            if (line.startsWith("(") && line.endsWith(")")) {
                return line.substring(1, line.length() - 1);
            }
        }
        return null;
    }

    private int skipBlankLines(List<String> lines, int index) {
        while (index < lines.size() && lines.get(index).trim().isEmpty()) {
            index++;
        }
        return index;
    }

    private boolean looksLikeMatchStart(List<String> lines, int index) {
        if (index >= lines.size()) {
            return false;
        }
        String candidate = lines.get(index).trim();
        if (candidate.isEmpty()
                || DATE_PATTERN.matcher(candidate).matches()
                || TIME_PATTERN.matcher(candidate).matches()
                || candidate.equals("First Stage")
                || candidate.startsWith("Round of")
                || candidate.startsWith("Group ")
                || candidate.equals("·")) {
            return false;
        }
        int timeIndex = skipBlankLines(lines, index + 1);
        if (timeIndex >= lines.size()) {
            return false;
        }
        return TIME_PATTERN.matcher(lines.get(timeIndex).trim()).matches();
    }

    record ParsedFixture(String homeName, String awayName, Instant kickoffUtc, MatchStage stage,
                         String groupName, String venue, String city) {
    }
}
