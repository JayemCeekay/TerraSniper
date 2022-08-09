package com.jayemceekay.TerraSniper.sniper.snipe.performer.message;

import com.jayemceekay.TerraSniper.brush.property.BrushProperties;
import com.jayemceekay.TerraSniper.performer.property.PerformerProperties;
import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.TerraSniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.TerraSniper.util.message.Messenger;
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
