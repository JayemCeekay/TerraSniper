package com.jayemceekay.TerraSniper.brush.type;

import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.TerraSniper.sniper.snipe.Snipe;
import com.jayemceekay.TerraSniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.TerraSniper.util.material.Materials;
import com.jayemceekay.TerraSniper.util.math.MathHelper;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.List;
import java.util.stream.Stream;

public class DrainBrush extends AbstractBrush {
    private double trueCircle;
    private boolean disc;

    public DrainBrush() {
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        String firstParameter = parameters[0];
        if (firstParameter.equalsIgnoreCase("info")) {
            messenger.sendMessage(ChatFormatting.GOLD + "Drain Brush Parameters:");
            messenger.sendMessage(ChatFormatting.AQUA + "/b drain [true|false] -- Uses a true sphere algorithm instead of the skinnier version with classic sniper nubs. Default is false.");
            messenger.sendMessage(ChatFormatting.AQUA + "/b drain d -- Toggles disc drain mode, as opposed to a ball drain mode.");
        } else if (parameters.length == 1) {
            if (firstParameter.equalsIgnoreCase("true")) {
                this.trueCircle = 0.5D;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode ON.");
            } else if (firstParameter.equalsIgnoreCase("false")) {
                this.trueCircle = 0.0D;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode OFF.");
            } else if (firstParameter.equalsIgnoreCase("d")) {
                if (this.disc) {
                    this.disc = false;
                    messenger.sendMessage(ChatFormatting.AQUA + "Disc drain mode OFF");
                } else {
                    this.disc = true;
                    messenger.sendMessage(ChatFormatting.AQUA + "Disc drain mode ON");
                }
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
            return SuggestionHelper.limitByPrefix(Stream.of("true", "false", "d"), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("true", "false", "d"), "");
        }
    }

    public void handleArrowAction(Snipe snipe) {
        try {
            this.drain(snipe);
        } catch (MaxChangedBlocksException var3) {
            var3.printStackTrace();
        }

    }

    public void handleGunpowderAction(Snipe snipe) {
        try {
            this.drain(snipe);
        } catch (MaxChangedBlocksException var3) {
            var3.printStackTrace();
        }

    }

    private void drain(Snipe snipe) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        double brushSizeSquared = Math.pow((double) brushSize + this.trueCircle, 2.0D);
        BlockVector3 targetBlock = this.getTargetBlock();
        int targetBlockX = targetBlock.getX();
        int targetBlockY = targetBlock.getY();
        int targetBlockZ = targetBlock.getZ();
        int x;
        double ySquared;
        int y;
        double xSquared;
        BlockType type;
        if (this.disc) {
            for (x = brushSize; x >= 0; --x) {
                ySquared = MathHelper.square(x);

                for (y = brushSize; y >= 0; --y) {
                    xSquared = MathHelper.square(y);
                    if (ySquared + xSquared <= brushSizeSquared) {
                        BlockType typePlusPlus = this.getBlockType(targetBlock.add(x, 0, y));
                        if (Materials.isLiquid(typePlusPlus)) {
                            this.setBlock(targetBlock.add(x, 0, y), BlockTypes.AIR.getDefaultState());
                        }

                        type = this.getBlockType(targetBlockX + x, targetBlockY, targetBlockZ - y);
                        if (Materials.isLiquid(type)) {
                            this.setBlock(targetBlockX + x, targetBlockY, targetBlockZ - y, BlockTypes.AIR.getDefaultState());
                        }

                        BlockType typeMinusPlus = this.getBlockType(targetBlockX - x, targetBlockY, targetBlockZ + y);
                        if (Materials.isLiquid(typeMinusPlus)) {
                            this.setBlock(targetBlockX - x, targetBlockY, targetBlockZ + y, BlockTypes.AIR.getDefaultState());
                        }

                        BlockType typeMinusMinus = this.getBlockType(targetBlockX - x, targetBlockY, targetBlockZ - y);
                        if (Materials.isLiquid(typeMinusMinus)) {
                            this.setBlock(targetBlockX - x, targetBlockY, targetBlockZ - y, BlockTypes.AIR.getDefaultState());
                        }
                    }
                }
            }
        } else {
            for (x = (brushSize + 1) * 2; x >= 0; --x) {
                ySquared = MathHelper.square(x - brushSize);

                for (y = (brushSize + 1) * 2; y >= 0; --y) {
                    xSquared = MathHelper.square(y - brushSize);

                    for (int z = (brushSize + 1) * 2; z >= 0; --z) {
                        if (xSquared + (double) MathHelper.square(z - brushSize) + ySquared <= brushSizeSquared) {
                            type = this.getBlockType(targetBlockX + y - brushSize, targetBlockY + z - brushSize, targetBlockZ + x - brushSize);
                            if (Materials.isLiquid(type)) {
                                this.setBlock(targetBlockX + y - brushSize, targetBlockY + z - brushSize, targetBlockZ + x - brushSize, BlockTypes.AIR.getDefaultState());
                            }
                        }
                    }
                }
            }
        }

    }

    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
        messenger.sendBrushSizeMessage();
        messenger.sendMessage(ChatFormatting.AQUA + (Double.compare(this.trueCircle, 0.5D) == 0 ? "True circle mode ON" : "True circle mode OFF"));
        messenger.sendMessage(ChatFormatting.AQUA + (this.disc ? "Disc drain mode ON" : "Disc drain mode OFF"));
    }
}
