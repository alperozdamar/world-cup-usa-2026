package com.alper.worldcup.service;

import com.alper.worldcup.dao.UserProfileRepository;
import com.alper.worldcup.entity.UserProfile;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserProfilePhotoService {

    public static final int MAX_PHOTO_BYTES = 2 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE);

    private final UserProfileRepository userProfileRepository;
    private final String defaultTimezone;

    public UserProfilePhotoService(UserProfileRepository userProfileRepository,
                                   @Value("${app.fixture-timezone:America/New_York}") String defaultTimezone) {
        this.userProfileRepository = userProfileRepository;
        this.defaultTimezone = defaultTimezone;
    }

    @Transactional(readOnly = true)
    public boolean hasUploadedPhoto(String username) {
        return findStoredPhoto(username).isPresent();
    }

    @Transactional(readOnly = true)
    public Optional<StoredPhoto> findStoredPhoto(String username) {
        String normalized = normalizeUsername(username);
        if (normalized == null) {
            return Optional.empty();
        }
        return userProfileRepository.findById(normalized)
                .filter(this::hasStoredPhoto)
                .map(profile -> new StoredPhoto(
                        profile.getPhotoContentType(),
                        profile.getPhotoData(),
                        profile.getPhotoUpdatedAt()));
    }

    @Transactional
    public void savePhoto(String username, MultipartFile file) {
        String normalized = requireUsername(username);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a JPEG or PNG image to upload.");
        }
        if (file.getSize() > MAX_PHOTO_BYTES) {
            throw new IllegalArgumentException("Photo must be 2 MB or smaller.");
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPEG and PNG images are allowed.");
        }
        validateFilename(file.getOriginalFilename());

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not read the uploaded image.");
        }
        if (bytes.length > MAX_PHOTO_BYTES) {
            throw new IllegalArgumentException("Photo must be 2 MB or smaller.");
        }
        validateImageBytes(bytes);

        UserProfile profile = userProfileRepository.findById(normalized)
                .orElse(new UserProfile(normalized, defaultTimezone, null));
        profile.setPhotoContentType(contentType);
        profile.setPhotoData(bytes);
        profile.setPhotoUpdatedAt(Instant.now());
        userProfileRepository.save(profile);
    }

    @Transactional
    public void removeUploadedPhoto(String username) {
        String normalized = requireUsername(username);
        userProfileRepository.findById(normalized).ifPresent(profile -> {
            profile.setPhotoContentType(null);
            profile.setPhotoData(null);
            profile.setPhotoUpdatedAt(null);
            userProfileRepository.save(profile);
        });
    }

    private boolean hasStoredPhoto(UserProfile profile) {
        return profile.getPhotoContentType() != null
                && profile.getPhotoData() != null
                && profile.getPhotoData().length > 0;
    }

    private static String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "";
        }
        return contentType.toLowerCase(Locale.ROOT).split(";", 2)[0].trim();
    }

    private static void validateFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png"))) {
            throw new IllegalArgumentException("Only JPEG and PNG images are allowed.");
        }
    }

    private static void validateImageBytes(byte[] bytes) {
        try (InputStream input = new ByteArrayInputStream(bytes)) {
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                throw new IllegalArgumentException("File must be a valid JPEG or PNG image.");
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("File must be a valid JPEG or PNG image.");
        }
    }

    private static String requireUsername(String username) {
        String normalized = normalizeUsername(username);
        if (normalized == null) {
            throw new IllegalArgumentException("User not found.");
        }
        return normalized;
    }

    static String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return username.toLowerCase(Locale.ROOT);
    }

    public record StoredPhoto(String contentType, byte[] data, Instant updatedAt) {
    }
}
