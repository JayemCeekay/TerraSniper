package com.jayemceekay.terrasniper.brush.type;

import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.math.MathHelper;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.List;
import java.util.stream.Stream;

public class CleanSnowBrush extends AbstractBrush {
    private double trueCircle;

    public CleanSnowBrush() {
    }

    @Override
    public final void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        String firstParameter = parameters[0];
        if (firstParameter.equalsIgnoreCase("info")) {
            messenger.sendMessage(ChatFormatting.GOLD + "Clean Snow Brush Parameters:");
            messenger.sendMessage(ChatFormatting.AQUA + "/b cls [true|false] -- Uses a true sphere algorithm instead of the skinnier version with classic sniper nubs. Default is false.");
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

    @Override
    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("true", "false"), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("true", "false"), "");
        }
    }

    @Override
    public void handleArrowAction(Snipe snipe) {
        this.cleanSnow(snipe);
    }

    @Override
    public void handleGunpowderAction(Snipe snipe) {
        this.cleanSnow(snipe);
    }

    private void cleanSnow(Snipe snipe) {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        double brushSizeSquared = Math.pow((double) brushSize + this.trueCircle, 2.0D);

        for (int y = (brushSize + 1) * 2; y >= 0; --y) {
            double ySquared = MathHelper.square(y - brushSize);

            for (int x = (brushSize + 1) * 2; x >= 0; --x) {
                double xSquared = MathHelper.square(x - brushSize);

                for (int z = (brushSize + 1) * 2; z >= 0; --z) {
                    if (xSquared + (double) MathHelper.square(z - brushSize) + ySquared <= brushSizeSquared) {
                        BlockVector3 targetBlock = this.getTargetBlock();
                        int targetBlockX = targetBlock.getX();
                        int targetBlockY = targetBlock.getY();
                        int targetBlockZ = targetBlock.getZ();
                        if (this.clampY(targetBlockX + x - brushSize, targetBlockY + z - brushSize, targetBlockZ + y - brushSize).getBlockType() == BlockTypes.SNOW && (this.clampY(targetBlockX + x - brushSize, targetBlockY + z - brushSize - 1, targetBlockZ + y - brushSize).getBlockType() == BlockTypes.SNOW || this.clampY(targetBlockX + x - brushSize, targetBlockY + z - brushSize - 1, targetBlockZ + y - brushSize).getBlockType().getMaterial().isAir())) {
                            try {
                                this.setBlockData(BlockVector3.at(targetBlockZ + y - brushSize, targetBlockX + x - brushSize, targetBlockY + z - brushSize), BlockTypes.AIR.getDefaultState());
                            } catch (MaxChangedBlocksException var18) {
                                var18.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
        messenger.sendBrushSizeMessage();
    }
}
