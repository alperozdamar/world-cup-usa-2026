package com.alper.worldcup.service;

import java.util.Map;

public record PeerGroupRowView(String username, String displayName, Map<String, PeerGroupPickView> picksByGroup) {
}
