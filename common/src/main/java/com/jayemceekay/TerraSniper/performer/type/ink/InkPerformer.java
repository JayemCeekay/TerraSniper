package com.jayemceekay.TerraSniper.performer.type.ink;

import com.jayemceekay.TerraSniper.performer.type.AbstractPerformer;
import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.TerraSniper.sniper.snipe.performer.PerformerSnipe;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.block.BlockState;

public class InkPerformer extends AbstractPerformer {

    private Pattern pattern;

    @Override
    public void initialize(PerformerSnipe snipe) {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        this.pattern = toolkitProperties.getPattern().getPattern();
    }

    @Override
    public void perform(EditSession editSession, int x, int y, int z, BlockState block) {
        setBlock(editSession, x, y, z, this.pattern);
    }

    @Override
    public void sendInfo(PerformerSnipe snipe) {
        snipe.createMessageSender()
                .performerNameMessage()
                .patternMessage()
                .send();
    }

}
