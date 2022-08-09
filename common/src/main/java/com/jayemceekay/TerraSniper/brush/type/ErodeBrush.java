package com.jayemceekay.TerraSniper.brush.type;

import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.TerraSniper.sniper.snipe.Snipe;
import com.jayemceekay.TerraSniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.TerraSniper.util.material.Materials;
import com.jayemceekay.TerraSniper.util.text.NumericParser;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

public class ErodeBrush extends AbstractBrush {
    private static final List<Direction> FACES_TO_CHECK;

    static {
        FACES_TO_CHECK = Arrays.asList(Direction.SOUTH, Direction.NORTH, Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST);
    }

    private ErosionPreset currentPreset;

    public ErodeBrush() {
        this.currentPreset = Preset.DEFAULT.getPreset();
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        int var5 = parameters.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            String parameter = parameters[var6];
            if (parameter.equalsIgnoreCase("info")) {
                messenger.sendMessage(ChatFormatting.GOLD + "Erode Brush Parameters:");
                messenger.sendMessage(ChatFormatting.AQUA + "/b e e[n] -- Sets erosion faces to n.");
                messenger.sendMessage(ChatFormatting.AQUA + "/b e f[n] -- Sets fill faces to n.");
                messenger.sendMessage(ChatFormatting.AQUA + "/b e E[n] -- Sets erosion recursions to n.");
                messenger.sendMessage(ChatFormatting.AQUA + "/b e F[n] -- Sets fill recursions to n.");
                messenger.sendMessage(ChatFormatting.GOLD + "Erode Brush Presets:");
                messenger.sendMessage(ChatFormatting.AQUA + "/b eb default -- Sets erosion faces to 0, erosion recursions to 1, fill faces to 0 and fill recursions to 1.");
                messenger.sendMessage(ChatFormatting.AQUA + "/b eb melt -- Sets erosion faces to 2, erosion recursions to 1, fill faces to 5 and fill recursions to 1.");
                messenger.sendMessage(ChatFormatting.AQUA + "/b eb fill -- Sets erosion faces to 5, erosion recursions to 1, fill faces to 2 and fill recursions to 1.");
                messenger.sendMessage(ChatFormatting.AQUA + "/b eb smooth -- Sets erosion faces to 3, erosion recursions to 1, fill faces to 3 and fill recursions to 1.");
                messenger.sendMessage(ChatFormatting.AQUA + "/b eb lift -- Sets erosion faces to 6, erosion recursions to 0, fill faces to 1 and fill recursions to 1.");
                messenger.sendMessage(ChatFormatting.AQUA + "/b eb floatclean -- Sets erosion faces to 0, erosion recursions to 1, fill faces to 6 and fill recursions to 1.");
                return;
            }

            Preset preset = Preset.getPreset(parameter);
            if (preset != null) {
                try {
                    this.currentPreset = preset.getPreset();
                    messenger.sendMessage(ChatFormatting.LIGHT_PURPLE + "Brush preset set to: " + preset.getName());
                    return;
                } catch (IllegalArgumentException var11) {
                    messenger.sendMessage(ChatFormatting.RED + "Invalid preset.");
                    return;
                }
            }

            ErosionPreset currentPresetBackup = this.currentPreset;
            Integer erosionRecursion;
            if (parameter.startsWith("f[")) {
                erosionRecursion = NumericParser.parseInteger(parameter.replace("f[", "").replace("]", ""));
                if (erosionRecursion != null) {
                    this.currentPreset = new ErosionPreset(this.currentPreset.getErosionFaces(), this.currentPreset.getErosionRecursion(), erosionRecursion, this.currentPreset.getFillRecursion());
                } else {
                    messenger.sendMessage(ChatFormatting.RED + "Invalid number for f.");
                }
            } else if (parameter.startsWith("e[")) {
                erosionRecursion = NumericParser.parseInteger(parameter.replace("e[", "").replace("]", ""));
                if (erosionRecursion != null) {
                    this.currentPreset = new ErosionPreset(erosionRecursion, this.currentPreset.getErosionRecursion(), this.currentPreset.getFillFaces(), this.currentPreset.getFillRecursion());
                } else {
                    messenger.sendMessage(ChatFormatting.RED + "Invalid number for e.");
                }
            } else if (parameter.startsWith("F[")) {
                erosionRecursion = NumericParser.parseInteger(parameter.replace("F[", "").replace("]", ""));
                if (erosionRecursion != null) {
                    this.currentPreset = new ErosionPreset(this.currentPreset.getErosionFaces(), this.currentPreset.getErosionRecursion(), this.currentPreset.getFillFaces(), erosionRecursion);
                } else {
                    messenger.sendMessage(ChatFormatting.RED + "Invalid number for F.");
                }
            } else if (parameter.startsWith("E[")) {
                erosionRecursion = NumericParser.parseInteger(parameter.replace("E[", "").replace("]", ""));
                if (erosionRecursion != null) {
                    this.currentPreset = new ErosionPreset(this.currentPreset.getErosionFaces(), erosionRecursion, this.currentPreset.getFillFaces(), this.currentPreset.getFillRecursion());
                } else {
                    messenger.sendMessage(ChatFormatting.RED + "Invalid number for E.");
                }
            } else {
                messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters length! Use the \"info\" parameter to display parameter info.");
            }

            if (!this.currentPreset.equals(currentPresetBackup)) {
                if (this.currentPreset.getErosionFaces() != currentPresetBackup.getErosionFaces()) {
                    messenger.sendMessage(ChatFormatting.AQUA + "Erosion faces set to: " + ChatFormatting.WHITE + this.currentPreset.getErosionFaces());
                }

                if (this.currentPreset.getFillFaces() != currentPresetBackup.getFillFaces()) {
                    messenger.sendMessage(ChatFormatting.AQUA + "Fill faces set to: " + ChatFormatting.WHITE + this.currentPreset.getFillFaces());
                }

                if (this.currentPreset.getErosionRecursion() != currentPresetBackup.getErosionRecursion()) {
                    messenger.sendMessage(ChatFormatting.AQUA + "Erosion recursions set to: " + ChatFormatting.WHITE + this.currentPreset.getErosionRecursion());
                }

                if (this.currentPreset.getFillRecursion() != currentPresetBackup.getFillRecursion()) {
                    messenger.sendMessage(ChatFormatting.AQUA + "Fill recursions set to: " + ChatFormatting.WHITE + this.currentPreset.getFillRecursion());
                }
            }
        }

    }

    public List<String> handleCompletions(String[] parameters) {
        return parameters.length > 0 ? SuggestionHelper.limitByPrefix(Stream.of("f[", "e[", "F[", "E[", "default", "melt", "fill", "smooth", "lift", "floatclean"), parameters[parameters.length - 1]) : SuggestionHelper.limitByPrefix(Stream.of("f[", "e[", "F[", "E[", "default", "melt", "fill", "smooth", "lift", "floatclean"), "");
    }

    public void handleArrowAction(Snipe snipe) {
        this.erosion(snipe, this.currentPreset);
    }

    public void handleGunpowderAction(Snipe snipe) {
        this.erosion(snipe, this.currentPreset.getInverted());
    }

    private void erosion(Snipe snipe, ErosionPreset erosionPreset) {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        BlockVector3 targetBlock = this.getTargetBlock();
        BlockChangeTracker blockChangeTracker = new BlockChangeTracker(this.getEditSession());

        int i;
        for (i = 0; i < erosionPreset.getErosionRecursion(); ++i) {
            this.erosionIteration(toolkitProperties, erosionPreset, blockChangeTracker, targetBlock);
        }

        for (i = 0; i < erosionPreset.getFillRecursion(); ++i) {
            this.fillIteration(toolkitProperties, erosionPreset, blockChangeTracker, targetBlock);
        }

        Iterator var13 = blockChangeTracker.getAll().iterator();

        while (var13.hasNext()) {
            BlockWrapper blockWrapper = (BlockWrapper) var13.next();
            BlockState block = blockWrapper.getBlock();
            if (block != null) {
                BlockState blockData = blockWrapper.getBlockData();

                try {
                    this.setBlockData(BlockVector3.at(blockWrapper.getX(), blockWrapper.getY(), blockWrapper.getZ()), blockData);
                } catch (MaxChangedBlocksException var12) {
                    var12.printStackTrace();
                }
            }
        }

    }

    private void fillIteration(ToolkitProperties toolkitProperties, ErosionPreset erosionPreset, BlockChangeTracker blockChangeTracker, BlockVector3 targetBlockVector) {
        int currentIteration = blockChangeTracker.nextIteration();
        BlockVector3 targetBlock = this.getTargetBlock();
        int brushSize = toolkitProperties.getBrushSize();

        for (int x = targetBlock.getX() - brushSize; x <= targetBlock.getX() + brushSize; ++x) {
            for (int z = targetBlock.getZ() - brushSize; z <= targetBlock.getZ() + brushSize; ++z) {
                for (int y = targetBlock.getY() - brushSize; y <= targetBlock.getY() + brushSize; ++y) {
                    BlockVector3 currentPosition = BlockVector3.at(x, y, z);
                    if (Math.pow(currentPosition.getX() - targetBlockVector.getX(), 2.0D) + Math.pow(currentPosition.getY() - targetBlockVector.getY(), 2.0D) + Math.pow(currentPosition.getZ() - targetBlockVector.getZ(), 2.0D) <= Math.pow(brushSize, 2.0D)) {
                        BlockWrapper currentBlock = blockChangeTracker.get(currentPosition, currentIteration);
                        if (currentBlock.isEmpty() || currentBlock.isLiquid()) {
                            int count = 0;
                            Map<BlockWrapper, Integer> blockCount = new HashMap();
                            Iterator var15 = FACES_TO_CHECK.iterator();

                            BlockWrapper wrapper;
                            while (var15.hasNext()) {
                                Direction direction = (Direction) var15.next();
                                BlockVector3 relativePosition = currentPosition.add(direction.toBlockVector());
                                wrapper = blockChangeTracker.get(relativePosition, currentIteration);
                                if (!wrapper.isEmpty() && !wrapper.isLiquid()) {
                                    ++count;
                                    BlockWrapper typeBlock = new BlockWrapper(x, y, z, null, wrapper.getBlockData());
                                    if (blockCount.containsKey(typeBlock)) {
                                        blockCount.put(typeBlock, blockCount.get(typeBlock) + 1);
                                    } else {
                                        blockCount.put(typeBlock, 1);
                                    }
                                }
                            }

                            BlockWrapper currentBlockWrapper = new BlockWrapper(x, y, z, null, BlockTypes.AIR.getDefaultState());
                            int amount = 0;
                            Iterator var22 = blockCount.keySet().iterator();

                            while (var22.hasNext()) {
                                wrapper = (BlockWrapper) var22.next();
                                Integer currentCount = blockCount.get(wrapper);
                                if (amount <= currentCount) {
                                    currentBlockWrapper = wrapper;
                                    amount = currentCount;
                                }
                            }

                            if (count >= erosionPreset.getFillFaces()) {
                                blockChangeTracker.put(currentPosition, new BlockWrapper(x, y, z, currentBlock.getBlock(), currentBlockWrapper.getBlockData()), currentIteration);
                            }
                        }
                    }
                }
            }
        }

    }

    private void erosionIteration(ToolkitProperties toolkitProperties, ErosionPreset erosionPreset, BlockChangeTracker blockChangeTracker, BlockVector3 targetBlockVector) {
        int currentIteration = blockChangeTracker.nextIteration();
        BlockVector3 targetBlock = this.getTargetBlock();
        int brushSize = toolkitProperties.getBrushSize();

        for (int x = targetBlock.getX() - brushSize; x <= targetBlock.getX() + brushSize; ++x) {
            for (int z = targetBlock.getZ() - brushSize; z <= targetBlock.getZ() + brushSize; ++z) {
                for (int y = targetBlock.getY() - brushSize; y <= targetBlock.getY() + brushSize; ++y) {
                    BlockVector3 currentPosition = BlockVector3.at(x, y, z);
                    if (Math.pow(currentPosition.getX() - targetBlockVector.getX(), 2.0D) + Math.pow(currentPosition.getY() - targetBlockVector.getY(), 2.0D) + Math.pow(currentPosition.getZ() - targetBlockVector.getZ(), 2.0D) <= Math.pow(brushSize, 2.0D)) {
                        BlockWrapper currentBlock = blockChangeTracker.get(currentPosition, currentIteration);
                        if (!currentBlock.isEmpty() && !currentBlock.isLiquid()) {
                            int count = (int) FACES_TO_CHECK.stream().map((direction) -> {
                                return this.getRelativeBlock(currentPosition, direction);
                            }).map((relativePosition) -> {
                                return blockChangeTracker.get(relativePosition, currentIteration);
                            }).filter((relativeBlock) -> {
                                return relativeBlock.isEmpty() || relativeBlock.isLiquid();
                            }).count();
                            if (count >= erosionPreset.getErosionFaces()) {
                                blockChangeTracker.put(currentPosition, new BlockWrapper(x, y, z, currentBlock.getBlock(), BlockTypes.AIR.getDefaultState()), currentIteration);
                            }
                        }
                    }
                }
            }
        }

    }

    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
        messenger.sendBrushSizeMessage();
        messenger.sendMessage(ChatFormatting.AQUA + "Erosion minimum exposed faces set to: " + this.currentPreset.getErosionFaces());
        messenger.sendMessage(ChatFormatting.BLUE + "Fill minimum touching faces set to: " + this.currentPreset.getFillFaces());
        messenger.sendMessage(ChatFormatting.BLUE + "Erosion recursion amount set to: " + this.currentPreset.getErosionRecursion());
        messenger.sendMessage(ChatFormatting.DARK_GREEN + "Fill recursion amount set to: " + this.currentPreset.getFillRecursion());
    }

    private enum Preset {
        DEFAULT("default", new ErosionPreset(0, 1, 0, 1)),
        MELT("melt", new ErosionPreset(2, 1, 5, 1)),
        FILL("fill", new ErosionPreset(5, 1, 2, 1)),
        SMOOTH("smooth", new ErosionPreset(3, 1, 3, 1)),
        LIFT("lift", new ErosionPreset(6, 0, 1, 1)),
        FLOAT_CLEAN("floatclean", new ErosionPreset(6, 1, 6, 1));

        private final String name;
        private final ErosionPreset preset;

        Preset(String name, ErosionPreset preset) {
            this.name = name;
            this.preset = preset;
        }

        @Nullable
        public static Preset getPreset(String name) {
            return Arrays.stream(values()).filter((preset) -> preset.name.equalsIgnoreCase(name)).findFirst().orElse(null);
        }

        public String getName() {
            return this.name;
        }

        public ErosionPreset getPreset() {
            return this.preset;
        }
    }

    static class ErosionPreset implements Serializable {
        private static final long serialVersionUID = 8997952776355430411L;
        private final int erosionFaces;
        private final int erosionRecursion;
        private final int fillFaces;
        private final int fillRecursion;

        private ErosionPreset(int erosionFaces, int erosionRecursion, int fillFaces, int fillRecursion) {
            this.erosionFaces = erosionFaces;
            this.erosionRecursion = erosionRecursion;
            this.fillFaces = fillFaces;
            this.fillRecursion = fillRecursion;
        }

        public int hashCode() {
            return Objects.hash(this.erosionFaces, this.erosionRecursion, this.fillFaces, this.fillRecursion);
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ErosionPreset)) {
                return false;
            } else {
                ErosionPreset other = (ErosionPreset) obj;
                return this.erosionFaces == other.erosionFaces && this.erosionRecursion == other.erosionRecursion && this.fillFaces == other.fillFaces && this.fillRecursion == other.fillRecursion;
            }
        }

        public int getErosionFaces() {
            return this.erosionFaces;
        }

        public int getErosionRecursion() {
            return this.erosionRecursion;
        }

        public int getFillFaces() {
            return this.fillFaces;
        }

        public int getFillRecursion() {
            return this.fillRecursion;
        }

        public ErosionPreset getInverted() {
            return new ErosionPreset(this.fillFaces, this.fillRecursion, this.erosionFaces, this.erosionRecursion);
        }
    }

    static class BlockChangeTracker {
        private final Map<Integer, Map<BlockVector3, BlockWrapper>> blockChanges;
        private final Map<BlockVector3, BlockWrapper> flatChanges;
        private final EditSession editSession;
        private int nextIterationId;

        private BlockChangeTracker(EditSession editSession) {
            this.blockChanges = new HashMap<>();
            this.flatChanges = new HashMap<>();
            this.editSession = editSession;
        }

        public BlockWrapper get(BlockVector3 position, int iteration) {
            for (int i = iteration - 1; i >= 0; --i) {
                if (this.blockChanges.containsKey(i) && this.blockChanges.get(i).containsKey(position)) {
                    return (BlockWrapper) ((Map) this.blockChanges.get(i)).get(position);
                }
            }

            return new BlockWrapper(position.getBlockX(), position.getBlockY(), position.getBlockZ(), this.editSession.getBlock(position));
        }

        public Collection<BlockWrapper> getAll() {
            return this.flatChanges.values();
        }

        public int nextIteration() {
            return this.nextIterationId++;
        }

        public void put(BlockVector3 position, BlockWrapper changedBlock, int iteration) {
            if (!this.blockChanges.containsKey(iteration)) {
                this.blockChanges.put(iteration, new HashMap<>());
            }

            ((Map) this.blockChanges.get(iteration)).put(position, changedBlock);
            this.flatChanges.put(position, changedBlock);
        }
    }

    static class BlockWrapper {
        private final int x;
        private final int y;
        private final int z;
        @Nullable
        private final BlockState block;
        private final BlockState blockData;

        private BlockWrapper(int x, int y, int z, BlockState block) {
            this(x, y, z, block, block);
        }

        private BlockWrapper(int x, int y, int z, @Nullable BlockState block, BlockState blockData) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.block = block;
            this.blockData = blockData;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getZ() {
            return this.z;
        }

        @Nullable
        public BlockState getBlock() {
            return this.block;
        }

        public BlockState getBlockData() {
            return this.blockData;
        }

        public boolean isEmpty() {
            BlockType type = this.blockData.getBlockType();
            return Materials.isEmpty(type);
        }

        public boolean isLiquid() {
            BlockType type = this.blockData.getBlockType();
            return Materials.isLiquid(type);
        }
    }

}
