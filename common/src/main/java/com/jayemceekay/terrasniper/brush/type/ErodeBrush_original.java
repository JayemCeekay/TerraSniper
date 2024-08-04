/*package com.jayemceekay.terrasniper.brush.type;

import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.material.Materials;
import com.jayemceekay.terrasniper.util.text.NumericParser;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.*;
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
        BlockChangeTracker blockChangeTracker = new BlockChangeTracker(this.getEditSession(), targetBlock);

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
        private final Map<BlockVector3, BlockWrapper[]> flatChanges;
        private final EditSession editSession;
        private int nextIterationId;
        private BlockVector3 targetBlock;
        private static final IntegerProperty LAYER3, LAYER4, LAYERS;
        private static final BooleanProperty WATERLOGGED;
        private static final DirectionalProperty FACING;
        private static final EnumProperty TYPE, HALF, HINGE, SHAPE;

        static {
            List<Integer> intList3 = new ArrayList<>(List.of(1, 2, 3));
            List<Integer> intList4 = new ArrayList<>(List.of(1, 2, 3, 4));
            List<Integer> intList8 = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8));
            List<Boolean> boolList = new ArrayList<>(List.of(true, false));
            List<Direction> NESW = new ArrayList<>(List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST));
            List<String> bottomTop = new ArrayList<>(List.of("bottom", "top"));
            List<String> LR = new ArrayList<>(List.of("left", "right"));
            List<String> shapeList = new ArrayList<>(List.of("straight", "inner_left", "inner_right", "outer_left", "outer_right"));
            LAYER3 = new IntegerProperty("layer", intList3); // for quarter slabs
            LAYER4 = new IntegerProperty("layer", intList4); // for vertical slab/corner/quarter
            LAYERS = new IntegerProperty("layers", intList8); // for slabs
            WATERLOGGED = new BooleanProperty("waterlogged", boolList);
            FACING = new DirectionalProperty("facing", NESW);
            TYPE = new EnumProperty("type", bottomTop);
            HALF = new EnumProperty("half", bottomTop);
            HINGE = new EnumProperty("hinge", LR);
            SHAPE = new EnumProperty("shape", shapeList);
        }

        // materials (from vanilla minecraft) where the full block, slab AND stairs variants are all vanilla blocks (e.g. stone):
        private static final List<String> VARIANTS_EXIST_IN_VANILLA = new ArrayList<>(List.of("stone", "diorite", "andesite", "granite", "cobblestone", "sandstone"));
        // keys:   materials from Conquest where the full-block variant is a vanilla block (e.g. terracotta):
        // values: corresponding vanilla name
        private static final Map<String, String> VANILLA_TO_CONQUEST_MATERIAL = Map.ofEntries(
                Map.entry("stone", "limestone"),
                Map.entry("diorite", "light_limestone"),
                Map.entry("andesite", "andesite"),
                Map.entry("granite", "granite"),
                Map.entry("cobblestone", "limestone_cobble"),
                Map.entry("sandstone", "sandstone"),
                Map.entry("dirt", "lomy_dirt"),
                Map.entry("grass_block", "gold_bar_pile"), // this is (currently) retextured & renamed ingame to "Full Grass Block" and has all required variants
                Map.entry("terracotta", "umbre_mudstone"),
                Map.entry("brown_terracotta", "brown_mudstone"),
                Map.entry("black_terracotta", "black_hardened_clay"),
                Map.entry("gray_terracotta", "gray_cave_silt"),
                Map.entry("light_gray_terracotta", "worn_light_gray_plaster"),
                Map.entry("white_terracotta", "light_mudstone"),
                Map.entry("pink_terracotta", "pink_clay_tile"),
                Map.entry("magenta_terracotta", "worn_magenta_plaster"),
                Map.entry("purple_terracotta", "worn_purple_plaster"),
                Map.entry("blue_terracotta", "blue_clay_beaver_tail_tile"),
                Map.entry("light_blue_terracotta", "dirty_blue_clay_beaver_tail_tile"),
                Map.entry("cyan_terracotta", "old_slate_roof_tile"),
                Map.entry("green_terracotta", "green_clay_shingle"),
                Map.entry("lime_terracotta", "overgrown_green_clay_shingle"),
                Map.entry("yellow_terracotta", "yellow_mudstone"),
                Map.entry("orange_terracotta", "orange_mudstone"),
                Map.entry("red_terracotta", "red_mudstone")
        );
        private static final Map<String, String> CONQUEST_TO_VANILLA_MATERIAL= new HashMap<>(); // = inverse of CONQUEST_TO_VANILLA_MATERIAL
        static{
            for (Map.Entry<String, String> entry : VANILLA_TO_CONQUEST_MATERIAL.entrySet()) {
                CONQUEST_TO_VANILLA_MATERIAL.put(entry.getValue(), entry.getKey());
            }
        }

        private static final List<String> VARIANTS = new ArrayList<>(List.of("_vertical_slab", "_stairs", "_quarter_slab", "_vertical_quarter", "_eighth_slab", "_vertical_corner_slab", "_corner_slab", "_vertical_corner", "_slab"));
        private static final Set<Integer> possibleShapes;
        static{
            Integer[] tmp = {0b00000000,
                    0b10000000,0b01000000,0b00100000,0b00010000,0b00001000,0b00000100,0b00000010,0b00000001,
                    0b11000000,0b10001000,0b00001100,0b01000100,0b00110000,0b00100010,0b00000011,0b00010001,0b10100000,0b00001010,0b00000101,0b01010000,
                    0b11001000,0b10001100,0b01001100,0b11000100,0b00110010,0b00100011,0b00010011,0b00110001,0b10100010,0b00001011,0b00010101,0b01110000,0b00101010,0b00000111,0b01010001,0b10110000,0b10001010,0b00001101,0b01010100,0b11100000,0b10101000,0b00001110,0b01000101,0b11010000,
                    0b11001100,0b00110011,0b10101010,0b01010101,0b11110000,0b00001111,
                    0b00110111,0b01110011,0b10110011,0b00111011,0b11001101,0b11011100,0b11101100,0b11001110,
                    0b00111111,0b01110111,0b11110011,0b10111011,0b11001111,0b11011101,0b11111100,0b11101110,0b01011111,0b11110101,0b11111010,0b10101111,
                    0b01111111,0b10111111,0b11011111,0b11101111,0b11110111,0b11111011,0b11111101,0b11111110,
                    0b11111111};
            possibleShapes = new HashSet<>(Arrays.asList(tmp));
        }
        private static int BinaryCrossSum(int n) {
            int binaryCrossSum = 0;
            while (n != 0) {
                binaryCrossSum ^= n & 1;
                n >>= 1;
            }
            return binaryCrossSum;
        }

        private static String fixConquestNames(String material, String variant) {
            int i = material.indexOf(":") + 1;
            String blockName = material.substring(i);
            String origin = material.substring(0,i);
            if(variant.isEmpty()) { // thus material comes from editSession.getBlock(...)
                switch (origin) {
                    case "minecraft:":
                        return material; // material comes from minecraft so its full-block name is already correct
                    case "conquest:": // if material comes from a Conquest variant of a vanilla block, it has to be changed:
                        if (blockName.contains("_wool") || blockName.contains("_stained_glass")) { // some families of blocks that exist as full blocks in vanilla, but have the same name
                            return "minecraft:" + blockName;
                        }
                        if (CONQUEST_TO_VANILLA_MATERIAL.containsKey(blockName)){ // full-block name is different in vanilla (e.g. limestone --> stone)
                            return "minecraft:" + CONQUEST_TO_VANILLA_MATERIAL.get(blockName);
                        }
                        return material; // by default assume that even the full-block variant comes from conquest
                }
            }
            else { // thus material is itself a valid id of a full block, so has to be modified if variant specifies a Conquest block
                switch (origin) {
                    case "conquest:":
                        return material + variant; // full block comes from conquest so all variants do as well
                    case "minecraft:":
                        if(variant.equals("_stairs") && VARIANTS_EXIST_IN_VANILLA.contains(blockName)) { // block is a stair or slab that exists in the vanilla game (e.g. stone_stairs)
                            return material + variant;
                        }
                        if(VANILLA_TO_CONQUEST_MATERIAL.containsKey(blockName)) { // variants do NOT exist in vanilla (only in conquest) but with different names (e.g. limestone_quarter_slab)
                            return "conquest:" + VANILLA_TO_CONQUEST_MATERIAL.get(blockName) + variant;
                        }
                        return "conquest:" + blockName + variant; // by default assume that it is a conquest variant AND it uses the same name as the vanilla game
                }
            }
            return "minecraft:air";
        }

        private static int separatorIndex(String blockId) { // returns index of first "_" character that separates the block name from the variation (e.g. limestone_slab)
            int index;
            for(String variant : VARIANTS) {
                index = blockId.indexOf(variant);
                if(index>=0) {return index;}
            }
            return -1;
        }

        private static Object safeCall(Map<String, Object> blockStates, String key) {
            System.out.println("querying " + blockStates + " for key " + key);
            if(blockStates.containsKey(key)) {
                System.out.println("  returned value " + blockStates.get(key) + " of type " + blockStates.get(key).getClass().getSimpleName());
                return blockStates.get(key);
            }
            System.out.println("  returned standard value");
            switch(key) {
                case "layers", "layer": return 1;
                case "type", "half": return "bottom";
                case "shape": return "straight";
                case "facing": return Direction.NORTH;
                case "hinge": return "left";
            }
            System.out.println("  ERROR: returned null");
            return null;
        }
        private static int blockShape(String blockVariant, Map<String, Object> blockStates) {
            //returns a int from 0 to 255 encoding the block shape, i.e. which 1/8-sub-blocks are present
            // (assuming that for each of the variants all the relevant state-value pairs exist in 'blockStates')
            Integer layers;
            String half;
            System.out.println("BLOCK VARIANT: " + blockVariant + " ----------------------------------------------------------------");
            switch(blockVariant){
                case "": return  0b11111111;
                case "slab": layers = (Integer)safeCall(blockStates, "layers");
                    switch((String)safeCall(blockStates, "type")){
                        case "bottom":  return (layers<3 ? 0 : (layers<7 ? 0b00110011 : 0b11111111));
                        case "top":     return (layers<3 ? 0 : (layers<7 ? 0b11001100 : 0b11111111));
                    }
                case "vertical_slab": layers = (Integer)safeCall(blockStates, "layer");
                    switch((Direction)safeCall(blockStates, "facing")){
                        case NORTH: return (layers<3 ? 0 : 0b10101010);
                        case EAST:  return (layers<3 ? 0 : 0b00001111);
                        case SOUTH: return (layers<3 ? 0 : 0b01010101);
                        case WEST:  return (layers<3 ? 0 : 0b11110000);
                    }
                case "stairs": half = (String)safeCall(blockStates, "half");
                    switch((Direction)safeCall(blockStates, "facing")){
                        case NORTH:   switch((String)safeCall(blockStates, "shape")){
                            case "straight":    return (half.equals("bottom") ? 0b01110111 : 0b11011101);
                            case "inner_left":  return (half.equals("bottom") ? 0b01111111 : 0b11011111);
                            case "inner_right": return (half.equals("bottom") ? 0b11110111 : 0b11111101);
                            case "outer_left":  return (half.equals("bottom") ? 0b00110111 : 0b11001101);
                            case "outer_right": return (half.equals("bottom") ? 0b01110011 : 0b11011100);
                        }
                        case EAST:    switch((String)safeCall(blockStates, "shape")){
                            case "straight":    return (half.equals("bottom") ? 0b11110011 : 0b11111100);
                            case "inner_left":  return (half.equals("bottom") ? 0b11110111 : 0b11111101);
                            case "inner_right": return (half.equals("bottom") ? 0b11111011 : 0b11111110);
                            case "outer_left":  return (half.equals("bottom") ? 0b01110011 : 0b11011100);
                            case "outer_right": return (half.equals("bottom") ? 0b10110011 : 0b11101100);
                        }
                        case SOUTH:   switch((String)safeCall(blockStates, "shape")){
                            case "straight":    return (half.equals("bottom") ? 0b10111011 : 0b11101110);
                            case "inner_left":  return (half.equals("bottom") ? 0b11111011 : 0b11111110);
                            case "inner_right": return (half.equals("bottom") ? 0b10111111 : 0b11101111);
                            case "outer_left":  return (half.equals("bottom") ? 0b10110011 : 0b11101100);
                            case "outer_right": return (half.equals("bottom") ? 0b00111011 : 0b11001110);
                        }
                        case WEST:    switch((String)safeCall(blockStates, "shape")){
                            case "straight":    return (half.equals("bottom") ? 0b00111111 : 0b11001111);
                            case "inner_left":  return (half.equals("bottom") ? 0b10111111 : 0b11101111);
                            case "inner_right": return (half.equals("bottom") ? 0b01111111 : 0b11011111);
                            case "outer_left":  return (half.equals("bottom") ? 0b00111011 : 0b11001110);
                            case "outer_right": return (half.equals("bottom") ? 0b00110111 : 0b11001101);
                        }
                    }
                case "vertical_corner": layers = (Integer)safeCall(blockStates, "layer");
                    switch((Direction)safeCall(blockStates, "facing")){
                        case NORTH:   switch(layers) {
                            case 1: return 0;
                            case 2: return 0b10100000; // replace with vertical quarter
                            case 3: return 0b11111010;
                            case 4: return 0b11111111;
                        }
                        case EAST:    switch(layers) {
                            case 1: return 0;
                            case 2: return 0b00001010; // replace with vertical quarter
                            case 3: return 0b10101111;
                            case 4: return 0b11111111;
                        }
                        case SOUTH:   switch(layers) {
                            case 1: return 0;
                            case 2: return 0b00000101; // replace with vertical quarter
                            case 3: return 0b01011111;
                            case 4: return 0b11111111;
                        }
                        case WEST:    switch(layers) {
                            case 1: return 0;
                            case 2: return 0b01010000; // replace with vertical quarter
                            case 3: return 0b11110101;
                            case 4: return 0b11111111;
                        }
                    }
                case "quarter_slab": layers = (Integer)safeCall(blockStates, "layer");
                    switch((Direction)safeCall(blockStates, "facing")){
                        case NORTH:   switch((String)safeCall(blockStates, "type")) {
                            case "bottom":  return (layers<2 ? 0 : 0b00100010);
                            case "top":     return (layers<2 ? 0 : 0b10001000);
                        }
                        case EAST:    switch((String)safeCall(blockStates, "type")) {
                            case "bottom":  return (layers<2 ? 0 : 0b00000011);
                            case "top":     return (layers<2 ? 0 : 0b00001100);
                        }
                        case SOUTH:    switch((String)safeCall(blockStates, "type")) {
                            case "bottom":  return (layers<2 ? 0 : 0b00010001);
                            case "top":     return (layers<2 ? 0 : 0b01000100);
                        }
                        case WEST:    switch((String)safeCall(blockStates, "type")) {
                            case "bottom":  return (layers<2 ? 0 : 0b00110000);
                            case "top":     return (layers<2 ? 0 : 0b11000000);
                        }
                    }
                case "vertical_quarter": layers = (Integer)safeCall(blockStates, "layer");
                    switch((Direction)safeCall(blockStates, "facing")){
                        case NORTH:   return (layers<3 ? 0 : 0b10100000);
                        case EAST:    return (layers<3 ? 0 : 0b00001010);
                        case SOUTH:   return (layers<3 ? 0 : 0b00000101);
                        case WEST:    return (layers<3 ? 0 : 0b01010000);
                    }
                case "eighth_slab": half = (String)safeCall(blockStates, "type");
                    switch((Direction)safeCall(blockStates, "facing")){
                        case NORTH:   return (half.equals("bottom") ? 0b00100000 : 0b10000000);
                        case EAST:    return (half.equals("bottom") ? 0b00000010 : 0b00001000);
                        case SOUTH:   return (half.equals("bottom") ? 0b00000001 : 0b00000100);
                        case WEST:    return (half.equals("bottom") ? 0b00010000 : 0b01000000);
                    }
                case "corner_slab": half = (String)safeCall(blockStates, "type");
                    switch((Direction)safeCall(blockStates, "facing")){
                        case NORTH:   return (half.equals("bottom") ? 0b00110010 : 0b11001000);
                        case EAST:    return (half.equals("bottom") ? 0b00100011 : 0b10001100);
                        case SOUTH:   return (half.equals("bottom") ? 0b00010011 : 0b01001100);
                        case WEST:    return (half.equals("bottom") ? 0b00110001 : 0b11000100);
                    }
                case "vertical_corner_slab": half = (String)safeCall(blockStates, "type");
                    switch((Direction)safeCall(blockStates, "facing")){
                        case NORTH:   switch((String)safeCall(blockStates, "hinge")) {
                            case "left":    return (half.equals("bottom") ? 0b10100010 : 0b10101000);
                            case "right":   return (half.equals("bottom") ? 0b00101010 : 0b10001010);
                        }
                        case EAST:    switch((String)safeCall(blockStates, "hinge")) {
                            case "left":    return (half.equals("bottom") ? 0b00001011 : 0b00001110);
                            case "right":   return (half.equals("bottom") ? 0b00000111 : 0b00001101);
                        }
                        case SOUTH:   switch((String)safeCall(blockStates, "hinge")) {
                            case "left":    return (half.equals("bottom") ? 0b01010001 : 0b01010100);
                            case "right":   return (half.equals("bottom") ? 0b00010101 : 0b01000101);
                        }
                        case WEST:    switch((String)safeCall(blockStates, "hinge")) {
                            case "left":    return (half.equals("bottom") ? 0b10110000 : 0b11100000);
                            case "right":   return (half.equals("bottom") ? 0b01110000 : 0b11010000);
                        }
                    }
                default: return 0; //replace unknown block states with air
            }
        }

        private BlockChangeTracker(EditSession editSession, BlockVector3 targetBlock) {
            this.blockChanges = new HashMap<>();
            this.flatChanges = new HashMap<>();
            this.editSession = editSession;
            this.targetBlock = targetBlock;
        }

        public BlockWrapper get(BlockVector3 position, int iteration) {
            for (int i = iteration - 1; i >= 0; --i) {
                if (this.blockChanges.containsKey(i) && this.blockChanges.get(i).containsKey(position)) {
                    return (BlockWrapper) ((Map) this.blockChanges.get(i)).get(position);
                }
            }
            BlockVector3 halfPosition = divBy2(position);  // halfed coordinate relative to the target block
            return new BlockWrapper(halfPosition.getBlockX(), halfPosition.getBlockY(), halfPosition.getBlockZ(), getSubBlock(halfPosition, mod2(position)));
            //return new BlockWrapper(position.getBlockX(), position.getBlockY(), position.getBlockZ(), this.editSession.getBlock(position));
        }

        public Collection<BlockWrapper> getAll() {
            // turning 2x2x2 area of full blocks into stairs, quarter blocks, etc.
            final List<BlockWrapper> scaledFlatChanges = new ArrayList<>();
            BlockVector3 position;
            BlockWrapper[] block2x2x2; // block as 2x2x2
            boolean waterlogged;
            int shape, max, n;
            String currentMaterial, material;
            Map<String, Integer> possibleMaterials = new HashMap<>();
            for (Map.Entry<BlockVector3, BlockWrapper[]> entry : this.flatChanges.entrySet()) {
                position = entry.getKey();
                block2x2x2 = entry.getValue(); // block as 2x2x2
                waterlogged = false;
                shape = 0;
                material = "minecraft:air";
                max = 0;
                possibleMaterials = new HashMap<>();
                for (int i = 0; i < 8; i++) {
                    if (block2x2x2[i] == null) { // get block from editSession if not present in flatChanges:
                        block2x2x2[i] = new BlockWrapper(position.getX(), position.getY(), position.getZ(), getSubBlock(position, i));
                    }
                    currentMaterial = block2x2x2[i].getBlock().getBlockType().toString();
                    if (currentMaterial.equals("minecraft:water")) {
                        waterlogged = true;
                    }
                    if (!block2x2x2[i].isLiquid() && !block2x2x2[i].isEmpty() && !currentMaterial.equals("minecraft:air")) {
                        shape += (1 << i);
                        if (possibleMaterials.containsKey(currentMaterial)) {
                            n = possibleMaterials.get(currentMaterial) + 1;
                            possibleMaterials.put(currentMaterial, n);
                        } else {
                            n = 1;
                            possibleMaterials.put(currentMaterial, 1);
                        }
                        if (n > max) {
                            max = n;
                            material = currentMaterial;
                        }
                    }
                }
                scaledFlatChanges.add(composeBlock(position, material, shape, waterlogged));
            }
            return scaledFlatChanges;
        }

        private BlockWrapper composeBlock(BlockVector3 position, String material, int shape, boolean waterlogged) {
            int diff = 9;
            int d,binaryCrossSum,newShape=0;
            if (!possibleShapes.contains(shape)) { // the configuration of 1/8th-Blocks is not representable with slabs,stairs,etc.. So pick the closest one.
                for(int s : possibleShapes) {
                    d = s ^ shape;
                    binaryCrossSum = BinaryCrossSum(d);
                    if (diff > binaryCrossSum) {
                        diff = binaryCrossSum;
                        newShape = s;
                    }
                }
                shape = newShape;
            }
            if(shape==0b11111111) { // full block
                return new BlockWrapper(position.getX(),position.getY(),position.getZ(), new BlockType(material).getDefaultState());
            }
            if(shape==0) { // air or water
                return new BlockWrapper(position.getX(),position.getY(),position.getZ(), new BlockType(waterlogged ? "minecraft:water" : "minecraft:air").getDefaultState());
            }
            // obtain the block variant (stair/slab/...) first:
            String blockVariant;
            boolean upper = (shape & 0b11001100)==0; // true iff upper half is empty
            boolean lower = (shape & 0b00110011)==0; // true iff lower half is empty
            boolean isVertical = upper && lower; // true iff both upper and lower half of block are occupied
            boolean SE = (shape & 0b10100000)==0; // south-east quarter is EMPTY
            boolean SW = (shape & 0b00001010)==0; // south-west ...
            boolean NW = (shape & 0b00000101)==0; // ...
            boolean NE = (shape & 0b01010000)==0; // ...
            boolean SE_full = (shape & 0b10100000)==0b10100000; // south-east quarter is FULL
            boolean SW_full = (shape & 0b00001010)==0b00001010; // south-west ...
            boolean NW_full = (shape & 0b00000101)==0b00000101; // ...
            boolean NE_full = (shape & 0b01010000)==0b01010000; // ...
            boolean N = NE && NW; // north half is empty
            boolean S = SE && SW; // south ...
            boolean W = NW && SW; // ...
            boolean E = NE && SE; // ...
            binaryCrossSum = BinaryCrossSum(shape);
            switch(binaryCrossSum) {
                case 1: blockVariant = "_eighth_slab"; break;
                case 2: blockVariant = (isVertical ? "_vertical_quarter" : "_quarter_slab"); break;
                case 3: blockVariant = (isVertical ? "_vertical_corner_slab" : "_corner_slab"); break;
                case 4: blockVariant = (isVertical ? "_vertical_slab" : "_slab"); break;
                default:
                    if(SE || SW || NW || NE) {
                        blockVariant = "_vertical_corner";
                    }
                    else{
                        blockVariant = "_stairs";
                    }
            }

            // create the BlockState object
            BlockState blockState = new BlockType(fixConquestNames(material, blockVariant)).getDefaultState(); // NOTE: ONLY works for full conquest block types, e.g. not stone/limestone
            blockState = blockState.with(WATERLOGGED, waterlogged);

            // obtain the block data from the given shape:
            switch(blockVariant) {
                case "_slab": blockState = blockState.with(LAYERS, 4);
                    blockState = blockState.with(TYPE, upper ? "bottom" : "top");
                case "_vertical_slab": blockState = blockState.with(LAYER4, 3);
                    if(N) {blockState = blockState.with(FACING, "north");}
                    if(S) {blockState = blockState.with(FACING, "south");}
                    if(W) {blockState = blockState.with(FACING, "west");}
                    if(E) {blockState = blockState.with(FACING, "east");}
                case "_stairs":
                    switch(binaryCrossSum) {
                        case 5: blockState = blockState.with(SHAPE, "outer_left");
                            if(NW_full) {
                                blockState = blockState.with(FACING, "north");
                                blockState = blockState.with(HALF, shape==0b00110111 ? "bottom" : "top");
                            }
                            if(NE_full) {
                                blockState = blockState.with(FACING, "east");
                                blockState = blockState.with(HALF, shape==0b01110011 ? "bottom" : "top");
                            }
                            if(SE_full) {
                                blockState = blockState.with(FACING, "south");
                                blockState = blockState.with(HALF, shape==0b10110011 ? "bottom" : "top");
                            }
                            if(SW_full) {
                                blockState = blockState.with(FACING, "west");
                                blockState = blockState.with(HALF, shape==0b00111011 ? "bottom" : "top");
                            }
                        case 6: blockState = blockState.with(SHAPE, "straight");
                            if(NW_full && NE_full) {
                                blockState = blockState.with(FACING, "north");
                                blockState = blockState.with(HALF, shape==0b01110111 ? "bottom" : "top");
                            }
                            if(NE_full && SE_full) {
                                blockState = blockState.with(FACING, "east");
                                blockState = blockState.with(HALF, shape==0b11110011 ? "bottom" : "top");
                            }
                            if(SE_full && SW_full) {
                                blockState = blockState.with(FACING, "south");
                                blockState = blockState.with(HALF, shape==0b10111011 ? "bottom" : "top");
                            }
                            if(SW_full && NW_full) {
                                blockState = blockState.with(FACING, "west");
                                blockState = blockState.with(HALF, shape==0b00111111 ? "bottom" : "top");
                            }
                        case 7: blockState = blockState.with(SHAPE, "inner_left");
                            if(!NW_full) {
                                blockState = blockState.with(FACING, "south");
                                blockState = blockState.with(HALF, shape==0b11111011 ? "bottom" : "top");
                            }
                            if(!NE_full) {
                                blockState = blockState.with(FACING, "west");
                                blockState = blockState.with(HALF, shape==0b10111111 ? "bottom" : "top");
                            }
                            if(!SE_full) {
                                blockState = blockState.with(FACING, "north");
                                blockState = blockState.with(HALF, shape==0b01111111 ? "bottom" : "top");
                            }
                            if(!SW_full) {
                                blockState = blockState.with(FACING, "east");
                                blockState = blockState.with(HALF, shape==0b11110111 ? "bottom" : "top");
                            }
                    }
                case "_vertical_corner": blockState = blockState.with(LAYER4, 3);
                    if(SE) {blockState = blockState.with(FACING, "south");}
                    if(SW) {blockState = blockState.with(FACING, "west");}
                    if(NW) {blockState = blockState.with(FACING, "north");}
                    if(NE) {blockState = blockState.with(FACING, "east");}
                case "_quarter_slab": blockState = blockState.with(LAYER3, 2);
                    blockState = blockState.with(TYPE, upper ? "bottom" : "top");
                    if(N) {blockState = blockState.with(FACING, "north");}
                    if(S) {blockState = blockState.with(FACING, "south");}
                    if(W) {blockState = blockState.with(FACING, "west");}
                    if(E) {blockState = blockState.with(FACING, "east");}
                case "_vertical_quarter": blockState = blockState.with(LAYER4, 3);
                    if(!SE) {blockState = blockState.with(FACING, "north");}
                    if(!SW) {blockState = blockState.with(FACING, "east");}
                    if(!NW) {blockState = blockState.with(FACING, "south");}
                    if(!NE) {blockState = blockState.with(FACING, "west");}
                case "_eighth_slab": blockState = blockState.with(TYPE, upper ? "bottom" : "top");
                    if(!SE) {blockState = blockState.with(FACING, "north");}
                    if(!SW) {blockState = blockState.with(FACING, "east");}
                    if(!NW) {blockState = blockState.with(FACING, "south");}
                    if(!NE) {blockState = blockState.with(FACING, "west");}
                case "_corner_slab": blockState = blockState.with(TYPE, upper ? "bottom" : "top");
                    if(SE) {blockState = blockState.with(FACING, "south");}
                    if(SW) {blockState = blockState.with(FACING, "west");}
                    if(NW) {blockState = blockState.with(FACING, "north");}
                    if(NE) {blockState = blockState.with(FACING, "east");}
                case "_vertical_corner_slab":
                    if(N) {
                        blockState = blockState.with(FACING, "north");
                        if(SE_full) {
                            blockState = blockState.with(HINGE, "left");
                            blockState = blockState.with(TYPE, shape==0b10100010 ? "bottom" : "top");
                        }
                        else {
                            blockState = blockState.with(HINGE, "right");
                            blockState = blockState.with(TYPE, shape==0b00101010 ? "bottom" : "top");
                        }
                    }
                    if(S) {
                        blockState = blockState.with(FACING, "south");
                        if(NW_full) {
                            blockState = blockState.with(HINGE, "left");
                            blockState = blockState.with(TYPE, shape==0b00010101 ? "bottom" : "top");
                        }
                        else {
                            blockState = blockState.with(HINGE, "right");
                            blockState = blockState.with(TYPE, shape==0b01010001 ? "bottom" : "top");
                        }

                    }
                    if(W) {
                        blockState = blockState.with(FACING, "west");
                        if(NE_full) {
                            blockState = blockState.with(HINGE, "left");
                            blockState = blockState.with(TYPE, shape==0b01110000 ? "bottom" : "top");
                        }
                        else {
                            blockState = blockState.with(HINGE, "right");
                            blockState = blockState.with(TYPE, shape==0b10110000 ? "bottom" : "top");
                        }

                    }
                    if(E) {
                        blockState = blockState.with(FACING, "east");
                        if(SW_full) {
                            blockState = blockState.with(HINGE, "left");
                            blockState = blockState.with(TYPE, shape==0b00001011 ? "bottom" : "top");
                        }
                        else {
                            blockState = blockState.with(HINGE, "right");
                            blockState = blockState.with(TYPE, shape==0b00000111 ? "bottom" : "top");
                        }
                    }
            }

            //turn into BlockWrapper object:
            return new BlockWrapper(position.getX(),position.getY(),position.getZ(), blockState);
        }

        public int nextIteration() {
            return this.nextIterationId++;
        }

        public void put(BlockVector3 position, BlockWrapper changedBlock, int iteration) {
            if (!this.blockChanges.containsKey(iteration)) {
                this.blockChanges.put(iteration, new HashMap<>());
            }

            ((Map) this.blockChanges.get(iteration)).put(position, changedBlock);
            BlockVector3 halfPosition = divBy2(position);
            if(!this.flatChanges.containsKey(halfPosition)) {
                this.flatChanges.put(halfPosition, new BlockWrapper[8]);
            }
            this.flatChanges.get(halfPosition)[mod2(position)] = changedBlock;
        }

        private BlockState getSubBlock(BlockVector3 position, int subBlockPosition) {
            BlockState block = this.editSession.getBlock(position);
            if(block==null) {block = BlockTypes.AIR.getDefaultState();} // replace null by air block
            String blockId = block.getBlockType().getId();

            // 'subBlockPosition': position inside the block at 'position' coded as binary number (xyz) with coordinates x,y,z=0 or 1
            // translate from (subBlockPosition, blockId) to either air or a full block of same material:

            // 1) separate material name and translate to the minecraft name (e.g. "stone" from "stone_stairs" but also from "limestone_slab")
            int i = separatorIndex(blockId);
            String material = i==-1 ? fixConquestNames(blockId,"") : fixConquestNames(blockId.substring(0,i),"");
            String blockVariant = i==-1 ? "" : blockId.substring(i+1);

            // 2) get a Map encoding all the relevant block-states and ...
            Map<String, Object> blockStates = new HashMap<>();
            for (Map.Entry<Property<?>, Object> entry : block.getStates().entrySet()) {
                String propertyName = entry.getKey().getName();
                Object propertyValue = entry.getValue();
                blockStates.put(propertyName, (propertyValue instanceof String) ? ((String)propertyValue).toLowerCase(Locale.ROOT) : propertyValue);
            }
            // 3) ... put it through a translation map to tell you whether to place "material" or "air" or "water" (when waterlogged)
            BlockState subBlock;
            if((blockShape(blockVariant, blockStates) & (1 << subBlockPosition)) != 0){
                // full block (material)
                subBlock = (new BlockType(material)).getDefaultState();
            }
            else if(blockStates.containsKey("waterlogged") ? (boolean) blockStates.get("waterlogged") : false) {
                // water
                subBlock = BlockTypes.WATER.getDefaultState();
            }
            else {
                //air
                subBlock = BlockTypes.AIR.getDefaultState();
            }
            return subBlock;
        }

        private BlockVector3 divBy2(BlockVector3 position) {
            return BlockVector3.at(Math.floorDiv(position.getX(),2), Math.floorDiv(position.getY(),2), Math.floorDiv(position.getZ(),2));
        }

        private int mod2(BlockVector3 position) {
            return 4*Math.floorMod(position.getX(),2) + 2*Math.floorMod(position.getY(),2) + Math.floorMod(position.getZ(),2);
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
*/