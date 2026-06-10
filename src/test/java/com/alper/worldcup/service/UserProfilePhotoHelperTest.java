package com.alper.worldcup.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfilePhotoHelperTest {

    @Mock
    private ResourceLoader resourceLoader;
    @Mock
    private Resource resource;

    private UserProfilePhotoHelper helper;

    @BeforeEach
    void setUp() {
        helper = new UserProfilePhotoHelper(resourceLoader);
    }

    @Test
    void returnsPhotoUrlWhenResourceExists() {
        when(resourceLoader.getResource(eq("classpath:static/images/alper.png"))).thenReturn(resource);
        when(resource.exists()).thenReturn(true);

        assertTrue(helper.hasPhoto("alper"));
        assertEquals("/images/alper.png", helper.photoUrl("alper"));
    }

    @Test
    void normalizesUsernameCase() {
        when(resourceLoader.getResource(eq("classpath:static/images/gonenc.png"))).thenReturn(resource);
        when(resource.exists()).thenReturn(true);

        assertEquals("/images/gonenc.png", helper.photoUrl("Gonenc"));
    }

    @Test
    void returnsNullWhenPhotoMissing() {
        when(resourceLoader.getResource(eq("classpath:static/images/unknown.png"))).thenReturn(resource);
        when(resource.exists()).thenReturn(false);

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
