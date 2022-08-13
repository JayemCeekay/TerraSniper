package com.jayemceekay.terrasniper.forge;

import com.jayemceekay.terrasniper.TerraSniper;
import com.jayemceekay.terrasniper.command.TerraSniperCommandHandler;
import com.jayemceekay.terrasniper.events.TerraSniperEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(TerraSniper.MOD_ID)
public class TerraSniperForge {
    public TerraSniperForge() {
        // Submit our event bus to let architectury register our content on the right time
        TerraSniper.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerJoin(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        TerraSniperEventHandler.onPlayerJoinEvent(event.getPlayer().getServer().getPlayerList().getPlayer(event.getPlayer().getUUID()));
    }

    @SubscribeEvent
    public void onPlayerLeave(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        TerraSniperEventHandler.onPlayerLeaveEvent(event.getPlayer().getServer().getPlayerList().getPlayer(event.getPlayer().getUUID()));
    }

    @SubscribeEvent
    public void onPlayerUse(PlayerInteractEvent.RightClickItem event) {
        TerraSniperEventHandler.onToolRightClick(event.getPlayer(), event.getHand());
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        TerraSniperCommandHandler.registerCommands(event.getDispatcher());
    }
}