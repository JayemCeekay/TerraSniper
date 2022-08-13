package com.jayemceekay.terrasniper.util.message;

import com.jayemceekay.terrasniper.brush.property.BrushPattern;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.serializer.legacy.LegacyComponentSerializer;
import com.sk89q.worldedit.world.block.BlockState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MessageSender {
    private final Player sender;
    private final ArrayList<String> messages = new ArrayList(0);

    public MessageSender(Player sender) {
        this.sender = sender;
    }

    public MessageSender brushNameMessage(String brushName) {
        this.messages.add(ChatFormatting.AQUA + "Brush Type: " + ChatFormatting.LIGHT_PURPLE + brushName);
        return this;
    }

    public MessageSender performerNameMessage(String performerName) {
        this.messages.add(ChatFormatting.DARK_PURPLE + "Performer: " + ChatFormatting.DARK_GREEN + performerName);
        return this;
    }

    public MessageSender patternMessage(BrushPattern brushPattern) {
        this.messages.add(ChatFormatting.GOLD + "Voxel: " + ChatFormatting.RED + brushPattern.getName());
        return this;
    }

    public MessageSender replacePatternMessage(BrushPattern replaceBrushPattern) {
        this.messages.add(ChatFormatting.AQUA + "Replace: " + ChatFormatting.RED + replaceBrushPattern.getName());
        return this;
    }


    public MessageSender brushSizeMessage(int brushSize) {
        this.messages.add(ChatFormatting.GREEN + "Brush Size: " + ChatFormatting.DARK_RED + brushSize);
        if (brushSize >= 15) {
            this.messages.add(ChatFormatting.RED + "WARNING: Large brush size selected!");
        }

        return this;
    }

    public MessageSender cylinderCenterMessage(int cylinderCenter) {
        this.messages.add(ChatFormatting.BLUE + "Brush Center: " + ChatFormatting.DARK_RED + cylinderCenter);
        return this;
    }

    public MessageSender voxelHeightMessage(int voxelHeight) {
        this.messages.add(ChatFormatting.DARK_AQUA + "Brush Height: " + ChatFormatting.DARK_RED + voxelHeight);
        return this;
    }

    public MessageSender voxelListMessage(List<? extends BlockState> voxelList) {
        if (voxelList.isEmpty()) {
            this.messages.add(ChatFormatting.DARK_GREEN + "No blocks selected!");
        }

        String message = voxelList.stream().map((state) -> {
            return ChatFormatting.AQUA + state.getAsString();
        }).collect(Collectors.joining(ChatFormatting.WHITE + ", ", ChatFormatting.DARK_GREEN + "Block Types Selected: ", ""));
        this.messages.add(message);
        return this;
    }

    public MessageSender message(String message) {
        this.messages.add(message);
        return this;
    }

    public MessageSender message(Component message) {
        this.messages.add(LegacyComponentSerializer.INSTANCE.serialize(message));
        return this;
    }

    public void send() {
        this.messages.forEach((s) -> {
            this.sender.sendMessage(new TextComponent(s), this.sender.getUUID());
        });
    }
}
