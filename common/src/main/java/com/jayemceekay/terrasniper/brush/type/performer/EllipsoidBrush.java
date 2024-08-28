package com.jayemceekay.terrasniper.brush.type.performer;

import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.painter.Painters;
import com.jayemceekay.terrasniper.util.text.NumericParser;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class EllipsoidBrush extends AbstractPerformerBrush {
    private static final int DEFAULT_X_RAD = 0;
    private static final int DEFAULT_Y_RAD = 0;
    private static final int DEFAULT_Z_RAD = 0;
    private boolean offset;
    private double xRad = 0.0D;
    private double yRad = 0.0D;
    private double zRad = 0.0D;
    private boolean centerBlock=false;
    private String shape="full";

    public EllipsoidBrush() {
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        int var5 = parameters.length;

        for (String parameter : parameters) {
            if (parameter.equalsIgnoreCase("info")) {
                messenger.sendMessage(ChatFormatting.GOLD + "Ellipse Brush Parameters:");
                messenger.sendMessage(ChatFormatting.AQUA + "/b elo [true|false] -- Toggles offset. Default is false.");
                messenger.sendMessage(ChatFormatting.AQUA + "/b elo x [n] -- Sets X radius to n.");
                messenger.sendMessage(ChatFormatting.AQUA + "/b elo y [n] -- Sets Y radius to n.");
                messenger.sendMessage(ChatFormatting.AQUA + "/b elo z [n] -- Sets Z radius to n.");
                messenger.sendMessage(ChatFormatting.DARK_GREEN + "/b elo center -- Toggles whether the brush will center on a block or the corner of a block in smallBlocks mode");
                messenger.sendMessage(ChatFormatting.GREEN + "/b elo up/down/north/east/south/west/full -- changes shape to a half-ellipsoid facing the specified direction, 'full' changes back to a full ellipsoid");
                return;
            }

            if (parameter.equalsIgnoreCase("true")) {
                this.offset = true;
                messenger.sendMessage(ChatFormatting.AQUA + "Offset ON.");
            } else if (parameter.equalsIgnoreCase("false")) {
                this.offset = false;
                messenger.sendMessage(ChatFormatting.AQUA + "Offset OFF.");
            } else {
                Integer zRad;
                if (parameter.startsWith("x[")) {
                    zRad = NumericParser.parseInteger(parameter.replace("x[", "").replace("]", ""));
                    if (zRad != null) {
                        this.xRad = (double) zRad;
                        messenger.sendMessage(ChatFormatting.AQUA + "X radius set to: " + this.xRad);
                    } else {
                        messenger.sendMessage(ChatFormatting.RED + "Invalid number.");
                    }
                } else if (parameter.startsWith("y[")) {
                    zRad = NumericParser.parseInteger(parameter.replace("y[", "").replace("]", ""));
                    if (zRad != null) {
                        this.yRad = (double) zRad;
                        messenger.sendMessage(ChatFormatting.AQUA + "Y radius set to: " + this.yRad);
                    } else {
                        messenger.sendMessage(ChatFormatting.RED + "Invalid number.");
                    }
                } else if (parameter.startsWith("z[")) {
                    zRad = NumericParser.parseInteger(parameter.replace("z[", "").replace("]", ""));
                    if (zRad != null) {
                        this.zRad = (double) zRad;
                        messenger.sendMessage(ChatFormatting.AQUA + "Z radius set to: " + this.zRad);
                    } else {
                        messenger.sendMessage(ChatFormatting.RED + "Invalid number.");
                    }
                } else { if (parameter.equalsIgnoreCase("center")) {
                    if (!this.centerBlock) {
                        this.centerBlock = true;
                        messenger.sendMessage(ChatFormatting.AQUA + "centerBlock ON. The brush will now be centered on a block in smallBlocks mode.");
                    } else {
                        this.centerBlock = false;
                        messenger.sendMessage(ChatFormatting.AQUA + "centerBlock OFF. The brush will now be centered on the corner of a block in smallBlocks mode.");
                    }
                    } else { if (parameter.equalsIgnoreCase("full")) {
                        this.shape = "full";
                        messenger.sendMessage(ChatFormatting.AQUA + "changed back to a FULL ellipsoid.");
                    } else { if (parameter.equalsIgnoreCase("up")) {
                        this.shape = "up";
                        messenger.sendMessage(ChatFormatting.AQUA + "changed back to a half-ellipsoid facing UP.");
                    } else { if (parameter.equalsIgnoreCase("down")) {
                        this.shape = "down";
                        messenger.sendMessage(ChatFormatting.AQUA + "changed back to a half-ellipsoid facing DOWN.");
                    } else { if (parameter.equalsIgnoreCase("north")) {
                        this.shape = "north";
                        messenger.sendMessage(ChatFormatting.AQUA + "changed back to a half-ellipsoid facing NORTH.");
                    } else { if (parameter.equalsIgnoreCase("east")) {
                        this.shape = "east";
                        messenger.sendMessage(ChatFormatting.AQUA + "changed back to a half-ellipsoid facing EAST.");
                    } else { if (parameter.equalsIgnoreCase("south")) {
                        this.shape = "south";
                        messenger.sendMessage(ChatFormatting.AQUA + "changed back to a half-ellipsoid facing SOUTH.");
                    } else { if (parameter.equalsIgnoreCase("west")) {
                        this.shape = "west";
                        messenger.sendMessage(ChatFormatting.AQUA + "changed back to a half-ellipsoid facing WEST.");
                    } else {
                        messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters! Use the \"info\" parameter to display parameter info.");
                    }
                    }
                    }
                    }
                    }
                    }
                    }
                    }
                }
            }
        }

    }

    @Override
    public HashMap<String, String> getSettings() {
        this.settings.put("xRad", Double.toString(this.xRad));
        this.settings.put("yRad", Double.toString(this.yRad));
        this.settings.put("zRad", Double.toString(this.zRad));
        this.settings.put("offset", Boolean.toString(this.offset));
        return super.getSettings();
    }

    public List<String> handleCompletions(String[] parameters, Snipe snip) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("center", "full", "up", "down", "north", "east", "south", "west", "true", "false", "x[", "y[", "z["), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("center", "full", "up", "down", "north", "east", "south", "west", "true", "false", "x[", "y[", "z["), "");
        }
    }

    public void handleArrowAction(Snipe snipe) {
        BlockVector3 targetBlock = this.getTargetBlock();

        try {
            this.execute(targetBlock);
        } catch (MaxChangedBlocksException var4) {
            var4.printStackTrace();
        }

    }

    public void handleGunpowderAction(Snipe snipe) {
        BlockVector3 lastBlock = this.getLastBlock();

        try {
            this.execute(lastBlock);
        } catch (MaxChangedBlocksException var4) {
            var4.printStackTrace();
        }

    }

    private void execute(BlockVector3 targetBlock) throws MaxChangedBlocksException {
        int xDirection = 0;
        int yDirection = 0;
        int zDirection = 0;
        switch (this.shape) {
            case "up":
                yDirection = 1;
                break;
            case "down":
                yDirection = -1;
                break;
            case "east":
                xDirection = 1;
                break;
            case "west":
                xDirection = -1;
                break;
            case "south":
                zDirection = 1;
                break;
            case "north":
                zDirection = -1;
                break;
        }
        boolean centering = (useSmallBlocks && centerBlock);
        int sizeOffset = centering ? 1 : 0;
        double coordOffset = centering ? 1.0D/2.0D : 0.0D;

        int blockX = centering ? targetBlock.getX()*2-this.offsetVector.getX() : targetBlock.getX();
        int blockY = centering ? targetBlock.getY()*2-this.offsetVector.getY() : targetBlock.getY();
        int blockZ = centering ? targetBlock.getZ()*2-this.offsetVector.getZ() : targetBlock.getZ();

        this.performer.perform(this.getEditSession(), blockX, blockY, blockZ, this.getBlock(blockX, blockY, blockZ));
        double trueOffset = this.offset ? 0.5D : 0.0D;

        for (int x = 0; x <= this.xRad + sizeOffset; ++x) {
            double xSquared = (x - coordOffset) / (this.xRad + trueOffset + coordOffset) * ((x - coordOffset) / (this.xRad + trueOffset + coordOffset));

            for (int z = 0; z <= this.zRad + sizeOffset; ++z) {
                double zSquared = (z - coordOffset) / (this.zRad + trueOffset + coordOffset) * ((z - coordOffset) / (this.zRad + trueOffset + coordOffset));

                for (int y = 0; y <= this.yRad + sizeOffset; ++y) {
                    double ySquared = (y - coordOffset) / (this.yRad + trueOffset + coordOffset) * ((y - coordOffset) / (this.yRad + trueOffset + coordOffset));
                    if (xSquared + ySquared + zSquared <= 1.0D) {
                        if (xDirection >= 0 || (centering && x==1)) {
                            if (yDirection >= 0 || (centering && y==1)) {
                                if (zDirection >= 0 || (centering && z==1)) {
                                    this.performer.perform(this.getEditSession(), (int) ((double) blockX + x), this.clampY((int) ((double) blockY + y)), (int) ((double) blockZ + z), this.clampY((int) ((double) blockX + x), (int) ((double) blockY + y), (int) ((double) blockZ + z)));
                                }
                                if (zDirection <= 0 || (centering && z==1)) {
                                    this.performer.perform(this.getEditSession(), (int) ((double) blockX + x), this.clampY((int) ((double) blockY + y)), (int) ((double) blockZ - z + sizeOffset), this.clampY((int) ((double) blockX + x), (int) ((double) blockY + y), (int) ((double) blockZ - z + sizeOffset)));
                                }
                            }
                            if (yDirection <= 0 || (centering && y==1)) {
                                if (zDirection >= 0 || (centering && z==1)) {
                                    this.performer.perform(this.getEditSession(), (int) ((double) blockX + x), this.clampY((int) ((double) blockY - y + sizeOffset)), (int) ((double) blockZ + z), this.clampY((int) ((double) blockX + x), (int) ((double) blockY - y + sizeOffset), (int) ((double) blockZ + z)));
                                }
                                if (zDirection <= 0 || (centering && z==1)) {
                                    this.performer.perform(this.getEditSession(), (int) ((double) blockX + x), this.clampY((int) ((double) blockY - y + sizeOffset)), (int) ((double) blockZ - z + sizeOffset), this.clampY((int) ((double) blockX + x), (int) ((double) blockY - y + sizeOffset), (int) ((double) blockZ - z + sizeOffset)));
                                }
                            }
                        }
                        if (xDirection <= 0 || (centering && x==1)) {
                            if (yDirection >= 0 || (centering && y==1)) {
                                if (zDirection >= 0 || (centering && z==1)) {
                                    this.performer.perform(this.getEditSession(), (int) ((double) blockX - x + sizeOffset), this.clampY((int) ((double) blockY + y)), (int) ((double) blockZ + z), this.clampY((int) ((double) blockX - x + sizeOffset), (int) ((double) blockY + y), (int) ((double) blockZ + z)));
                                }
                                if (zDirection <= 0 || (centering && z==1)) {
                                    this.performer.perform(this.getEditSession(), (int) ((double) blockX - x + sizeOffset), this.clampY((int) ((double) blockY + y)), (int) ((double) blockZ - z + sizeOffset), this.clampY((int) ((double) blockX - x + sizeOffset), (int) ((double) blockY + y), (int) ((double) blockZ - z + sizeOffset)));
                                }
                            }
                            if (yDirection <= 0 || (centering && y==1)) {
                                if (zDirection >= 0 || (centering && z==1)) {
                                    this.performer.perform(this.getEditSession(), (int) ((double) blockX - x + sizeOffset), this.clampY((int) ((double) blockY - y + sizeOffset)), (int) ((double) blockZ + z), this.clampY((int) ((double) blockX - x + sizeOffset), (int) ((double) blockY - y + sizeOffset), (int) ((double) blockZ + z)));
                                }
                                if (zDirection <= 0 || (centering && z==1)) {
                                    this.performer.perform(this.getEditSession(), (int) ((double) blockX - x + sizeOffset), this.clampY((int) ((double) blockY - y + sizeOffset)), (int) ((double) blockZ - z + sizeOffset), this.clampY((int) ((double) blockX - x + sizeOffset), (int) ((double) blockY - y + sizeOffset), (int) ((double) blockZ - z + sizeOffset)));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void sendInfo(Snipe snipe) {
        snipe.createMessageSender().brushNameMessage().message(ChatFormatting.AQUA + "X-size set to: " + ChatFormatting.DARK_AQUA + this.xRad).message(ChatFormatting.AQUA + "Y-size set to: " + ChatFormatting.DARK_AQUA + this.yRad).message(ChatFormatting.AQUA + "Z-size set to: " + ChatFormatting.DARK_AQUA + this.zRad).send();
    }
}
