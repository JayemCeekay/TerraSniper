package com.jayemceekay.terrasniper.brush.type;

import com.jayemceekay.terrasniper.brush.Brush;
import com.jayemceekay.terrasniper.brush.property.BrushProperties;
import com.jayemceekay.terrasniper.sniper.Sniper;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolAction;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.entity.player.Player;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractBrush implements Brush {
    protected static final int CHUNK_SIZE = 16;
    protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(".##");
    public final HashMap<String, String> settings = new HashMap<>();
    private BrushProperties properties;
    private EditSession editSession;
    private BlockVector3 targetBlock;
    private BlockVector3 lastBlock;

    @Override
    public void handleCommand(String[] parameters, Snipe snipe) {
        Sniper sniper = snipe.getSniper();
        Player player = sniper.getPlayer();
        player.sendSystemMessage(MutableComponent.create(new LiteralContents(ChatFormatting.RED + "This brush does not accept additional parameters.")));
    }

    @Override
    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        return parameters.length == 0 ? this.sortCompletions(Stream.empty(), "", 0) : Collections.emptyList();
    }

    public List<String> sortCompletions(Stream<String> completions, String parameter, int index) {
        if (parameter.length() == 0) {
            return completions.sorted().collect(Collectors.toList());
        } else {
            String parameterLowered = (parameter.startsWith("minecraft:") ? parameter.substring(10) : parameter).toLowerCase(Locale.ROOT);
            return (index == 0 ? Stream.concat(completions, Stream.of("info")) : completions).filter((completion) -> completion.toLowerCase(Locale.ROOT).startsWith(parameterLowered)).sorted().collect(Collectors.toList());
        }
    }

    @Override
    public void perform(Snipe snipe, ToolAction action, EditSession editSession, BlockVector3 targetBlock, BlockVector3 lastBlock) {
        this.editSession = editSession;
        this.targetBlock = targetBlock;
        this.lastBlock = lastBlock;
        if (action == ToolAction.ARROW) {
            handleArrowAction(snipe);
        } else if (action == ToolAction.WAND) {
            handleGunpowderAction(snipe);
        }

    }

    public int clampY(int y) {
        int clampedY = y;
        int minHeight = this.editSession.getMinimumPoint().getY();
        if (y <= minHeight) {
            clampedY = minHeight;
        } else {
            int maxHeight = this.editSession.getMaximumPoint().getY();
            if (y > maxHeight) {
                clampedY = maxHeight;
            }
        }

        return clampedY;
    }

    public BlockState clampY(BlockVector3 position) {
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();
        return this.clampY(x, y, z);
    }

    public BlockState clampY(int x, int y, int z) {
        return this.getBlock(x, this.clampY(y), z);
    }

    public void setBiome(int x, int y, int z, BiomeType biomeType) {
        this.editSession.setBiome(BlockVector3.at(x, y, z), biomeType);
    }

    public int getHighestTerrainBlock(int x, int z, int minY, int maxY) {
        return this.editSession.getHighestTerrainBlock(x, z, minY, maxY);
    }

    public Direction getDirection(BlockVector3 first, BlockVector3 second) {
        for (Direction direction : Direction.values()) {
            if (first.getX() + direction.toBlockVector().getX() == second.getX() && first.getY() + direction.toBlockVector().getY() == second.getY() && first.getZ() + direction.toBlockVector().getZ() == second.getZ()) {
                return direction;
            }
        }

        return null;
    }

    public BlockVector3 getRelativeBlock(BlockVector3 origin, Direction direction) {
        int x = origin.getX();
        int y = origin.getY();
        int z = origin.getZ();
        return this.getRelativeBlock(x, y, z, direction);
    }

    public BlockVector3 getRelativeBlock(int x, int y, int z, Direction direction) {
        return direction.toBlockVector().add(x, y, z);
    }

    public BlockType getBlockType(BlockVector3 position) {
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();
        return this.getBlockType(x, y, z);
    }

    public BlockType getBlockType(int x, int y, int z) {
        BlockState block = this.getBlock(x, y, z);
        return block.getBlockType();
    }

    public void setBlock(BlockVector3 position, Pattern pattern) throws MaxChangedBlocksException {
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();
        setBlock(x, y, z, pattern);
    }

    public void setBlock(int x, int y, int z, Pattern pattern) throws MaxChangedBlocksException {
        if (pattern instanceof BlockType blockType) {
            setBlockData(x, y, z, blockType.getDefaultState());
        } else {
            editSession.setBlock(BlockVector3.at(x, y, z), pattern);
        }
    }

    public void setBlockData(int x, int y, int z, BlockState blockState) throws MaxChangedBlocksException {
        editSession.setBlock(BlockVector3.at(x, y, z), blockState);
    }

    public void setBlockData(BlockVector3 position, BlockState blockState) throws MaxChangedBlocksException {
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();
        setBlockData(x, y, z, blockState);
    }

    public BaseBlock getFullBlock(BlockVector3 position) {
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();
        return this.getFullBlock(x, y, z);
    }

    public BaseBlock getFullBlock(int x, int y, int z) {
        return this.editSession.getFullBlock(BlockVector3.at(x, y, z));
    }

    public BlockState getBlock(BlockVector3 position) {
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();
        return this.getBlock(x, y, z);
    }

    public BlockState getBlock(int x, int y, int z) {
        return this.editSession.getBlock(BlockVector3.at(x, y, z));
    }

    public void setBlock(int x, int y, int z, BaseBlock block) throws MaxChangedBlocksException {
        this.editSession.setBlock(BlockVector3.at(x, y, z), block);
    }

    @Override
    public BrushProperties getProperties() {
        return this.properties;
    }

    @Override
    public void setProperties(BrushProperties properties) {
        this.properties = properties;
    }

    @Override
    public void loadProperties() {
    }

    public EditSession getEditSession() {
        return this.editSession;
    }

    public BlockVector3 getTargetBlock() {
        return this.targetBlock;
    }

    public void setTargetBlock(BlockVector3 targetBlock) {
        this.targetBlock = targetBlock;
    }

    public BlockVector3 getLastBlock() {
        return this.lastBlock;
    }

    public void setLastBlock(BlockVector3 lastBlock) {
        this.lastBlock = lastBlock;
    }

    @Override
    public HashMap<String, String> getSettings() {
        return this.settings;
    }
}
