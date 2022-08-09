package com.jayemceekay.TerraSniper.brush.type.blend;

import com.sk89q.worldedit.world.block.BlockType;

import javax.annotation.Nullable;

class CommonMaterial {
    @Nullable
    private BlockType type;
    private int frequency;

    CommonMaterial() {
    }

    @Nullable
    public BlockType getBlockType() {
        return this.type;
    }

    public void setBlockType(@Nullable BlockType type) {
        this.type = type;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
