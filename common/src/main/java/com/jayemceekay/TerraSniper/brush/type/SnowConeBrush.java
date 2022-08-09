package com.jayemceekay.TerraSniper.brush.type;

import com.jayemceekay.TerraSniper.sniper.snipe.Snipe;
import com.jayemceekay.TerraSniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.TerraSniper.util.material.Materials;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.minecraft.ChatFormatting;

public class SnowConeBrush extends AbstractBrush {
    public SnowConeBrush() {
    }

    @Override
    public void handleArrowAction(Snipe snipe) {
    }

    @Override
    public void handleGunpowderAction(Snipe snipe) {
        BlockVector3 targetBlock = getTargetBlock();
        if (getBlockType(targetBlock) == BlockTypes.SNOW) {
            try {
                addSnow(targetBlock);
            } catch (MaxChangedBlocksException e) {
                e.printStackTrace();
            }
        } else {
            BlockVector3 blockAbove = BlockVector3.at(targetBlock.getX(), targetBlock.getY() + 1, targetBlock.getZ());
            BlockType type = getBlockType(blockAbove);
            if (Materials.isEmpty(type)) {
                try {
                    addSnow(blockAbove);
                } catch (MaxChangedBlocksException e) {
                    e.printStackTrace();
                }
            } else {
                SnipeMessenger messenger = snipe.createMessenger();
                messenger.sendMessage(ChatFormatting.RED + "Error: Center block neither snow nor air.");
            }
        }
    }

    private void addSnow(BlockVector3 targetBlock) throws MaxChangedBlocksException {
        int blockPositionX = targetBlock.getX();
        int blockPositionY = targetBlock.getY();
        int blockPositionZ = targetBlock.getZ();
        int brushSize = Materials.isEmpty(getBlockType(blockPositionX, blockPositionY, blockPositionZ))
                ? 0
                : blockDataToSnowLayers(getBlock(blockPositionX, clampY(blockPositionY), blockPositionZ)) + 1;
        int brushSizeDoubled = 2 * brushSize;
        BlockType[][] snowCone = new BlockType[brushSizeDoubled + 1][brushSizeDoubled + 1]; // Will hold block IDs
        BlockState[][] snowConeData = new BlockState[brushSizeDoubled + 1][brushSizeDoubled + 1]; // Will hold data values for snowCone
        int[][] yOffset = new int[brushSizeDoubled + 1][brushSizeDoubled + 1];
        // prime the arrays
        for (int x = 0; x <= brushSizeDoubled; x++) {
            for (int z = 0; z <= brushSizeDoubled; z++) {
                boolean flag = true;
                for (int i = 0; i < 10; i++) { // overlay
                    if (flag) {
                        if ((Materials.isEmpty(getBlockType(
                                blockPositionX - brushSize + x,
                                blockPositionY - i,
                                blockPositionZ - brushSize + z
                        )) || getBlockType(
                                blockPositionX - brushSize + x,
                                blockPositionY - i,
                                blockPositionZ - brushSize + z
                        ) == BlockTypes.SNOW) && !Materials.isEmpty(getBlockType(
                                blockPositionX - brushSize + x,
                                blockPositionY - i - 1,
                                blockPositionZ - brushSize + z
                        )) && getBlockType(
                                blockPositionX - brushSize + x,
                                blockPositionY - i - 1,
                                blockPositionZ - brushSize + z
                        ) != BlockTypes.SNOW) {
                            flag = false;
                            yOffset[x][z] = i;
                        }
                    }
                }
                snowCone[x][z] = getBlockType(
                        blockPositionX - brushSize + x,
                        blockPositionY - yOffset[x][z],
                        blockPositionZ - brushSize + z
                );
                snowConeData[x][z] = getBlock(
                        blockPositionX - brushSize + x,
                        clampY(blockPositionY - yOffset[x][z]),
                        blockPositionZ - brushSize + z
                );
            }
        }
        // figure out new snowheights
        for (int x = 0; x <= brushSizeDoubled; x++) {
            double xSquared = Math.pow(x - brushSize, 2);
            for (int z = 0; z <= 2 * brushSize; z++) {
                double zSquared = Math.pow(z - brushSize, 2);
                double dist = Math.pow(xSquared + zSquared, 0.5); // distance from center of array
                int snowData = brushSize - (int) Math.ceil(dist);
                if (snowData >= 0) { // no funny business
                    // Increase snowtile size, if smaller than target
                    if (snowData == 0) {
                        if (Materials.isEmpty(snowCone[x][z])) {
                            snowCone[x][z] = BlockTypes.SNOW;
                            snowConeData[x][z] = BlockTypes.SNOW.getDefaultState();
                        }
                    } else if (snowData == 7) { // Turn largest snowtile into snowblock
                        if (snowCone[x][z] == BlockTypes.SNOW) {
                            snowCone[x][z] = BlockTypes.SNOW_BLOCK;
                            snowConeData[x][z] = BlockTypes.SNOW_BLOCK.getDefaultState();
                        }
                    } else {
                        if (snowData > blockDataToSnowLayers(snowConeData[x][z])) {
                            if (Materials.isEmpty(snowCone[x][z])) {
                                snowConeData[x][z] = setSnowLayers(snowConeData[x][z], snowData);
                                snowCone[x][z] = BlockTypes.SNOW;
                            } else if (snowCone[x][z] == BlockTypes.SNOW) {
                                snowConeData[x][z] = setSnowLayers(snowConeData[x][z], snowData);
                            }
                        } else if (yOffset[x][z] > 0 && snowCone[x][z] == BlockTypes.SNOW) {
                            snowConeData[x][z] = setSnowLayers(snowConeData[x][z], blockDataToSnowLayers(snowConeData[x][z]) + 1);
                            if (blockDataToSnowLayers(snowConeData[x][z]) == 7) {
                                snowConeData[x][z] = BlockTypes.SNOW.getDefaultState();
                                snowCone[x][z] = BlockTypes.SNOW_BLOCK;
                            }
                        }
                    }
                }
            }
        }
        for (int x = 0; x <= brushSizeDoubled; x++) {
            for (int z = 0; z <= brushSizeDoubled; z++) {
                setBlock(
                        blockPositionX - brushSize + x,
                        blockPositionY - yOffset[x][z],
                        blockPositionZ - brushSize + z,
                        snowCone[x][z].getDefaultState()
                );
                setBlockData(
                        blockPositionX - brushSize + x,
                        clampY(blockPositionY - yOffset[x][z]),
                        blockPositionZ - brushSize + z,
                        snowConeData[x][z]
                );
            }
        }
    }

    private int blockDataToSnowLayers(BlockState blockData) {
        BlockType type = blockData.getBlockType();
        Property<Integer> layersProperty = type.getProperty("layers");
        if (layersProperty == null) {
            return 0;
        }
        return blockData.getState(layersProperty);
    }

    private BlockState setSnowLayers(BlockState blockData, int layers) {
        BlockType type = blockData.getBlockType();
        Property<Integer> layersProperty = type.getProperty("layers");
        if (layersProperty == null) {
            return blockData;
        }
        return blockData.with(layersProperty, layers);
    }

    @Override
    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
    }

}
