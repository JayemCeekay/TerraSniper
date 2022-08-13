package com.jayemceekay.terrasniper.sniper.snipe.performer.message;


import com.jayemceekay.terrasniper.brush.property.BrushProperties;
import com.jayemceekay.terrasniper.performer.property.PerformerProperties;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessageSender;
import com.jayemceekay.terrasniper.util.message.MessageSender;
import net.minecraft.world.entity.player.Player;


public class PerformerSnipeMessageSender extends SnipeMessageSender {
    private final PerformerProperties performerProperties;

    public PerformerSnipeMessageSender(ToolkitProperties toolkitProperties, BrushProperties brushProperties, PerformerProperties performerProperties, Player player) {
        super(toolkitProperties, brushProperties, player);
        this.performerProperties = performerProperties;
    }

    public PerformerSnipeMessageSender performerNameMessage() {
        MessageSender messageSender = this.getMessageSender();
        String performerName = this.performerProperties.getName();
        messageSender.performerNameMessage(performerName);
        return this;
    }
}
