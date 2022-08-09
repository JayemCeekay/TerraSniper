package com.jayemceekay.TerraSniper.brush.type.performer.disc;

import com.jayemceekay.TerraSniper.brush.type.performer.AbstractPerformerBrush;
import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.TerraSniper.sniper.snipe.Snipe;
import com.jayemceekay.TerraSniper.sniper.snipe.message.SnipeMessenger;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.List;
import java.util.stream.Stream;

public class DiscFaceBrush extends AbstractPerformerBrush {
    private double trueCircle;

    public DiscFaceBrush() {
    }

    public void loadProperties() {
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        int var5 = parameters.length;

        for (String parameter : parameters) {
            if (parameter.equalsIgnoreCase("info")) {
                messenger.sendMessage(ChatFormatting.GOLD + "Disc Face Brush Parameters:");
                messenger.sendMessage(ChatFormatting.AQUA + "/b df [true|false] -- Uses a true circle algorithm instead of the skinnier version with classic sniper nubs. (false is default)");
                return;
            }

            if (parameter.equalsIgnoreCase("true")) {
                this.trueCircle = 0.5D;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode ON.");
            } else if (parameter.equalsIgnoreCase("false")) {
                this.trueCircle = 0.0D;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode OFF.");
            } else {
                messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters length! Use the \"info\" parameter to display parameter info.");
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
        try {
            this.pre(snipe, targetBlock);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    public void handleGunpowderAction(Snipe snipe) {
        BlockVector3 lastBlock = this.getLastBlock();
        try {
            this.pre(snipe, lastBlock);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    private void discUpDown(Snipe snipe, BlockVector3 targetBlock) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        double brushSizeSquared = Math.pow((double) brushSize + this.trueCircle, 2.0D);
        int blockX = targetBlock.getX();
        int blockY = targetBlock.getY();
        int blockZ = targetBlock.getZ();

        for (int x = brushSize; x >= 0; --x) {
            double xSquared = Math.pow(x, 2.0D);

            for (int z = brushSize; z >= 0; --z) {
                if (xSquared + Math.pow(z, 2.0D) <= brushSizeSquared) {
                    this.performer.perform(this.getEditSession(), blockX + x, blockY, blockZ + z, this.getBlock(blockX + x, blockY, blockZ + z));
                    this.performer.perform(this.getEditSession(), blockX + x, blockY, blockZ - z, this.getBlock(blockX + x, blockY, blockZ - z));
                    this.performer.perform(this.getEditSession(), blockX - x, blockY, blockZ + z, this.getBlock(blockX - x, blockY, blockZ + z));
                    this.performer.perform(this.getEditSession(), blockX - x, blockY, blockZ - z, this.getBlock(blockX - x, blockY, blockZ - z));
                }
            }
        }

    }

    private void discNorthSouth(Snipe snipe, BlockVector3 targetBlock) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        double brushSizeSquared = Math.pow((double) brushSize + this.trueCircle, 2.0D);
        int blockX = targetBlock.getX();
        int blockY = targetBlock.getY();
        int blockZ = targetBlock.getZ();

        for (int x = brushSize; x >= 0; --x) {
            double xSquared = Math.pow(x, 2.0D);

            for (int y = brushSize; y >= 0; --y) {
                if (xSquared + Math.pow(y, 2.0D) <= brushSizeSquared) {
                    this.performer.perform(this.getEditSession(), blockX + x, blockY + y, blockZ, this.getBlock(blockX + x, blockY + y, blockZ));
                    this.performer.perform(this.getEditSession(), blockX + x, blockY - y, blockZ, this.getBlock(blockX + x, blockY - y, blockZ));
                    this.performer.perform(this.getEditSession(), blockX - x, blockY + y, blockZ, this.getBlock(blockX - x, blockY + y, blockZ));
                    this.performer.perform(this.getEditSession(), blockX - x, blockY - y, blockZ, this.getBlock(blockX - x, blockY - y, blockZ));
                }
            }
        }

    }

    private void discEastWest(Snipe snipe, BlockVector3 targetBlock) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        double brushSizeSquared = Math.pow((double) brushSize + this.trueCircle, 2.0D);
        int blockX = targetBlock.getX();
        int blockY = targetBlock.getY();
        int blockZ = targetBlock.getZ();

        for (int x = brushSize; x >= 0; --x) {
            double xSquared = Math.pow(x, 2.0D);

            for (int y = brushSize; y >= 0; --y) {
                if (xSquared + Math.pow(y, 2.0D) <= brushSizeSquared) {
                    this.performer.perform(this.getEditSession(), blockX, blockY + x, blockZ + y, this.getBlock(blockX, blockY + x, blockZ + y));
                    this.performer.perform(this.getEditSession(), blockX, blockY + x, blockZ - y, this.getBlock(blockX, blockY + x, blockZ - y));
                    this.performer.perform(this.getEditSession(), blockX, blockY - x, blockZ + y, this.getBlock(blockX, blockY - x, blockZ + y));
                    this.performer.perform(this.getEditSession(), blockX, blockY - x, blockZ - y, this.getBlock(blockX, blockY - x, blockZ - y));
                }
            }
        }

    }

    private void pre(Snipe snipe, BlockVector3 targetBlock) throws MaxChangedBlocksException {
        BlockVector3 lastBlock = getLastBlock();
        Direction blockFace = getDirection(getTargetBlock(), lastBlock);
        if (blockFace == null) {
            return;
        }
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
                discUpDown(snipe, targetBlock);
                break;
            default:
                break;
        }
    }


    public void sendInfo(Snipe snipe) {
        snipe.createMessageSender().brushNameMessage().brushSizeMessage().send();
    }
}
