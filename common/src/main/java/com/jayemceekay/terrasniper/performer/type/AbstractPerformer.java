package com.jayemceekay.terrasniper.performer.type;

import com.jayemceekay.terrasniper.brush.Brush;
import com.jayemceekay.terrasniper.brush.type.AbstractBrush;
import com.jayemceekay.terrasniper.performer.Performer;
import com.jayemceekay.terrasniper.performer.property.PerformerProperties;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;

public abstract class AbstractPerformer implements Performer {

    private AbstractBrush brushReference;
    private PerformerProperties properties;

    public void setBlock(EditSession editSession, int x, int y, int z, Pattern pattern) {
        if (pattern instanceof BlockType blockType) {
            setBlockData(editSession, x, y, z, blockType.getDefaultState());
        } else {
            try {
                brushReference.setBlock(BlockVector3.at(x, y, z), pattern);
                //editSession.setBlock(BlockVector3.at(x, y, z), pattern);
            } catch (MaxChangedBlocksException e) {
                e.printStackTrace();
            }
        }
    }

    public void setBlockData(EditSession editSession, int x, int y, int z, BlockState blockState) {
        try {
            brushReference.setBlock(BlockVector3.at(x, y, z), blockState);
            //editSession.setBlock(BlockVector3.at(x, y, z), blockState);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    public BaseBlock simulateSetBlock(int x, int y, int z, Pattern pattern) {
        return pattern.applyBlock(BlockVector3.at(x, y, z));
    }

    @Override
    public PerformerProperties getProperties() {
        return properties;
    }

    @Override
    public void setProperties(final PerformerProperties properties, AbstractBrush brushReference) {
        this.properties = properties;
        this.brushReference = brushReference;
    }

    @Override
    public void loadProperties() {
    }

}
