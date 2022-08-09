package com.jayemceekay.TerraSniper.brush;

import com.jayemceekay.TerraSniper.performer.PerformerRegistry;
import com.jayemceekay.TerraSniper.sniper.snipe.Snipe;

public interface PerformerBrush extends Brush {
    void handlePerformerCommand(String[] parameters, Snipe snipe, PerformerRegistry performerRegistry);

    void initialize(Snipe snipe);

    void sendPerformerInfo(Snipe snipe);
}
