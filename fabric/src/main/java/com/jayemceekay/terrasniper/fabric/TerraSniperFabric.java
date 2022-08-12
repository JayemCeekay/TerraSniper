package com.jayemceekay.terrasniper.fabric;

import com.jayemceekay.terrasniper.TerraSniper;
import com.jayemceekay.terrasniper.command.TerraSniperCommandHandler;
import com.jayemceekay.terrasniper.events.TerraSniperEventHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class TerraSniperFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TerraSniper.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, commandSelection) -> TerraSniperCommandHandler.registerCommands(dispatcher));
        UseItemCallback.EVENT.register((player, world, hand) -> TerraSniperEventHandler.onToolRightClick(player, hand));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> TerraSniperEventHandler.onPlayerJoinEvent(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> TerraSniperEventHandler.onPlayerLeaveEvent(handler.getPlayer()));
    }
}