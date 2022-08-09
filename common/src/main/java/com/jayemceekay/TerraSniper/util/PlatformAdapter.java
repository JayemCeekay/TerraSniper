package com.jayemceekay.TerraSniper.util;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.forge.ForgeAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.world.World;
import dev.architectury.injectables.targets.ArchitecturyTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class PlatformAdapter {

    public static BlockVector3 adapt(BlockPos o) {
        if (ArchitecturyTarget.getCurrentTarget() == "forge") {
            return ForgeAdapter.adapt(o);
        } else {
            return FabricAdapter.adapt(o);
        }
    }

    public static Player adaptPlayer(ServerPlayer o) {
        if (ArchitecturyTarget.getCurrentTarget() == "forge") {
            return ForgeAdapter.adaptPlayer(o);
        } else {
            return FabricAdapter.adaptPlayer(o);
        }
    }

    public static World adapt(ServerLevel o) {
        if (ArchitecturyTarget.getCurrentTarget() == "forge") {
            return ForgeAdapter.adapt(o);
        } else {
            return FabricAdapter.adapt(o);
        }
    }

    public static Vector3 adapt(Vec3 o) {
        if (ArchitecturyTarget.getCurrentTarget() == "forge") {
            return ForgeAdapter.adapt(o);
        } else {
            return FabricAdapter.adapt(o);
        }
    }

}
