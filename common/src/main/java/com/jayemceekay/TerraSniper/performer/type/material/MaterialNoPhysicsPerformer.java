package com.jayemceekay.TerraSniper.performer.type.material;

import com.jayemceekay.TerraSniper.performer.type.AbstractPerformer;
import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.TerraSniper.sniper.snipe.performer.PerformerSnipe;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;

public class MaterialNoPhysicsPerformer extends AbstractPerformer {

    private Pattern pattern;

    @Override
    public void initialize(PerformerSnipe snipe) {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        this.pattern = toolkitProperties.getPattern().getPattern();
    }

    @Override
    public void perform(EditSession editSession, int x, int y, int z, BlockState block) {
        BaseBlock baseBlock = simulateSetBlock(x, y, z, pattern);
        if (block.getBlockType() != baseBlock.getBlockType()) {
            setBlock(editSession, x, y, z, baseBlock);
        }
    }

    @Override
    public void sendInfo(PerformerSnipe snipe) {
        snipe.createMessageSender()
                .performerNameMessage()
                .patternMessage()
                .send();
    }

}
