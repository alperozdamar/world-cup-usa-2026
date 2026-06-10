package com.alper.worldcup.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class UserProfilePhotoHelper {

    private static final String PHOTO_PATH = "classpath:static/images/%s.png";
    private static final String GROUP2_PHOTO_PATH = "classpath:static/images/group2/%s.png";

    private final ResourceLoader resourceLoader;

    public UserProfilePhotoHelper(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public boolean hasPhoto(String username) {
        return resolvePhotoPath(username) != null;
    }

    public String photoUrl(String username) {
        String normalized = normalizeUsername(username);
        if (normalized == null) {
            return null;
        }
        if (resourceLoader.getResource(PHOTO_PATH.formatted(normalized)).exists()) {
            return "/images/" + normalized + ".png";
        }
        if (resourceLoader.getResource(GROUP2_PHOTO_PATH.formatted(normalized)).exists()) {
            return "/images/group2/" + normalized + ".png";
        }
        return null;
    }

    private String resolvePhotoPath(String username) {
        String normalized = normalizeUsername(username);
        if (normalized == null) {
            return null;
        }
        if (resourceLoader.getResource(PHOTO_PATH.formatted(normalized)).exists()) {
            return PHOTO_PATH.formatted(normalized);
        }
        if (resourceLoader.getResource(GROUP2_PHOTO_PATH.formatted(normalized)).exists()) {
            return GROUP2_PHOTO_PATH.formatted(normalized);
        }
        return null;
    }

    private static String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return username.toLowerCase();
    }
}
