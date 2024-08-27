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
    private boolean centerBlock=false;
    private String shape="full";

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
                messenger.sendMessage(ChatFormatting.DARK_GREEN + "/b b center -- Toggles whether the brush will center on a block or the corner of a block in smallBlocks mode");
                messenger.sendMessage(ChatFormatting.GREEN + "/b b up/down/north/east/south/west/full -- changes shape to a half-ball facing the specified direction, 'full' changes back to a full ball");
                return;
            }

            if (parameter.equalsIgnoreCase("true")) {
                this.trueCircle = true;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode ON.");
            } else if (parameter.equalsIgnoreCase("false")) {
                this.trueCircle = false;
                messenger.sendMessage(ChatFormatting.AQUA + "True circle mode OFF.");
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
                        messenger.sendMessage(ChatFormatting.AQUA + "changed back to a FULL ball.");
                    } else { if (parameter.equalsIgnoreCase("up")) {
                            this.shape = "up";
                            messenger.sendMessage(ChatFormatting.AQUA + "changed back to a half-ball facing UP.");
                        } else { if (parameter.equalsIgnoreCase("down")) {
                                this.shape = "down";
                                messenger.sendMessage(ChatFormatting.AQUA + "changed back to a half-ball facing DOWN.");
                            } else { if (parameter.equalsIgnoreCase("north")) {
                                    this.shape = "north";
                                    messenger.sendMessage(ChatFormatting.AQUA + "changed back to a half-ball facing NORTH.");
                                } else { if (parameter.equalsIgnoreCase("east")) {
                                        this.shape = "east";
                                        messenger.sendMessage(ChatFormatting.AQUA + "changed back to a half-ball facing EAST.");
                                    } else { if (parameter.equalsIgnoreCase("south")) {
                                            this.shape = "south";
                                            messenger.sendMessage(ChatFormatting.AQUA + "changed back to a half-ball facing SOUTH.");
                                        } else { if (parameter.equalsIgnoreCase("west")) {
                                                this.shape = "west";
                                                messenger.sendMessage(ChatFormatting.AQUA + "changed back to a half-ball facing WEST.");
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

    @Override
    public HashMap<String, String> getSettings() {
        this.settings.put("True Circle", String.valueOf(this.trueCircle));
        return super.getSettings();
    }

    @Override
    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("center", "full", "up", "down", "north", "east", "south", "west", "true", "false"), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("center", "full", "up", "down", "north", "east", "south", "west", "true", "false"), "");
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
        Painters.sphere().center(targetBlock).radius(brushSize).trueCircle(this.trueCircle).offsetVector(this.offsetVector).centerBlock(this.centerBlock && useSmallBlocks).shape(this.shape).blockSetter((position) -> {
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
