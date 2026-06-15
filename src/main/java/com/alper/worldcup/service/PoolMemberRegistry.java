package com.alper.worldcup.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PoolMemberRegistry {

    private final List<PoolMember> members;
    private final Set<String> usernames;

    public PoolMemberRegistry(@Value("${app.user-bootstrap.profile:default}") String profile) {
        this.members = List.copyOf(membersForProfile(profile));
        this.usernames = members.stream()
                .map(PoolMember::username)
                .collect(Collectors.toUnmodifiableSet());
    }

    public List<PoolMember> getMembers() {
        return members;
    }

    public boolean isMember(String username) {
        return username != null && usernames.contains(username.toLowerCase());
    }

    private static List<PoolMember> membersForProfile(String profile) {
        if ("group2".equalsIgnoreCase(profile)) {
            return List.of(
                    new PoolMember("alper", "Alper Ozdamar", true),
                    new PoolMember("emre", "Emre", false),
                    new PoolMember("can", "Can Tekyetim", false),
                    new PoolMember("caglar", "Caglar Panus", false),
                    new PoolMember("ozcan", "Ozcan Sakir", false));
        }
        return List.of(
                new PoolMember("alper", "Alper Ozdamar", true),
                new PoolMember("gonenc", "Gonenc Gorgulu", false),
                new PoolMember("tcan", "Tayyip Can", false),
                new PoolMember("kubilay", "Kubilay Kahraman", false),
                new PoolMember("ali", "Ali Sahin", false),
                new PoolMember("sadik", "Sadik Demirdogen", false),
                new PoolMember("adem", "Adem Sari", false),
                new PoolMember("irem", "Irem Kahraman", false));
    }
}
