package com.jayemceekay.terrasniper.brush.type;

//import com.conquestrefabricated.content.blocks.init.BlockRegistrar;
//import com.conquestrefabricated.content.blocks.block.Slab;
//import com.conquestrefabricated.core.item.family.FamilyRegistry;
import com.conquestrefabricated.RefabricatedModClient;
import com.jayemceekay.terrasniper.brush.Brush;
import com.jayemceekay.terrasniper.brush.property.BrushProperties;
import com.jayemceekay.terrasniper.sniper.Sniper;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolAction;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.util.PlatformAdapter;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.registry.state.*;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractBrush implements Brush {
    private boolean useSmallBlocks = false;
    private boolean canUseSmallBlocks = true;
    protected static final int CHUNK_SIZE = 16;
    protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(".##");
    public final HashMap<String, String> settings = new HashMap<>();
    private BrushProperties properties;
    private EditSession editSession;
    private BlockVector3 targetBlock;
    private BlockVector3 lastBlock;
    private Map<BlockVector3, String[]> toDoList;
    private BlockVector3 offsetVector;
    private boolean additiveBrush = true; //whether the arrow action adds or removes blocks
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
    private static final Map<Integer,Set<Integer>> GET_SHAPE_ADDITIVE = new HashMap<>();
    private static final Map<Integer,Set<Integer>> GET_SHAPE_SUBTRACTIVE = new HashMap<>();
    static{
        List<Integer> initializationKeys = new ArrayList<>(List.of(0b10000100,0b00100100,0b01101000,0b10100100,0b00010111,0b00011011,0b10010011,0b10010110,0b10100101,0b00011111,0b01011011,0b10010111,0b01111011,0b11011011));
        for (int key : initializationKeys) {
            GET_SHAPE_ADDITIVE.put(key, new HashSet<>());
            GET_SHAPE_SUBTRACTIVE.put(key, new HashSet<>());
        }

        // initialize with maps, that leave the volume the same
        // 2
        GET_SHAPE_ADDITIVE.get(0b10000100).add(0b10001000);
        GET_SHAPE_ADDITIVE.get(0b00100100).add(0b00100010);
        // 3
        GET_SHAPE_ADDITIVE.get(0b01101000).add(0b10101000); GET_SHAPE_SUBTRACTIVE.get(0b01101000).add(0b10101000);
        GET_SHAPE_ADDITIVE.get(0b10100100).add(0b10101000); GET_SHAPE_SUBTRACTIVE.get(0b10100100).add(0b10101000);
        // 4
                                                            GET_SHAPE_SUBTRACTIVE.get(0b00010111).add(0b00110011);
                                                            GET_SHAPE_SUBTRACTIVE.get(0b00011011).add(0b00110011);
        GET_SHAPE_ADDITIVE.get(0b10010011).add(0b00110011); GET_SHAPE_SUBTRACTIVE.get(0b10010011).add(0b00110011);
        GET_SHAPE_ADDITIVE.get(0b10010110).add(0b00110011); GET_SHAPE_SUBTRACTIVE.get(0b10010110).add(0b00110011);
        GET_SHAPE_ADDITIVE.get(0b10100101).add(0b00001111); GET_SHAPE_SUBTRACTIVE.get(0b10100101).add(0b00001111);
        // 5
        GET_SHAPE_ADDITIVE.get(0b10010111).add(0b00110111); GET_SHAPE_SUBTRACTIVE.get(0b10010111).add(0b00110111);
        // 6
                                                            GET_SHAPE_SUBTRACTIVE.get(0b01111011).add(0b00111111);
                                                            GET_SHAPE_SUBTRACTIVE.get(0b11011011).add(0b10111011);


        // add mappings where the volume increases/decreases:
        // 2
        GET_SHAPE_ADDITIVE.get(0b10000100).add(0b10001100); GET_SHAPE_SUBTRACTIVE.get(0b10000100).add(0b00000100);
                                                            GET_SHAPE_SUBTRACTIVE.get(0b00100100).add(0b00000100);
        // 3
        GET_SHAPE_ADDITIVE.get(0b01101000).add(0b10101010); GET_SHAPE_SUBTRACTIVE.get(0b01101000).add(0b10100000);
        GET_SHAPE_ADDITIVE.get(0b10100100).add(0b10101010); GET_SHAPE_SUBTRACTIVE.get(0b10100100).add(0b10100000);
        // 4
        GET_SHAPE_ADDITIVE.get(0b00010111).add(0b01011111);
        GET_SHAPE_ADDITIVE.get(0b00010111).add(0b00110111); GET_SHAPE_SUBTRACTIVE.get(0b00010111).add(0b00010011);
        GET_SHAPE_ADDITIVE.get(0b00011011).add(0b01011111);
        GET_SHAPE_ADDITIVE.get(0b00011011).add(0b00111111);
        GET_SHAPE_ADDITIVE.get(0b00011011).add(0b00111011); GET_SHAPE_SUBTRACTIVE.get(0b00011011).add(0b00010011);
        GET_SHAPE_ADDITIVE.get(0b10010011).add(0b10110011); GET_SHAPE_SUBTRACTIVE.get(0b10010011).add(0b00010011);
        GET_SHAPE_ADDITIVE.get(0b10010110).add(0b10110011); GET_SHAPE_SUBTRACTIVE.get(0b10010110).add(0b00010011);
        // 5
        GET_SHAPE_ADDITIVE.get(0b00011111).add(0b00111111); GET_SHAPE_SUBTRACTIVE.get(0b00011111).add(0b00001111);
        GET_SHAPE_ADDITIVE.get(0b01011011).add(0b01011111); GET_SHAPE_SUBTRACTIVE.get(0b01011011).add(0b00001111);
        GET_SHAPE_ADDITIVE.get(0b10010111).add(0b00111111); GET_SHAPE_SUBTRACTIVE.get(0b10010111).add(0b00001111);
        // 6
        GET_SHAPE_ADDITIVE.get(0b01111011).add(0b11111011); GET_SHAPE_SUBTRACTIVE.get(0b01111011).add(0b00111011);
        GET_SHAPE_ADDITIVE.get(0b10010111).add(0b11111011);

        // create the symmetry-group of a group as a list of maps that operate on the 'shape' integers
        Function<Integer[],Function<Integer,Integer>> createPermutation = (values) -> ((shape) -> {
            int result = 0;
            int v;
            for (int i=0; i<8; i++) {
                v = values[i];
                result += ((shape & (1<<i))!=0 ? (1<<v) : 0);
            }
            return result;
        });
        Function<Integer,Integer> identity = createPermutation.apply(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7});
        Function<Integer,Integer> rot90x = createPermutation.apply(new Integer[]{1, 3, 0, 2, 5, 7, 4, 6});
        Function<Integer,Integer> rot90y = createPermutation.apply(new Integer[]{4, 0, 6, 2, 5, 1, 7, 3});
        Function<Integer,Integer> rot90z = createPermutation.apply(new Integer[]{2, 3, 6, 7, 0, 1, 4, 5});
        Function<Integer,Integer> rot120_07 = createPermutation.apply(new Integer[]{0, 2, 4, 6, 1, 3, 5, 7});
        Function<Integer,Integer> rot120_16 = createPermutation.apply(new Integer[]{3, 1, 7, 5, 2, 0, 6, 4});
        Function<Integer,Integer> rot120_25 = createPermutation.apply(new Integer[]{3, 7, 2, 6, 1, 5, 0, 4});
        Function<Integer,Integer> rot120_34 = createPermutation.apply(new Integer[]{5, 7, 1, 3, 4, 6, 0, 2});
        Function<Integer,Integer> rot180xy   = createPermutation.apply(new Integer[]{7, 6, 3, 2, 5, 4, 1, 0});
        Function<Integer,Integer> rot180xy_z = createPermutation.apply(new Integer[]{1, 0, 5, 4, 3, 2, 7, 6});
        Function<Integer,Integer> rot180yz   = createPermutation.apply(new Integer[]{7, 5, 6, 4, 3, 1, 2, 0});
        Function<Integer,Integer> rot180yz_x = createPermutation.apply(new Integer[]{4, 6, 5, 7, 0, 2, 1, 3});
        Function<Integer,Integer> rot180zx   = createPermutation.apply(new Integer[]{7, 3, 5, 1, 6, 2, 4, 0});
        Function<Integer,Integer> rot180zx_y = createPermutation.apply(new Integer[]{2, 6, 0, 4, 3, 7, 1, 5});
        Function<Integer,Integer> invert = createPermutation.apply(new Integer[]{7, 6, 5, 4, 3, 2, 1, 0});
        List<Function<Integer,Integer>> symmetryGroup = new ArrayList<>(List.of(
                identity,
                rot90x, rot90x.compose(rot90x), rot90x.compose(rot90x).compose(rot90x),
                rot90y, rot90y.compose(rot90y), rot90y.compose(rot90y).compose(rot90y),
                rot90z, rot90z.compose(rot90z), rot90z.compose(rot90z).compose(rot90z),
                rot120_07, rot120_07.compose(rot120_07),   rot120_16, rot120_16.compose(rot120_16),
                rot120_25, rot120_25.compose(rot120_25),   rot120_34, rot120_34.compose(rot120_34),
                rot180xy, rot180xy_z, rot180yz, rot180yz_x, rot180zx, rot180zx_y
        ));
        for (int i=0; i<24; i++) {
            symmetryGroup.add(symmetryGroup.get(i).compose(invert));
        }

        // use symmetry to complete the two Maps:
        Set<Integer> tmp;
        int perm_key;
        for (int key : initializationKeys) {
            if (!GET_SHAPE_ADDITIVE.containsKey(key)) {
                GET_SHAPE_ADDITIVE.put(key, new HashSet<>());}
            tmp = Set.copyOf(GET_SHAPE_ADDITIVE.get(key));
            for (Function<Integer,Integer> permutation : symmetryGroup) {
                perm_key = permutation.apply(key);
                for (int value : tmp) {
                    if (!GET_SHAPE_ADDITIVE.containsKey(perm_key)) {
                        GET_SHAPE_ADDITIVE.put(perm_key, new HashSet<>());}
                    GET_SHAPE_ADDITIVE.get(perm_key).add(permutation.apply(value));
                }
            }
        }
        for (int key : initializationKeys) {
            if (!GET_SHAPE_SUBTRACTIVE.containsKey(key)) {
                GET_SHAPE_SUBTRACTIVE.put(key, new HashSet<>());}
            tmp = Set.copyOf(GET_SHAPE_SUBTRACTIVE.get(key));
            for (Function<Integer,Integer> permutation : symmetryGroup) {
                perm_key = permutation.apply(key);
                for (int value : tmp) {
                    if (!GET_SHAPE_SUBTRACTIVE.containsKey(perm_key)) {
                        GET_SHAPE_SUBTRACTIVE.put(perm_key, new HashSet<>());}
                    GET_SHAPE_SUBTRACTIVE.get(perm_key).add(permutation.apply(value));
                }
            }
        }

        // remove illegal shapes (the 16 rotated corner-stair):
        Set<Integer> illegalShapes = new HashSet<>(Set.of(0b01011101,0b11110100,0b11101010,0b10001111,0b11010101,0b11111000,0b10101110,0b01001111,0b01110101,0b11110010,0b10101011,0b00011111,0b01010111,0b11110001,0b10111010,0b00101111));
        for (int i=0; i<256; i++) {
            if(GET_SHAPE_ADDITIVE.containsKey(i))    {
                GET_SHAPE_ADDITIVE.get(i).removeAll(illegalShapes);}
            if(GET_SHAPE_SUBTRACTIVE.containsKey(i)) {
                GET_SHAPE_SUBTRACTIVE.get(i).removeAll(illegalShapes);}
        }
        // remove the keys that correspond to the 8 legal corner-stair variants
        int[] legalStairs = new int[]{0b11101100,0b11011100,0b11001110,0b11001101,0b10110011,0b01110011,0b00111011,0b00110111};
        for (int shape : legalStairs) {
            GET_SHAPE_ADDITIVE.remove(shape);
            GET_SHAPE_SUBTRACTIVE.remove(shape);
        }

    }
    private static final Set<Integer> impossibleShapes = GET_SHAPE_ADDITIVE.keySet();

    // materials (from vanilla minecraft) where the full block AND stairs variants are all vanilla blocks (e.g. stone):
    private static final List<String> VARIANTS_EXIST_IN_VANILLA = new ArrayList<>(List.of("stone", "diorite", "andesite", "granite", "polished_diorite", "polished_andesite", "polished_granite", "cobblestone", "sandstone"));

    // keys:   materials from Conquest where the full-block variant is a vanilla block (e.g. terracotta):
    // values: corresponding vanilla name
    private static final Map<String, String> VANILLA_TO_CONQUEST_MATERIAL;
    static {
        VANILLA_TO_CONQUEST_MATERIAL = Map.ofEntries(
                Map.entry("stone", "limestone"),
                Map.entry("diorite", "light_limestone"),
                Map.entry("andesite", "andesite"),
                Map.entry("granite", "granite"),
                Map.entry("polished_diorite", "large_light_limestone_brick"),
                Map.entry("polished_andesite", "large_andesite_masonry"),
                Map.entry("polished_granite", "large_granite_brick"),
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
    }
    private static final Map<String, String> CONQUEST_TO_VANILLA_MATERIAL= new HashMap<>(); // = inverse of CONQUEST_TO_VANILLA_MATERIAL
    static{
        for (Map.Entry<String, String> entry : VANILLA_TO_CONQUEST_MATERIAL.entrySet()) {
            CONQUEST_TO_VANILLA_MATERIAL.put(entry.getValue(), entry.getKey());
        }
    }
    private static final List<String> VARIANTS = new ArrayList<>(List.of("_vertical_slab", "_stairs", "_quarter_slab", "_vertical_quarter", "_eighth_slab", "_vertical_corner_slab", "_corner_slab", "_vertical_corner", "_slab"));

    // some static methods for translation between sub-block lists and actual blocks:
    private static int binaryCrossSum(int n) {
        int binaryCrossSum = 0;
        while (n != 0) {
            binaryCrossSum += n & 1;
            n >>= 1;
        }
        return binaryCrossSum;
    }
    private static String fixConquestNames(String material, String variant) {
        int i = material.indexOf(":") + 1;
        String blockName = material.substring(i);
        String origin = material.substring(0,i);
        if (variant.isEmpty()) { // thus material comes from editSession.getBlock(...)
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
                    if (variant.equals("_stairs") && VARIANTS_EXIST_IN_VANILLA.contains(blockName)) { // block is a stair or slab that exists in the vanilla game (e.g. stone_stairs)
                        return material + variant;
                    }
                    if (VANILLA_TO_CONQUEST_MATERIAL.containsKey(blockName)) { // variants do NOT exist in vanilla (only in conquest) but with different names (e.g. limestone_quarter_slab)
                        return "conquest:" + VANILLA_TO_CONQUEST_MATERIAL.get(blockName) + variant;
                    }
                    return "conquest:" + blockName + variant; // by default assume that it is a conquest variant AND it uses the same name as the vanilla game
            }
        }
        return "minecraft:air";
    }
    private static int separatorIndex(String blockId) { // returns index of first "_" character that separates the block name from the variation (e.g. limestone_slab)
        int index;
        for (String variant : VARIANTS) {
            index = blockId.indexOf(variant);
            if (index>=0) {return index;}
        }
        return -1;
    }
    private static Object safeCall(Map<String, Object> blockStates, String key) {
        if (blockStates.containsKey(key)) {
            return blockStates.get(key);
        }
        return switch (key) {
            case "layers", "layer" -> 1;
            case "type", "half" -> "bottom";
            case "shape" -> "straight";
            case "facing" -> Direction.NORTH;
            case "hinge" -> "left";
            default -> null;
        };
    }
    private static int blockShape(String blockId, String blockVariant, Map<String, Object> blockStates) {
        //returns an int from 0 to 255 encoding the block shape, i.e. which 1/8-sub-blocks are present
        // (assuming that for each of the variants all the relevant state-value pairs exist in 'blockStates')
        Integer layers;
        String half;
        switch(blockVariant){
            case "": return  0b11111111;
            case "slab":
                if (blockStates.containsKey("layers")) {layers = (Integer) blockStates.get("layers");} // for 8-layer slabs
                else {layers = (Integer)safeCall(blockStates, "layer");} // for 4-layer slabs
                if (blockId.startsWith("minecraft")) {layers = 4;} // vanilla slabs don't have layers, so interpret as conquest slab with 4 layers
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
    private static int nExposedFaces(int shape, int[] neighbors) {
        int N = 0;
        // add silhouettes in x-/y-/z- directions
        N += (shape & 0b00000011) !=0 ? 2 : 0;
        N += (shape & 0b00001100) !=0 ? 2 : 0;
        N += (shape & 0b00110000) !=0 ? 2 : 0;
        N += (shape & 0b11000000) !=0 ? 2 : 0;

        N += (shape & 0b10001000) !=0 ? 2 : 0;
        N += (shape & 0b01000100) !=0 ? 2 : 0;
        N += (shape & 0b00100010) !=0 ? 2 : 0;
        N += (shape & 0b00010001) !=0 ? 2 : 0;

        N += (shape & 0b10100000) !=0 ? 2 : 0;
        N += (shape & 0b01010000) !=0 ? 2 : 0;
        N += (shape & 0b00001010) !=0 ? 2 : 0;
        N += (shape & 0b00000101) !=0 ? 2 : 0;

        // subtract 1 for each point where two faces touch
        int[] I = new int[]{0,0,0,0, 1,1,1,1, 2,2,2,2, 3,3,3,3, 4,4,4,4, 5,5,5,5};
        int[] n = new int[]{4,5,6,7, 0,1,2,3, 2,3,6,7, 0,1,4,5, 1,3,5,7, 0,2,4,6};
        int[] m = new int[]{0,1,2,3, 4,5,6,7, 0,1,4,5, 2,3,6,7, 0,2,4,6, 1,3,5,7};
        for (int i=0; i<24; i++) {
            N -= ((shape & 1<<n[i]) != 0 && (neighbors[I[i]] & 1<<m[i]) != 0) ? 1 : 0;
        }

        return N;
    }

    // some non-static methods (because they need the 'offsetVector' or 'additive' field or the getBlock method):
    private BlockState composeBlock(String material, int shape, boolean waterlogged) {
        int binaryCrossSum;
        if (shape==0b11111111) { // full block
            return (new BlockType(material)).getDefaultState();
        }
        if (shape==0) { // air or water
            return new BlockType(waterlogged ? "minecraft:water" : "minecraft:air").getDefaultState();
        }
        // obtain the block variant (stair/slab/...) first:
        String blockVariant;
        boolean upper = (shape & 0b11001100)==0; // true iff upper half is EMPTY
        boolean lower = (shape & 0b00110011)==0; // true iff lower half is EMPTY
        boolean isVertical = !upper && !lower; // true iff both upper and lower half of block are occupied
        boolean SE = (shape & 0b10100000)==0; // south-east quarter is EMPTY
        boolean SW = (shape & 0b00001010)==0; // south-west ...
        boolean NW = (shape & 0b00000101)==0; // ...
        boolean NE = (shape & 0b01010000)==0; // ...
        boolean SE_full = (shape & 0b10100000)==0b10100000; // south-east quarter is FULL
        boolean SW_full = (shape & 0b00001010)==0b00001010; // south-west ...
        boolean NW_full = (shape & 0b00000101)==0b00000101; // ...
        boolean NE_full = (shape & 0b01010000)==0b01010000; // ...
        boolean N = NE && NW; // north half is EMPTY
        boolean S = SE && SW; // south ...
        boolean W = NW && SW; // ...
        boolean E = NE && SE; // ...
        binaryCrossSum = binaryCrossSum(shape); // number of occupied 1/8th sub-blocks
        switch(binaryCrossSum) {
            case 1: blockVariant = "_eighth_slab"; break;
            case 2: blockVariant = (isVertical ? "_vertical_quarter" : "_quarter_slab"); break;
            case 3: blockVariant = (isVertical ? "_vertical_corner_slab" : "_corner_slab"); break;
            case 4: blockVariant = (isVertical ? "_vertical_slab" : "_slab"); break;
            default:
                if (SE || SW || NW || NE) {
                    blockVariant = "_vertical_corner";
                }
                else{
                    blockVariant = "_stairs";
                }
        }

        // create the BlockState object
        BlockState blockState = new BlockType(fixConquestNames(material, blockVariant)).getDefaultState();
        blockState = blockState.with(WATERLOGGED, waterlogged);

        // obtain the block data from the given shape:
        switch(blockVariant) {
            case "_slab": blockState = blockState.with(LAYERS, 4).with(LAYER4,3); // give both LAYERS (layers=1..8) and LAYER4 (layer=1..4) properties so it works for all conquest blocks, the invalid property will be ignored when placed
                blockState = blockState.with(TYPE, upper ? "bottom" : "top");
                break;
            case "_vertical_slab": blockState = blockState.with(LAYER4, 3);
                if (N) {blockState = blockState.with(FACING, Direction.NORTH);}
                if (S) {blockState = blockState.with(FACING, Direction.SOUTH);}
                if (W) {blockState = blockState.with(FACING, Direction.WEST);}
                if (E) {blockState = blockState.with(FACING, Direction.EAST);}
                break;
            case "_stairs":
                switch(binaryCrossSum) {
                    case 5: blockState = blockState.with(SHAPE, "outer_left");
                        if (NW_full) {
                            blockState = blockState.with(FACING, Direction.NORTH);
                            blockState = blockState.with(HALF, shape==0b00110111 ? "bottom" : "top");
                        }
                        if (NE_full) {
                            blockState = blockState.with(FACING, Direction.EAST);
                            blockState = blockState.with(HALF, shape==0b01110011 ? "bottom" : "top");
                        }
                        if (SE_full) {
                            blockState = blockState.with(FACING, Direction.SOUTH);
                            blockState = blockState.with(HALF, shape==0b10110011 ? "bottom" : "top");
                        }
                        if (SW_full) {
                            blockState = blockState.with(FACING, Direction.WEST);
                            blockState = blockState.with(HALF, shape==0b00111011 ? "bottom" : "top");
                        }
                        break;
                    case 6: blockState = blockState.with(SHAPE, "straight");
                        if (NW_full && NE_full) {
                            blockState = blockState.with(FACING, Direction.NORTH);
                            blockState = blockState.with(HALF, shape==0b01110111 ? "bottom" : "top");
                        }
                        if (NE_full && SE_full) {
                            blockState = blockState.with(FACING, Direction.EAST);
                            blockState = blockState.with(HALF, shape==0b11110011 ? "bottom" : "top");
                        }
                        if (SE_full && SW_full) {
                            blockState = blockState.with(FACING, Direction.SOUTH);
                            blockState = blockState.with(HALF, shape==0b10111011 ? "bottom" : "top");
                        }
                        if (SW_full && NW_full) {
                            blockState = blockState.with(FACING, Direction.WEST);
                            blockState = blockState.with(HALF, shape==0b00111111 ? "bottom" : "top");
                        }
                        break;
                    case 7: blockState = blockState.with(SHAPE, "inner_left");
                        if (!NW_full) {
                            blockState = blockState.with(FACING, Direction.SOUTH);
                            blockState = blockState.with(HALF, shape==0b11111011 ? "bottom" : "top");
                        }
                        if (!NE_full) {
                            blockState = blockState.with(FACING, Direction.WEST);
                            blockState = blockState.with(HALF, shape==0b10111111 ? "bottom" : "top");
                        }
                        if (!SE_full) {
                            blockState = blockState.with(FACING, Direction.NORTH);
                            blockState = blockState.with(HALF, shape==0b01111111 ? "bottom" : "top");
                        }
                        if (!SW_full) {
                            blockState = blockState.with(FACING, Direction.EAST);
                            blockState = blockState.with(HALF, shape==0b11110111 ? "bottom" : "top");
                        }
                }
                break;
            case "_vertical_corner": blockState = blockState.with(LAYER4, 3);
                if (SE) {blockState = blockState.with(FACING, Direction.SOUTH);}
                if (SW) {blockState = blockState.with(FACING, Direction.WEST);}
                if (NW) {blockState = blockState.with(FACING, Direction.NORTH);}
                if (NE) {blockState = blockState.with(FACING, Direction.EAST);}
                break;
            case "_quarter_slab": blockState = blockState.with(LAYER3, 2);
                blockState = blockState.with(TYPE, upper ? "bottom" : "top");
                if (N) {blockState = blockState.with(FACING, Direction.NORTH);}
                if (S) {blockState = blockState.with(FACING, Direction.SOUTH);}
                if (W) {blockState = blockState.with(FACING, Direction.WEST);}
                if (E) {blockState = blockState.with(FACING, Direction.EAST);}
                break;
            case "_vertical_quarter": blockState = blockState.with(LAYER4, 3);
                if (!SE) {blockState = blockState.with(FACING, Direction.NORTH);}
                if (!SW) {blockState = blockState.with(FACING, Direction.EAST);}
                if (!NW) {blockState = blockState.with(FACING, Direction.SOUTH);}
                if (!NE) {blockState = blockState.with(FACING, Direction.WEST);}
                break;
            case "_eighth_slab": blockState = blockState.with(TYPE, upper ? "bottom" : "top");
                if (!SE) {blockState = blockState.with(FACING, Direction.NORTH);}
                if (!SW) {blockState = blockState.with(FACING, Direction.EAST);}
                if (!NW) {blockState = blockState.with(FACING, Direction.SOUTH);}
                if (!NE) {blockState = blockState.with(FACING, Direction.WEST);}
                break;
            case "_corner_slab": blockState = blockState.with(TYPE, upper ? "bottom" : "top");
                if (SE) {blockState = blockState.with(FACING, Direction.SOUTH);}
                if (SW) {blockState = blockState.with(FACING, Direction.WEST);}
                if (NW) {blockState = blockState.with(FACING, Direction.NORTH);}
                if (NE) {blockState = blockState.with(FACING, Direction.EAST);}
                break;
            case "_vertical_corner_slab":
                if (N) {
                    blockState = blockState.with(FACING, Direction.NORTH);
                    if (SE_full) {
                        blockState = blockState.with(HINGE, "left");
                        blockState = blockState.with(TYPE, shape==0b10100010 ? "bottom" : "top");
                    }
                    else {
                        blockState = blockState.with(HINGE, "right");
                        blockState = blockState.with(TYPE, shape==0b00101010 ? "bottom" : "top");
                    }
                }
                if (S) {
                    blockState = blockState.with(FACING, Direction.SOUTH);
                    if (NW_full) {
                        blockState = blockState.with(HINGE, "right");
                        blockState = blockState.with(TYPE, shape==0b00010101 ? "bottom" : "top");
                    }
                    else {
                        blockState = blockState.with(HINGE, "left");
                        blockState = blockState.with(TYPE, shape==0b01010001 ? "bottom" : "top");
                    }

                }
                if (W) {
                    blockState = blockState.with(FACING, Direction.WEST);
                    if (NE_full) {
                        blockState = blockState.with(HINGE, "right");
                        blockState = blockState.with(TYPE, shape==0b01110000 ? "bottom" : "top");
                    }
                    else {
                        blockState = blockState.with(HINGE, "left");
                        blockState = blockState.with(TYPE, shape==0b10110000 ? "bottom" : "top");
                    }

                }
                if (E) {
                    blockState = blockState.with(FACING, Direction.EAST);
                    if (SW_full) {
                        blockState = blockState.with(HINGE, "left");
                        blockState = blockState.with(TYPE, shape==0b00001011 ? "bottom" : "top");
                    }
                    else {
                        blockState = blockState.with(HINGE, "right");
                        blockState = blockState.with(TYPE, shape==0b00000111 ? "bottom" : "top");
                    }
                }
        }
        return blockState;
    }
    private String getSubBlock(BlockVector3 position, int subBlockPosition) {
        BlockState block = this.editSession.getBlock(position);
        if (block==null) {block = BlockTypes.AIR.getDefaultState();} // replace null by air block
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
        if ((blockShape(blockId, blockVariant, blockStates) & (1 << subBlockPosition)) != 0){
            return material;
        }
        else if (blockStates.containsKey("waterlogged") ? (boolean) blockStates.get("waterlogged") : false) {
            // water
            return "minecraft:water";
        }
        else {
            //air
            return "minecraft:air";
        }
    }
    private BlockVector3 divBy2(BlockVector3 position) {
        BlockVector3 diff = position.subtract(this.offsetVector);
        return BlockVector3.at(Math.floorDiv(diff.getX(),2), Math.floorDiv(diff.getY(),2), Math.floorDiv(diff.getZ(),2)).add(this.offsetVector);
    }
    private int mod2(BlockVector3 position) {
        BlockVector3 diff = position.subtract(this.offsetVector);
        return 4*Math.floorMod(diff.getX(),2) + 2*Math.floorMod(diff.getY(),2) + Math.floorMod(diff.getZ(),2);
    }

    // original (except for perform and all the getBlock[Data]/setBlock methods)
    @Override
    public void handleCommand(String[] parameters, Snipe snipe) {
        Sniper sniper = snipe.getSniper();
        Player player = sniper.getPlayer();
        player.sendSystemMessage(Component.literal(ChatFormatting.RED + "This brush does not accept additional parameters."));
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

    private void setOffsetVector(Snipe snipe) {
        // use player data to determine which subblock you're looking at:
        Sniper sniper = snipe.getSniper();
        Player player = sniper.getPlayer();

        // the exact point on the hitbox of the block the player is looking at
        Vector3 location = PlatformAdapter.adapt(player.level().clip(new ClipContext(player.getEyePosition(1.0F), player.getEyePosition(1.0F).add(player.getLookAngle().scale((double) sniper.getCurrentToolkit().getProperties().getBlockTracerRange())), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getLocation());

        // a normal vector to the surface (i.e. pointing to the outside of the targeted sub-block)
        Vector3 normal = PlatformAdapter.adapt(new Vec3(player.level().clip(new ClipContext(player.getEyePosition(1.0F), player.getEyePosition(1.0F).add(player.getLookAngle().scale((double) sniper.getCurrentToolkit().getProperties().getBlockTracerRange())), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getDirection().step()));

        double dx,dy,dz;
        dx = location.getX() - targetBlock.getX();
        dy = location.getY() - targetBlock.getY();
        dz = location.getZ() - targetBlock.getZ();
        boolean uncertain = false;
        BlockVector3 subBlock1 = BlockVector3.ZERO;
        BlockVector3 subBlock2 = BlockVector3.ZERO;

        double eps = 1e-13;
        if (Math.abs(dx - 0.5)<eps) {subBlock1 = subBlock1.withX(1); uncertain = true;}
        else if (dx>0.5)            {subBlock1 = subBlock1.withX(1); subBlock2 = subBlock2.withX(1);}
        if (Math.abs(dy - 0.5)<eps) {subBlock1 = subBlock1.withY(1); uncertain = true;}
        else if (dy>0.5)            {subBlock1 = subBlock1.withY(1); subBlock2 = subBlock2.withY(1);}
        if (Math.abs(dz - 0.5)<eps) {subBlock1 = subBlock1.withZ(1); uncertain = true;}
        else if (dz>0.5)            {subBlock1 = subBlock1.withZ(1); subBlock2 = subBlock2.withZ(1);}

        // in case 'location' is exactly in between two sub-blocks (which is very likely),
        // use normal vector to determine which sub-block is targeted:
        this.offsetVector = targetBlock.add(uncertain && (((subBlock1.toVector3().multiply(0.5).add(Vector3.ONE.multiply(0.25))).subtract(dx,dy,dz)).dot(normal)<0) ? subBlock1 : subBlock2);

    }

    @Override
    public void perform(Snipe snipe, ToolAction action, EditSession editSession, BlockVector3 targetBlock, BlockVector3 lastBlock) {
        this.editSession = editSession;
        this.targetBlock = targetBlock;
        this.lastBlock = lastBlock;
        this.useSmallBlocks = this.canUseSmallBlocks && snipe.getSniper().smallBlocksEnabled();

        if (useSmallBlocks) {
            setOffsetVector(snipe);
            // set the offset vector used in the scaling method divBy2(),
            // this vector is equal to targetBlock +0 or +1 in each coordinate
            // and contains the information, which 1/8th-sub-block the player is targeting

            this.toDoList = new HashMap<>();
            // initialize the to-do-list
        }

        // perform the brush action
        // in SmallBlocks mode, instead of placing the blocks, they will be added to 'toDoList'
        if (action == ToolAction.ARROW) {
            handleArrowAction(snipe);
        }
        else if (action == ToolAction.WAND) {
            handleGunpowderAction(snipe);
        }

        if (useSmallBlocks) {
            // actually place down all the blocks from 'toDoList':
            Map<BlockVector3,BlockInformation> blocks = new HashMap<>();
            BlockVector3 position;
            String[] block2x2x2;
            boolean waterlogged;
            int shape, max, n;
            String currentMaterial, material;
            Map<String, Integer> possibleMaterials;
            for (Map.Entry<BlockVector3, String[]> entry : this.toDoList.entrySet()) {
                position = entry.getKey();
                block2x2x2 = entry.getValue(); // block as 2x2x2

                // determine material and shape of the block to be set:
                waterlogged = false;
                shape = 0;
                material = "minecraft:air";
                max = 0;
                possibleMaterials = new HashMap<>();
                for (int i = 0; i < 8; i++) {
                    currentMaterial = block2x2x2[i];
                    if (currentMaterial == null) { // get block from editSession if not present in toDoList:
                        currentMaterial = getSubBlock(position, i);
                    }
                    if (currentMaterial.equals("minecraft:water")) {
                        waterlogged = true;
                    }
                    if (!currentMaterial.equals("minecraft:air") && !currentMaterial.equals("minecraft:cave_air") && !currentMaterial.equals("minecraft:void_air") && !currentMaterial.equals("minecraft:water") && !currentMaterial.equals("minecraft:lava")) {
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
                blocks.put(position, new BlockInformation(material, shape, waterlogged));
            }

            // remove the (potentially quite long) list to free memory
            this.toDoList = null;

            BlockInformation block;
            int newShape,min;
            int[] neighbors;
            BlockVector3[] neighborPositions;
            Set<Integer> candidates;
            for (Map.Entry<BlockVector3, BlockInformation> entry : blocks.entrySet()) {
                position = entry.getKey();
                block = entry.getValue();
                newShape = block.shape;
                if (impossibleShapes.contains(block.shape)) {
                    neighborPositions = new BlockVector3[]{position.add(BlockVector3.UNIT_X), position.add(BlockVector3.UNIT_MINUS_X), position.add(BlockVector3.UNIT_Y), position.add(BlockVector3.UNIT_MINUS_Y), position.add(BlockVector3.UNIT_Z), position.add(BlockVector3.UNIT_MINUS_Z)};
                    neighbors = new int[8];
                    for (int i=0; i<6; i++) {
                        if (blocks.containsKey(neighborPositions[i])) {
                            neighbors[i] = blocks.get(neighborPositions[i]).shape;
                        } else {
                            BlockState blockState = this.editSession.getBlock(neighborPositions[i]);
                            if (blockState==null) {blockState = BlockTypes.AIR.getDefaultState();} // replace null by air block
                            String blockId = blockState.getBlockType().getId();
                            if (blockId.equals("minecraft:air") || blockId.equals("minecraft:cave_air") || blockId.equals("minecraft:void_air") || blockId.equals("minecraft:water") || blockId.equals("minecraft:lava")) {
                                neighbors[i] = 0;
                            }
                            else {
                                // 1) extract block variant (stairs/slab/quarter_slab/...)
                                int index = separatorIndex(blockId);
                                String blockVariant = index==-1 ? "" : blockId.substring(index+1);

                                // 2) get a Map encoding all the relevant block-states and ...
                                Map<String, Object> blockStates = new HashMap<>();
                                for (Map.Entry<Property<?>, Object> entr : blockState.getStates().entrySet()) {
                                    String propertyName = entr.getKey().getName();
                                    Object propertyValue = entr.getValue();
                                    blockStates.put(propertyName, (propertyValue instanceof String) ? ((String)propertyValue).toLowerCase(Locale.ROOT) : propertyValue);
                                }
                                // 3) ... obtain the block shape
                                neighbors[i] = blockShape(blockId, blockVariant, blockStates);
                            }
                        }
                    }

                    boolean additive = (action == ToolAction.ARROW) == this.additiveBrush;
                    // whether the brush action will add (true) or remove (false) blocks, inverted for gunpowder action
                    // this changes how block-shapes, that do not exist in Conquest, are represented
                    candidates = additive ? GET_SHAPE_ADDITIVE.get(block.shape) : GET_SHAPE_SUBTRACTIVE.get(block.shape);
                    min=25;
                    for (int candidate : candidates){
                        n = nExposedFaces(candidate, neighbors);
                        if (n<min)
                        {
                            min = n;
                            newShape = candidate;
                        }
                        else if (n==min && binaryCrossSum(candidate)==6) { // give priority for stairs/corner-blocks, looks better oftentimes
                            newShape = candidate;
                        }
                    }
                }

                // finally set the block:
                try {
                    this.editSession.setBlock(position, composeBlock(block.material, newShape, block.waterlogged));
                } catch (MaxChangedBlocksException except) {
                    except.printStackTrace();
                }
            }

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

    private void addBlockToList(BlockVector3 position, String blockName) {
        // add block-name at half position at the correct sub-block coordinate into the toDoList
        BlockVector3 halfPosition = divBy2(position);
        if (!toDoList.containsKey(halfPosition)) {
            toDoList.put(halfPosition, new String[8]);
        }
        toDoList.get(halfPosition)[mod2(position)] = blockName;
    }

    public void setBlock(BlockVector3 position, Pattern pattern) throws MaxChangedBlocksException {
        if (useSmallBlocks) {
            String blockName = pattern.applyBlock(position).getBlockType().getId(); // extract block Type from Pattern object
            addBlockToList(position,blockName);
        }
        else {
            editSession.setBlock(position, pattern);
        }
    }

    public void setBlock(int x, int y, int z, Pattern pattern) throws MaxChangedBlocksException {
        setBlock(BlockVector3.at(x, y, z), pattern);
    }

    public void setBlock(int x, int y, int z, BaseBlock block) throws MaxChangedBlocksException {
        if (useSmallBlocks){
            String blockName = block.getBlockType().getId(); // extract block Type from BaseBlock object
            addBlockToList(BlockVector3.at(x, y, z),blockName);
        }
        else {
            this.editSession.setBlock(BlockVector3.at(x, y, z), block);
        }
    }

    public void setBlockData(BlockVector3 position, BlockState blockState) throws MaxChangedBlocksException {
        if (useSmallBlocks) {
            String blockName = blockState.getBlockType().getId();
            addBlockToList(position,blockName);
        }
        else {
            editSession.setBlock(position, blockState);
        }
    }

    public void setBlockData(int x, int y, int z, BlockState blockState) throws MaxChangedBlocksException {
        setBlockData(BlockVector3.at(x, y, z),blockState);
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

    public BlockState getBlock(int x, int y, int z) {
        BlockVector3 position = BlockVector3.at(x, y, z);
        if (useSmallBlocks) {
            // return the sub-block at half coordinates instead
            BlockVector3 halfPosition = divBy2(position);  // halfed coordinate relative to the target block
            return (new BlockType(getSubBlock(halfPosition, mod2(position)))).getDefaultState();
        }
        else{
            return this.editSession.getBlock(position);
        }
    }

    public BlockState getBlock(BlockVector3 position) {
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();
        return this.getBlock(x, y, z);
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

    public void setAdditiveBrush(boolean additive) {
        this.additiveBrush = additive;
    }

    public boolean getAdditiveBrush() {
        return this.additiveBrush;
    }

    public void setCanUseSmallBlocks(boolean canUseSmallBlocks) {
        this.canUseSmallBlocks = canUseSmallBlocks;
    }

    @Override
    public HashMap<String, String> getSettings() {
        return this.settings;
    }


    class BlockInformation {
        final String material;
        final int shape;
        final boolean waterlogged;
        BlockInformation(String material, int shape, boolean waterlogged) {
            this.material = material;
            this.shape = shape;
            this.waterlogged = waterlogged;
        }
    }

    // WIP
    class EditSessionWrapper {
        private EditSession editSession;
        EditSessionWrapper(EditSession editSession) {
            this.editSession = editSession;
        }
    }
}
