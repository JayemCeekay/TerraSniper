package com.jayemceekay.terrasniper.sniper.snipe.message;

import com.jayemceekay.terrasniper.brush.property.BrushProperties;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.util.message.Messenger;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.serializer.legacy.LegacyComponentSerializer;
import com.sk89q.worldedit.world.block.BlockState;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class SnipeMessenger {
    private final ToolkitProperties toolkitProperties;
    private final BrushProperties brushProperties;
    private final Messenger messenger;

    public SnipeMessenger(ToolkitProperties toolkitProperties, BrushProperties brushProperties, Player player) {
        this.toolkitProperties = toolkitProperties;
        this.brushProperties = brushProperties;
        this.messenger = new Messenger(player);
    }

    public void sendBrushNameMessage() {
        String brushName = this.brushProperties.getName();
        this.messenger.sendBrushNameMessage(brushName);
    }

    public void sendPatternMessage() {
        this.messenger.sendPatternMessage(this.toolkitProperties.getPattern());
    }

    public void sendReplacePatternMessage() {
        this.messenger.sendPatternMessage(this.toolkitProperties.getReplacePattern());
    }

    public void sendBrushSizeMessage() {
        int brushSize = this.toolkitProperties.getBrushSize();
        this.messenger.sendBrushSizeMessage(brushSize);
    }

    public void sendCylinderCenterMessage() {
        int cylinderCenter = this.toolkitProperties.getCylinderCenter();
        this.messenger.sendCylinderCenterMessage(cylinderCenter);
    }

    public void sendVoxelHeightMessage() {
        int voxelHeight = this.toolkitProperties.getVoxelHeight();
        this.messenger.sendVoxelHeightMessage(voxelHeight);
    }

    public void sendVoxelListMessage() {
        List<BlockState> voxelList = this.toolkitProperties.getVoxelList();
        this.messenger.sendVoxelListMessage(voxelList);
    }

    public void sendMessage(String message) {
        this.messenger.sendMessage(message);
    }

    public void sendMessage(Component message) {
        this.messenger.sendMessage(LegacyComponentSerializer.INSTANCE.serialize(message));
    }
    public Messenger getMessenger() {
        return this.messenger;
    }
}
