package com.jayemceekay.terrasniper.sniper.ToolKit;

import com.jayemceekay.terrasniper.brush.property.BrushPattern;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ToolkitProperties {
    private final List<BlockState> voxelList = new ArrayList<>();
    private BrushPattern pattern;
    private BrushPattern replacePattern;
    private int brushSize;
    private int voxelHeight;
    private int cylinderCenter;
    private Integer blockTracerRange = 128;
    private boolean lightningEnabled;

    public ToolkitProperties() {
        this.pattern = new BrushPattern(BlockTypes.AIR.getDefaultState());
        this.replacePattern = new BrushPattern(BlockTypes.AIR.getDefaultState());
        this.brushSize = 3;
        this.voxelHeight = 1;
        this.cylinderCenter = 0;
    }

    public void reset() {
        this.resetBlockData();
        this.resetReplacePattern();
        this.brushSize = 3;
        this.voxelHeight = 1;
        this.cylinderCenter = 0;
        this.blockTracerRange = 128;
        this.lightningEnabled = false;
        this.voxelList.clear();
    }

    public void resetBlockData() {
        this.pattern = new BrushPattern(BlockTypes.AIR.getDefaultState());
    }

    public void resetReplacePattern() {
        this.replacePattern = new BrushPattern(BlockTypes.AIR.getDefaultState());
    }

    public BrushPattern getPattern() {
        return this.pattern;
    }

    public void setPattern(BrushPattern brushPattern) {
        this.pattern = brushPattern;
    }

    public BrushPattern getReplacePattern() {
        return this.replacePattern;
    }

    public void setReplacePattern(BrushPattern replaceBrushPattern) {
        this.replacePattern = replaceBrushPattern;
    }

    public void addToVoxelList(BlockState blockData) {
        this.voxelList.add(blockData);
    }

    public void removeFromVoxelList(BlockState blockData) {
        this.voxelList.remove(blockData);
    }

    public void clearVoxelList() {
        this.voxelList.clear();
    }

    public boolean isVoxelListContains(BlockState blockData) {
        return this.voxelList.contains(blockData);
    }

    public int getBrushSize() {
        return this.brushSize;
    }

    public void setBrushSize(int brushSize) {
        this.brushSize = brushSize;
    }

    public int getVoxelHeight() {
        return this.voxelHeight;
    }

    public void setVoxelHeight(int voxelHeight) {
        this.voxelHeight = voxelHeight;
    }

    public int getCylinderCenter() {
        return this.cylinderCenter;
    }

    public void setCylinderCenter(int cylinderCenter) {
        this.cylinderCenter = cylinderCenter;
    }

    @Nullable
    public Integer getBlockTracerRange() {
        return this.blockTracerRange;
    }

    public void setBlockTracerRange(Integer blockTracerRange) {
        this.blockTracerRange = blockTracerRange;
    }

    public boolean isLightningEnabled() {
        return this.lightningEnabled;
    }

    public void setLightningEnabled(boolean lightningEnabled) {
        this.lightningEnabled = lightningEnabled;
    }

    public List<BlockState> getVoxelList() {
        return Collections.unmodifiableList(this.voxelList);
    }
}
