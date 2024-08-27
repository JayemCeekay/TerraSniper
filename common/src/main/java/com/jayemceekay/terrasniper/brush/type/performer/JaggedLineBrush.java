package com.jayemceekay.terrasniper.brush.type.performer;

import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.text.NumericParser;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class JaggedLineBrush extends AbstractPerformerBrush {
    private static final Vector3 HALF_BLOCK_OFFSET = Vector3.at(0.5D, 0.5D, 0.5D);
    private static final int RECURSION_MIN = 1;
    private static final int RECURSION_MAX = 10;
    private static final int DEFAULT_RECURSION = 3;
    private static final int DEFAULT_SPREAD = 3;
    private final Random random = new Random();
    private Vector3 originCoordinates;
    private Vector3 targetCoordinates;
    private int recursionMin;
    private int recursionMax;
    private int recursions;
    private int spread;

    public JaggedLineBrush() {
    }

    public void loadProperties() {
        this.recursionMin = 1;
        this.recursionMax = 10;
        this.recursions = 3;
        this.spread = 3;
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        int var5 = parameters.length;

        for (String parameter : parameters) {
            if (parameter.equalsIgnoreCase("info")) {
                messenger.sendMessage(ChatFormatting.DARK_AQUA + "Right click first point with the arrow. Right click with gunpowder to draw a jagged line to set the second point.");
                messenger.sendMessage(ChatFormatting.GOLD + "Jagged Line Brush Parameters:");
                messenger.sendMessage(ChatFormatting.AQUA + "/b j r[n] - Sets the number of recursions to n. Default is " + 3 + ", must be an integer " + this.recursionMin + "-" + this.recursionMax + ".");
                messenger.sendMessage(ChatFormatting.AQUA + "/b j s[n] - Sets the spread to n. Default is " + 3 + ".");
                return;
            }

            Integer spread;
            if (parameter.startsWith("r[")) {
                spread = NumericParser.parseInteger(parameter.replace("r[", "").replace("]", ""));
                if (spread != null && spread >= this.recursionMin && spread <= this.recursionMax) {
                    this.recursions = spread;
                    messenger.sendMessage(ChatFormatting.GREEN + "Recursions set to: " + this.recursions);
                } else {
                    messenger.sendMessage(ChatFormatting.RED + "Recusions must be an integer " + this.recursionMin + "-" + this.recursionMax + ".");
                }
            } else if (parameter.startsWith("s[")) {
                spread = NumericParser.parseInteger(parameter.replace("s[", "").replace("]", ""));
                if (spread != null) {
                    this.spread = spread;
                    messenger.sendMessage(ChatFormatting.GREEN + "Spread set to: " + this.spread);
                } else {
                    messenger.sendMessage(ChatFormatting.RED + "Invalid number.");
                }
            } else {
                messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters length! Use the \"info\" parameter to display parameter info.");
            }
        }

    }

    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("r[", "s["), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("r[", "s["), "");
        }
    }

    public void handleArrowAction(Snipe snipe) {
        BlockVector3 targetBlock = this.getTargetBlock();
        this.originCoordinates = targetBlock.toVector3();
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendMessage(ChatFormatting.DARK_PURPLE + "First point selected.");
    }

    public void handleGunpowderAction(Snipe snipe) {
        if (this.originCoordinates == null) {
            SnipeMessenger messenger = snipe.createMessenger();
            messenger.sendMessage(ChatFormatting.RED + "Warning: You did not select a first coordinate with the arrow");
        } else {
            BlockVector3 targetBlock = this.getTargetBlock();
            this.targetCoordinates = targetBlock.toVector3();
            this.jaggedP();
        }

    }

    private void jaggedP() {
        Vector3 originClone = this.originCoordinates.add(HALF_BLOCK_OFFSET);
        Vector3 targetClone = this.targetCoordinates.add(HALF_BLOCK_OFFSET);
        Vector3 direction = targetClone.subtract(originClone);
        double length = this.targetCoordinates.distance(this.originCoordinates);
        if (length == 0.0D) {
            try {
                this.performer.perform(this.getEditSession(), this.targetCoordinates.toBlockPoint().getBlockX(), this.targetCoordinates.toBlockPoint().getBlockY(), this.targetCoordinates.toBlockPoint().getBlockZ(), this.getBlock(this.targetCoordinates.toBlockPoint().getBlockX(), this.targetCoordinates.toBlockPoint().getBlockY(), this.targetCoordinates.toBlockPoint().getBlockZ()));
            } catch (MaxChangedBlocksException var15) {
                var15.printStackTrace();
            }
        } else {
            for (double distance = 0.0D; distance < direction.length(); distance += 0.25D) {
                BlockVector3 block = originClone.add(direction.normalize().multiply(distance)).toBlockPoint();

                for (int i = 0; i < this.recursions; ++i) {
                    int x = Math.round((float) (block.getX() + this.random.nextInt(this.spread * 2) - this.spread));
                    int y = Math.round((float) (block.getY() + this.random.nextInt(this.spread * 2) - this.spread));
                    int z = Math.round((float) (block.getZ() + this.random.nextInt(this.spread * 2) - this.spread));

                    try {
                        this.performer.perform(this.getEditSession(), x, this.clampY(y), z, this.clampY(x, y, z));
                    } catch (MaxChangedBlocksException var14) {
                        var14.printStackTrace();
                    }
                }
            }
        }

    }

    public void sendInfo(Snipe snipe) {
        snipe.createMessageSender().brushNameMessage().message(ChatFormatting.GRAY + "Recursion set to: " + this.recursions).message(ChatFormatting.GRAY + "Spread set to: " + this.spread).send();
    }
}
