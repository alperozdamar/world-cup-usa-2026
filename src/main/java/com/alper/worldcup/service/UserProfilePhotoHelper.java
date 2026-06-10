package com.alper.worldcup.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class UserProfilePhotoHelper {

    private static final String PHOTO_PATH = "classpath:static/images/%s.png";

    private final ResourceLoader resourceLoader;

    public UserProfilePhotoHelper(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public boolean hasPhoto(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        Resource resource = resourceLoader.getResource(PHOTO_PATH.formatted(username.toLowerCase()));
        return resource.exists();
    }

    public String photoUrl(String username) {
        if (!hasPhoto(username)) {
            return null;
        }
        return "/images/" + username.toLowerCase() + ".png";
    }
}
