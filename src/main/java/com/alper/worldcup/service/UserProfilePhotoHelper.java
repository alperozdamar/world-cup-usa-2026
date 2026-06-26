package com.alper.worldcup.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class UserProfilePhotoHelper {

    private static final String PHOTO_PATH = "classpath:static/images/%s.png";
    private static final String GROUP2_PHOTO_PATH = "classpath:static/images/group2/%s.png";

    private final ResourceLoader resourceLoader;
    private final UserProfilePhotoService userProfilePhotoService;

    public UserProfilePhotoHelper(ResourceLoader resourceLoader,
                                  UserProfilePhotoService userProfilePhotoService) {
        this.resourceLoader = resourceLoader;
        this.userProfilePhotoService = userProfilePhotoService;
    }

    public boolean hasPhoto(String username) {
        return hasUploadedPhoto(username) || resolveStaticPhotoPath(username) != null;
    }

    public boolean hasUploadedPhoto(String username) {
        return userProfilePhotoService.hasUploadedPhoto(username);
    }

    public String photoUrl(String username) {
        String normalized = UserProfilePhotoService.normalizeUsername(username);
        if (normalized == null) {
            return null;
        }
        return userProfilePhotoService.findStoredPhoto(username)
                .map(photo -> {
                    long version = photo.updatedAt() != null ? photo.updatedAt().toEpochMilli() : 0L;
                    return "/profile/photos/" + normalized + "?v=" + version;
                })
                .orElseGet(() -> staticPhotoUrl(normalized));
    }

    private String staticPhotoUrl(String normalizedUsername) {
        if (resourceLoader.getResource(PHOTO_PATH.formatted(normalizedUsername)).exists()) {
            return "/images/" + normalizedUsername + ".png";
        }
        if (resourceLoader.getResource(GROUP2_PHOTO_PATH.formatted(normalizedUsername)).exists()) {
            return "/images/group2/" + normalizedUsername + ".png";
        }
        return null;
    }

    private String resolveStaticPhotoPath(String username) {
        String normalized = UserProfilePhotoService.normalizeUsername(username);
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
}
