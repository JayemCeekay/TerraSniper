package com.jayemceekay.terrasniper.brush.type.performer;

import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;

public class SnipeBrush extends AbstractPerformerBrush {
    public SnipeBrush() {
    }

    @Override
    public void loadProperties() {
    }

    @Override
    public void handleArrowAction(Snipe snipe) {
        printBlockInfo(snipe);
        BlockVector3 targetBlock = getTargetBlock();
        if(this.useSmallBlocks)
        {
            // use AbstractBrush.setBlockData to place an Air-eighth-block instead
            try {
                setBlockData(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), BlockTypes.AIR.getDefaultState());
            } catch (MaxChangedBlocksException var4) {
                var4.printStackTrace();
            }
        }
        else
        {
            try {
                this.performer.perform(getEditSession(), targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), getBlock(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ()));
            } catch (MaxChangedBlocksException var4) {
                var4.printStackTrace();
            }
        }
    }

    @Override
    public void handleGunpowderAction(Snipe snipe) {
        printBlockInfo(snipe);
        BlockVector3 lastBlock = getLastBlock();

        try {
            this.performer.perform(getEditSession(), lastBlock.getX(), lastBlock.getY(), lastBlock.getZ(), getBlock(lastBlock.getX(), lastBlock.getY(), lastBlock.getZ()));
        } catch (MaxChangedBlocksException var4) {
            var4.printStackTrace();
        }

    }

    private void printBlockInfo(Snipe snipe) {
        /*
        SnipeMessenger messenger = snipe.createMessenger();
        BlockState targetBlockState = this.getBlock(this.getTargetBlock());
        String targetId = targetBlockState.toString();
        messenger.sendMessage(ChatFormatting.AQUA + "ID: "+targetId);
        messenger.sendMessage(ChatFormatting.AQUA + "BS: "+targetBlockState);

        messenger.sendMessage(ChatFormatting.AQUA + "Properties: ");
        var properties = targetBlockState.getBlockType().getPropertyMap();
        for (String property : properties.keySet()) {
            messenger.sendMessage(ChatFormatting.BLUE +"  "+property+" = "+properties.get(property));
        }*/
    }
    @Override
    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
    }
}

