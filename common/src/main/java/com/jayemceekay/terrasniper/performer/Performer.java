package com.jayemceekay.terrasniper.performer;

import com.jayemceekay.terrasniper.brush.type.AbstractBrush;
import com.jayemceekay.terrasniper.performer.property.PerformerProperties;
import com.jayemceekay.terrasniper.sniper.snipe.performer.PerformerSnipe;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.world.block.BlockState;

public interface Performer {
    void initialize(PerformerSnipe var1);

    void perform(EditSession var1, int var2, int var3, int var4, BlockState var5) throws MaxChangedBlocksException;

    void sendInfo(PerformerSnipe var1);

    PerformerProperties getProperties();

    void setProperties(PerformerProperties var1, AbstractBrush brushReference);

    void loadProperties();
}
