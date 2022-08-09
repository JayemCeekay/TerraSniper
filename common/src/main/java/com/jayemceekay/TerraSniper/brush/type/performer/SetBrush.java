package com.jayemceekay.TerraSniper.brush.type.performer;

import com.jayemceekay.TerraSniper.sniper.snipe.Snipe;
import com.jayemceekay.TerraSniper.sniper.snipe.message.SnipeMessenger;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import net.minecraft.ChatFormatting;

import javax.annotation.Nullable;

public class SetBrush extends AbstractPerformerBrush {
    private static final int SELECTION_SIZE_MAX = 5000000;
    @Nullable
    private BlockVector3 block;
    private World world;
    private int selectionSizeMax;

    public SetBrush() {
    }

    public void loadProperties() {
        this.selectionSizeMax = 5000000;
    }

    public void handleArrowAction(Snipe snipe) {
        BlockVector3 targetBlock = this.getTargetBlock();

        try {
            if (this.set(targetBlock, this.getEditSession().getWorld(), snipe)) {
                SnipeMessenger messenger = snipe.createMessenger();
                messenger.sendMessage(ChatFormatting.GRAY + "Point one");
            }
        } catch (MaxChangedBlocksException var4) {
            var4.printStackTrace();
        }

    }

    public void handleGunpowderAction(Snipe snipe) {
        BlockVector3 lastBlock = this.getLastBlock();

        try {
            if (this.set(lastBlock, this.getEditSession().getWorld(), snipe)) {
                SnipeMessenger messenger = snipe.createMessenger();
                messenger.sendMessage(ChatFormatting.GRAY + "Point one");
            }
        } catch (MaxChangedBlocksException var4) {
            var4.printStackTrace();
        }

    }

    private boolean set(BlockVector3 block, World world, Snipe snipe) throws MaxChangedBlocksException {
        if (this.block == null) {
            this.block = block;
            this.world = world;
            return true;
        } else {
            SnipeMessenger messenger = snipe.createMessenger();
            String name = this.world.getName();
            String parameterBlockWorldName = world.getName();
            if (!name.equals(parameterBlockWorldName)) {
                messenger.sendMessage(ChatFormatting.RED + "You selected points in different worlds!");
                this.block = null;
                return true;
            } else {
                int x1 = this.block.getX();
                int x2 = block.getX();
                int y1 = this.block.getY();
                int y2 = block.getY();
                int z1 = this.block.getZ();
                int z2 = block.getZ();
                int lowX = Math.min(x1, x2);
                int lowY = Math.min(y1, y2);
                int lowZ = Math.min(z1, z2);
                int highX = Math.max(x1, x2);
                int highY = Math.max(y1, y2);
                int highZ = Math.max(z1, z2);
                if (Math.abs(highX - lowX) * Math.abs(highZ - lowZ) * Math.abs(highY - lowY) > this.selectionSizeMax) {
                    messenger.sendMessage(ChatFormatting.RED + "Selection size above " + this.selectionSizeMax + " limit, please use a smaller selection.");
                } else {
                    for (int y = lowY; y <= highY; ++y) {
                        for (int x = lowX; x <= highX; ++x) {
                            for (int z = lowZ; z <= highZ; ++z) {
                                this.performer.perform(this.getEditSession(), x, this.clampY(y), z, this.clampY(x, y, z));
                            }
                        }
                    }
                }

                this.block = null;
                return false;
            }
        }
    }

    public void sendInfo(Snipe snipe) {
        this.block = null;
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
    }
}
