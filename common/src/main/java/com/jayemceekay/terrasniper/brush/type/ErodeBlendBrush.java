package com.jayemceekay.terrasniper.brush.type;

import com.jayemceekay.terrasniper.brush.type.blend.BlendBallBrush;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolAction;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.math.BlockVector3;

import java.util.List;
import java.util.stream.Stream;

public class ErodeBlendBrush extends AbstractBrush {
    private final BlendBallBrush blendBall = new BlendBallBrush();
    private final ErodeBrush erode = new ErodeBrush();

    public ErodeBlendBrush() {
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        String firstParameter = parameters[0];
        if (firstParameter.equalsIgnoreCase("water")) {
            this.blendBall.handleCommand(parameters, snipe);
        } else {
            this.erode.handleCommand(parameters, snipe);
        }

    }

    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        if (parameters.length == 1) {
            String parameter = parameters[0];
            return super.sortCompletions(Stream.concat(this.blendBall.handleCompletions(parameters, snipe).stream(), this.erode.handleCompletions(parameters).stream()), parameter, 0);
        } else {
            return super.handleCompletions(parameters, snipe);
        }
    }

    public void handleArrowAction(Snipe snipe) {
        EditSession editSession = this.getEditSession();
        BlockVector3 targetBlock = this.getTargetBlock();
        BlockVector3 lastBlock = this.getLastBlock();
        this.erode.perform(snipe, ToolAction.ARROW, editSession, targetBlock, lastBlock);
        this.blendBall.setAirExcluded(false);
        this.blendBall.perform(snipe, ToolAction.ARROW, editSession, targetBlock, lastBlock);
    }

    public void handleGunpowderAction(Snipe snipe) {
        EditSession editSession = this.getEditSession();
        BlockVector3 targetBlock = this.getTargetBlock();
        BlockVector3 lastBlock = this.getLastBlock();
        this.erode.perform(snipe, ToolAction.WAND, editSession, targetBlock, lastBlock);
        this.blendBall.setAirExcluded(false);
        this.blendBall.perform(snipe, ToolAction.WAND, editSession, targetBlock, lastBlock);
    }

    public void sendInfo(Snipe snipe) {
        this.blendBall.sendInfo(snipe);
        this.erode.sendInfo(snipe);
    }
}
