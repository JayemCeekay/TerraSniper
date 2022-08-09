package com.jayemceekay.TerraSniper.brush.type.performer.disc;

import com.jayemceekay.TerraSniper.brush.type.performer.AbstractPerformerBrush;
import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.TerraSniper.sniper.snipe.Snipe;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;

public class VoxelDiscFaceBrush extends AbstractPerformerBrush {
    public VoxelDiscFaceBrush() {
    }

    public void loadProperties() {
    }

    public void handleArrowAction(Snipe snipe) {
        BlockVector3 lastBlock = this.getLastBlock();
        BlockVector3 targetBlock = this.getTargetBlock();
        Direction face = this.getDirection(targetBlock, lastBlock);
        if (face != null) {
            try {
                this.pre(snipe, face, targetBlock);
            } catch (MaxChangedBlocksException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleGunpowderAction(Snipe snipe) {
        BlockVector3 lastBlock = this.getLastBlock();
        BlockVector3 targetBlock = this.getTargetBlock();
        Direction face = this.getDirection(targetBlock, lastBlock);
        if (face != null) {
            try {
                this.pre(snipe, face, lastBlock);
            } catch (MaxChangedBlocksException e) {
                e.printStackTrace();
            }
        }
    }

    private void pre(Snipe snipe, Direction blockFace, BlockVector3 targetBlock) throws MaxChangedBlocksException {
        switch (blockFace) {
            case NORTH:
            case SOUTH:
                discNorthSouth(snipe, targetBlock);
                break;
            case EAST:
            case WEST:
                discEastWest(snipe, targetBlock);
                break;
            case UP:
            case DOWN:
                disc(snipe, targetBlock);
                break;
            default:
                break;
        }
    }


    private void discNorthSouth(Snipe snipe, BlockVector3 targetBlock) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        int blockX = targetBlock.getX();
        int blockY = targetBlock.getY();
        int blockZ = targetBlock.getZ();

        for (int x = brushSize; x >= -brushSize; --x) {
            for (int y = brushSize; y >= -brushSize; --y) {
                this.performer.perform(this.getEditSession(), blockX + x, this.clampY(blockY + y), blockZ, this.getBlock(blockX + x, this.clampY(blockY + y), blockZ));
            }
        }

    }

    private void discEastWest(Snipe snipe, BlockVector3 targetBlock) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        int blockX = targetBlock.getX();
        int blockY = targetBlock.getY();
        int blockZ = targetBlock.getZ();

        for (int x = brushSize; x >= -brushSize; --x) {
            for (int y = brushSize; y >= -brushSize; --y) {
                this.performer.perform(this.getEditSession(), blockX, this.clampY(blockY + x), blockZ + y, this.getBlock(blockX, this.clampY(blockY + x), blockZ + y));
            }
        }

    }

    private void disc(Snipe snipe, BlockVector3 targetBlock) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        int blockX = targetBlock.getX();
        int blockY = targetBlock.getY();
        int blockZ = targetBlock.getZ();

        for (int x = brushSize; x >= -brushSize; --x) {
            for (int y = brushSize; y >= -brushSize; --y) {
                this.performer.perform(this.getEditSession(), blockX + x, this.clampY(blockY), blockZ + y, this.getBlock(blockX + x, this.clampY(blockY), blockZ + y));
            }
        }

    }

    public void sendInfo(Snipe snipe) {
        snipe.createMessageSender().brushNameMessage().brushSizeMessage().send();
    }
}
