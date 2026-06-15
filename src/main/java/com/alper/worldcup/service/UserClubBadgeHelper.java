package com.alper.worldcup.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UserClubBadgeHelper {

    private record ClubBadge(String clubName, String imagePath) {
    }

    private static final Map<String, ClubBadge> CLUB_BY_USERNAME = Map.ofEntries(
            Map.entry("alper", new ClubBadge("Beşiktaş", "/images/bjk.gif")),
            Map.entry("tcan", new ClubBadge("Beşiktaş", "/images/bjk.gif")),
            Map.entry("kubilay", new ClubBadge("Beşiktaş", "/images/bjk.gif")),
            Map.entry("irem", new ClubBadge("Beşiktaş", "/images/bjk.gif")),
            Map.entry("ali", new ClubBadge("Fenerbahçe", "/images/fb.png")),
            Map.entry("sadik", new ClubBadge("Fenerbahçe", "/images/fb.png")),
            Map.entry("gonenc", new ClubBadge("Galatasaray", "/images/gs.png")),
            Map.entry("adem", new ClubBadge("Galatasaray", "/images/gs.png")),
            Map.entry("emre", new ClubBadge("Fenerbahçe", "/images/fb.png")),
            Map.entry("can", new ClubBadge("Galatasaray", "/images/gs.png")),
            Map.entry("caglar", new ClubBadge("Galatasaray", "/images/gs.png")),
            Map.entry("ozcan", new ClubBadge("Galatasaray", "/images/gs.png")));

    public boolean hasClubBadge(String username) {
        return username != null && CLUB_BY_USERNAME.containsKey(username.toLowerCase());
    }

    public String clubName(String username) {
        return findBadge(username).map(ClubBadge::clubName).orElse(null);
    }

    public String imagePath(String username) {
        return findBadge(username).map(ClubBadge::imagePath).orElse(null);
    }

    private Optional<ClubBadge> findBadge(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(CLUB_BY_USERNAME.get(username.toLowerCase()));
    }
}
