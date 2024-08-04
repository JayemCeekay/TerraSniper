package com.jayemceekay.terrasniper.brush;

import com.jayemceekay.terrasniper.performer.PerformerRegistry;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;

public interface PerformerBrush extends Brush {
    void handlePerformerCommand(String[] parameters, Snipe snipe, PerformerRegistry performerRegistry);

    void initialize(Snipe snipe);

    void sendPerformerInfo(Snipe snipe);
}
