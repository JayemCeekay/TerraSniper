package com.jayemceekay.terrasniper.brush.type.performer;

import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.jayemceekay.terrasniper.util.PlatformAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.world.World;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;


public class LineBrush extends AbstractPerformerBrush {
    private static final Vector3 HALF_BLOCK_OFFSET = Vector3.at(0.5D, 0.5D, 0.5D);
    private Vector3 originCoordinates;
    private Vector3 originCoordinatesFullBlock;
    private Vector3 originOffsetCoordinates;
    private Vector3 targetCoordinates;
    private World targetWorld;

    public LineBrush() {
        setAdditiveBrush(false);
    }

    public void loadProperties() {
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        String firstParameter = parameters[0];
        if (firstParameter.equalsIgnoreCase("info")) {
            messenger.sendMessage(ChatFormatting.GOLD + "Line Brush instructions: Right click first point with the arrow. Right click with gunpowder to draw a line to set the second point.");
        }
    }


    public void handleArrowAction(Snipe snipe) {
        BlockVector3 targetBlock = this.getTargetBlock();
        Player player = snipe.getSniper().getPlayer();
        this.originCoordinatesFullBlock = targetBlock.toVector3();
        this.setOffsetVector(snipe);
        this.originOffsetCoordinates = this.offsetVector.toVector3();
        this.targetWorld = PlatformAdapter.adapt(player.getServer().getLevel(player.level().dimension()));
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendMessage(ChatFormatting.DARK_PURPLE + "First point selected.");
    }

    public void handleGunpowderAction(Snipe snipe) {
        BlockVector3 targetBlock = this.getTargetBlock();
        World world = this.getEditSession().getWorld();
        if (this.originCoordinatesFullBlock != null && world.equals(this.targetWorld)) {
            this.targetCoordinates = targetBlock.toVector3();

            if(this.useSmallBlocks) {
                this.originCoordinates = this.originCoordinatesFullBlock.add(this.originOffsetCoordinates).subtract(this.offsetVector.toVector3());
            }
            else {
                this.originCoordinates = this.originCoordinatesFullBlock;
            }

            try {
                this.lineGunpowder();
            } catch (MaxChangedBlocksException var5) {
                var5.printStackTrace();
            }
        } else {
            SnipeMessenger messenger = snipe.createMessenger();
            messenger.sendMessage(ChatFormatting.RED + "Warning: You did not select a first coordinate with the arrow");
        }

    }

    private void lineGunpowder() throws MaxChangedBlocksException {
        Vector3 originClone = this.originCoordinates.add(HALF_BLOCK_OFFSET);
        Vector3 targetClone = this.targetCoordinates.add(HALF_BLOCK_OFFSET);
        double length = this.targetCoordinates.distance(this.originCoordinates);
        if (length == 0.0D) {
            this.performer.perform(this.getEditSession(), this.targetCoordinates.toBlockPoint().getBlockX(), this.targetCoordinates.toBlockPoint().getBlockY(), this.targetCoordinates.toBlockPoint().getBlockZ(), this.getBlock(this.targetCoordinates.toBlockPoint().getBlockX(), this.targetCoordinates.toBlockPoint().getBlockY(), this.targetCoordinates.toBlockPoint().getBlockZ()));
        } else {
            Vector3 direction = targetClone.subtract(originClone);
            double distance = 0.0D;

            for (double increment = 0.25D; distance < direction.length(); distance += increment) {
                BlockVector3 currentBlock = originClone.add(direction.normalize().multiply(distance)).toBlockPoint();
                this.performer.perform(this.getEditSession(), currentBlock.getX(), currentBlock.getY(), currentBlock.getZ(), this.getBlock(currentBlock.getX(), currentBlock.getY(), currentBlock.getZ()));
            }
        }

    }

    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
    }
}
