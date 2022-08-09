package com.jayemceekay.TerraSniper.brush.type.performer;

import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.TerraSniper.sniper.snipe.Snipe;
import com.jayemceekay.TerraSniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.TerraSniper.util.material.Materials;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockType;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.List;
import java.util.stream.Stream;

public class FillDownBrush extends AbstractPerformerBrush {
    private double trueCircle;
    private boolean fillLiquid = true;
    private boolean fromExisting;

    public FillDownBrush() {
    }

    public void loadProperties() {
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        int var5 = parameters.length;

        for (String parameter : parameters) {
            if (parameter.equalsIgnoreCase("info")) {
                messenger.sendMessage(ChatFormatting.GOLD + "Fill Down Brush Parameters:");
                messenger.sendMessage(ChatFormatting.AQUA + "/b fd [true|false] -- Uses a true circle algorithm. Default is false.");
                messenger.sendMessage(ChatFormatting.AQUA + "/b fd all -- Fills into liquids as well. (Default)");
                messenger.sendMessage(ChatFormatting.AQUA + "/b fd some -- Fills only into air.");
                messenger.sendMessage(ChatFormatting.AQUA + "/b fd e -- Fills into only existing blocks. (Toggle)");
                return;
            }

            if (parameter.equalsIgnoreCase("true")) {
                this.trueCircle = 0.5D;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode ON.");
            } else if (parameter.equalsIgnoreCase("false")) {
                this.trueCircle = 0.0D;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode OFF.");
            } else if (parameter.equalsIgnoreCase("all")) {
                this.fillLiquid = true;
                messenger.sendMessage(ChatFormatting.AQUA + "Now filling liquids as well as air.");
            } else if (parameter.equalsIgnoreCase("some")) {
                this.fillLiquid = false;
                ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
                toolkitProperties.resetReplacePattern();
                messenger.sendMessage(ChatFormatting.AQUA + "Now only filling air.");
            } else if (parameter.equalsIgnoreCase("e")) {
                this.fromExisting = !this.fromExisting;
                messenger.sendMessage(ChatFormatting.AQUA + "Now filling down from " + (this.fromExisting ? "existing" : "all") + " blocks.");
            } else {
                messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters! Use the \"info\" parameter to display parameter info.");
            }
        }

    }

    public List<String> handleCompletions(String[] parameters) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("true", "false", "some", "all", "e"), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("true", "false", "some", "all", "e"), "");
        }
    }

    public void handleArrowAction(Snipe snipe) {
        try {
            this.fillDown(snipe);
        } catch (MaxChangedBlocksException var3) {
            var3.printStackTrace();
        }

    }

    public void handleGunpowderAction(Snipe snipe) {
        try {
            this.fillDown(snipe);
        } catch (MaxChangedBlocksException var3) {
            var3.printStackTrace();
        }

    }

    private void fillDown(Snipe snipe) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        double brushSizeSquared = Math.pow((double) brushSize + this.trueCircle, 2.0D);
        BlockVector3 targetBlock = this.getTargetBlock();

        for (int x = -brushSize; x <= brushSize; ++x) {
            double currentXSquared = Math.pow(x, 2.0D);

            for (int z = -brushSize; z <= brushSize; ++z) {
                if (currentXSquared + Math.pow(z, 2.0D) <= brushSizeSquared) {
                    int y = 0;
                    if (this.fromExisting) {
                        boolean found = false;

                        for (y = -toolkitProperties.getVoxelHeight(); y < toolkitProperties.getVoxelHeight(); ++y) {
                            BlockType currentBlockType = this.getBlockType(targetBlock.getX() + x, targetBlock.getY() + y, targetBlock.getZ() + z);
                            if (!Materials.isEmpty(currentBlockType)) {
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            continue;
                        }

                        --y;
                    }

                    while (y >= -targetBlock.getY()) {
                        BlockType currentBlockType = this.getBlockType(targetBlock.getX() + x, targetBlock.getY() + y, targetBlock.getZ() + z);
                        if (!Materials.isEmpty(currentBlockType) && (!this.fillLiquid || !Materials.isLiquid(currentBlockType))) {
                            break;
                        }

                        this.performer.perform(this.getEditSession(), targetBlock.getX() + x, targetBlock.getY() + y, targetBlock.getZ() + z, this.getBlock(targetBlock.getX() + x, targetBlock.getY() + y, targetBlock.getZ() + z));
                        --y;
                    }
                }
            }
        }

    }

    public void sendInfo(Snipe snipe) {
        snipe.createMessageSender().brushNameMessage().brushSizeMessage().send();
    }
}
