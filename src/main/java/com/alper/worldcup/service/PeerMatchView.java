package com.alper.worldcup.service;

import com.alper.worldcup.entity.Match;
import java.util.List;

public record PeerMatchView(Match match, List<PeerPlayerMatchPrediction> predictions, boolean predictionsHidden) {

    public PeerMatchView(Match match, List<PeerPlayerMatchPrediction> predictions) {
        this(match, predictions, false);
    }
}
