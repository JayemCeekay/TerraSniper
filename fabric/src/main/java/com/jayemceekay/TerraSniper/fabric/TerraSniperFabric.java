package com.jayemceekay.TerraSniper.fabric;

import com.jayemceekay.TerraSniper.TerraSniper;
import com.jayemceekay.TerraSniper.command.TerraSniperCommandHandler;
import com.jayemceekay.TerraSniper.events.TerraSniperEventHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class TerraSniperFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TerraSniper.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> TerraSniperCommandHandler.registerCommands(dispatcher));
        UseItemCallback.EVENT.register((player, world, hand) -> TerraSniperEventHandler.onToolRightClick(player, hand));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> TerraSniperEventHandler.onPlayerJoinEvent(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> TerraSniperEventHandler.onPlayerLeaveEvent(handler.getPlayer()));
    }
}