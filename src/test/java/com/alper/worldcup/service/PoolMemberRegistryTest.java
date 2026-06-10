package com.alper.worldcup.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoolMemberRegistryTest {

    @Test
    void defaultProfileIncludesGroup1Players() {
        PoolMemberRegistry registry = new PoolMemberRegistry("default");

        assertTrue(registry.isMember("gonenc"));
        assertTrue(registry.isMember("alper"));
        assertFalse(registry.isMember("emre"));
        assertEquals(7, registry.getMembers().size());
    }

    @Test
    void group2ProfileIncludesOnlyGroup2Players() {
        PoolMemberRegistry registry = new PoolMemberRegistry("group2");

        assertTrue(registry.isMember("emre"));
        assertTrue(registry.isMember("ozcan"));
        assertFalse(registry.isMember("gonenc"));
        assertEquals(5, registry.getMembers().size());
    }
}
