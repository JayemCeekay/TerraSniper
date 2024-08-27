package com.jayemceekay.terrasniper.brush.type.performer;

import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.material.MaterialSets;
import com.jayemceekay.terrasniper.util.material.Materials;
import com.jayemceekay.terrasniper.util.text.NumericParser;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockType;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class OverlayBrush extends AbstractPerformerBrush {
    private boolean allBlocks = false;
    private int depth = 3;

    public OverlayBrush() {
    }

    public void loadProperties() {
        this.depth = 3;
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        int var5 = parameters.length;

        for (String parameter : parameters) {
            if (parameter.equalsIgnoreCase("info")) {
                messenger.sendMessage(ChatFormatting.GOLD + "Overlay Brush Parameters:");
                messenger.sendMessage(ChatFormatting.BLUE + "/b over all -- Sets the brush to overlay over ALL materials, not just natural surface ones (will no longer ignore trees and buildings).");
                messenger.sendMessage(ChatFormatting.BLUE + "/b over some -- Sets the brush to overlay over natural surface materials.");
                messenger.sendMessage(ChatFormatting.AQUA + "/b over d[n] -- Sets how many blocks deep you want to replace from the surface to n.");
                return;
            }

            if (parameter.equalsIgnoreCase("all")) {
                this.allBlocks = true;
                messenger.sendMessage(ChatFormatting.BLUE + "Will overlay over any block: " + this.depth);
            } else if (parameter.equalsIgnoreCase("some")) {
                this.allBlocks = false;
                messenger.sendMessage(ChatFormatting.BLUE + "Will overlay only natural block types: " + this.depth);
            } else if (parameter.startsWith("d[")) {
                Integer depth = NumericParser.parseInteger(parameter.replace("d[", "").replace("]", ""));
                if (depth != null) {
                    this.depth = depth < 1 ? 1 : depth;
                    messenger.sendMessage(ChatFormatting.AQUA + "Depth set to: " + this.depth);
                } else {
                    messenger.sendMessage(ChatFormatting.RED + "Invalid number.");
                }
            } else {
                messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters! Use the \"info\" parameter to display parameter info.");
            }
        }
    }

    @Override
    public HashMap<String, String> getSettings() {
        this.settings.put("Depth", String.valueOf(this.depth));
        this.settings.put("All Blocks", String.valueOf(this.allBlocks));
        return super.getSettings();
    }

    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("all", "some", "d["), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("all", "some", "d["), "");
        }
    }

    public void handleArrowAction(Snipe snipe) {
        this.overlay(snipe);
    }

    public void handleGunpowderAction(Snipe snipe) {
        this.overlayTwo(snipe);
    }

    private void overlay(Snipe snipe) {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        EditSession editSession = this.getEditSession();
        int brushSize = toolkitProperties.getBrushSize();
        double brushSizeSquared = Math.pow((double) brushSize + 0.5D, 2.0D);

        for (int z = brushSize; z >= -brushSize; --z) {
            label51:
            for (int x = brushSize; x >= -brushSize; --x) {
                BlockVector3 targetBlock = this.getTargetBlock();
                int blockX = targetBlock.getX();
                int blockY = targetBlock.getY();
                int blockZ = targetBlock.getZ();
                BlockType type = this.getBlockType(blockX + x, blockY + 1, blockZ + z);
                if (this.isIgnoredBlock(type) && Math.pow(x, 2.0D) + Math.pow(z, 2.0D) <= brushSizeSquared) {
                    for (int y = blockY; y >= editSession.getMinimumPoint().getY(); --y) {
                        BlockType layerBlockType = this.getBlockType(blockX + x, y, blockZ + z);
                        if (!this.isIgnoredBlock(layerBlockType)) {
                            int currentDepth = y;

                            while (true) {
                                if (y - currentDepth >= this.depth) {
                                    continue label51;
                                }

                                BlockType currentBlockType = this.getBlockType(blockX + x, currentDepth, blockZ + z);
                                if (this.isOverrideableMaterial(currentBlockType)) {
                                    try {
                                        this.performer.perform(this.getEditSession(), blockX + x, this.clampY(currentDepth), blockZ + z, this.clampY(blockX + x, currentDepth, blockZ + z));
                                    } catch (MaxChangedBlocksException var19) {
                                        var19.printStackTrace();
                                    }
                                }

                                --currentDepth;
                            }
                        }
                    }
                }
            }
        }

    }

    private boolean isIgnoredBlock(BlockType type) {
        return type.getMaterial().isLiquid() || type.getMaterial().isAir() || !type.getMaterial().isMovementBlocker();
    }

    private boolean isOverrideableMaterial(BlockType type) {
        return this.allBlocks && !Materials.isEmpty(type) || MaterialSets.OVERRIDEABLE.contains(type);
    }

    private void overlayTwo(Snipe snipe) {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        EditSession editSession = this.getEditSession();
        int brushSize = toolkitProperties.getBrushSize();
        double brushSizeSquared = Math.pow((double) brushSize + 0.5D, 2.0D);
        int[][] memory = new int[brushSize * 2 + 1][brushSize * 2 + 1];

        for (int z = brushSize; z >= -brushSize; --z) {
            for (int x = brushSize; x >= -brushSize; --x) {
                boolean surfaceFound = false;
                BlockVector3 targetBlock = this.getTargetBlock();
                int blockX = targetBlock.getX();
                int blockY = targetBlock.getY();
                int blockZ = targetBlock.getZ();

                for (int y = blockY; y >= editSession.getMinimumPoint().getY() && !surfaceFound; --y) {
                    if (memory[x + brushSize][z + brushSize] != 1 && Math.pow(x, 2.0D) + Math.pow(z, 2.0D) <= brushSizeSquared && !Materials.isEmpty(this.getBlockType(blockX + x, y - 1, blockZ + z)) && Materials.isEmpty(this.getBlockType(blockX + x, y + 1, blockZ + z))) {
                        if (this.allBlocks) {
                            for (int index = 1; index < this.depth + 1; ++index) {
                                try {
                                    this.performer.perform(this.getEditSession(), blockX + x, this.clampY(y + index), blockZ + z, this.clampY(blockX + x, y + index, blockZ + z));
                                } catch (MaxChangedBlocksException var19) {
                                    var19.printStackTrace();
                                }

                                memory[x + brushSize][z + brushSize] = 1;
                            }

                            surfaceFound = true;
                        } else {
                            BlockType type = this.getBlockType(blockX + x, y, blockZ + z);
                            if (MaterialSets.OVERRIDEABLE_WITH_ORES.contains(type)) {
                                for (int index = 1; index < this.depth + 1; ++index) {
                                    try {
                                        this.performer.perform(this.getEditSession(), blockX + x, this.clampY(y + index), blockZ + z, this.clampY(blockX + x, y + index, blockZ + z));
                                    } catch (MaxChangedBlocksException var20) {
                                        var20.printStackTrace();
                                    }

                                    memory[x + brushSize][z + brushSize] = 1;
                                }

                                surfaceFound = true;
                            }
                        }
                    }
                }
            }
        }

    }

    public void sendInfo(Snipe snipe) {
        snipe.createMessageSender().brushNameMessage().brushSizeMessage().send();
    }
}
