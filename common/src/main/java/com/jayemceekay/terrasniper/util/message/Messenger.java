package com.jayemceekay.terrasniper.util.message;

import com.jayemceekay.terrasniper.brush.property.BrushPattern;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.serializer.legacy.LegacyComponentSerializer;
import com.sk89q.worldedit.world.block.BlockState;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.stream.Collectors;

public class Messenger {
    private final Player sender;

    public Messenger(Player sender) {
        this.sender = sender;
    }

    public void sendBrushNameMessage(String brushName) {
        this.sendMessage(ChatFormatting.AQUA + "Brush Type: " + ChatFormatting.LIGHT_PURPLE + brushName);
    }

    public void sendPerformerNameMessage(String performerName) {
        this.sendMessage(ChatFormatting.DARK_PURPLE + "Performer: " + ChatFormatting.DARK_GREEN + performerName);
    }

    public void sendPatternMessage(BrushPattern brushPattern) {
        sendMessage(ChatFormatting.GOLD + "Voxel: " + ChatFormatting.RED + brushPattern.getName());
    }

    public void sendReplacePatternMessage(BrushPattern replaceBrushPattern) {
        sendMessage(ChatFormatting.AQUA + "Replace: " + ChatFormatting.RED + replaceBrushPattern.getName());
    }

    public void sendBrushSizeMessage(int brushSize) {
        this.sendMessage(ChatFormatting.GREEN + "Brush Size: " + ChatFormatting.DARK_RED + brushSize);
        if (brushSize >= 15) {
            this.sendMessage(ChatFormatting.RED + "WARNING: Large brush size selected!");
        }

    }

    public void sendCylinderCenterMessage(int cylinderCenter) {
        this.sendMessage(ChatFormatting.BLUE + "Brush Center: " + ChatFormatting.DARK_RED + cylinderCenter);
    }

    public void sendVoxelHeightMessage(int voxelHeight) {
        this.sendMessage(ChatFormatting.DARK_AQUA + "Brush Height: " + ChatFormatting.DARK_RED + voxelHeight);
    }

    public void sendVoxelListMessage(List<? extends BlockState> voxelList) {
        if (voxelList.isEmpty()) {
            this.sendMessage(ChatFormatting.DARK_GREEN + "No blocks selected!");
        }

        String message = voxelList.stream().map((state) -> ChatFormatting.AQUA + state.getAsString()).collect(Collectors.joining(ChatFormatting.WHITE + ", ", ChatFormatting.DARK_GREEN + "Block Types Selected: ", ""));
        this.sendMessage(message);
    }

    public void sendMessage(Component message) {
        this.sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(LegacyComponentSerializer.INSTANCE.serialize(message)));
    }

    public void sendMessage(String message) {
        this.sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
    }
}
