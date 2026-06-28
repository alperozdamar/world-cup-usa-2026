package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class TeamFlagAssetsTest {

    @Test
    void everyMappedFlagHasSvgAsset() throws IOException {
        Map<String, String> teamToCode;
        try (InputStream input = new ClassPathResource("data/team-flags.json").getInputStream()) {
            teamToCode = new ObjectMapper().readValue(input, new TypeReference<>() { });
        }

        Set<String> codes = teamToCode.values().stream()
                .collect(Collectors.toSet());

        for (String code : codes) {
            assertTrue(
                    new ClassPathResource("static/images/flags/" + code + ".svg").exists(),
                    () -> "Missing flag SVG for code: " + code);
        }
    }
}
