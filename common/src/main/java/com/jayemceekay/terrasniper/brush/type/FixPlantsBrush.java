package com.jayemceekay.terrasniper.brush.type;

import com.jayemceekay.terrasniper.sniper.Sniper;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.PlatformAdapter;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class FixPlantsBrush extends AbstractBrush {

    public FixPlantsBrush() {
        setCanUseSmallBlocks(false);
        setCanUseAutoLayer(false);
    }

    @Override
    public void handleArrowAction(Snipe snipe) {
        if (this.setBlockBuffer==null) {this.setBlockBuffer = new HashMap<>();}
        if (this.plantMap==null) {this.plantMap = new PlantMap(snipe,this);}
        this.keepPlants = true;

        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();

        int radiusSquared = brushSize*brushSize;
        for (int x=-brushSize; x<=brushSize; x++) {
            int xSquared = x*x;
            for (int z=-brushSize; z<=brushSize; z++) {
                if (xSquared+z*z<=radiusSquared) {
                    for (int y=brushSize; y>=-brushSize; y--) {
                        BlockVector3 position = BlockVector3.at(x,y,z).add(this.getTargetBlock());
                        BlockState block = this.getEditSession().getBlock(position);
                        BlockType blockType = block.getBlockType();
                        if (PLANTS.contains(blockType.getId())) {
                            try {
                                this.plantMap.init(position);
                            } catch (MaxChangedBlocksException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleGunpowderAction(Snipe snipe) {
        this.keepPlants = false;

        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();

        int radiusSquared = brushSize*brushSize;
        for (int x=-brushSize; x<=brushSize; x++) {
            int xSquared = x*x;
            for (int z=-brushSize; z<=brushSize; z++) {
                if (xSquared+z*z<=radiusSquared) {
                    for (int y=brushSize; y>=-brushSize; y--) {
                        BlockVector3 position = BlockVector3.at(x,y,z).add(this.getTargetBlock());
                        BlockState block = this.getEditSession().getBlock(position);
                        BlockType blockType = block.getBlockType();
                        if (PLANTS.contains(blockType.getId())) { // replace "plant" blocks with AIR (or WATER if it is waterlogged)
                            var properties = blockType.getPropertyMap();
                            boolean waterlogged=false;
                            if (properties.containsKey("waterlogged")) {waterlogged = (boolean) block.getState(properties.get("waterlogged"));}
                            if (waterlogged) {
                                try {
                                    setBlock(position, BlockTypes.WATER.getDefaultState());
                                } catch (MaxChangedBlocksException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                try {
                                    setBlock(position, BlockTypes.AIR.getDefaultState());
                                } catch (MaxChangedBlocksException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
        messenger.sendBrushSizeMessage();
    }
}
