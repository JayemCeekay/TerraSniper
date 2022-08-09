package com.jayemceekay.TerraSniper.brush.type.performer;

import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.TerraSniper.sniper.snipe.Snipe;
import com.jayemceekay.TerraSniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.TerraSniper.util.text.NumericParser;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class BlobBrush extends AbstractPerformerBrush {
    public BlobBrush() {
    }

    public void loadProperties() {
        this.growthPercentMin = 1;
        this.growthPercentMax = 9999;
        this.growthPercent = 1000;
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        int var5 = parameters.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            String parameter = parameters[var6];
            if (parameter.equalsIgnoreCase("info")) {
                messenger.sendMessage(ChatFormatting.GOLD + "Blob Brush Brush Parameters:");
                messenger.sendMessage(ChatFormatting.AQUA + "/b blob g[n] -- Sets the growth percentage to n (" + this.growthPercentMin + "-" + this.growthPercentMax + "). Default is " + 1000 + ".");
                return;
            }

            if (parameter.startsWith("g[")) {
                Integer growPercent = NumericParser.parseInteger(parameter.replace("g[", "").replace("]", ""));
                if (growPercent != null && growPercent >= super.growthPercentMin && growPercent <= super.growthPercentMax) {
                    this.growthPercent = growPercent;
                    messenger.sendMessage(ChatFormatting.AQUA + "Growth percent set to: " + this.growthPercent / 100 + "%");
                } else {
                    messenger.sendMessage(ChatFormatting.RED + "Growth percent must be an integer " + this.growthPercentMin + "-" + this.growthPercentMax + ".");
                }
            } else {
                messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters length! Use the \"info\" parameter to display parameter info.");
            }
        }

    }

    @Override
    public HashMap<String, String> getSettings() {
        this.settings.put("Growth Percent", this.growthPercent / 100 + "");
        return super.getSettings();
    }

    public List<String> handleCompletions(String[] parameters) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("g["), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("g["), "");
        }
    }

    public void handleArrowAction(Snipe snipe) {
        try {
            this.growBlob(snipe);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    public void handleGunpowderAction(Snipe snipe) {
        try {
            this.digBlob(snipe);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }


    private void digBlob(Snipe snipe) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        if (checkValidGrowPercent()) {
            SnipeMessenger messenger = snipe.createMessenger();
            messenger.sendMessage(ChatFormatting.BLUE + "Growth percent set to: " + this.growthPercent / 100 + "%");
        }
        // Seed the array
        int brushSizeDoubled = 2 * brushSize;
        int[][][] splat = new int[brushSizeDoubled + 1][brushSizeDoubled + 1][brushSizeDoubled + 1];
        for (int x = brushSizeDoubled; x >= 0; x--) {
            for (int y = brushSizeDoubled; y >= 0; y--) {
                for (int z = brushSizeDoubled; z >= 0; z--) {
                    splat[x][y][z] =
                            (x == 0 || y == 0 | z == 0 || x == brushSizeDoubled || y == brushSizeDoubled || z == brushSizeDoubled) && super.generator
                                    .nextInt(super.growthPercentMax + 1) <= this.growthPercent ? 0 : 1;
                }
            }
        }
        // Grow the seed
        int[][][] tempSplat = new int[brushSizeDoubled + 1][brushSizeDoubled + 1][brushSizeDoubled + 1];
        for (int r = 0; r < brushSize; r++) {
            for (int x = brushSizeDoubled; x >= 0; x--) {
                for (int y = brushSizeDoubled; y >= 0; y--) {
                    for (int z = brushSizeDoubled; z >= 0; z--) {
                        tempSplat[x][y][z] = splat[x][y][z];
                        double growCheck = 0;
                        if (splat[x][y][z] == 1) {
                            if (x != 0 && splat[x - 1][y][z] == 0) {
                                growCheck++;
                            }
                            if (y != 0 && splat[x][y - 1][z] == 0) {
                                growCheck++;
                            }
                            if (z != 0 && splat[x][y][z - 1] == 0) {
                                growCheck++;
                            }
                            if (x != 2 * brushSize && splat[x + 1][y][z] == 0) {
                                growCheck++;
                            }
                            if (y != 2 * brushSize && splat[x][y + 1][z] == 0) {
                                growCheck++;
                            }
                            if (z != 2 * brushSize && splat[x][y][z + 1] == 0) {
                                growCheck++;
                            }
                        }
                        if (growCheck >= 1 && super.generator.nextInt(super.growthPercentMax + 1) <= this.growthPercent) {
                            tempSplat[x][y][z] = 0; // prevent bleed into splat
                        }
                    }
                }
            }
            // shouldn't this just be splat = tempsplat;? -Gavjenks
            // integrate tempsplat back into splat at end of iteration
            for (int x = brushSizeDoubled; x >= 0; x--) {
                for (int y = brushSizeDoubled; y >= 0; y--) {
                    System.arraycopy(tempSplat[x][y], 0, splat[x][y], 0, brushSizeDoubled + 1);
                }
            }
        }
        double rSquared = Math.pow(brushSize + 1, 2);
        // Make the changes
        for (int x = brushSizeDoubled; x >= 0; x--) {
            double xSquared = Math.pow(x - brushSize - 1, 2);
            for (int y = brushSizeDoubled; y >= 0; y--) {
                double ySquared = Math.pow(y - brushSize - 1, 2);
                for (int z = brushSizeDoubled; z >= 0; z--) {
                    if (splat[x][y][z] == 1 && xSquared + ySquared + Math.pow(z - brushSize - 1, 2) <= rSquared) {
                        BlockVector3 targetBlock = this.getTargetBlock();
                        this.performer.perform(
                                getEditSession(),
                                targetBlock.getX() - brushSize + x,
                                clampY(targetBlock.getY() - brushSize + z),
                                targetBlock.getZ() - brushSize + y,
                                this.clampY(
                                        targetBlock.getX() - brushSize + x,
                                        targetBlock.getY() - brushSize + z,
                                        targetBlock.getZ() - brushSize + y
                                )
                        );
                    }
                }
            }
        }
    }

    private void growBlob(Snipe snipe) throws MaxChangedBlocksException {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        if (checkValidGrowPercent()) {
            SnipeMessenger messenger = snipe.createMessenger();
            messenger.sendMessage(ChatFormatting.BLUE + "Growth percent set to: " + this.growthPercent / 100 + "%");
        }
        // Seed the array
        int brushSizeDoubled = 2 * brushSize;
        int[][][] splat = new int[brushSizeDoubled + 1][brushSizeDoubled + 1][brushSizeDoubled + 1];
        splat[brushSize][brushSize][brushSize] = 1;
        // Grow the seed
        int[][][] tempSplat = new int[brushSizeDoubled + 1][brushSizeDoubled + 1][brushSizeDoubled + 1];
        for (int r = 0; r < brushSize; r++) {
            for (int x = brushSizeDoubled; x >= 0; x--) {
                for (int y = brushSizeDoubled; y >= 0; y--) {
                    for (int z = brushSizeDoubled; z >= 0; z--) {
                        tempSplat[x][y][z] = splat[x][y][z];
                        int growCheck = 0;
                        if (splat[x][y][z] == 0) {
                            if (x != 0 && splat[x - 1][y][z] == 1) {
                                growCheck++;
                            }
                            if (y != 0 && splat[x][y - 1][z] == 1) {
                                growCheck++;
                            }
                            if (z != 0 && splat[x][y][z - 1] == 1) {
                                growCheck++;
                            }
                            if (x != 2 * brushSize && splat[x + 1][y][z] == 1) {
                                growCheck++;
                            }
                            if (y != 2 * brushSize && splat[x][y + 1][z] == 1) {
                                growCheck++;
                            }
                            if (z != 2 * brushSize && splat[x][y][z + 1] == 1) {
                                growCheck++;
                            }
                        }
                        if (growCheck >= 1 && super.generator.nextInt(super.growthPercentMax + 1) <= this.growthPercent) {
                            // prevent bleed into splat
                            tempSplat[x][y][z] = 1;
                        }
                    }
                }
            }
            // integrate tempsplat back into splat at end of iteration
            for (int x = brushSizeDoubled; x >= 0; x--) {
                for (int y = brushSizeDoubled; y >= 0; y--) {
                    System.arraycopy(tempSplat[x][y], 0, splat[x][y], 0, brushSizeDoubled + 1);
                }
            }
        }
        double rSquared = Math.pow(brushSize + 1, 2);
        // Make the changes
        for (int x = brushSizeDoubled; x >= 0; x--) {
            double xSquared = Math.pow(x - brushSize - 1, 2);
            for (int y = brushSizeDoubled; y >= 0; y--) {
                double ySquared = Math.pow(y - brushSize - 1, 2);
                for (int z = brushSizeDoubled; z >= 0; z--) {
                    if (splat[x][y][z] == 1 && xSquared + ySquared + Math.pow(z - brushSize - 1, 2) <= rSquared) {
                        BlockVector3 targetBlock = this.getTargetBlock();
                        this.performer.perform(
                                getEditSession(),
                                targetBlock.getX() - brushSize + x,
                                clampY(targetBlock.getY() - brushSize + z),
                                targetBlock.getZ() - brushSize + y,
                                this.clampY(
                                        targetBlock.getX() - brushSize + x,
                                        targetBlock.getY() - brushSize + z,
                                        targetBlock.getZ() - brushSize + y
                                )
                        );
                    }
                }
            }
        }
    }

    private boolean checkValidGrowPercent() {
        if (this.growthPercent < super.growthPercentMin || this.growthPercent > super.growthPercentMax) {
            this.growthPercent = DEFAULT_GROWTH_PERCENT;
            return true;
        }
        return false;
    }

    @Override
    public void sendInfo(Snipe snipe) {
        checkValidGrowPercent();
        snipe.createMessageSender()
                .brushNameMessage()
                .brushSizeMessage()
                .message(ChatFormatting.BLUE + "Growth percent set to: " + this.growthPercent / 100 + "%")
                .send();
    }
}