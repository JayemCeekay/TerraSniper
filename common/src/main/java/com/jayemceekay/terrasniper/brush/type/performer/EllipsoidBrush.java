package com.jayemceekay.terrasniper.brush.type.performer;

import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
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
                } else {
                    messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters! Use the \"info\" parameter to display parameter info.");
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

    public List<String> handleCompletions(String[] parameters) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("true", "false", "x[", "y[", "z["), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("true", "false", "x[", "y[", "z["), "");
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
        int blockX = targetBlock.getX();
        int blockY = targetBlock.getY();
        int blockZ = targetBlock.getZ();
        this.performer.perform(this.getEditSession(), blockX, blockY, blockZ, this.getBlock(blockX, blockY, blockZ));
        double trueOffset = this.offset ? 0.5D : 0.0D;

        for (double x = 0.0D; x <= this.xRad; ++x) {
            double xSquared = x / (this.xRad + trueOffset) * (x / (this.xRad + trueOffset));

            for (double z = 0.0D; z <= this.zRad; ++z) {
                double zSquared = z / (this.zRad + trueOffset) * (z / (this.zRad + trueOffset));

                for (double y = 0.0D; y <= this.yRad; ++y) {
                    double ySquared = y / (this.yRad + trueOffset) * (y / (this.yRad + trueOffset));
                    if (xSquared + ySquared + zSquared <= 1.0D) {
                        this.performer.perform(this.getEditSession(), (int) ((double) blockX + x), this.clampY((int) ((double) blockY + y)), (int) ((double) blockZ + z), this.clampY((int) ((double) blockX + x), (int) ((double) blockY + y), (int) ((double) blockZ + z)));
                        this.performer.perform(this.getEditSession(), (int) ((double) blockX + x), this.clampY((int) ((double) blockY + y)), (int) ((double) blockZ - z), this.clampY((int) ((double) blockX + x), (int) ((double) blockY + y), (int) ((double) blockZ - z)));
                        this.performer.perform(this.getEditSession(), (int) ((double) blockX + x), this.clampY((int) ((double) blockY - y)), (int) ((double) blockZ + z), this.clampY((int) ((double) blockX + x), (int) ((double) blockY - y), (int) ((double) blockZ + z)));
                        this.performer.perform(this.getEditSession(), (int) ((double) blockX + x), this.clampY((int) ((double) blockY - y)), (int) ((double) blockZ - z), this.clampY((int) ((double) blockX + x), (int) ((double) blockY - y), (int) ((double) blockZ - z)));
                        this.performer.perform(this.getEditSession(), (int) ((double) blockX - x), this.clampY((int) ((double) blockY + y)), (int) ((double) blockZ + z), this.clampY((int) ((double) blockX - x), (int) ((double) blockY + y), (int) ((double) blockZ + z)));
                        this.performer.perform(this.getEditSession(), (int) ((double) blockX - x), this.clampY((int) ((double) blockY + y)), (int) ((double) blockZ - z), this.clampY((int) ((double) blockX - x), (int) ((double) blockY + y), (int) ((double) blockZ - z)));
                        this.performer.perform(this.getEditSession(), (int) ((double) blockX - x), this.clampY((int) ((double) blockY - y)), (int) ((double) blockZ + z), this.clampY((int) ((double) blockX - x), (int) ((double) blockY - y), (int) ((double) blockZ + z)));
                        this.performer.perform(this.getEditSession(), (int) ((double) blockX - x), this.clampY((int) ((double) blockY - y)), (int) ((double) blockZ - z), this.clampY((int) ((double) blockX - x), (int) ((double) blockY - y), (int) ((double) blockZ - z)));
                    }
                }
            }
        }

    }

    public void sendInfo(Snipe snipe) {
        snipe.createMessageSender().brushNameMessage().message(ChatFormatting.AQUA + "X-size set to: " + ChatFormatting.DARK_AQUA + this.xRad).message(ChatFormatting.AQUA + "Y-size set to: " + ChatFormatting.DARK_AQUA + this.yRad).message(ChatFormatting.AQUA + "Z-size set to: " + ChatFormatting.DARK_AQUA + this.zRad).send();
    }
}
