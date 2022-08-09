package com.jayemceekay.TerraSniper.sniper;


import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SniperRegistry {
    private final Map<UUID, Sniper> snipers = new HashMap<>();

    public SniperRegistry() {
    }

    public void removeSniper(Player player) {
        snipers.remove(player.getUUID());
    }

    public Sniper getOrRegisterSniper(Player player) {

        if (getSniper(player.getUUID()) == null) {
            return this.snipers.put(player.getUUID(), new Sniper(player));
        }
        return getSniper(player.getUUID());
    }

    public Sniper getSniper(UUID uuid) {
        return this.snipers.get(uuid);
    }
}
