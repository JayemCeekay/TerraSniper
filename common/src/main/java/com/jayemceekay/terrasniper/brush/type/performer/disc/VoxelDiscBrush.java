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

public class VoxelDiscBrush extends AbstractPerformerBrush {
    private boolean centerBlock=false;

    public VoxelDiscBrush() {
    }

    public void loadProperties() {
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        int var5 = parameters.length;

        for (String parameter : parameters) {
            if (parameter.equalsIgnoreCase("info")) {
                messenger.sendMessage(ChatFormatting.DARK_GREEN + "/b vd center -- Toggles whether the brush will center on a block or the corner of a block in smallBlocks mode");
                return;
            }

            if (parameter.equalsIgnoreCase("center")) {
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

    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("center"), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("center"), "");
        }
    }

    public void handleArrowAction(Snipe snipe) {
        BlockVector3 targetBlock = this.getTargetBlock();
        try {
            this.disc(snipe, targetBlock);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    public void handleGunpowderAction(Snipe snipe) {
        BlockVector3 lastBlock = this.getLastBlock();
        try {
            this.disc(snipe, lastBlock);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    private void disc(Snipe snipe, BlockVector3 targetBlock) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();

        int blockX = targetBlock.getX();
        int blockY = targetBlock.getY();
        int blockZ = targetBlock.getZ();

        int xOffset = (useSmallBlocks && centerBlock) ? 2*(this.offsetVector.getX()-this.getTargetBlock().getX()) - 1 : 0;
        int zOffset = (useSmallBlocks && centerBlock) ? 2*(this.offsetVector.getZ()-this.getTargetBlock().getZ()) - 1 : 0;

        int xmin = -brushSize - (xOffset== 1 ? 1 : 0);
        int xmax =  brushSize + (xOffset==-1 ? 1 : 0);
        int zmin = -brushSize - (zOffset== 1 ? 1 : 0);
        int zmax =  brushSize + (zOffset==-1 ? 1 : 0);

        for (int x = xmax; x >= xmin; --x) {
            for (int z = zmax; z >= zmin; --z) {
                this.performer.perform(this.getEditSession(), blockX + x, blockY, blockZ + z, this.getBlock(blockX + x, blockY, blockZ + z));
            }
        }
    }

    public void sendInfo(Snipe snipe) {
        snipe.createMessageSender().brushNameMessage().brushSizeMessage().send();
    }
}
