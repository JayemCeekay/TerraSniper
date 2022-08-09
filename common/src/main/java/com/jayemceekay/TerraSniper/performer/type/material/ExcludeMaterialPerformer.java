package com.jayemceekay.TerraSniper.performer.type.material;

import com.jayemceekay.TerraSniper.performer.type.AbstractPerformer;
import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.TerraSniper.sniper.snipe.performer.PerformerSnipe;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.block.BlockState;

import java.util.List;

public class ExcludeMaterialPerformer extends AbstractPerformer {

    private List<BlockState> excludeList;
    private Pattern pattern;

    @Override
    public void initialize(PerformerSnipe snipe) {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        this.pattern = toolkitProperties.getPattern().getPattern();
        this.excludeList = toolkitProperties.getVoxelList();
    }

    @Override
    public void perform(EditSession editSession, int x, int y, int z, BlockState block) {
        if (!this.excludeList.contains(block)) {
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
