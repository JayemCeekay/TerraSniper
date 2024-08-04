package com.jayemceekay.terrasniper.brush.type.performer;

import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.painter.Painters;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import net.minecraft.ChatFormatting;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class BallBrush extends AbstractPerformerBrush {
    private boolean trueCircle;

    public BallBrush() {
    }

    public void loadProperties() {
    }

    @Override
    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();

        for (String parameter : parameters) {
            if (parameter.equalsIgnoreCase("info")) {
                messenger.sendMessage(ChatFormatting.GOLD + "Ball Brush Brush Parameters:");
                messenger.sendMessage(ChatFormatting.AQUA + "/b b [true|false] -- Uses a true sphere algorithm instead of the skinnier version with classic sniper nubs. Default is false.");
                return;
            }

            if (parameter.equalsIgnoreCase("true")) {
                this.trueCircle = true;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode ON.");
            } else if (parameter.equalsIgnoreCase("false")) {
                this.trueCircle = false;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode OFF.");
            } else {
                messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters! Use the \"info\" parameter to display parameter info.");
            }
        }

    }

    @Override
    public HashMap<String, String> getSettings() {
        this.settings.put("True Circle", String.valueOf(this.trueCircle));
        return super.getSettings();
    }

    @Override
    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("true", "false"), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("true", "false"), "");
        }
    }

    @Override
    public void handleArrowAction(Snipe snipe) {
        BlockVector3 targetBlock = this.getTargetBlock();
        this.ball(snipe, targetBlock);
    }

    @Override
    public void handleGunpowderAction(Snipe snipe) {
        BlockVector3 lastBlock = this.getLastBlock();
        this.ball(snipe, lastBlock);
    }

    private void ball(Snipe snipe, BlockVector3 targetBlock) {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        Painters.sphere().center(targetBlock).radius(brushSize).trueCircle(this.trueCircle).blockSetter((position) -> {
            BlockState block = this.clampY(position);

            try {
                this.performer.perform(this.getEditSession(), position.getX(), this.clampY(position.getY()), position.getZ(), block);
            } catch (MaxChangedBlocksException e) {
                e.printStackTrace();
            }

        }).paint();
    }

    @Override
    public void sendInfo(Snipe snipe) {
        snipe.createMessageSender().brushNameMessage().brushSizeMessage().send();
    }
}
