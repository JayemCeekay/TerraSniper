package com.jayemceekay.terrasniper.brush.type;

import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.material.MaterialSet;
import com.jayemceekay.terrasniper.util.material.MaterialSets;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

public class EraserBrush extends AbstractBrush {
    private static final MaterialSet EXCLUSIVE_MATERIALS;
    private static final MaterialSet EXCLUSIVE_LIQUIDS;

    static {
        EXCLUSIVE_MATERIALS = MaterialSet.builder().with(BlockCategories.SAND).with(MaterialSets.SANDSTONES).with(MaterialSets.RED_SANDSTONES).with(MaterialSets.AIRS).with(MaterialSets.STONES).with(MaterialSets.GRASSES).with(BlockCategories.DIRT_LIKE).add(BlockTypes.GRAVEL).build();
        EXCLUSIVE_LIQUIDS = MaterialSet.builder().with(MaterialSets.LIQUIDS).build();
    }

    public EraserBrush() {
        setCanUseSmallBlocks(false);
    }

    public void handleArrowAction(Snipe snipe) {
        try {
            this.doErase(snipe, false);
        } catch (MaxChangedBlocksException var3) {
            var3.printStackTrace();
        }

    }

    public void handleGunpowderAction(Snipe snipe) {
        try {
            this.doErase(snipe, true);
        } catch (MaxChangedBlocksException var3) {
            var3.printStackTrace();
        }

    }

    private void doErase(Snipe snipe, boolean keepWater) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        int brushSizeDoubled = 2 * brushSize;
        BlockVector3 targetBlock = this.getTargetBlock();

        for (int x = brushSizeDoubled; x >= 0; --x) {
            int currentX = targetBlock.getX() - brushSize + x;

            for (int y = 0; y <= brushSizeDoubled; ++y) {
                int currentY = targetBlock.getY() - brushSize + y;

                for (int z = brushSizeDoubled; z >= 0; --z) {
                    int currentZ = targetBlock.getZ() - brushSize + z;
                    BlockState currentBlock = this.getBlock(currentX, currentY, currentZ);
                    if (!EXCLUSIVE_MATERIALS.contains(currentBlock) && (!keepWater || !EXCLUSIVE_LIQUIDS.contains(currentBlock))) {
                        this.setBlock(currentX, currentY, currentZ, BlockTypes.AIR.getDefaultState());
                    }
                }
            }
        }

    }

    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
        messenger.sendBrushSizeMessage();
    }
}
