package com.jayemceekay.terrasniper.brush.type;

import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.text.NumericParser;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.lang.Thread;
import java.util.stream.Stream;


public class LayerBrush extends AbstractBrush {
    private boolean smallBlocksEnabled = false;
    private static final Map<Integer,BlockVector3[]> POSITIONS = new HashMap<>();
    private static final Map<BlockVector3,Integer> LAYER_DIRECTION_FROM_BLOCKVEC = Map.ofEntries(
        Map.entry(BlockVector3.UNIT_X      , LAYER_WEST),
        Map.entry(BlockVector3.UNIT_MINUS_X, LAYER_EAST),
        Map.entry(BlockVector3.UNIT_Y      , LAYER_DOWN),
        Map.entry(BlockVector3.UNIT_MINUS_Y, LAYER_UP),
        Map.entry(BlockVector3.UNIT_Z      , LAYER_NORTH),
        Map.entry(BlockVector3.UNIT_MINUS_Z, LAYER_SOUTH)
    );
    private static final Map<Integer,BlockVector3> BLOCKVEC_FROM_LAYER_DIRECTION = Map.ofEntries(
            Map.entry(LAYER_WEST , BlockVector3.UNIT_X      ),
            Map.entry(LAYER_EAST , BlockVector3.UNIT_MINUS_X),
            Map.entry(LAYER_DOWN , BlockVector3.UNIT_Y      ),
            Map.entry(LAYER_UP   , BlockVector3.UNIT_MINUS_Y),
            Map.entry(LAYER_NORTH, BlockVector3.UNIT_Z      ),
            Map.entry(LAYER_SOUTH, BlockVector3.UNIT_MINUS_Z)
    );

    private static final ReplacementRule AVERAGE    = (h,nh) -> ((2*(h + Arrays.stream(nh).sum()) + 5 ) / 10);
    private static final ReplacementRule RAISE      = (h,nh) -> ((2*(h + Arrays.stream(nh).sum()) + 10) / 10);
    private static final ReplacementRule LOWER      = (h,nh) -> ((2*(h + Arrays.stream(nh).sum())     ) / 10);
    private static final ReplacementRule PERSISTENT = (h,nh) -> (((2*h + Arrays.stream(nh).sum()) + 3 ) / 6);
    private static final ReplacementRule MAX        = (h,nh) -> Math.max(Math.max(Math.max((2*(h+nh[0]+nh[1])+3)/6, (2*(h+nh[0]+nh[2])+3)/6), (2*(h+nh[0]+nh[3])+3)/6), Math.max(Math.max((2*(h+nh[1]+nh[2])+3)/6, (2*(h+nh[1]+nh[3])+3)/6), (2*(h+nh[2]+nh[3])+3)/6));
    private static final ReplacementRule MIN        = (h,nh) -> Math.min(Math.min(Math.min((2*(h+nh[0]+nh[1])+3)/6, (2*(h+nh[0]+nh[2])+3)/6), (2*(h+nh[0]+nh[3])+3)/6), Math.min(Math.min((2*(h+nh[1]+nh[2])+3)/6, (2*(h+nh[1]+nh[3])+3)/6), (2*(h+nh[2]+nh[3])+3)/6));
    private static final ReplacementRule MAXIG      = (h,nh) -> Math.max(Math.max(Math.max(((nh[0]+nh[1])+1)/2, ((nh[0]+nh[2])+1)/2), ((nh[0]+nh[3])+1)/2), Math.max(Math.max(((nh[1]+nh[2])+1)/2, ((nh[1]+nh[3])+1)/2), ((nh[2]+nh[3])+1)/2));
    private static final ReplacementRule MINIG      = (h,nh) -> Math.min(Math.min(Math.min(((nh[0]+nh[1])+1)/2, ((nh[0]+nh[2])+1)/2), ((nh[0]+nh[3])+1)/2), Math.min(Math.min(((nh[1]+nh[2])+1)/2, ((nh[1]+nh[3])+1)/2), ((nh[2]+nh[3])+1)/2));

    private static final Map<String,ReplacementRule[]> RULE_MAP = Map.ofEntries(
            Map.entry("average", new ReplacementRule[]{AVERAGE, AVERAGE}),
            Map.entry("raise", new ReplacementRule[]{RAISE, LOWER}),
            Map.entry("lower", new ReplacementRule[]{LOWER, RAISE}),
            Map.entry("max", new ReplacementRule[]{MAX, MIN}),
            Map.entry("min", new ReplacementRule[]{MIN, MAX}),
            Map.entry("maxig", new ReplacementRule[]{MAXIG, MINIG}),
            Map.entry("minig", new ReplacementRule[]{MINIG, MAXIG}),
            Map.entry("persistent", new ReplacementRule[]{PERSISTENT, PERSISTENT})
            );
    private static final Map<String, Boolean> IS_ADDITIVE = Map.ofEntries(
            Map.entry("average", true),
            Map.entry("raise", true),
            Map.entry("max", true),
            Map.entry("min", false),
            Map.entry("maxig", true),
            Map.entry("minig", false),
            Map.entry("persistent", true)
    );

    private String smoothingMode = "average";
    private int numberOfIterations = 1;

    public interface ReplacementRule {
        int apply(int height, int[] neighborHeights);
    }

    static {
        POSITIONS.put(LAYER_UP,    new BlockVector3[] {BlockVector3.UNIT_MINUS_Z,BlockVector3.UNIT_X,BlockVector3.UNIT_Z,BlockVector3.UNIT_MINUS_X});
        POSITIONS.put(LAYER_DOWN,  new BlockVector3[] {BlockVector3.UNIT_MINUS_Z,BlockVector3.UNIT_MINUS_X,BlockVector3.UNIT_Z,BlockVector3.UNIT_X});
        POSITIONS.put(LAYER_NORTH, new BlockVector3[] {BlockVector3.UNIT_Y,BlockVector3.UNIT_MINUS_X,BlockVector3.UNIT_MINUS_Y,BlockVector3.UNIT_X});
        POSITIONS.put(LAYER_EAST,  new BlockVector3[] {BlockVector3.UNIT_Y,BlockVector3.UNIT_MINUS_Z,BlockVector3.UNIT_MINUS_Y,BlockVector3.UNIT_Z});
        POSITIONS.put(LAYER_SOUTH, new BlockVector3[] {BlockVector3.UNIT_Y,BlockVector3.UNIT_X,BlockVector3.UNIT_MINUS_Y,BlockVector3.UNIT_MINUS_X});
        POSITIONS.put(LAYER_WEST,  new BlockVector3[] {BlockVector3.UNIT_Y,BlockVector3.UNIT_Z,BlockVector3.UNIT_MINUS_Y,BlockVector3.UNIT_MINUS_Z});
    }

    public LayerBrush() {
        this.setCanUseSmallBlocks(false);
        this.setCanUseAutoLayer(false);
        this.setAdditiveBrush(true);
    }

    public void handleArrowAction(Snipe snipe) {
        this.smallBlocksEnabled = snipe.getSniper().smallBlocksEnabled();
        this.layer(snipe, RULE_MAP.get(this.smoothingMode)[0]);
    }

    public void handleGunpowderAction(Snipe snipe) {
        this.smallBlocksEnabled = snipe.getSniper().smallBlocksEnabled();
        this.layer(snipe, RULE_MAP.get(this.smoothingMode)[1]);
    }

    public static void pause() {pause(2);}
    public static void pause(int t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void layer(Snipe snipe, ReplacementRule replacementRule) {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        BlockVector3 targetBlock = this.getTargetBlock();
        BlockChangeTracker blockChangeTracker = new BlockChangeTracker(this, brushSize, targetBlock);
        
        //pause(); System.out.println(" ==== FLATTENING ================================================================================================================================================================");
        // (1) replace blocks in "flat" areas with matching layers:
        blockChangeTracker.init();

        //pause(); System.out.println(" ==== SMOOTHING ("+iterations+" iterations) ===============================================================================================================================================================");
        // (2) "smoothing" iterations:
        for (int i = 1; i <= this.numberOfIterations; i++) {
            //pause(); System.out.println(" ==== iteration "+i+" ===============================================================================================================================================================");
            blockChangeTracker.layerIteration(i, replacementRule);
        }

        // (3) get list of all changed blocks
        Iterator blockIterator = blockChangeTracker.getAll().iterator();

        // (4) set the blocks
        while (blockIterator.hasNext()) {
            BlockWrapper blockWrapper = (BlockWrapper) blockIterator.next();
            int shape = blockWrapper.getShape() + (1<<8)*blockWrapper.getHeight() + (1<<11)*blockWrapper.getDirection();
            if (blockWrapper.getHeight()==0 && blockWrapper.getDirection()!=0) {shape = 0;}
            if (blockWrapper.getHeight()==8) {shape = 0b11111111;}
            //pause(); System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
            //pause(); System.out.println("composing block with");
            //pause(); System.out.println("position:    " + blockWrapper.getX() + ", " + blockWrapper.getY() + ", " + blockWrapper.getZ());
            //pause(); System.out.println("material:    " + blockWrapper.getMaterial());
            //pause(); System.out.println("shape:       " + Integer.toBinaryString(shape));
            //pause(); System.out.println("waterlogged: " + blockWrapper.getWaterlogged());
            BlockState block = composeBlock(blockWrapper.getMaterial(), shape, blockWrapper.getWaterlogged());
            try {
                //System.out.println("setting    BLOCK "+block+"    at    LOCATION "+blockWrapper.getX()+","+blockWrapper.getY()+","+blockWrapper.getZ());
                setBlock(BlockVector3.at(blockWrapper.getX(), blockWrapper.getY(), blockWrapper.getZ()), block);
            } catch (MaxChangedBlocksException except) {
                except.printStackTrace();
            }
        }
    }

    private boolean isSurface(int x, int y, int z) {
        return !isEmpty(x, y, z) && (isEmpty(x, y-1, z) || isEmpty(x, y+1, z) || isEmpty(x+1, y, z) || isEmpty(x-1, y, z) || isEmpty(x, y, z+1) || isEmpty(x, y, z-1));
    }

    private boolean isEmpty(int x, int y, int z) {
        return getBlock(x, y, z).getBlockType().getMaterial().isAir();
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();

        for (String parameter : parameters) {
            if (parameter.equalsIgnoreCase("info")) {
                messenger.sendMessage(ChatFormatting.AQUA + "smoothing mode: " + this.smoothingMode);
                messenger.sendMessage(ChatFormatting.AQUA + "iterations: " + this.numberOfIterations);
                continue;
            }

            if (RULE_MAP.containsKey(parameter)) {
                this.setAdditiveBrush(IS_ADDITIVE.get(parameter));
                this.smoothingMode = parameter;
                messenger.sendMessage(ChatFormatting.LIGHT_PURPLE + "Smoothing preset set to: " + parameter);
                continue;
            }

            try {
                this.numberOfIterations = Integer.parseInt(parameter);
                messenger.sendMessage(ChatFormatting.LIGHT_PURPLE + "Number of iterations set to: " + parameter);
            } catch (NumberFormatException nfe) {
                messenger.sendMessage(ChatFormatting.RED + "Invalid argument: " + parameter);
            }
        }
    }
/*
    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("average", "raise", "lower", "persistent"), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("average", "raise", "lower", "persistent"), "");
        }
    }*/
    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        return parameters.length > 0 ?
                SuggestionHelper.limitByPrefix(RULE_MAP.keySet().stream(), parameters[parameters.length - 1]) :
                SuggestionHelper.limitByPrefix(RULE_MAP.keySet().stream(), "");
    }

    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
        messenger.sendBrushSizeMessage();
        messenger.sendMessage(ChatFormatting.AQUA + "smoothing mode: "+this.smoothingMode);
        messenger.sendMessage(ChatFormatting.AQUA + "iterations: "+this.numberOfIterations);
   }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        if (this.setBlockBuffer != null) {
            if (this.setBlockBuffer.containsKey(position)) {
                // for the layer brush in autoLayer mode, read from the list of already set blocks instead:
                return this.setBlockBuffer.get(position);
            }
        }

        //return this.getEditSession().getBlock(position);
        return actuallyGetBlock(position);
    }

    static class BlockChangeTracker {
        private final Map<Integer, Map<BlockVector3, BlockWrapper>> blockChanges;
        //private final Map<BlockVector3, BlockWrapper> flatChanges;
        private final LayerBrush brushReference;
        private final int brushSize;
        private final int targetX;
        private final int targetY;
        private final int targetZ;
        private Set<BlockVector3> positionSet;

        private BlockChangeTracker(LayerBrush brushReference, int brushSize, BlockVector3 target) {
            this.blockChanges = new HashMap<>();
            //this.flatChanges = new HashMap<>();
            this.brushReference = brushReference;
            this.brushSize = brushSize;
            this.targetX = target.getX();
            this.targetY = target.getY();
            this.targetZ = target.getZ();
            this.positionSet = new HashSet<>();
        }

        public void init() {
            for (int x = targetX - brushSize; x <= targetX + brushSize; ++x) {
                for (int y = targetY - brushSize; y <= targetY + brushSize; ++y) {
                    for (int z = targetZ - brushSize; z <= targetZ + brushSize; ++z) {
                        if (Math.pow(x - targetX, 2.0D) + Math.pow(y - targetY, 2.0D) + Math.pow(z - targetZ, 2.0D) <= Math.pow(brushSize, 2.0D)) {
                            BlockVector3 currentPosition = BlockVector3.at(x,y,z);
                            BlockWrapper block = this.get(currentPosition, 0);
                            BlockVector3[] neighborPositions = new BlockVector3[]{currentPosition.add(BlockVector3.UNIT_Y), currentPosition.add(BlockVector3.UNIT_MINUS_Y), currentPosition.add(BlockVector3.UNIT_MINUS_Z), currentPosition.add(BlockVector3.UNIT_X), currentPosition.add(BlockVector3.UNIT_Z), currentPosition.add(BlockVector3.UNIT_MINUS_X)}; // U,D,N,E,S,W
                            BlockWrapper[] neighbors = new BlockWrapper[6];
                            for(int i=0; i<6; i++) {
                                neighbors[i] = this.get(neighborPositions[i], 0);
                            }
                            boolean isSurface = block.initSurface(neighbors, brushReference.smallBlocksEnabled);
                            if (isSurface) {this.put(currentPosition,block,0);}
                        }
                    }
                }
            }
        }

        public void layerIteration(int currentIteration, ReplacementRule rule) {
            if (!this.blockChanges.containsKey(currentIteration)) {
                this.blockChanges.put(currentIteration, new HashMap<>());
            }
            this.positionSet.addAll(this.blockChanges.get(currentIteration-1).keySet());

            /*
            for (int x = targetX - brushSize; x <= targetX + brushSize; ++x) {
                for (int y = targetY - brushSize; y <= targetY + brushSize; ++y) {
                    for (int z = targetZ - brushSize; z <= targetZ + brushSize; ++z) {
                        if (Math.pow(x - targetX, 2.0D) + Math.pow(y - targetY, 2.0D) + Math.pow(z - targetZ, 2.0D) <= Math.pow(brushSize, 2.0D)) {
                            BlockVector3 currentPosition = BlockVector3.at(x, y, z);
                            */
            
            for(BlockVector3 currentPosition : this.positionSet)
            {
                BlockWrapper currentBlock = this.get(currentPosition, currentIteration);
                int surface = currentBlock.getSurface();
                //pause(); System.out.println(" considering block in position "+currentPosition);
                //pause(); System.out.println(" surface :   " + surface);
                //pause(); System.out.println(" direction : " + currentBlock.getDirection());
                if(surface!=0)
                {
                    BlockVector3[] neighborPositions = POSITIONS.get(surface);
                    int[] neighborHeights = new int[4];
                    BlockWrapper neighbor;
                    for(int i=0; i<4; i++) {
                        neighbor = this.get(currentPosition.add(neighborPositions[i]), currentIteration);

                        if(neighbor.getSurface()==surface) {
                            neighborHeights[i] = neighbor.getHeight();
                        }
                        else { // neighbor is part of a surface of different orientation (and may not even be a slab) ==> get a "replacement height":
                            neighborHeights[i] = neighbor.getOuterSurface(neighborPositions[i]);
                            // might want to factor in the orientation of the outer surface as well ...
                        }
                    }
                    int height = currentBlock.getHeight();

                    int newHeight = Math.max(0,Math.min(8,rule.apply(height, neighborHeights)));
                    //pause(); System.out.println(" height          : " + height);
                    //pause(); System.out.println(" neighborHeights : " + Arrays.toString(neighborHeights));
                    //pause(); System.out.println(" newHeight       : " + newHeight);

                    if(newHeight!=height) {
                        // test below and MERGE with slabs in those layers!

                        int belowHeight = 8;
                        int aboveHeight = 0;

                        BlockVector3 belowPosition = currentPosition.subtract(BLOCKVEC_FROM_LAYER_DIRECTION.get(currentBlock.getDirection()));
                        BlockVector3 abovePosition = currentPosition.add(BLOCKVEC_FROM_LAYER_DIRECTION.get(currentBlock.getDirection()));
                        BlockWrapper belowBlock = this.get(belowPosition, currentIteration+1);
                        BlockWrapper aboveBlock = this.get(abovePosition, currentIteration+1);

                        if(this.blockChanges.get(currentIteration).containsKey(belowPosition) && belowBlock.getSurface()==surface) {
                            //belowBlock = this.blockChanges.get(currentIteration).get(belowPosition);
                            belowHeight = belowBlock.getHeight();
                            //pause(); System.out.println("    there's a changed layer BELOW!");
                        }
                        if(this.blockChanges.get(currentIteration).containsKey(abovePosition) && aboveBlock.getSurface()==surface) {
                            //aboveBlock = this.blockChanges.get(currentIteration).get(abovePosition);
                            aboveHeight = aboveBlock.getHeight();
                            //pause(); System.out.println("    there's a changed layer ABOVE!");
                        }

                        int totalHeight = belowHeight + newHeight + aboveHeight;
                        //pause(); System.out.println("    totalHeight: "+totalHeight+"="+belowHeight+"+"+newHeight+"+"+aboveHeight);

                        int[] heights = new int[3];
                        for(int i=0; i<3; i++) {
                            if(totalHeight>=8) {
                                heights[i] = 8;
                                totalHeight -= 8;
                            }
                            else {
                                heights[i] = totalHeight;
                                totalHeight = 0;
                            }
                        }

                        //pause(); System.out.println("    heights:" + Arrays.toString(heights));

                        if(belowHeight!=heights[0]) {
                            this.put(belowPosition, belowBlock.withHeight(heights[0]), currentIteration);
                            //pause(); System.out.println("    putting a layer BELOW!");
                        }
                        if(height!=heights[1]) {
                            this.put(currentPosition, currentBlock.withHeight(heights[1]), currentIteration);
                            //pause(); System.out.println("    putting a layer at CURRENT position!");
                            /*
                            if(height==0) {
                                for(int i=0; i<4; i++) {
                                    if (neighborHeights[i] == 0) { // neighbor of an air block that just got filled by a layer is also air
                                        BlockVector3 pos = currentPosition.add(neighborPositions[i]);
                                        neighbor = this.get(pos, currentIteration);
                                        BlockWrapper belowNeighbor = this.get(pos.add(BLOCKVEC_FROM_LAYER_DIRECTION.get(surface)), currentIteration);

                                        if (belowNeighbor.getSurface() == surface) {
                                            this.put(pos, neighbor, currentIteration);
                                            neighbor.surface = surface;
                                            neighbor.setDirection(surface);
                                        }
                                    }
                                }
                            }
                            if(height==8) {
                                for(int i=0; i<4; i++) {
                                    if (neighborHeights[i] == 8) { // neighbor of a full block that just got filled by a layer is also full
                                        BlockVector3 pos = currentPosition.add(neighborPositions[i]);
                                        neighbor = this.get(pos, currentIteration);
                                        BlockWrapper aboveNeighbor = this.get(pos.subtract(BLOCKVEC_FROM_LAYER_DIRECTION.get(surface)), currentIteration);

                                        if (aboveNeighbor.getSurface() == surface) {
                                            this.put(pos, neighbor, currentIteration);
                                            neighbor.surface = surface;
                                            neighbor.setDirection(surface);
                                        }
                                    }
                                }
                            }
                            */
                        }
                        if(aboveHeight!=heights[2]) {
                            this.put(abovePosition, aboveBlock.withHeight(heights[2]), currentIteration);
                            //pause(); System.out.println("    putting a layer ABOVE!");
                        }

                        //this.put(currentPosition, currentBlock.withHeight(newHeight), currentIteration);
                    }
                }
            }

                        /*
                        }
                    }
                }
            }
            */

        }


        public BlockWrapper get(int x, int y, int z, int iteration) {
            return (this.get(BlockVector3.at(x,y,z), iteration));
        }

        public BlockWrapper get(BlockVector3 position, int iteration) {
            for (int i = iteration - 1; i >= 0; --i) {
                if (this.blockChanges.containsKey(i) && this.blockChanges.get(i).containsKey(position)) {
                    return (BlockWrapper) ((Map) this.blockChanges.get(i)).get(position);
                }
            }

            String material;
            boolean is_waterlogged=false;

            // 1) get block from edit session
            BlockState block = this.brushReference.getBlock(position);
            if (block==null) {block = BlockTypes.AIR.getDefaultState();} // replace null by air block
            String blockId = block.getBlockType().getId();

            // 2) separate material name and translate to the minecraft name (e.g. "stone" from "stone_stairs" but also from "limestone_slab")
            int i = separatorIndex(blockId);
            material = i==-1 ? fixConquestNames(blockId,"") : fixConquestNames(blockId.substring(0,i),"");
            String blockVariant = i==-1 ? "" : blockId.substring(i+1);

            // 3) test if the block is waterlogged
            for (Map.Entry<Property<?>, Object> entry : block.getStates().entrySet()) {
                if(entry.getKey().getName().equals("waterlogged")) {is_waterlogged = (boolean) entry.getValue();}
            }
            // 4) ... get shape of the block (as int)
            int shape;
            if (blockId.equals("minecraft:air") || blockId.equals("minecraft:cave_air") || blockId.equals("minecraft:void_air") || blockId.equals("minecraft:water") || blockId.equals("minecraft:lava")) {
                shape = 0;
            }
            else {
                shape = this.brushReference.blockShape(material, blockVariant, block, position, false);
            }
            if (blockId.equals("minecraft:water")) {
                is_waterlogged = true;
            }

            return new BlockWrapper(position.getBlockX(), position.getBlockY(), position.getBlockZ(), material, shape, is_waterlogged);
        }

        public Collection<BlockWrapper> getAll() {
            Map<BlockVector3,BlockWrapper> flatChanges = new HashMap<>();
            for(int i=0; this.blockChanges.containsKey(i); i++) {
                for (Map.Entry<BlockVector3, BlockWrapper> entry : this.blockChanges.get(i).entrySet()) {
                    BlockVector3 position = entry.getKey();
                    BlockWrapper block = entry.getValue();
                    if(block.hasChanged()) {
                        if (Math.pow(position.getX() - targetX, 2.0D) + Math.pow(position.getY() - targetY, 2.0D) + Math.pow(position.getZ() - targetZ, 2.0D) <= Math.pow(brushSize, 2.0D)) {
                            flatChanges.put(position, block);
                        }
                    }
                }
            }

            return flatChanges.values();
        }

        public void put(BlockVector3 position, BlockWrapper changedBlock, int iteration) {
            if (!this.blockChanges.containsKey(iteration)) {
                this.blockChanges.put(iteration, new HashMap<>());
            }

            ((Map) this.blockChanges.get(iteration)).put(position, changedBlock);
        }
    }

    static class BlockWrapper {
        private final int x;
        private final int y;
        private final int z;
        @Nullable
        private String material;
        private final int shape;
        private int direction;
        private final int height;
        private final boolean waterlogged;
        private int surface;
        private boolean changed;

        private BlockWrapper(int x, int y, int z, String material) {
            this(x, y, z, material, 0b11111111, false); // default: full block
        }
        private BlockWrapper(int x, int y, int z, @Nullable String material, int shape, int direction, int height, boolean waterlogged, int surface, boolean changed) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.material = material;
            this.shape = shape;
            this.direction = direction;
            this.height = height;
            this.waterlogged = waterlogged;
            this.surface=surface;
            this.changed=changed;
        }
        private BlockWrapper(int x, int y, int z, @Nullable String material, int full_shape, boolean waterlogged) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.material = material;
            this.shape = full_shape % (1<<8);
            this.direction = full_shape>>11;
            this.height = direction!=0 ? ((full_shape % (1<<11))>>8) : binaryCrossSum(this.shape);
            this.waterlogged = waterlogged;
            this.surface=0;
            this.changed=false;
        }

        public BlockWrapper withHeight(int newHeight) {
            boolean changed = (newHeight!=this.height);
            return new BlockWrapper(this.x, this.y, this.z, this.material, this.shape, this.direction, newHeight, this.waterlogged, this.surface, changed);
        }

        private static int binaryCrossSum(int n) {
            int binaryCrossSum = 0;
            while (n != 0) {
                binaryCrossSum += n & 1;
                n >>= 1;
            }
            return binaryCrossSum;
        }

        private int getInnerSurface(int dir) // in 1/8th squared block-lengths
        {
            if(this.direction!=0) {
                return switch (dir) {
                    case LAYER_UP    -> direction==LAYER_DOWN  ? 8 : 0;
                    case LAYER_DOWN  -> direction==LAYER_UP    ? 8 : 0;
                    case LAYER_NORTH -> direction==LAYER_SOUTH ? 8 : 0;
                    case LAYER_EAST  -> direction==LAYER_WEST  ? 8 : 0;
                    case LAYER_SOUTH -> direction==LAYER_NORTH ? 8 : 0;
                    case LAYER_WEST  -> direction==LAYER_EAST  ? 8 : 0;
                    default -> 0;
                };
            }
            return switch (dir) {
                case LAYER_UP    -> -2 * ((((shape>>2)&1)==1 ? 1-((shape>>0)&1) : 0) + (((shape>>3)&1)==1 ? 1-((shape>>1)&1) : 0) + (((shape>>6)&1)==1 ? 1-((shape>>4)&1) : 0) + (((shape>>7)&1)==1 ? 1-((shape>>5)&1) : 0));
                case LAYER_DOWN  -> -2 * ((((shape>>0)&1)==1 ? 1-((shape>>2)&1) : 0) + (((shape>>1)&1)==1 ? 1-((shape>>3)&1) : 0) + (((shape>>4)&1)==1 ? 1-((shape>>6)&1) : 0) + (((shape>>5)&1)==1 ? 1-((shape>>7)&1) : 0));
                case LAYER_NORTH -> -2 * ((((shape>>0)&1)==1 ? 1-((shape>>1)&1) : 0) + (((shape>>2)&1)==1 ? 1-((shape>>3)&1) : 0) + (((shape>>4)&1)==1 ? 1-((shape>>5)&1) : 0) + (((shape>>6)&1)==1 ? 1-((shape>>7)&1) : 0));
                case LAYER_EAST  -> -2 * ((((shape>>4)&1)==1 ? 1-((shape>>0)&1) : 0) + (((shape>>5)&1)==1 ? 1-((shape>>1)&1) : 0) + (((shape>>6)&1)==1 ? 1-((shape>>2)&1) : 0) + (((shape>>7)&1)==1 ? 1-((shape>>3)&1) : 0));
                case LAYER_SOUTH -> -2 * ((((shape>>1)&1)==1 ? 1-((shape>>0)&1) : 0) + (((shape>>3)&1)==1 ? 1-((shape>>2)&1) : 0) + (((shape>>5)&1)==1 ? 1-((shape>>4)&1) : 0) + (((shape>>7)&1)==1 ? 1-((shape>>6)&1) : 0));
                case LAYER_WEST  -> -2 * ((((shape>>0)&1)==1 ? 1-((shape>>4)&1) : 0) + (((shape>>1)&1)==1 ? 1-((shape>>5)&1) : 0) + (((shape>>2)&1)==1 ? 1-((shape>>6)&1) : 0) + (((shape>>3)&1)==1 ? 1-((shape>>7)&1) : 0));
                default -> 0;
            };
        }
        private int getOuterSurface(int dir)
        {
            if(this.direction!=0) {
                return switch (dir) {
                    case LAYER_UP    -> direction==LAYER_UP    ? 8 : direction==LAYER_DOWN  ? 0 : this.height;
                    case LAYER_DOWN  -> direction==LAYER_DOWN  ? 8 : direction==LAYER_UP    ? 0 : this.height;
                    case LAYER_NORTH -> direction==LAYER_NORTH ? 8 : direction==LAYER_SOUTH ? 0 : this.height;
                    case LAYER_EAST  -> direction==LAYER_EAST  ? 8 : direction==LAYER_WEST  ? 0 : this.height;
                    case LAYER_SOUTH -> direction==LAYER_SOUTH ? 8 : direction==LAYER_NORTH ? 0 : this.height;
                    case LAYER_WEST  -> direction==LAYER_WEST  ? 8 : direction==LAYER_EAST  ? 0 : this.height;
                    default -> 0;
                };
            }
            return switch (dir) {
                case LAYER_UP    -> 2 * binaryCrossSum(this.shape & 0b11001100);
                case LAYER_DOWN  -> 2 * binaryCrossSum(this.shape & 0b00110011);
                case LAYER_NORTH -> 2 * binaryCrossSum(this.shape & 0b01010101);
                case LAYER_EAST  -> 2 * binaryCrossSum(this.shape & 0b11110000);
                case LAYER_SOUTH -> 2 * binaryCrossSum(this.shape & 0b10101010);
                case LAYER_WEST  -> 2 * binaryCrossSum(this.shape & 0b00001111);
                default -> 0;
            };
        }
        public int getOuterSurface(BlockVector3 neighborPosition) {
            return getOuterSurface(LAYER_DIRECTION_FROM_BLOCKVEC.get(neighborPosition));
        }
        public boolean initSurface(BlockWrapper[] neighbors, boolean smallBlocksEnabled) {
            this.surface = 0;
            int Nix = this.getInnerSurface(LAYER_EAST)  - this.getInnerSurface(LAYER_WEST) ;
            int Niy = this.getInnerSurface(LAYER_UP)    - this.getInnerSurface(LAYER_DOWN) ;
            int Niz = this.getInnerSurface(LAYER_SOUTH) - this.getInnerSurface(LAYER_NORTH);
            int Nox = this.getOuterSurface(LAYER_EAST)  - this.getOuterSurface(LAYER_WEST)  + neighbors[LAYER_WEST -1].getOuterSurface(LAYER_EAST)  - neighbors[LAYER_EAST -1].getOuterSurface(LAYER_WEST);
            int Noy = this.getOuterSurface(LAYER_UP)    - this.getOuterSurface(LAYER_DOWN)  + neighbors[LAYER_DOWN -1].getOuterSurface(LAYER_UP)    - neighbors[LAYER_UP   -1].getOuterSurface(LAYER_DOWN);
            int Noz = this.getOuterSurface(LAYER_SOUTH) - this.getOuterSurface(LAYER_NORTH) + neighbors[LAYER_NORTH-1].getOuterSurface(LAYER_SOUTH) - neighbors[LAYER_SOUTH-1].getOuterSurface(LAYER_NORTH);
            int Nx = Nix + Nox;
            int Ny = Niy + Noy;
            int Nz = Niz + Noz;
            //pause(); System.out.println(" ------------ init Surface --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- ");
            //pause(); System.out.println("block:         " + this.x + ", " + this.y + ", " + this.z);
            //pause(); System.out.println("material:      " + this.material);
            //pause(); System.out.println("height:        " + this.height);
            //pause(); System.out.println("direction:     " + this.direction);
            //pause(); System.out.println("normal vector: " + Nx + ", " + Ny + ", " + Nz);
            //pause(); System.out.println("inner part:    " + Nix + ", " + Niy + ", " + Niz);
            //pause(); System.out.println("outer part:    " + Nox + ", " + Noy + ", " + Noz);

            //BlockVector3 normalVector = BlockVector3.at(Nx,Ny,Nz);
            //Set<Integer> candidates = new HashSet<>();


            boolean isSurface = false;
            if(Math.abs(Ny)==8 && (isSmall(Math.abs(Nx), Math.abs(Nz)) || !smallBlocksEnabled)) {
                this.surface = Ny>0 ? LAYER_DOWN : LAYER_UP;
                setDirection(this.surface);
                //pause(); System.out.println(" ... set surface & direction to " + this.surface);
                isSurface = true;
            }
            if(Math.abs(Nx)==8 && isSmall(Math.abs(Ny), Math.abs(Nz)) && smallBlocksEnabled) {
                this.surface = Nx>0 ? LAYER_WEST : LAYER_EAST;
                setDirection(this.surface);
                //pause(); System.out.println(" ... set surface & direction to " + this.surface);
                isSurface = true;
            }
            if(Math.abs(Nz)==8 && isSmall(Math.abs(Nx), Math.abs(Ny)) && smallBlocksEnabled) {
                this.surface = Nz>0 ? LAYER_NORTH : LAYER_SOUTH;
                setDirection(this.surface);
                //pause(); System.out.println(" ... set surface & direction to " + this.surface);
                isSurface = true;
            }
            if(Nx!=0 || Ny!=0 || Nz!=0) {
                //pause(); System.out.println(" ... is surface (but may not be 'flat') ");
                isSurface = true;
            }

            // pull material of empty blocks that are marked to be replaced by layers to the material of the block underneath
            if(surface!=0 && (this.isAir() || this.isLiquid())) {
                //pause(); System.out.println(" ... is air or liquid, pulling material from direction " + this.surface + " with material " + neighbors[this.surface-1].getMaterial());
                this.material = neighbors[this.surface-1].getMaterial();
            }

            return isSurface;
        }

        private static boolean isSmall(int abs1, int abs2) {
            return (abs1<=4 && abs2<=6) || (abs1<=5 && abs2<=5) || (abs1<=6 && abs2<=4);
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

        private void setDirection(int direction) {
            if(direction!=this.direction && (this.height!=8 || this.direction==0)) { // for full blocks, changing the direction it's facing doesnt change the block
                this.direction = direction;
                this.changed = true;
            }
        }

        public boolean hasChanged() {
            return this.changed;
        }

        @Nullable
        public String getMaterial() {
            return this.material;
        }

        public int getShape() {
            return this.shape;
        }

        public int getHeight() {
            return this.height;
        }

        public int getDirection() {
            return this.direction;
        }

        public int getSurface() {
            return this.surface;
        }

        public boolean isEmpty() {
            return (this.shape==0);
        }

        private boolean isAir() {
            return (this.material.equals("minecraft:air") || this.material.equals("minecraft:cave_air") || this.material.equals("minecraft:void_air"));
        }

        public boolean isLiquid() {
            return (this.material.equals("minecraft:water") || this.material.equals("minecraft:lava"));
        }

        public boolean getWaterlogged() {
            return this.waterlogged;
        }
    }

}