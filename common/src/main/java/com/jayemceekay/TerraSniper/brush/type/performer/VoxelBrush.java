package com.jayemceekay.TerraSniper.brush.type.performer;

import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.TerraSniper.sniper.snipe.Snipe;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;

public class VoxelBrush extends AbstractPerformerBrush {
    public VoxelBrush() {
    }

    public void loadProperties() {
    }

    public void handleArrowAction(Snipe snipe) {
        this.voxel(snipe);
    }

    public void handleGunpowderAction(Snipe snipe) {
        this.voxel(snipe);
    }

    private void voxel(Snipe snipe) {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        BlockVector3 targetBlock = this.getTargetBlock();
        int blockX = targetBlock.getX();
        int blockY = targetBlock.getY();
        int blockZ = targetBlock.getZ();

        for (int z = brushSize; z >= -brushSize; --z) {
            for (int x = brushSize; x >= -brushSize; --x) {
                for (int y = brushSize; y >= -brushSize; --y) {
                    try {
                        this.performer.perform(this.getEditSession(), blockX + x, this.clampY(blockY + z), blockZ + y, this.clampY(blockX + x, blockY + z, blockZ + y));
                    } catch (MaxChangedBlocksException var12) {
                        var12.printStackTrace();
                    }
                }
            }
        }

    }

    public void sendInfo(Snipe snipe) {
        snipe.createMessageSender().brushNameMessage().brushSizeMessage().send();
    }
}
