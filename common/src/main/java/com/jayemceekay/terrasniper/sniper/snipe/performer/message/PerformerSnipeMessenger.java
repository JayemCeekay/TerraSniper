package com.jayemceekay.terrasniper.sniper.snipe.performer.message;

import com.jayemceekay.terrasniper.brush.property.BrushProperties;
import com.jayemceekay.terrasniper.performer.property.PerformerProperties;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.message.Messenger;
import net.minecraft.world.entity.player.Player;

public class PerformerSnipeMessenger extends SnipeMessenger {
    private final PerformerProperties performerProperties;

    public PerformerSnipeMessenger(ToolkitProperties toolkitProperties, BrushProperties brushProperties, PerformerProperties performerProperties, Player player) {
        super(toolkitProperties, brushProperties, player);
        this.performerProperties = performerProperties;
    }

    public void sendPerformerNameMessage() {
        Messenger messenger = this.getMessenger();
        String performerName = this.performerProperties.getName();
        messenger.sendPerformerNameMessage(performerName);
    }
}
