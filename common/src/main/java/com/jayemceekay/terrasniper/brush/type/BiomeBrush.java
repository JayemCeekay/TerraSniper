package com.jayemceekay.terrasniper.brush.type;

import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.List;
import java.util.stream.Collectors;

public class BiomeBrush extends AbstractBrush {

    private static final BiomeType DEFAULT_BIOME_TYPE = BiomeTypes.PLAINS;

    private BiomeType biomeType;


    @Override
    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        String firstParameter = parameters[0];

        if (firstParameter.equalsIgnoreCase("info")) {
            messenger.sendMessage(ChatFormatting.GOLD + "Biome Brush Parameters:");
            messenger.sendMessage(ChatFormatting.AQUA + "/b bio [t] -- Sets the selected biome type to t.");
            messenger.sendMessage(ChatFormatting.AQUA + "/b bio list -- Lists all available biomes.");
        } else {
            if (parameters.length == 1) {
                if (firstParameter.equalsIgnoreCase("list")) {
                    messenger.sendMessage(
                            BiomeType.REGISTRY.values().stream()
                                    .map(biomeType -> ((biomeType == this.biomeType) ? ChatFormatting.GOLD : ChatFormatting.GRAY) +
                                            biomeType.getId().substring(10))
                                    .collect(Collectors.joining(ChatFormatting.WHITE + ", ",
                                            ChatFormatting.AQUA + "Available biomes: ", ""
                                    ))
                    );
                } else {
                    BiomeType biomeType = BiomeTypes.get(firstParameter);

                    if (biomeType != null) {
                        this.biomeType = biomeType;
                        messenger.sendMessage(ChatFormatting.GOLD + "Biome type set to: " + ChatFormatting.DARK_GREEN + this.biomeType.getId());
                    } else {
                        messenger.sendMessage(ChatFormatting.RED + "Invalid biome type: " + firstParameter);
                    }
                }
            } else {
                messenger.sendMessage(ChatFormatting.RED + "Invalid brush parameters length! Use the \"info\" parameter to display parameter " +
                        "info.");
            }
        }
    }

    @Override
    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(BiomeType.REGISTRY.values().stream().map(BiomeType::toString), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(BiomeType.REGISTRY.values().stream().map(BiomeType::toString), "");
        }
    }

    @Override
    public void handleArrowAction(Snipe snipe) {
        biome(snipe);
    }

    @Override
    public void handleGunpowderAction(Snipe snipe) {
        biome(snipe);
    }

    private void biome(Snipe snipe) {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        double brushSizeSquared = Math.pow(brushSize, 2);
        EditSession editSession = getEditSession();
        BlockVector3 targetBlock = getTargetBlock();
        int targetBlockX = targetBlock.getX();
        int targetBlockZ = targetBlock.getZ();
        for (int x = -brushSize; x <= brushSize; x++) {
            double xSquared = Math.pow(x, 2);
            for (int z = -brushSize; z <= brushSize; z++) {
                if (xSquared + Math.pow(z, 2) <= brushSizeSquared) {
                    for (int y = editSession.getWorld().getMinY(); y <= editSession.getWorld().getMaxY(); ++y) {
                        setBiome(targetBlockX + x, y, targetBlockZ + z, this.biomeType);
                    }
                }
            }
        }
        int block1X = targetBlockX - brushSize;
        int block2X = targetBlockX + brushSize;
        int block1Z = targetBlockZ - brushSize;
        int block2Z = targetBlockZ + brushSize;
        int chunk1X = block1X >> 4;
        int chunk2X = block2X >> 4;
        int chunk1Z = block1Z >> 4;
        int chunk2Z = block2Z >> 4;
        int lowChunkX = block1X <= block2X ? chunk1X : chunk2X;
        int lowChunkZ = block1Z <= block2Z ? chunk1Z : chunk2Z;
        int highChunkX = block1X >= block2X ? chunk1X : chunk2X;
        int highChunkZ = block1Z >= block2Z ? chunk1Z : chunk2Z;
        for (int x = lowChunkX; x <= highChunkX; x++) {
            for (int z = lowChunkZ; z <= highChunkZ; z++) {
                snipe.getSniper().getPlayer().getServer().getPlayerList().getPlayer(snipe.getSniper().getPlayer().getUUID()).connection.send(new ClientboundForgetLevelChunkPacket(x, z));

                snipe.getSniper().getPlayer().getServer().getPlayerList().getPlayer(snipe.getSniper().getPlayer().getUUID()).connection.send(new ClientboundLevelChunkWithLightPacket(snipe.getSniper().getPlayer().getLevel().getChunk(x, z), snipe.getSniper().getPlayer().getLevel().getLightEngine(), null, null, true));
            }
        }
    }

    @Override
    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
        messenger.sendMessage(ChatFormatting.GOLD + "Currently selected biome type: " + ChatFormatting.DARK_GREEN + this.biomeType.getId());
    }

}
