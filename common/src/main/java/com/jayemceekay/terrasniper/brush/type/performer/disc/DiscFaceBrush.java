package com.jayemceekay.terrasniper.brush.type.performer.disc;

import com.jayemceekay.terrasniper.brush.type.performer.AbstractPerformerBrush;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Direction;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.List;
import java.util.stream.Stream;

public class DiscFaceBrush extends AbstractPerformerBrush {
    private double trueCircle;
    private boolean centerBlock=false;

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
                messenger.sendMessage(ChatFormatting.DARK_GREEN + "/b df center -- Toggles whether the brush will center on a block or the corner of a block in smallBlocks mode");
                return;
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
                    messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters length! Use the \"info\" parameter to display parameter info.");
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

        int sizeOffset = (useSmallBlocks && centerBlock) ? 1 : 0;
        int xOffset = (useSmallBlocks && centerBlock) ? 2*(this.offsetVector.getX()-this.getTargetBlock().getX()) - 1 : 0;
        int zOffset = (useSmallBlocks && centerBlock) ? 2*(this.offsetVector.getZ()-this.getTargetBlock().getZ()) - 1 : 0;
        Vector3 offsetVec3 = Vector3.at(xOffset/2.0D,0,zOffset/2.0D);

        double brushSizeSquared = Math.pow((double) brushSize + this.trueCircle + sizeOffset/2.0D, 2.0D);
        int blockX = targetBlock.getX();
        int blockY = targetBlock.getY();
        int blockZ = targetBlock.getZ();

        for (int x = brushSize+sizeOffset; x >= 0; --x) {
            double xSquared = Math.pow(x+xOffset/2.0D, 2.0D);

            for (int z = brushSize+sizeOffset; z >= 0; --z) {
                if (xSquared + Math.pow(z+zOffset/2.0D, 2.0D) <= brushSizeSquared) {
                    this.performer.perform(this.getEditSession(), blockX + x, blockY, blockZ + z, this.getBlock(blockX + x, blockY, blockZ + z));
                    this.performer.perform(this.getEditSession(), blockX + x, blockY, blockZ - z - zOffset, this.getBlock(blockX + x, blockY, blockZ - z - zOffset));
                    this.performer.perform(this.getEditSession(), blockX - x - xOffset, blockY, blockZ + z, this.getBlock(blockX - x - xOffset, blockY, blockZ + z));
                    this.performer.perform(this.getEditSession(), blockX - x - xOffset, blockY, blockZ - z - zOffset, this.getBlock(blockX - x - xOffset, blockY, blockZ - z - zOffset));
                }
            }
        }

    }

    private void discNorthSouth(Snipe snipe, BlockVector3 targetBlock) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();

        int sizeOffset = (useSmallBlocks && centerBlock) ? 1 : 0;
        int xOffset = (useSmallBlocks && centerBlock) ? 2*(this.offsetVector.getX()-this.getTargetBlock().getX()) - 1 : 0;
        int yOffset = (useSmallBlocks && centerBlock) ? 2*(this.offsetVector.getY()-this.getTargetBlock().getY()) - 1 : 0;
        Vector3 offsetVec3 = Vector3.at(xOffset/2.0D,yOffset/2.0D,0);

        double brushSizeSquared = Math.pow((double) brushSize + this.trueCircle + sizeOffset/2.0D, 2.0D);
        int blockX = targetBlock.getX();
        int blockY = targetBlock.getY();
        int blockZ = targetBlock.getZ();

        for (int x = brushSize+sizeOffset; x >= 0; --x) {
            double xSquared = Math.pow(x+xOffset, 2.0D);

            for (int y = brushSize+sizeOffset; y >= 0; --y) {
                if (xSquared + Math.pow(y+yOffset, 2.0D) <= brushSizeSquared) {
                    this.performer.perform(this.getEditSession(), blockX + x, blockY + y, blockZ, this.getBlock(blockX + x, blockY + y, blockZ));
                    this.performer.perform(this.getEditSession(), blockX + x, blockY - y - yOffset, blockZ, this.getBlock(blockX + x, blockY - y - yOffset, blockZ));
                    this.performer.perform(this.getEditSession(), blockX - x - xOffset, blockY + y, blockZ, this.getBlock(blockX - x - xOffset, blockY + y, blockZ));
                    this.performer.perform(this.getEditSession(), blockX - x - xOffset, blockY - y - yOffset, blockZ, this.getBlock(blockX - x - xOffset, blockY - y - yOffset, blockZ));
                }
            }
        }

    }

    private void discEastWest(Snipe snipe, BlockVector3 targetBlock) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();

        int sizeOffset = (useSmallBlocks && centerBlock) ? 1 : 0;
        int yOffset = (useSmallBlocks && centerBlock) ? 2*(this.offsetVector.getY()-this.getTargetBlock().getY()) - 1 : 0;
        int zOffset = (useSmallBlocks && centerBlock) ? 2*(this.offsetVector.getZ()-this.getTargetBlock().getZ()) - 1 : 0;
        Vector3 offsetVec3 = Vector3.at(0,yOffset/2.0D,zOffset/2.0D);

        double brushSizeSquared = Math.pow((double) brushSize + this.trueCircle + sizeOffset/2.0D, 2.0D);
        int blockX = targetBlock.getX();
        int blockY = targetBlock.getY();
        int blockZ = targetBlock.getZ();

        for (int y = brushSize+sizeOffset; y >= 0; --y) {
            double ySquared = Math.pow(y+yOffset, 2.0D);

            for (int z = brushSize+sizeOffset; z >= 0; --z) {
                if (ySquared + Math.pow(z+zOffset, 2.0D) <= brushSizeSquared) {
                    this.performer.perform(this.getEditSession(), blockX, blockY + y, blockZ + z, this.getBlock(blockX, blockY + y, blockZ + z));
                    this.performer.perform(this.getEditSession(), blockX, blockY + y, blockZ - z - zOffset, this.getBlock(blockX, blockY + y, blockZ - z - zOffset));
                    this.performer.perform(this.getEditSession(), blockX, blockY - y - yOffset, blockZ + z, this.getBlock(blockX, blockY - y - yOffset, blockZ + z));
                    this.performer.perform(this.getEditSession(), blockX, blockY - y - yOffset, blockZ - z - zOffset, this.getBlock(blockX, blockY - y - yOffset, blockZ - z - zOffset));
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
