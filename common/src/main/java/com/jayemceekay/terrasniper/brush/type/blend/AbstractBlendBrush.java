package com.jayemceekay.terrasniper.brush.type.blend;

import com.jayemceekay.terrasniper.brush.type.AbstractBrush;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.material.Materials;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

public abstract class AbstractBlendBrush extends AbstractBrush {
    private boolean airExcluded = true;
    private boolean waterExcluded = true;

    public AbstractBlendBrush() {
    }

    @Override
    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        for (String parameter : parameters) {
            if (parameter.equalsIgnoreCase("water")) {
                this.waterExcluded = !this.waterExcluded;
                messenger.sendMessage(ChatFormatting.AQUA + "Water Mode set to : " + (this.waterExcluded ? "exclude" : "include"));
            } else {
                messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters! Use the \"info\" parameter to display parameter info.");
            }
        }

    }

    @Override
    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("water"), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("water"), "");
        }
    }

    @Override
    public void handleArrowAction(Snipe snipe) {
        this.airExcluded = false;

        try {
            this.blend(snipe);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void handleGunpowderAction(Snipe snipe) {
        this.airExcluded = true;

        try {
            this.blend(snipe);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }

    }

    public abstract void blend(Snipe var1) throws MaxChangedBlocksException;

    protected void setBlocks(Map<BlockVector3, BlockType> blockTypes) throws MaxChangedBlocksException {

        for (Entry<BlockVector3, BlockType> entry : blockTypes.entrySet()) {
            BlockVector3 position = entry.getKey();
            BlockType type = entry.getValue();
            if (this.checkExclusions(type)) {
                BlockType currentBlockType = this.getBlockType(position);
                if (currentBlockType != type) {
                    this.clampY(position);
                }

                this.setBlock(position, type.getDefaultState());
            }
        }

    }

    protected CommonMaterial findCommonMaterial(Map<BlockType, Integer> blockTypesFrequencies) {
        CommonMaterial commonMaterial = new CommonMaterial();

        for (Entry<BlockType, Integer> entry : blockTypesFrequencies.entrySet()) {
            BlockType type = entry.getKey();
            int frequency = entry.getValue();
            if (frequency > commonMaterial.getFrequency() && this.checkExclusions(type)) {
                commonMaterial.setBlockType(type);
                commonMaterial.setFrequency(frequency);
            }
        }

        return commonMaterial;
    }

    private boolean checkExclusions(BlockType type) {
        return (!this.airExcluded || !Materials.isEmpty(type)) && (!this.waterExcluded || type != BlockTypes.WATER);
    }

    public void sendInfo(Snipe snipe) {
        snipe.createMessageSender().brushNameMessage().brushSizeMessage().patternMessage().message(ChatFormatting.BLUE + "Water Mode: " + (this.waterExcluded ? "exclude" : "include")).send();
    }

    public void setAirExcluded(boolean airExcluded) {
        this.airExcluded = airExcluded;
    }
}
