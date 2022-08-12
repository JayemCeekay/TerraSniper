package com.jayemceekay.terrasniper.performer.type.material;

import com.jayemceekay.terrasniper.performer.type.AbstractPerformer;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.performer.PerformerSnipe;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.block.BlockState;

import java.util.List;

public class IncludeMaterialPerformer extends AbstractPerformer {

    private List<BlockState> includeList;
    private Pattern pattern;

    @Override
    public void initialize(PerformerSnipe snipe) {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        this.pattern = toolkitProperties.getPattern().getPattern();
        this.includeList = toolkitProperties.getVoxelList();
    }

    @Override
    public void perform(EditSession editSession, int x, int y, int z, BlockState block) {
        if (this.includeList.contains(block)) {
            setBlock(editSession, x, y, z, this.pattern);
        }
    }

    @Override
    public void sendInfo(PerformerSnipe snipe) {
        snipe.createMessageSender()
                .performerNameMessage()
                .voxelListMessage()
                .patternMessage()
                .send();
    }

}
