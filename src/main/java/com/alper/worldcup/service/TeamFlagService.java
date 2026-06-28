package com.alper.worldcup.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class TeamFlagService {

    private static final Logger log = LoggerFactory.getLogger(TeamFlagService.class);
    private static final String FLAGS_PATH = "/images/flags/";

    private final Map<String, String> teamToCode;

    public TeamFlagService(ObjectMapper objectMapper) {
        this.teamToCode = loadTeamFlags(objectMapper);
    }

    public boolean hasFlag(String teamName) {
        return teamName != null && teamToCode.containsKey(teamName);
    }

    public String flagUrl(String teamName) {
        String code = teamToCode.get(teamName);
        if (code == null || code.isBlank()) {
            return null;
        }
        return FLAGS_PATH + code + ".svg";
    }

    private static Map<String, String> loadTeamFlags(ObjectMapper objectMapper) {
        try (InputStream input = new ClassPathResource("data/team-flags.json").getInputStream()) {
            Map<String, String> loaded = objectMapper.readValue(input, new TypeReference<>() { });
            log.info("Loaded {} team flag mappings", loaded.size());
            return Collections.unmodifiableMap(loaded);
        } catch (IOException ex) {
            log.warn("Could not load team-flags.json; flags disabled", ex);
            return Map.of();
        }
    }
}
