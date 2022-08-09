package com.jayemceekay.TerraSniper.brush.type.blend;

import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.TerraSniper.sniper.snipe.Snipe;
import com.jayemceekay.TerraSniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.TerraSniper.util.math.MathHelper;
import com.jayemceekay.TerraSniper.util.painter.Painters;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockType;
import net.minecraft.ChatFormatting;

import java.util.*;

public class BlendDiscBrush extends AbstractBlendBrush {
    public BlendDiscBrush() {
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        int var5 = parameters.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            String parameter = parameters[var6];
            if (parameter.equalsIgnoreCase("info")) {
                messenger.sendMessage(ChatFormatting.GOLD + "Blend Disc Brush Parameters:");
                messenger.sendMessage(ChatFormatting.AQUA + "/b bd water -- Toggles include or exclude (default) water.");
                return;
            }
        }

        super.handleCommand(parameters, snipe);
    }

    public void blend(Snipe snipe) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        BlockVector3 targetBlock = this.getTargetBlock();
        int smallCircleArea = (int) MathHelper.circleArea(brushSize);
        Set<BlockVector3> smallCircle = new HashSet(smallCircleArea);
        Map<BlockVector3, BlockType> smallCircleBlockTypes = new HashMap(smallCircleArea);
        Painters.circle().center(targetBlock).radius(brushSize).blockSetter((position) -> {
            BlockType type = this.getBlockType(position);
            smallCircle.add(position);
            smallCircleBlockTypes.put(position, type);
        }).paint();
        Iterator var8 = smallCircle.iterator();

        while (var8.hasNext()) {
            BlockVector3 smallCircleBlock = (BlockVector3) var8.next();
            Map<BlockType, Integer> blockTypesFrequencies = new HashMap();
            Painters.square().center(smallCircleBlock).radius(1).blockSetter((position) -> {
                if (!position.equals(smallCircleBlock)) {
                    BlockType type = this.getBlockType(position);
                    blockTypesFrequencies.merge(type, 1, Integer::sum);
                }
            }).paint();
            CommonMaterial commonMaterial = this.findCommonMaterial(blockTypesFrequencies);
            BlockType type = commonMaterial.getBlockType();
            if (type != null) {
                smallCircleBlockTypes.put(smallCircleBlock, type);
            }
        }

        this.setBlocks(smallCircleBlockTypes);
    }
}
