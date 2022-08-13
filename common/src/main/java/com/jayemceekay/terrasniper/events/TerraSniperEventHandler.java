package com.jayemceekay.terrasniper.events;

import com.jayemceekay.terrasniper.TerraSniper;
import com.jayemceekay.terrasniper.sniper.Sniper;
import com.jayemceekay.terrasniper.util.PlatformAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;

public class TerraSniperEventHandler {

    public TerraSniperEventHandler() {
    }

    public static void onPlayerJoinEvent(ServerPlayer player) {
        TerraSniper.sniperRegistry.getOrRegisterSniper(player);
    }

    public static void onPlayerLeaveEvent(ServerPlayer player) {
        TerraSniper.sniperRegistry.removeSniper(player);
    }


    public static InteractionResultHolder<ItemStack> onToolRightClick(Player player, InteractionHand interactionHand) {
        if (player instanceof ServerPlayer) {
            Sniper sniper = TerraSniper.sniperRegistry.getSniper(player.getUUID());
            ServerPlayer serverPlayer = player.getServer().getPlayerList().getPlayer(player.getUUID());
            if (player.isCreative()) {
                if (sniper.isEnabled()) {
                    if (sniper.getCurrentToolkit() != null) {
                        ItemStack usedItem = sniper.getPlayer().getMainHandItem();
                        BlockVector3 clickedBlock = PlatformAdapter.adapt(player.level.clip(new ClipContext(player.getEyePosition(1.0F), player.getEyePosition(1.0F).add(player.getLookAngle().scale((double) sniper.getCurrentToolkit().getProperties().getBlockTracerRange())), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getBlockPos());
                        sniper.snipe(serverPlayer, interactionHand, usedItem, clickedBlock);
                    }
                }
            }

        }
        return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, player.getItemInHand(interactionHand));
    }

}
