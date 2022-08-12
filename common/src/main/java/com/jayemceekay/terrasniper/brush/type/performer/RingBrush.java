package com.jayemceekay.terrasniper.brush.type.performer;

import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.text.NumericParser;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.List;
import java.util.stream.Stream;

public class RingBrush extends AbstractPerformerBrush {
    private static final double DEFAULT_INNER_SIZE = 0.0D;
    private double trueCircle;
    private double innerSize = 0.0D;

    public RingBrush() {
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        int var5 = parameters.length;

        for (String parameter : parameters) {
            if (parameter.equalsIgnoreCase("info")) {
                messenger.sendMessage(ChatFormatting.GOLD + "Ring Brush Parameters:");
                messenger.sendMessage(ChatFormatting.AQUA + "/b ri [true|false] -- Uses a true circle algorithm instead of the skinnier version with classic sniper nubs. (false is default)");
                messenger.sendMessage(ChatFormatting.AQUA + "/b ri ir[n] -- Sets the inner radius to n units.");
                return;
            }

            if (parameter.equalsIgnoreCase("true")) {
                this.trueCircle = 0.5D;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode ON.");
            } else if (parameter.equalsIgnoreCase("false")) {
                this.trueCircle = 0.0D;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode OFF.");
            } else if (parameter.startsWith("ir[")) {
                Double innerSize = NumericParser.parseDouble(parameter.replace("ir[", "").replace("]", ""));
                if (innerSize != null) {
                    this.innerSize = innerSize;
                    messenger.sendMessage(ChatFormatting.AQUA + "The inner radius has been set to: " + ChatFormatting.RED + this.innerSize);
                } else {
                    messenger.sendMessage(ChatFormatting.RED + "Invalid number.");
                }
            } else {
                messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters! Use the \"info\" parameter to display parameter info.");
            }
        }

    }

    public List<String> handleCompletions(String[] parameters) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("true", "false", "ir"), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("true", "false", "ir"), "");
        }
    }

    public void handleArrowAction(Snipe snipe) {
        BlockVector3 targetBlock = this.getTargetBlock();

        try {
            this.ring(snipe, targetBlock);
        } catch (MaxChangedBlocksException var4) {
            var4.printStackTrace();
        }

    }

    public void handleGunpowderAction(Snipe snipe) {
        BlockVector3 lastBlock = this.getLastBlock();

        try {
            this.ring(snipe, lastBlock);
        } catch (MaxChangedBlocksException var4) {
            var4.printStackTrace();
        }

    }

    private void ring(Snipe snipe, BlockVector3 targetBlock) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        double outerSquared = Math.pow((double) brushSize + this.trueCircle, 2.0D);
        double innerSquared = Math.pow(this.innerSize, 2.0D);
        int blockX = targetBlock.getX();
        int blockY = targetBlock.getY();
        int blockZ = targetBlock.getZ();

        for (int x = brushSize; x >= 0; --x) {
            double xSquared = Math.pow(x, 2.0D);

            for (int z = brushSize; z >= 0; --z) {
                double ySquared = Math.pow(z, 2.0D);
                if (xSquared + ySquared <= outerSquared && xSquared + ySquared >= innerSquared) {
                    this.performer.perform(this.getEditSession(), blockX + x, blockY, blockZ + z, this.getBlock(blockX + x, blockY, blockZ + z));
                    this.performer.perform(this.getEditSession(), blockX + x, blockY, blockZ - z, this.getBlock(blockX + x, blockY, blockZ - z));
                    this.performer.perform(this.getEditSession(), blockX - x, blockY, blockZ + z, this.getBlock(blockX - x, blockY, blockZ + z));
                    this.performer.perform(this.getEditSession(), blockX - x, blockY, blockZ - z, this.getBlock(blockX - x, blockY, blockZ - z));
                }
            }
        }

    }

    public void sendInfo(Snipe snipe) {
        snipe.createMessageSender().brushNameMessage().brushSizeMessage().message(ChatFormatting.AQUA + "The inner radius is " + ChatFormatting.RED + this.innerSize).send();
    }
}
