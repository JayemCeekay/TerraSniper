package com.jayemceekay.terrasniper.brush.type.performer.disc;

import com.jayemceekay.terrasniper.brush.type.performer.AbstractPerformerBrush;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.List;
import java.util.stream.Stream;

public class DiscBrush extends AbstractPerformerBrush {
    private double trueCircle;
    private boolean centerBlock=false;

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
                messenger.sendMessage(ChatFormatting.DARK_GREEN + "/b d center -- Toggles whether the brush will center on a block or the corner of a block in smallBlocks mode");
            }

            if (parameter.equalsIgnoreCase("true")) {
                this.trueCircle = 0.5D;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode ON.");
            } else if (parameter.equalsIgnoreCase("false")) {
                this.trueCircle = 0.0D;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode OFF.");
            } else { if (parameter.equalsIgnoreCase("center")) {
                    if (!this.centerBlock) {
                        this.centerBlock = true;
                        messenger.sendMessage(ChatFormatting.AQUA + "centerBlock ON. The brush will now be centered on a block in smallBlocks mode.");
                    } else {
                        this.centerBlock = false;
                        messenger.sendMessage(ChatFormatting.AQUA + "centerBlock OFF. The brush will now be centered on the corner of a block in smallBlocks mode.");
                    }
                } else {
                    messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters! Use the \"info\" parameter to display parameter info.");
                }
            }
        }

    }

    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("center", "true", "false"), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("center", "true", "false"), "");
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

        int sizeOffset = (useSmallBlocks && centerBlock) ? 1 : 0;
        int xOffset = (useSmallBlocks && centerBlock) ? 2*(this.offsetVector.getX()-this.getTargetBlock().getX()) - 1 : 0;
        int zOffset = (useSmallBlocks && centerBlock) ? 2*(this.offsetVector.getZ()-this.getTargetBlock().getZ()) - 1 : 0;

        double radiusSquared = ((double) brushSize + this.trueCircle + sizeOffset/2.0D) * ((double) brushSize + this.trueCircle + sizeOffset/2.0D);
        int blockX = targetBlock.getX();
        int blockY = targetBlock.getY();
        int blockZ = targetBlock.getZ();

        for (int x = -brushSize-sizeOffset; x <= brushSize+sizeOffset; ++x) {
            double xSquared = Math.pow(x+xOffset/2.0D, 2.0D);

            for (int z = -brushSize-sizeOffset; z <= brushSize+sizeOffset; ++z) {
                if (xSquared + Math.pow(z+zOffset/2.0D, 2.0D) <= radiusSquared) {
                    try {
                        this.performer.perform(this.getEditSession(), blockX + x, blockY, blockZ + z, this.getBlock(blockX + x, blockY, blockZ + z));
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
