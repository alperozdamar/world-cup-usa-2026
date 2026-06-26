package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alper.worldcup.dao.UserProfileRepository;
import com.alper.worldcup.entity.UserProfile;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class UserProfilePhotoServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    private UserProfilePhotoService service;

    @BeforeEach
    void setUp() {
        service = new UserProfilePhotoService(userProfileRepository, "America/New_York");
    }

    @Test
    void savesValidPngPhoto() throws IOException {
        byte[] png = pngBytes(32, 32);
        MockMultipartFile file = new MockMultipartFile(
                "photo", "me.png", MediaType.IMAGE_PNG_VALUE, png);
        UserProfile profile = new UserProfile("alper", "America/New_York", "Alper");

        when(userProfileRepository.findById("alper")).thenReturn(Optional.of(profile));

        service.savePhoto("alper", file);

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());
        UserProfile saved = captor.getValue();
        assertEquals(MediaType.IMAGE_PNG_VALUE, saved.getPhotoContentType());
        assertEquals(png.length, saved.getPhotoData().length);
        assertTrue(saved.getPhotoUpdatedAt() != null);
    }

    @Test
    void rejectsOversizedPhoto() throws IOException {
        byte[] png = pngBytes(32, 32);
        MockMultipartFile file = new MockMultipartFile(
                "photo", "me.png", MediaType.IMAGE_PNG_VALUE, png) {
            @Override
            public long getSize() {
                return UserProfilePhotoService.MAX_PHOTO_BYTES + 1;
            }
        };

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.savePhoto("alper", file));
        assertEquals("Photo must be 2 MB or smaller.", ex.getMessage());
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void rejectsUnsupportedContentType() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "photo", "me.gif", MediaType.IMAGE_GIF_VALUE, pngBytes(16, 16));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.savePhoto("alper", file));
        assertEquals("Only JPEG and PNG images are allowed.", ex.getMessage());
    }

    @Test
    void rejectsInvalidImageBytes() {
        MockMultipartFile file = new MockMultipartFile(
                "photo", "me.png", MediaType.IMAGE_PNG_VALUE, "not-an-image".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.savePhoto("alper", file));
        assertEquals("File must be a valid JPEG or PNG image.", ex.getMessage());
    }

    @Test
    void removeClearsStoredPhoto() {
        UserProfile profile = new UserProfile("alper", "America/New_York", "Alper");
        profile.setPhotoContentType(MediaType.IMAGE_PNG_VALUE);
        profile.setPhotoData(new byte[] {1, 2, 3});

        when(userProfileRepository.findById("alper")).thenReturn(Optional.of(profile));

        service.removeUploadedPhoto("alper");

        assertEquals(null, profile.getPhotoContentType());
        assertEquals(null, profile.getPhotoData());
        verify(userProfileRepository).save(profile);
    }

    @Test
    void hasUploadedPhotoWhenStored() {
        UserProfile profile = new UserProfile("alper", "America/New_York", "Alper");
        profile.setPhotoContentType(MediaType.IMAGE_PNG_VALUE);
        profile.setPhotoData(new byte[] {1});

        when(userProfileRepository.findById("alper")).thenReturn(Optional.of(profile));

        assertTrue(service.hasUploadedPhoto("alper"));
        assertFalse(service.hasUploadedPhoto("missing"));
    }

    private static byte[] pngBytes(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, Color.BLUE.getRGB());
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
