package com.alper.worldcup.service;

import com.alper.worldcup.entity.Match;
import java.util.List;

public record HostKnockoutMatchView(Match match, List<HostKnockoutPickView> picks) {
}
