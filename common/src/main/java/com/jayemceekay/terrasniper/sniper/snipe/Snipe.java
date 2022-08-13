package com.jayemceekay.terrasniper.sniper.snipe;

import com.jayemceekay.terrasniper.brush.Brush;
import com.jayemceekay.terrasniper.brush.property.BrushProperties;
import com.jayemceekay.terrasniper.sniper.Sniper;
import com.jayemceekay.terrasniper.sniper.ToolKit.Toolkit;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessageSender;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import net.minecraft.world.entity.player.Player;

public class Snipe {
    private final Sniper sniper;
    private final Toolkit toolkit;
    private final ToolkitProperties toolkitProperties;
    private final BrushProperties brushProperties;
    private final Brush brush;

    public Snipe(Sniper sniper, Toolkit toolkit, ToolkitProperties toolkitProperties, BrushProperties brushProperties, Brush brush) {
        this.sniper = sniper;
        this.toolkit = toolkit;
        this.toolkitProperties = toolkitProperties;
        this.brushProperties = brushProperties;
        this.brush = brush;
    }

    public SnipeMessenger createMessenger() {
        Player player = this.sniper.getPlayer();
        return new SnipeMessenger(this.toolkitProperties, this.brushProperties, player);
    }

    public SnipeMessageSender createMessageSender() {
        Player player = this.sniper.getPlayer();
        return new SnipeMessageSender(this.toolkitProperties, this.brushProperties, player);
    }

    public Sniper getSniper() {
        return this.sniper;
    }

    public Toolkit getToolkit() {
        return this.toolkit;
    }

    public ToolkitProperties getToolkitProperties() {
        return this.toolkitProperties;
    }

    public BrushProperties getBrushProperties() {
        return this.brushProperties;
    }

    public Brush getBrush() {
        return this.brush;
    }
}
