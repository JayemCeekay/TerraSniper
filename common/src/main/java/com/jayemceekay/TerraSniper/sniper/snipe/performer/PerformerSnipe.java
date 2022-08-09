package com.jayemceekay.TerraSniper.sniper.snipe.performer;

import com.jayemceekay.TerraSniper.brush.Brush;
import com.jayemceekay.TerraSniper.brush.property.BrushProperties;
import com.jayemceekay.TerraSniper.performer.Performer;
import com.jayemceekay.TerraSniper.performer.property.PerformerProperties;
import com.jayemceekay.TerraSniper.sniper.Sniper;
import com.jayemceekay.TerraSniper.sniper.ToolKit.Toolkit;
import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.TerraSniper.sniper.snipe.Snipe;
import com.jayemceekay.TerraSniper.sniper.snipe.performer.message.PerformerSnipeMessageSender;
import com.jayemceekay.TerraSniper.sniper.snipe.performer.message.PerformerSnipeMessenger;
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
