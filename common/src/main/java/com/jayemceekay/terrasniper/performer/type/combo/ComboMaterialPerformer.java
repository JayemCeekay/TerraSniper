package com.jayemceekay.terrasniper.performer.type.combo;

import com.jayemceekay.terrasniper.performer.type.AbstractPerformer;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.performer.PerformerSnipe;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.block.BlockState;

public class ComboMaterialPerformer extends AbstractPerformer {

    private Pattern pattern;
    private BlockState replaceBlockData;

    @Override
    public void initialize(PerformerSnipe snipe) {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        this.pattern = toolkitProperties.getPattern().getPattern();
        this.replaceBlockData = toolkitProperties.getReplacePattern().asBlockState();
    }

    @Override
    public void perform(EditSession editSession, int x, int y, int z, BlockState block) {
        if (block.getBlockType() == this.replaceBlockData.getBlockType()) {
            setBlock(editSession, x, y, z, this.pattern);
        }
    }

    @Override
    public void sendInfo(PerformerSnipe snipe) {
        snipe.createMessageSender()
                .performerNameMessage()
                .patternMessage()
                .replacePatternMessage()
                .send();
    }

}
