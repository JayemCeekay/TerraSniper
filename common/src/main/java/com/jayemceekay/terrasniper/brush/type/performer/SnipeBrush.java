package com.jayemceekay.terrasniper.brush.type.performer;

import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockTypes;

public class SnipeBrush extends AbstractPerformerBrush {
    public SnipeBrush() {
    }

    @Override
    public void loadProperties() {
    }

    @Override
    public void handleArrowAction(Snipe snipe) {
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
        BlockVector3 lastBlock = getLastBlock();

        try {
            this.performer.perform(getEditSession(), lastBlock.getX(), lastBlock.getY(), lastBlock.getZ(), getBlock(lastBlock.getX(), lastBlock.getY(), lastBlock.getZ()));
        } catch (MaxChangedBlocksException var4) {
            var4.printStackTrace();
        }

    }

    @Override
    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
    }
}

