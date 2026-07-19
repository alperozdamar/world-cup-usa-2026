package com.alper.worldcup.service;

/**
 * One funny/highlight number for the season recap.
 */
public record WrapUpStat(
        String value,
        String label,
        String quip) {
}
