package com.jayemceekay.terrasniper.sniper.snipe.performer;

import com.jayemceekay.terrasniper.brush.Brush;
import com.jayemceekay.terrasniper.brush.property.BrushProperties;
import com.jayemceekay.terrasniper.performer.Performer;
import com.jayemceekay.terrasniper.performer.property.PerformerProperties;
import com.jayemceekay.terrasniper.sniper.Sniper;
import com.jayemceekay.terrasniper.sniper.ToolKit.Toolkit;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.performer.message.PerformerSnipeMessageSender;
import com.jayemceekay.terrasniper.sniper.snipe.performer.message.PerformerSnipeMessenger;
import net.minecraft.world.entity.player.Player;

public class PerformerSnipe extends Snipe {
    private final PerformerProperties performerProperties;
    private final Performer performer;

    public PerformerSnipe(Snipe snipe, PerformerProperties performerProperties, Performer performer) {
        this(snipe.getSniper(), snipe.getToolkit(), snipe.getToolkitProperties(), snipe.getBrushProperties(), snipe.getBrush(), performerProperties, performer);
    }

    public PerformerSnipe(Sniper sniper, Toolkit toolkit, ToolkitProperties toolkitProperties, BrushProperties brushProperties, Brush brush, PerformerProperties performerProperties, Performer performer) {
        super(sniper, toolkit, toolkitProperties, brushProperties, brush);
        this.performerProperties = performerProperties;
        this.performer = performer;
    }

    public PerformerSnipeMessenger createMessenger() {
        ToolkitProperties toolkitProperties = this.getToolkitProperties();
        BrushProperties brushProperties = this.getBrushProperties();
        Sniper sniper = this.getSniper();
        Player player = sniper.getPlayer();
        return new PerformerSnipeMessenger(toolkitProperties, brushProperties, this.performerProperties, player);
    }

    public PerformerSnipeMessageSender createMessageSender() {
        ToolkitProperties toolkitProperties = this.getToolkitProperties();
        BrushProperties brushProperties = this.getBrushProperties();
        Sniper sniper = this.getSniper();
        Player player = sniper.getPlayer();
        return new PerformerSnipeMessageSender(toolkitProperties, brushProperties, this.performerProperties, player);
    }

    public PerformerProperties getPerformerProperties() {
        return this.performerProperties;
    }

    public Performer getPerformer() {
        return this.performer;
    }
}
