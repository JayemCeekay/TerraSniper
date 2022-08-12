package com.jayemceekay.terrasniper.brush.type.performer.disc;

import com.jayemceekay.terrasniper.brush.type.performer.AbstractPerformerBrush;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.List;
import java.util.stream.Stream;

public class DiscBrush extends AbstractPerformerBrush {
    private double trueCircle;

    public DiscBrush() {
    }

    public void loadProperties() {
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();

        for (String parameter : parameters) {
            if (parameter.equalsIgnoreCase("info")) {
                messenger.sendMessage(ChatFormatting.GOLD + "Disc Brush Parameters:");
                messenger.sendMessage(ChatFormatting.AQUA + "/b d [true|false] -- Uses a true circle algorithm instead of the skinnier version with classic sniper nubs. (false is default)");
            }

            if (parameter.equalsIgnoreCase("true")) {
                this.trueCircle = 0.5D;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode ON.");
            } else if (parameter.equalsIgnoreCase("false")) {
                this.trueCircle = 0.0D;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode OFF.");
            } else {
                messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters! Use the \"info\" parameter to display parameter info.");
            }
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

    public void handleArrowAction(Snipe snipe) {
        BlockVector3 targetBlock = this.getTargetBlock();
        this.disc(snipe, targetBlock);
    }

    public void handleGunpowderAction(Snipe snipe) {
        BlockVector3 lastBlock = this.getLastBlock();
        this.disc(snipe, lastBlock);
    }

    private void disc(Snipe snipe, BlockVector3 targetBlock) {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        double radiusSquared = ((double) brushSize + this.trueCircle) * ((double) brushSize + this.trueCircle);
        BlockVector3 currentPoint = targetBlock;

        for (int x = -brushSize; x <= brushSize; ++x) {
            currentPoint = currentPoint.withX(targetBlock.getX() + x);

            for (int z = -brushSize; z <= brushSize; ++z) {
                currentPoint = currentPoint.withZ(targetBlock.getZ() + z);
                if ((double) targetBlock.distanceSq(currentPoint) <= radiusSquared) {
                    try {
                        this.performer.perform(this.getEditSession(), currentPoint.getBlockX(), this.clampY(currentPoint.getBlockY()), currentPoint.getBlockZ(), this.clampY(currentPoint.getBlockX(), currentPoint.getBlockY(), currentPoint.getBlockZ()));
                    } catch (MaxChangedBlocksException var11) {
                        var11.printStackTrace();
                    }
                }
            }
        }

    }

    public void sendInfo(Snipe snipe) {
        snipe.createMessageSender().brushNameMessage().brushSizeMessage().send();
    }
}
