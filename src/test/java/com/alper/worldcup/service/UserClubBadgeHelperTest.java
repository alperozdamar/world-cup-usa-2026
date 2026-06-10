package com.alper.worldcup.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserClubBadgeHelperTest {

    private final UserClubBadgeHelper helper = new UserClubBadgeHelper();

    @Test
    void returnsBesiktasForAlperTcanKubilay() {
        assertTrue(helper.hasClubBadge("alper"));
        assertEquals("Beşiktaş", helper.clubName("alper"));
        assertEquals("/images/bjk.gif", helper.imagePath("alper"));
        assertEquals("/images/bjk.gif", helper.imagePath("tcan"));
        assertEquals("/images/bjk.gif", helper.imagePath("kubilay"));
    }

    @Test
    void returnsFenerbahceForAliSadik() {
        assertEquals("Fenerbahçe", helper.clubName("ali"));
        assertEquals("/images/fb.png", helper.imagePath("sadik"));
    }

    @Test
    void returnsGalatasarayForGonencAdem() {
        assertEquals("Galatasaray", helper.clubName("gonenc"));
        assertEquals("/images/gs.png", helper.imagePath("adem"));
    }

    @Test
    void returnsClubsForGroup2Players() {
        assertEquals("Fenerbahçe", helper.clubName("emre"));
        assertEquals("/images/fb.png", helper.imagePath("emre"));
        assertEquals("Galatasaray", helper.clubName("can"));
        assertEquals("/images/gs.png", helper.imagePath("caglar"));
        assertEquals("/images/gs.png", helper.imagePath("ozcan"));
    }

    @Test
    void returnsNothingForUnknownUser() {
        assertFalse(helper.hasClubBadge("unknown"));
        assertNull(helper.clubName("unknown"));
        assertNull(helper.imagePath(null));
    }
}
