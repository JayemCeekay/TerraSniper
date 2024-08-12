package com.jayemceekay.terrasniper.brush.type;

import com.jayemceekay.terrasniper.sniper.Sniper;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.PlatformAdapter;
import com.jayemceekay.terrasniper.util.material.Materials;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class WaterLevelBrush extends AbstractBrush {

    public WaterLevelBrush() {
        setCanUseSmallBlocks(false);
    }

    @Override
    public void handleArrowAction(Snipe snipe) {
        Sniper sniper = snipe.getSniper();
        Player player = sniper.getPlayer();

        // the exact point on the hitbox of the block the player is looking at
        Vector3 location = PlatformAdapter.adapt(player.level().clip(new ClipContext(player.getEyePosition(1.0F), player.getEyePosition(1.0F).add(player.getLookAngle().scale((double) sniper.getCurrentToolkit().getProperties().getBlockTracerRange())), ClipContext.Block.OUTLINE, ClipContext.Fluid.WATER, player)).getLocation());

        // a normal vector to the surface (i.e. pointing to the outside of the targeted sub-block)
        Vector3 normal = PlatformAdapter.adapt(new Vec3(player.level().clip(new ClipContext(player.getEyePosition(1.0F), player.getEyePosition(1.0F).add(player.getLookAngle().scale((double) sniper.getCurrentToolkit().getProperties().getBlockTracerRange())), ClipContext.Block.OUTLINE, ClipContext.Fluid.WATER, player)).getDirection().step()));

        Vector3 targetLocation = location.subtract(normal.multiply(1./1024.));
        Vector3 lastLocation = location.add(normal.multiply(1023./1024.));

        BlockVector3 targetBlock = BlockVector3.at((int) Math.floor(targetLocation.getX()), (int) Math.floor(targetLocation.getY()), (int) Math.floor(targetLocation.getZ()));
        BlockVector3 lastBlock = BlockVector3.at((int) Math.floor(lastLocation.getX()), (int) Math.floor(lastLocation.getY()), (int) Math.floor(lastLocation.getZ()));

        BlockState blockState = getBlock(targetBlock);
        String blockId = blockState.getBlockType().getId();
        boolean waterlogged=false, waterloggable=false;
        int level=0;
        for(int i=0; i<2; i++) {
            for (Map.Entry<Property<?>, Object> entry : blockState.getStates().entrySet()) {
                String propertyName = entry.getKey().getName();
                Object propertyValue = entry.getValue();
                if (propertyName.equals("waterlogged")) {
                    waterlogged = (boolean) propertyValue;
                    waterloggable = true;
                }
                if (propertyName.equals("level")) {
                    level = (int) propertyValue;
                }
            }

            if (i==1 || waterloggable || blockId.equals("minecraft:water")) {break;}
            targetBlock = lastBlock;
            blockState = getBlock(targetBlock);
            blockId = blockState.getBlockType().getId();
            waterlogged = false; waterloggable = false; level=0;
        }

        var properties = blockState.getBlockType().getPropertyMap();

        boolean place_above=false;
        if(waterloggable)
        {
            if(waterlogged) {
                place_above=true;
            }
            else {
                blockState = blockState.with((BooleanProperty) properties.get("waterlogged"), true);
            }
        }
        else
        {
            if(blockId.equals("minecraft:water")) {
                if(level>0 && level<8) {
                    // raise the waterlevel by 1:
                    blockState = blockState.with((IntegerProperty) properties.get("level"),level-1);
                }
                else {
                    place_above=true;
                }
            }
            else {
                if(blockId.equals("minecraft:air") || blockId.equals("minecraft:cave_air") || blockId.equals("minecraft:void_air")) {
                    blockState = BlockTypes.WATER.getDefaultState();
                    properties = blockState.getBlockType().getPropertyMap();
                    blockState = blockState.with((IntegerProperty) properties.get("level"), 7);
                }
                else {
                    place_above=true;
                }
            }
        }

        if(place_above) {
            targetBlock = targetBlock.add(0,1,0);
            blockState = getBlock(targetBlock);
            // add water[level=7] on top:
            blockId = blockState.getBlockType().getId();
            if(blockId.equals("minecraft:air") || blockId.equals("minecraft:cave_air") || blockId.equals("minecraft:void_air")) {
                blockState = BlockTypes.WATER.getDefaultState();
                properties = blockState.getBlockType().getPropertyMap();
                blockState = blockState.with((IntegerProperty) properties.get("level"),7);
            }
            else {return;} // block above is non-air ==> do nothing!
        }

        try {
            setBlock(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), blockState);
        } catch (MaxChangedBlocksException var4) {
            var4.printStackTrace();
        }
    }

    @Override
    public void handleGunpowderAction(Snipe snipe) {
        Sniper sniper = snipe.getSniper();
        Player player = sniper.getPlayer();

        // the exact point on the hitbox of the block the player is looking at
        Vector3 location = PlatformAdapter.adapt(player.level().clip(new ClipContext(player.getEyePosition(1.0F), player.getEyePosition(1.0F).add(player.getLookAngle().scale((double) sniper.getCurrentToolkit().getProperties().getBlockTracerRange())), ClipContext.Block.OUTLINE, ClipContext.Fluid.WATER, player)).getLocation());

        // a normal vector to the surface (i.e. pointing to the outside of the targeted sub-block)
        Vector3 normal = PlatformAdapter.adapt(new Vec3(player.level().clip(new ClipContext(player.getEyePosition(1.0F), player.getEyePosition(1.0F).add(player.getLookAngle().scale((double) sniper.getCurrentToolkit().getProperties().getBlockTracerRange())), ClipContext.Block.OUTLINE, ClipContext.Fluid.WATER, player)).getDirection().step()));

        Vector3 targetLocation = location.subtract(normal.multiply(1./1024.));
        Vector3 lastLocation = location.add(normal.multiply(1023./1024.));

        BlockVector3 targetBlock = BlockVector3.at((int) Math.floor(targetLocation.getX()), (int) Math.floor(targetLocation.getY()), (int) Math.floor(targetLocation.getZ()));
        BlockVector3 lastBlock = BlockVector3.at((int) Math.floor(lastLocation.getX()), (int) Math.floor(lastLocation.getY()), (int) Math.floor(lastLocation.getZ()));

        BlockState blockState = getBlock(targetBlock);
        String blockId = blockState.getBlockType().getId();
        boolean waterlogged=false, waterloggable=false;
        int level=0;
        for(int i=0; i<2; i++) {
            for (Map.Entry<Property<?>, Object> entry : blockState.getStates().entrySet()) {
                String propertyName = entry.getKey().getName();
                Object propertyValue = entry.getValue();
                if (propertyName.equals("waterlogged")) {
                    waterlogged = (boolean) propertyValue;
                    waterloggable = true;
                }
                if (propertyName.equals("level")) {
                    level = (int) propertyValue;
                }
            }

            if (i==1 || waterloggable || blockId.equals("minecraft:water")) {break;}
            targetBlock = lastBlock;
            blockState = getBlock(targetBlock);
            blockId = blockState.getBlockType().getId();
            waterlogged = false; waterloggable = false; level=0;
        }

        var properties = blockState.getBlockType().getPropertyMap();

        if(waterloggable)
        {
            if(waterlogged) {
                blockState = blockState.with((BooleanProperty) properties.get("waterlogged"), false);
            }
            else {
                return; // no water to be removed ==> do nothing!
            }
        }
        else
        {
            if(blockId.equals("minecraft:water")) {
                if(level!=7) {
                    // lower the waterlevel by 1:
                    if(level>7) {level=0;}
                    blockState = blockState.with((IntegerProperty) properties.get("level"),level+1);
                }
                else {
                    // for the smallest waterlevel (level=7): replace with AIR
                    blockState = BlockTypes.AIR.getDefaultState();
                }
            }
            else {
                return; // no water to be removed ==> do nothing!
            }
        }

        try {
            setBlock(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), blockState);
        } catch (MaxChangedBlocksException var4) {
            var4.printStackTrace();
        }
    }

    @Override
    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
        messenger.sendBrushSizeMessage();
    }
}
