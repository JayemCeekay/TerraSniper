package com.jayemceekay.terrasniper.brush.type.performer.splatter;

import com.jayemceekay.terrasniper.brush.type.performer.AbstractPerformerBrush;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.text.NumericParser;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import net.minecraft.ChatFormatting;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class SplatterVoxelDiscBrush extends AbstractPerformerBrush {

    @Override
    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        String firstParameter = parameters[0];

        if (firstParameter.equalsIgnoreCase("info")) {
            messenger.sendMessage(ChatFormatting.GOLD + "Splatter Voxel Disc Brush Parameters:");
            messenger.sendMessage(ChatFormatting.AQUA + "/b svd s [n] -- Sets a seed percentage to n (1-9999). 100 = 1% Default is " +
                    "1000.");
            messenger.sendMessage(ChatFormatting.AQUA + "/b svd g [n] -- Sets a growth percentage to n (1-9999). Default is 1000.");
            messenger.sendMessage(ChatFormatting.AQUA + "/b svd r [n] -- Sets a recursion i (1-10). Default is 3.");
        } else {
            if (parameters.length == 2) {
                if (firstParameter.equalsIgnoreCase("s")) {
                    Integer seedPercent = NumericParser.parseInteger(parameters[1]);
                    if (seedPercent != null && seedPercent >= super.seedPercentMin && seedPercent <= super.seedPercentMax) {
                        this.seedPercent = seedPercent;
                        messenger.sendMessage(ChatFormatting.AQUA + "Seed percent set to: " + this.seedPercent / 100 + "%");
                    } else {
                        messenger.sendMessage(ChatFormatting.RED + "Seed percent must be an integer " + this.seedPercentMin +
                                "-" + this.seedPercentMax + ".");
                    }
                } else if (firstParameter.equalsIgnoreCase("g")) {
                    Integer growPercent = NumericParser.parseInteger(parameters[1]);
                    if (growPercent != null && growPercent >= super.growthPercentMin && growPercent <= super.growthPercentMax) {
                        this.growthPercent = growPercent;
                        messenger.sendMessage(ChatFormatting.AQUA + "Growth percent set to: " + this.growthPercent / 100 + "%");
                    } else {
                        messenger.sendMessage(ChatFormatting.RED + "Growth percent must be an integer " + this.growthPercentMin +
                                "-" + this.growthPercentMax + ".");
                    }
                } else if (firstParameter.equalsIgnoreCase("r")) {
                    Integer splatterRecursions = NumericParser.parseInteger(parameters[1]);
                    if (splatterRecursions != null && splatterRecursions >= super.splatterRecursionsMin
                            && splatterRecursions <= super.splatterRecursionsMax) {
                        this.splatterRecursions = splatterRecursions;
                        messenger.sendMessage(ChatFormatting.AQUA + "Recursions set to: " + this.splatterRecursions);
                    } else {
                        messenger.sendMessage(ChatFormatting.RED + "Recursions must be an integer " + this.splatterRecursionsMin +
                                "-" + this.splatterRecursionsMax + ".");
                    }
                } else {
                    messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters! Use the \"info\" parameter to display parameter info.");
                }
            } else {
                messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters length! Use the \"info\" parameter to display " +
                        "parameter info.");
            }
        }
    }

    @Override
    public HashMap<String, String> getSettings() {
        this.settings.put("Seed Percent", this.seedPercent / 100 + "");
        this.settings.put("Growth Percent", this.growthPercent / 100 + "");
        this.settings.put("Recursions", this.splatterRecursions + "");
        return super.getSettings();
    }

    @Override
    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        if (parameters.length == 1) {
            String parameter = parameters[0];
            return super.sortCompletions(Stream.of("s", "g", "r"), parameter, 0);
        }
        return super.handleCompletions(parameters, snipe);
    }

    @Override
    public void handleArrowAction(Snipe snipe) {
        BlockVector3 targetBlock = getTargetBlock();
        try {
            vSplatterDisc(snipe, targetBlock);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleGunpowderAction(Snipe snipe) {
        BlockVector3 lastBlock = getLastBlock();
        try {
            vSplatterDisc(snipe, lastBlock);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    private void vSplatterDisc(Snipe snipe, BlockVector3 targetBlock) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        SnipeMessenger messenger = snipe.createMessenger();
        if (this.seedPercent < super.seedPercentMin || this.seedPercent > super.seedPercentMax) {
            this.seedPercent = DEFAULT_SEED_PERCENT;
            messenger.sendMessage(ChatFormatting.BLUE + "Seed percent set to: " + this.seedPercent / 100 + "%");
        }
        if (this.growthPercent < super.growthPercentMin || this.growthPercent > super.growthPercentMax) {
            this.growthPercent = DEFAULT_GROWTH_PERCENT;
            messenger.sendMessage(ChatFormatting.BLUE + "Growth percent set to: " + this.growthPercent / 100 + "%");
        }
        if (this.splatterRecursions < super.splatterRecursionsMin || this.splatterRecursions > super.splatterRecursionsMax) {
            this.splatterRecursions = DEFAULT_SPLATTER_RECURSIONS;
            messenger.sendMessage(ChatFormatting.BLUE + "Recursions set to: " + this.splatterRecursions);
        }
        int brushSize = toolkitProperties.getBrushSize();
        int[][] splat = new int[2 * brushSize + 1][2 * brushSize + 1];
        // Seed the array
        for (int x = 2 * brushSize; x >= 0; x--) {
            for (int y = 2 * brushSize; y >= 0; y--) {
                if (super.generator.nextInt(super.seedPercentMax + 1) <= this.seedPercent) {
                    splat[x][y] = 1;
                }
            }
        }
        // Grow the seeds
        int gref = this.growthPercent;
        int[][] tempSplat = new int[2 * brushSize + 1][2 * brushSize + 1];
        for (int r = 0; r < this.splatterRecursions; r++) {
            this.growthPercent = gref - ((gref / this.splatterRecursions) * (r));
            for (int x = 2 * brushSize; x >= 0; x--) {
                for (int y = 2 * brushSize; y >= 0; y--) {
                    tempSplat[x][y] = splat[x][y]; // prime tempsplat
                    int growcheck = 0;
                    if (splat[x][y] == 0) {
                        if (x != 0 && splat[x - 1][y] == 1) {
                            growcheck++;
                        }
                        if (y != 0 && splat[x][y - 1] == 1) {
                            growcheck++;
                        }
                        if (x != 2 * brushSize && splat[x + 1][y] == 1) {
                            growcheck++;
                        }
                        if (y != 2 * brushSize && splat[x][y + 1] == 1) {
                            growcheck++;
                        }
                    }
                    if (growcheck >= 1 && super.generator.nextInt(super.growthPercentMax + 1) <= this.growthPercent) {
                        tempSplat[x][y] = 1; // prevent bleed into splat
                    }
                }
            }
            // integrate tempsplat back into splat at end of iteration
            for (int x = 2 * brushSize; x >= 0; x--) {
                if (2 * brushSize + 1 >= 0) {
                    System.arraycopy(tempSplat[x], 0, splat[x], 0, 2 * brushSize + 1);
                }
            }
        }
        this.growthPercent = gref;
        // Fill 1x1 holes
        for (int x = 2 * brushSize; x >= 0; x--) {
            for (int y = 2 * brushSize; y >= 0; y--) {
                if (splat[Math.max(x - 1, 0)][y] == 1 && splat[Math.min(x + 1, 2 * brushSize)][y] == 1 && splat[x][Math.max(
                        0,
                        y - 1
                )] == 1 && splat[x][Math.min(2 * brushSize, y + 1)] == 1) {
                    splat[x][y] = 1;
                }
            }
        }
        // Make the changes
        int blockX = targetBlock.getX();
        int blockY = targetBlock.getY();
        int blockZ = targetBlock.getZ();
        for (int x = 2 * brushSize; x >= 0; x--) {
            for (int y = 2 * brushSize; y >= 0; y--) {
                if (splat[x][y] == 1) {
                    this.performer.perform(
                            getEditSession(),
                            blockX - brushSize + x,
                            clampY(blockY),
                            blockZ - brushSize + y,
                            clampY(blockX - brushSize + x, blockY, blockZ - brushSize + y)
                    );
                }
            }
        }
    }

    @Override
    public void sendInfo(Snipe snipe) {
        if (this.seedPercent < super.seedPercentMin || this.seedPercent > super.seedPercentMax) {
            this.seedPercent = DEFAULT_SEED_PERCENT;
        }
        if (this.growthPercent < super.growthPercentMin || this.growthPercent > super.growthPercentMax) {
            this.growthPercent = DEFAULT_GROWTH_PERCENT;
        }
        if (this.splatterRecursions < super.splatterRecursionsMin || this.splatterRecursions > super.splatterRecursionsMax) {
            this.splatterRecursions = DEFAULT_SPLATTER_RECURSIONS;
        }
        snipe.createMessageSender()
                .brushNameMessage()
                .brushSizeMessage()
                .message(ChatFormatting.BLUE + "Seed percent set to: " + this.seedPercent / 100 + "%")
                .message(ChatFormatting.BLUE + "Growth percent set to: " + this.growthPercent / 100 + "%")
                .message(ChatFormatting.BLUE + "Recursions set to: " + this.splatterRecursions)
                .send();
    }

}
