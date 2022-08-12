package com.jayemceekay.terrasniper;

import com.jayemceekay.terrasniper.brush.BrushRegistry;
import com.jayemceekay.terrasniper.command.PatternParser;
import com.jayemceekay.terrasniper.performer.PerformerRegistry;
import com.jayemceekay.terrasniper.sniper.SniperRegistry;
import com.sk89q.worldedit.WorldEdit;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TerraSniper {
    public static final String MOD_ID = "terrasniper";
    public static SniperRegistry sniperRegistry = new SniperRegistry();
    public static BrushRegistry brushRegistry;
    public static PerformerRegistry performerRegistry;
    public static ResourceLocation TERRASNIPER_CONFIG_FOLDER = new ResourceLocation(MOD_ID, "config");
    public static final Logger LOGGER = LogManager.getLogger();
    public static PatternParser TerraSniperPatternParser;

    public static void init() {
        brushRegistry = loadBrushRegistry();
        performerRegistry = loadPerformerRegistry();
        TerraSniperPatternParser = new PatternParser(WorldEdit.getInstance());
    }

    private static BrushRegistry loadBrushRegistry() {
        BrushRegistry brushRegistry = new BrushRegistry();
        new BrushRegistrar(brushRegistry);
        return brushRegistry;
    }

    private static PerformerRegistry loadPerformerRegistry() {
        PerformerRegistry performerRegistry = new PerformerRegistry();
        new PerformerRegistrar(performerRegistry);
        return performerRegistry;
    }
}