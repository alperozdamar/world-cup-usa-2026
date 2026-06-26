package com.alper.worldcup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class UserProfilePhotoHelperTest {

    @Mock
    private ResourceLoader resourceLoader;
    @Mock
    private UserProfilePhotoService userProfilePhotoService;
    @Mock
    private Resource resource;

    private UserProfilePhotoHelper helper;

    @BeforeEach
    void setUp() {
        helper = new UserProfilePhotoHelper(resourceLoader, userProfilePhotoService);
    }

    @Test
    void returnsUploadedPhotoUrlWhenStoredInDatabase() {
        Instant updatedAt = Instant.parse("2026-06-01T12:00:00Z");
        when(userProfilePhotoService.findStoredPhoto("alper")).thenReturn(Optional.of(
                new UserProfilePhotoService.StoredPhoto(MediaType.IMAGE_PNG_VALUE, new byte[] {1}, updatedAt)));
        when(userProfilePhotoService.hasUploadedPhoto("alper")).thenReturn(true);

        assertTrue(helper.hasPhoto("alper"));
        assertTrue(helper.hasUploadedPhoto("alper"));
        assertEquals("/profile/photos/alper?v=" + updatedAt.toEpochMilli(), helper.photoUrl("alper"));
    }

    @Test
    void returnsStaticPhotoUrlWhenNoUploadExists() {
        when(userProfilePhotoService.findStoredPhoto("alper")).thenReturn(Optional.empty());
        when(userProfilePhotoService.hasUploadedPhoto("alper")).thenReturn(false);
        when(resourceLoader.getResource(eq("classpath:static/images/alper.png"))).thenReturn(resource);
        when(resource.exists()).thenReturn(true);

        assertTrue(helper.hasPhoto("alper"));
        assertFalse(helper.hasUploadedPhoto("alper"));
        assertEquals("/images/alper.png", helper.photoUrl("alper"));
    }

    @Test
    void returnsGroup2PhotoUrlWhenResourceExists() {
        Resource defaultResource = org.mockito.Mockito.mock(Resource.class);
        Resource group2Resource = org.mockito.Mockito.mock(Resource.class);
        when(userProfilePhotoService.findStoredPhoto("caglar")).thenReturn(Optional.empty());
        when(userProfilePhotoService.hasUploadedPhoto("caglar")).thenReturn(false);
        when(resourceLoader.getResource(eq("classpath:static/images/caglar.png"))).thenReturn(defaultResource);
        when(defaultResource.exists()).thenReturn(false);
        when(resourceLoader.getResource(eq("classpath:static/images/group2/caglar.png"))).thenReturn(group2Resource);
        when(group2Resource.exists()).thenReturn(true);

        assertTrue(helper.hasPhoto("caglar"));
        assertEquals("/images/group2/caglar.png", helper.photoUrl("caglar"));
    }

    @Test
    void returnsNullWhenPhotoMissing() {
        Resource defaultResource = org.mockito.Mockito.mock(Resource.class);
        Resource group2Resource = org.mockito.Mockito.mock(Resource.class);
        when(userProfilePhotoService.findStoredPhoto("unknown")).thenReturn(Optional.empty());
        when(userProfilePhotoService.hasUploadedPhoto("unknown")).thenReturn(false);
        when(resourceLoader.getResource(eq("classpath:static/images/unknown.png"))).thenReturn(defaultResource);
        when(defaultResource.exists()).thenReturn(false);
        when(resourceLoader.getResource(eq("classpath:static/images/group2/unknown.png"))).thenReturn(group2Resource);
        when(group2Resource.exists()).thenReturn(false);

        assertFalse(helper.hasPhoto("unknown"));
        assertNull(helper.photoUrl("unknown"));
    }

    @Test
    void rejectsBlankUsername() {
        assertFalse(helper.hasPhoto(null));
        assertFalse(helper.hasPhoto(" "));
        assertNull(helper.photoUrl(null));
    }
}
