package com.jayemceekay.terrasniper.brush.type;

import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.material.Materials;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.List;
import java.util.stream.Stream;

public class CopyPastaBrush extends AbstractBrush {
    private static final int BLOCK_LIMIT = 1000000;
    private final int[] pastePoint = new int[3];
    private final int[] minPoint = new int[3];
    private final int[] offsetPoint = new int[3];
    private final int[] arraySize = new int[3];
    private int blockLimit;
    private boolean pasteAir = true;
    private int points;
    private int numBlocks;
    private int[] firstPoint = new int[3];
    private int[] secondPoint = new int[3];
    private int[] firstPointFullBlock = new int[3];
    private int[] secondPointFullBlock = new int[3];
    private int[] firstOffsetPoint = new int[3];
    private int[] secondOffsetPoint = new int[3];
    private BlockType[] blockArray;
    private BlockState[] dataArray;
    private int pivot;

    public CopyPastaBrush() {
        setCanUseSmallBlocks(true);
        setAdditiveBrush(false);
    }

    public void loadProperties() {
        this.blockLimit = BLOCK_LIMIT;
    }

    @Override
    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        String firstParameter = parameters[0];

        if (firstParameter.equalsIgnoreCase("info")) {
            messenger.sendMessage(ChatFormatting.GOLD + "CopyPasta Brush Parameters:");
            messenger.sendMessage(ChatFormatting.AQUA + "/b cp air -- Toggles include (default) or exclude air during paste.");
            messenger.sendMessage(ChatFormatting.AQUA + "/b cp [0|90|180|270] -- Toggles rotation (0 default)");
        } else {
            if (parameters.length == 1) {
                if (firstParameter.equalsIgnoreCase("air")) {
                    this.pasteAir = !this.pasteAir;
                    messenger.sendMessage(ChatFormatting.GOLD + "Paste air set to: " + this.pasteAir);
                } else if (Stream.of("0", "90", "180", "270")
                        .anyMatch(firstParameter::equalsIgnoreCase)) {
                    this.pivot = Integer.parseInt(firstParameter);
                    messenger.sendMessage(ChatFormatting.GOLD + "Pivot angle set to: " + this.pivot);
                } else {
                    messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters! Use the \"info\" parameter to display parameter info.");
                }
            } else {
                messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters length! Use the \"info\" parameter to display parameter " +
                        "info.");
            }
        }
    }


    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("air", "90", "180", "270", "0"), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("air", "90", "180", "270", "0"), "");
        }
    }

    public void handleArrowAction(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        BlockVector3 targetBlock = this.getTargetBlock();
        if (this.points == 0) {
            this.firstPointFullBlock[0] = targetBlock.getX();
            this.firstPointFullBlock[1] = targetBlock.getY();
            this.firstPointFullBlock[2] = targetBlock.getZ();
            this.setOffsetVector(snipe);
            this.firstOffsetPoint[0] = this.offsetVector.getX();
            this.firstOffsetPoint[1] = this.offsetVector.getY();
            this.firstOffsetPoint[2] = this.offsetVector.getZ();
            messenger.sendMessage(ChatFormatting.GRAY + "First point");
            this.points = 1;
        } else if (this.points == 1) {
            this.secondPointFullBlock[0] = targetBlock.getX();
            this.secondPointFullBlock[1] = targetBlock.getY();
            this.secondPointFullBlock[2] = targetBlock.getZ();
            this.setOffsetVector(snipe);
            this.secondOffsetPoint[0] = this.offsetVector.getX();
            this.secondOffsetPoint[1] = this.offsetVector.getY();
            this.secondOffsetPoint[2] = this.offsetVector.getZ();
            messenger.sendMessage(ChatFormatting.GRAY + "Second point");
            this.points = 2;
        } else {
            this.firstPoint = new int[3];
            this.secondPoint = new int[3];
            this.firstPointFullBlock = new int[3];
            this.secondPointFullBlock = new int[3];
            this.firstOffsetPoint = new int[3];
            this.secondOffsetPoint = new int[3];
            this.numBlocks = 0;
            this.blockArray = new BlockType[1];
            this.dataArray = new BlockState[1];
            this.points = 0;
            messenger.sendMessage(ChatFormatting.GRAY + "Points cleared.");
        }

    }

    public void handleGunpowderAction(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        if (this.points == 2) {
            if (this.numBlocks == 0) {
                if(this.useSmallBlocks) {
                    this.firstPoint[0]  = this.firstPointFullBlock[0]  + this.firstOffsetPoint[0]  - this.offsetVector.getX();
                    this.firstPoint[1]  = this.firstPointFullBlock[1]  + this.firstOffsetPoint[1]  - this.offsetVector.getY();
                    this.firstPoint[2]  = this.firstPointFullBlock[2]  + this.firstOffsetPoint[2]  - this.offsetVector.getZ();
                    this.secondPoint[0] = this.secondPointFullBlock[0] + this.secondOffsetPoint[0] - this.offsetVector.getX();
                    this.secondPoint[1] = this.secondPointFullBlock[1] + this.secondOffsetPoint[1] - this.offsetVector.getY();
                    this.secondPoint[2] = this.secondPointFullBlock[2] + this.secondOffsetPoint[2] - this.offsetVector.getZ();
                }
                else {
                    this.firstPoint = this.firstPointFullBlock;
                    this.secondPoint = this.secondPointFullBlock;
                }

                this.doCopy(snipe);

            } else if (this.numBlocks > 0 && this.numBlocks < this.blockLimit) {
                BlockVector3 targetBlock = this.getTargetBlock();
                this.pastePoint[0] = targetBlock.getX();
                this.pastePoint[1] = targetBlock.getY();
                this.pastePoint[2] = targetBlock.getZ();
                this.doPasta(snipe);
            } else {
                messenger.sendMessage(ChatFormatting.RED + "Error");
            }
        } else {
            messenger.sendMessage(ChatFormatting.RED + "You must select exactly two points.");
        }

    }

    private void doCopy(Snipe snipe) {
        for (int i = 0; i < 3; ++i) {
            this.arraySize[i] = Math.abs(this.firstPoint[i] - this.secondPoint[i]) + 1;
            this.minPoint[i] = Math.min(this.firstPoint[i], this.secondPoint[i]);
            this.offsetPoint[i] = this.minPoint[i] - this.firstPoint[i];
        }

        this.numBlocks = this.arraySize[0] * this.arraySize[1] * this.arraySize[2];
        SnipeMessenger messenger = snipe.createMessenger();
        if (this.numBlocks > 0 && this.numBlocks < this.blockLimit) {
            this.blockArray = new BlockType[this.numBlocks];
            this.dataArray = new BlockState[this.numBlocks];

            for (int i = 0; i < this.arraySize[0]; ++i) {
                for (int j = 0; j < this.arraySize[1]; ++j) {
                    for (int k = 0; k < this.arraySize[2]; ++k) {
                        int currentPosition = i + this.arraySize[0] * j + this.arraySize[0] * this.arraySize[1] * k;
                        BlockState block = this.getBlock(this.minPoint[0] + i, this.minPoint[1] + j, this.minPoint[2] + k);
                        this.blockArray[currentPosition] = block.getBlockType();
                        BlockState clamp = this.clampY(this.minPoint[0] + i, this.minPoint[1] + j, this.minPoint[2] + k);
                        this.dataArray[currentPosition] = clamp;
                    }
                }
            }

            messenger.sendMessage(ChatFormatting.AQUA + String.valueOf(this.numBlocks) + " blocks copied.");
        } else {
            messenger.sendMessage(ChatFormatting.RED + "Copy area too big: " + this.numBlocks + "(Limit: " + this.blockLimit + ")");
        }

    }

    private void doPasta(Snipe snipe) {
        for (int i = 0; i < this.arraySize[0]; ++i) {
            for (int j = 0; j < this.arraySize[1]; ++j) {
                for (int k = 0; k < this.arraySize[2]; ++k) {
                    int currentPosition = i + this.arraySize[0] * j + this.arraySize[0] * this.arraySize[1] * k;
                    int x;
                    int y;
                    int z;
                    switch (this.pivot) {
                        case 90:
                            x = this.pastePoint[0] - this.offsetPoint[2] - k;
                            y = this.pastePoint[1] + this.offsetPoint[1] + j;
                            z = this.pastePoint[2] + this.offsetPoint[0] + i;
                            break;
                        case 180:
                            x = this.pastePoint[0] - this.offsetPoint[0] - i;
                            y = this.pastePoint[1] + this.offsetPoint[1] + j;
                            z = this.pastePoint[2] - this.offsetPoint[2] - k;
                            break;
                        case 270:
                            x = this.pastePoint[0] + this.offsetPoint[2] + k;
                            y = this.pastePoint[1] + this.offsetPoint[1] + j;
                            z = this.pastePoint[2] - this.offsetPoint[0] - i;
                            break;
                        default:
                            x = this.pastePoint[0] + this.offsetPoint[0] + i;
                            y = this.pastePoint[1] + this.offsetPoint[1] + j;
                            z = this.pastePoint[2] + this.offsetPoint[2] + k;
                    }

                    if (!Materials.isEmpty(this.blockArray[currentPosition]) || this.pasteAir) {
                        try {
                            this.setBlockData(BlockVector3.at(x, y, z), this.dataArray[currentPosition]);
                        } catch (MaxChangedBlocksException var10) {
                            var10.printStackTrace();
                        }
                    }
                }
            }
        }

        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendMessage(ChatFormatting.AQUA + String.valueOf(this.numBlocks) + " blocks pasted.");
    }

    public void sendInfo(Snipe snipe) {
        snipe.createMessageSender().brushNameMessage().message(ChatFormatting.GOLD + "Paste air: " + this.pasteAir).message(ChatFormatting.GOLD + "Pivot angle: " + this.pivot).send();
    }
}
