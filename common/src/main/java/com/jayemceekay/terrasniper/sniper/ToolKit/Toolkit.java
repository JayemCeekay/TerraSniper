package com.jayemceekay.terrasniper.sniper.ToolKit;


import com.jayemceekay.terrasniper.BrushRegistrar;
import com.jayemceekay.terrasniper.brush.Brush;
import com.jayemceekay.terrasniper.brush.property.BrushCreator;
import com.jayemceekay.terrasniper.brush.property.BrushProperties;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class Toolkit {
    private static final BrushProperties DEFAULT_BRUSH_PROPERTIES;

    static {
        DEFAULT_BRUSH_PROPERTIES = BrushRegistrar.DEFAULT_BRUSH_PROPERTIES;
    }

    private final String toolkitName;
    private final Map<ItemStack, ToolAction> toolActions = new HashMap<>();
    private final Map<BrushProperties, Brush> brushes = new HashMap<>();
    private final ToolkitProperties properties = new ToolkitProperties();
    private BrushProperties currentBrushProperties;
    private BrushProperties previousBrushProperties;

    public Toolkit(String toolkitName) {
        this.toolkitName = toolkitName;
        this.currentBrushProperties = DEFAULT_BRUSH_PROPERTIES;
        this.previousBrushProperties = DEFAULT_BRUSH_PROPERTIES;
        this.createBrush(DEFAULT_BRUSH_PROPERTIES);
    }

    public void reset() {
        this.currentBrushProperties = DEFAULT_BRUSH_PROPERTIES;
        this.previousBrushProperties = DEFAULT_BRUSH_PROPERTIES;
        this.brushes.clear();
        this.properties.reset();
        this.createBrush(DEFAULT_BRUSH_PROPERTIES);
    }

    public void addToolAction(ItemStack toolMaterial, ToolAction action) {
        this.toolActions.put(toolMaterial, action);
    }

    public boolean hasToolAction(ItemStack toolMaterial) {
        return this.toolActions.keySet().stream().anyMatch(itemStack -> itemStack.is(toolMaterial.getItem()));
    }

    @Nullable
    public ToolAction getToolAction(ItemStack toolMaterial) {
        for (ItemStack stack : this.toolActions.keySet()) {
            if (stack.is(toolMaterial.getItem())) {
                return this.toolActions.get(stack);
            }
        }
        return this.toolActions.get(toolMaterial);
    }

    public void removeToolAction(ItemStack toolMaterial) {
        this.toolActions.remove(toolMaterial);
    }

    public Brush useBrush(BrushProperties properties) {
        Brush brush = this.getBrush(properties);
        if (brush == null) {
            brush = this.createBrush(properties);
        }

        this.previousBrushProperties = this.currentBrushProperties;
        this.currentBrushProperties = properties;
        return brush;
    }

    private Brush createBrush(BrushProperties properties) {
        BrushCreator creator = properties.getCreator();
        Brush brush = creator.create();
        brush.setProperties(properties);
        brush.loadProperties();
        this.brushes.put(properties, brush);
        return brush;
    }

    @Nullable
    public Brush getCurrentBrush() {
        return this.getBrush(this.currentBrushProperties);
    }

    @Nullable
    public Brush getBrush(BrushProperties properties) {
        return this.brushes.get(properties);
    }

    public String getToolkitName() {
        return this.toolkitName;
    }

    public BrushProperties getCurrentBrushProperties() {
        return this.currentBrushProperties;
    }

    public BrushProperties getPreviousBrushProperties() {
        return this.previousBrushProperties;
    }

    public Map<ItemStack, ToolAction> getToolActions() {
        return this.toolActions;
    }

    public ToolkitProperties getProperties() {
        return this.properties;
    }
}
