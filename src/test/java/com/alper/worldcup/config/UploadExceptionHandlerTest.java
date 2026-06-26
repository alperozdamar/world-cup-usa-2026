package com.alper.worldcup.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class UploadExceptionHandlerTest {

    private final UploadExceptionHandler handler = new UploadExceptionHandler();

    @Test
    void redirectsToProfileSettingsWithFriendlyMessage() {
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String view = handler.handleMaxUploadSize(mock(MaxUploadSizeExceededException.class), redirectAttributes);

        assertEquals("redirect:/profile/settings", view);
        assertEquals("Photo must be 2 MB or smaller.", redirectAttributes.getFlashAttributes().get("errorMessage"));
    }
}
