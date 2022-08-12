package com.jayemceekay.terrasniper.brush.type;

import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BlockState;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

public class ExtrudeBrush extends AbstractBrush {
    private double trueCircle;

    public ExtrudeBrush() {
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        String firstParameter = parameters[0];
        if (firstParameter.equalsIgnoreCase("info")) {
            messenger.sendMessage(ChatFormatting.GOLD + "Extrude Brush Parameters:");
            messenger.sendMessage(ChatFormatting.AQUA + "/b ex [true|false] -- Uses a true circle algorithm instead of the skinnier version with classic sniper nubs. (false is default)");
        } else if (parameters.length == 1) {
            if (firstParameter.equalsIgnoreCase("true")) {
                this.trueCircle = 0.5D;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode ON.");
            } else if (firstParameter.equalsIgnoreCase("false")) {
                this.trueCircle = 0.0D;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode OFF.");
            } else {
                messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters! Use the \"info\" parameter to display parameter info.");
            }
        } else {
            messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters length! Use the \"info\" parameter to display parameter info.");
        }

    }

    public List<String> handleCompletions(String[] parameters) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("true", "false"), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("true", "false"), "");
        }
    }

    private void extrudeUpOrDown(Snipe snipe, boolean isUp) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        double brushSizeSquared = Math.pow(brushSize + this.trueCircle, 2);
        for (int x = -brushSize; x <= brushSize; x++) {
            double xSquared = Math.pow(x, 2);
            for (int z = -brushSize; z <= brushSize; z++) {
                if ((xSquared + Math.pow(z, 2)) <= brushSizeSquared) {
                    int direction = (isUp ? 1 : -1);
                    for (int y = 0; y < Math.abs(toolkitProperties.getVoxelHeight()); y++) {
                        int tempY = y * direction;
                        BlockVector3 targetBlock = getTargetBlock();
                        int targetBlockX = targetBlock.getX();
                        int targetBlockY = targetBlock.getY();
                        int targetBlockZ = targetBlock.getZ();
                        perform(
                                targetBlockX + x,
                                targetBlockY + tempY,
                                targetBlockZ + z,
                                clampY(targetBlockX + x, targetBlockY + tempY, targetBlockZ + z),
                                targetBlockX + x,
                                targetBlockY + tempY + direction,
                                targetBlockZ + z,
                                clampY(targetBlockX + x, targetBlockY + tempY + direction, targetBlockZ + z),
                                toolkitProperties
                        );
                    }
                }
            }
        }
    }

    private void extrudeNorthOrSouth(Snipe snipe, boolean isSouth) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        double brushSizeSquared = Math.pow(brushSize + this.trueCircle, 2);
        for (int x = -brushSize; x <= brushSize; x++) {
            double xSquared = Math.pow(x, 2);
            for (int y = -brushSize; y <= brushSize; y++) {
                if ((xSquared + Math.pow(y, 2)) <= brushSizeSquared) {
                    int direction = (isSouth) ? 1 : -1;
                    for (int z = 0; z < Math.abs(toolkitProperties.getVoxelHeight()); z++) {
                        int tempZ = z * direction;
                        BlockVector3 targetBlock = this.getTargetBlock();
                        perform(
                                targetBlock.getX() + x,
                                targetBlock.getY() + y,
                                targetBlock.getZ() + tempZ,
                                clampY(targetBlock.getX() + x, targetBlock.getY() + y, targetBlock.getZ() + tempZ),
                                targetBlock.getX() + x,
                                targetBlock.getY() + y,
                                targetBlock.getZ() + tempZ + direction,
                                this.clampY(
                                        targetBlock.getX() + x,
                                        targetBlock.getY() + y,
                                        targetBlock.getZ() + tempZ + direction
                                ),
                                toolkitProperties
                        );
                    }
                }
            }
        }
    }

    private void extrudeEastOrWest(Snipe snipe, boolean isEast) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        double brushSizeSquared = Math.pow(brushSize + this.trueCircle, 2);
        for (int y = -brushSize; y <= brushSize; y++) {
            double ySquared = Math.pow(y, 2);
            for (int z = -brushSize; z <= brushSize; z++) {
                if ((ySquared + Math.pow(z, 2)) <= brushSizeSquared) {
                    int direction = (isEast) ? 1 : -1;
                    for (int x = 0; x < Math.abs(toolkitProperties.getVoxelHeight()); x++) {
                        int tempX = x * direction;
                        BlockVector3 targetBlock = this.getTargetBlock();
                        perform(
                                targetBlock.getX() + tempX,
                                targetBlock.getY() + y,
                                targetBlock.getZ() + z,
                                this.clampY(targetBlock.getX() + tempX, targetBlock.getY() + y, targetBlock.getZ() + z),
                                targetBlock.getX() + tempX + direction,
                                targetBlock.getY() + y,
                                targetBlock.getZ() + z,
                                this.clampY(
                                        targetBlock.getX() + tempX + direction,
                                        targetBlock.getY() + y,
                                        targetBlock.getZ() + z
                                ),
                                toolkitProperties
                        );
                    }
                }
            }
        }
    }

    private void perform(
            int x1,
            int y1,
            int z1,
            BlockState block1,
            int x2,
            int y2,
            int z2,
            BlockState block2,
            ToolkitProperties toolkitProperties
    ) throws MaxChangedBlocksException {
        if (toolkitProperties.isVoxelListContains(getBlock(x1, y1, z1))) {
            setBlock(x2, y2, z2, getBlockType(x1, y1, z1).getDefaultState());
            setBlockData(BlockVector3.at(x2, clampY(y2), z2), getBlock(BlockVector3.at(x1, y1, z1)));
        }
    }

    private void selectExtrudeMethod(Snipe snipe, @Nullable Direction blockFace, boolean towardsUser) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        if (blockFace == null || toolkitProperties.getVoxelHeight() == 0) {
            return;
        }
        switch (blockFace) {
            case UP:
                extrudeUpOrDown(snipe, towardsUser);
                break;
            case SOUTH:
                extrudeNorthOrSouth(snipe, towardsUser);
                break;
            case EAST:
                extrudeEastOrWest(snipe, towardsUser);
                break;
            default:
                break;
        }
    }

    @Override
    public void handleArrowAction(Snipe snipe) {
        BlockVector3 targetBlock = getTargetBlock();
        BlockVector3 lastBlock = getLastBlock();
        try {
            selectExtrudeMethod(snipe, getDirection(targetBlock, lastBlock), false);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleGunpowderAction(Snipe snipe) {
        BlockVector3 targetBlock = getTargetBlock();
        BlockVector3 lastBlock = getLastBlock();
        try {
            selectExtrudeMethod(snipe, getDirection(targetBlock, lastBlock), true);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
        messenger.sendBrushSizeMessage();
        messenger.sendVoxelHeightMessage();
        messenger.sendVoxelListMessage();
        messenger.sendMessage(ChatFormatting.AQUA + (Double.compare(this.trueCircle, 0.5) == 0
                ? "True circle mode ON"
                : "True circle mode OFF"));
    }

}
