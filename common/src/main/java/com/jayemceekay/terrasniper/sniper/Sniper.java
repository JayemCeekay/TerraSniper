package com.jayemceekay.terrasniper.sniper;

import com.jayemceekay.terrasniper.brush.Brush;
import com.jayemceekay.terrasniper.brush.PerformerBrush;
import com.jayemceekay.terrasniper.brush.property.BrushProperties;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolAction;
import com.jayemceekay.terrasniper.sniper.ToolKit.Toolkit;
import com.jayemceekay.terrasniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.util.PlatformAdapter;
import com.jayemceekay.terrasniper.util.material.Materials;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockBehaviour;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Sniper {
    private static final String DEFAULT_TOOLKIT_NAME = "default";
    private final List<Toolkit> toolkits = new ArrayList<>();
    private final Player player;
    private boolean enabled = false;
    public boolean useSmallBlocks = false;
    public boolean autoLayer = false;
    public boolean keepPlants = false;

    public Sniper(Player player) {
        this.player = player;
        Toolkit defaultToolkit = this.createDefaultToolkit();
        this.toolkits.add(defaultToolkit);
    }

    private Toolkit createDefaultToolkit() {
        Toolkit toolkit = new Toolkit("default");
        CompoundTag tag = new CompoundTag();
        tag.putString("toolkit", "default");
        ItemStack arrow = new ItemStack(Items.ARROW);
        ItemStack wand = new ItemStack(Items.GUNPOWDER);
        arrow.setTag(tag);
        wand.setTag(tag);
        toolkit.addToolAction(arrow.copy(), ToolAction.ARROW);
        toolkit.addToolAction(wand.copy(), ToolAction.WAND);
        this.updateItemStackInfo(toolkit);
        return toolkit;
    }

    public Player getPlayer() {
        return this.player;
    }

    public UUID getUuid() {
        return this.player.getUUID();
    }

    @Nullable
    public Toolkit getCurrentToolkit() {
        Player player = this.getPlayer();
        ItemStack itemInHand = player.getMainHandItem();
        return getToolkit(itemInHand);
    }

    public void addToolkit(Toolkit toolkit) {
        this.toolkits.add(toolkit);
    }

    @Nullable
    public Toolkit getToolkit(ItemStack itemType) {
        if (itemType.hasTag() && itemType.getTag().contains("toolkit")) {
            return this.toolkits.stream().filter((toolkit) -> toolkit.hasToolAction(itemType)).findFirst().orElse(null);
        }
        return null;
    }

    @Nullable
    public Toolkit getToolkit(String toolkitName) {
        return this.toolkits.stream().filter((toolkit) -> toolkitName.equals(toolkit.getToolkitName())).findFirst().orElse(null);
    }

    public void removeToolkit(Toolkit toolkit) {
        this.toolkits.remove(toolkit);
    }

    public boolean snipe(ServerPlayer player, InteractionHand action, ItemStack usedItem, @Nullable BlockVector3 clickedBlock) {
        if (this.toolkits.isEmpty()) {
            return false;
        } else {
            Toolkit toolkit = this.getToolkit(usedItem);
            if (toolkit == null) {
                player.sendSystemMessage(Component.literal(TextColor.fromLegacyFormat(ChatFormatting.RED) + "No toolkit found for this item."));
                return false;
            } else {
                ToolAction toolAction = toolkit.getToolAction(usedItem);
                if (toolAction == null) {
                    player.sendSystemMessage(Component.literal(TextColor.fromLegacyFormat(ChatFormatting.RED) + "No tool action found for this item."));
                    return false;
                } else {
                    BrushProperties currentBrushProperties = toolkit.getCurrentBrushProperties();
                    this.snipeOnCurrentThread(player, action, clickedBlock, toolkit, toolAction, currentBrushProperties);
                    return true;
                }
            }
        }
    }

    public synchronized void snipeOnCurrentThread(ServerPlayer player, InteractionHand action, @Nullable BlockVector3 clickedBlock, Toolkit toolkit, ToolAction toolAction, BrushProperties currentBrushProperties) {

        LocalSession session = WorldEdit.getInstance().getSessionManager().findByName(player.getName().getString());

        if (session != null) {
            EditSession editSession = session.createEditSession(PlatformAdapter.adaptPlayer(player));

            try {
                ToolkitProperties toolkitProperties = toolkit.getProperties();
                BlockVector3 rayTraceTargetBlock = null;
                BlockVector3 rayTraceLastBlock = null;
                BlockVector3 targetBlock;
                clickedBlock = null; // ignore this block, use ray-tracing:
                if (clickedBlock == null) {
                    targetBlock = PlatformAdapter.adapt(player.level().clip(new ClipContext(player.getEyePosition(1.0F), player.getEyePosition(1.0F).add(player.getLookAngle().scale((double) this.getCurrentToolkit().getProperties().getBlockTracerRange())), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getBlockPos());
                    if (targetBlock != null) {
                        rayTraceTargetBlock = targetBlock;
                    }
                }
                Direction direction = player.level().clip(new ClipContext(player.getEyePosition(1.0F), player.getEyePosition(1.0F).add(player.getLookAngle().scale(128)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getDirection();
                BlockVector3 lastRayTraceResult = PlatformAdapter.adapt(player.level().clip(new ClipContext(player.getEyePosition(1.0F), player.getEyePosition(1.0F).add(player.getLookAngle().scale((double) this.getCurrentToolkit().getProperties().getBlockTracerRange())), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getBlockPos().offset(direction.getNormal()));
                if (lastRayTraceResult != null) {
                    rayTraceLastBlock = lastRayTraceResult;
                }

                targetBlock = clickedBlock == null ? rayTraceTargetBlock : clickedBlock;
                //System.out.println("clickedBlock: " + clickedBlock);
                //System.out.println("targetBlock: " + targetBlock);
                //System.out.println("Ray-Tracing (COLLIDER): " + PlatformAdapter.adapt(player.level().clip(new ClipContext(player.getEyePosition(1.0F), player.getEyePosition(1.0F).add(player.getLookAngle().scale((double) this.getCurrentToolkit().getProperties().getBlockTracerRange())), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getBlockPos()));
                //System.out.println("Ray-Tracing (VISUAL):   " + PlatformAdapter.adapt(player.level().clip(new ClipContext(player.getEyePosition(1.0F), player.getEyePosition(1.0F).add(player.getLookAngle().scale((double) this.getCurrentToolkit().getProperties().getBlockTracerRange())), ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player)).getBlockPos()));
                //System.out.println("Ray-Tracing (OUTLINE):  " + PlatformAdapter.adapt(player.level().clip(new ClipContext(player.getEyePosition(1.0F), player.getEyePosition(1.0F).add(player.getLookAngle().scale((double) this.getCurrentToolkit().getProperties().getBlockTracerRange())), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getBlockPos()));
                if (action != null) {
                    if (targetBlock.getY() < editSession.getWorld().getMinY() && Materials.isEmpty(editSession.getBlock(BlockVector3.at(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ())).getBlockType())) {
                        player.sendSystemMessage(Component.literal(TextColor.fromLegacyFormat(ChatFormatting.RED) + "Snipe target block must be visible."));
                        return;
                    }

                    Brush currentBrush = toolkit.getCurrentBrush();
                    if (currentBrush == null) {
                        return;
                    }

                    Snipe snipe = new Snipe(this, toolkit, toolkitProperties, currentBrushProperties, currentBrush);
                    if (currentBrush instanceof PerformerBrush performerBrush) {
                        performerBrush.initialize(snipe);
                    }

                    currentBrush.perform(snipe, toolAction, editSession, targetBlock, rayTraceLastBlock);
                }
            } finally {
                session.remember(editSession);
                editSession.flushSession();
                editSession.close();
            }
        }
    }

    public void sendInfo(Player sender) {
        Toolkit toolkit = this.getCurrentToolkit();
        if (toolkit == null) {
            sender.sendSystemMessage(Component.literal("Current toolkit: none"));
        } else {
            sender.sendSystemMessage(Component.literal("Current toolkit: " + toolkit.getToolkitName()));
            BrushProperties brushProperties = toolkit.getCurrentBrushProperties();
            Brush brush = toolkit.getCurrentBrush();
            if (brush == null) {
                sender.sendSystemMessage(Component.literal("No brush selected."));
            } else {
                ToolkitProperties toolkitProperties = toolkit.getProperties();
                Snipe snipe = new Snipe(this, toolkit, toolkitProperties, brushProperties, brush);
                brush.sendInfo(snipe);
                if (brush instanceof PerformerBrush) {
                    PerformerBrush performer = (PerformerBrush) brush;
                    performer.sendPerformerInfo(snipe);
                }

            }
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean smallBlocksEnabled() {
        return this.useSmallBlocks;
    }

    public void setSmallBlocksEnabled(boolean enabled) {
        this.useSmallBlocks = enabled;
    }

    public boolean autoLayerEnabled() {
        return this.autoLayer;
    }

    public void setAutoLayerEnabled(boolean enabled) {
        this.autoLayer = enabled;
    }

    public boolean keepPlantsEnabled() {
        return this.keepPlants;
    }

    public void setKeepPlantsEnabled(boolean enabled) {
        this.keepPlants = enabled;
    }


    public List<Toolkit> getToolkits() {
        return this.toolkits;
    }

    public void updateItemStackInfo(Toolkit toolkit) {
        toolkit.getToolActions().forEach((toolMaterial, action) -> {

            if (this.getPlayer().getInventory().contains(toolMaterial)) {

                int index = getItemSlot(toolMaterial);

                this.getPlayer().getInventory().removeItem(toolMaterial);

                toolMaterial.setHoverName(Component.literal(ChatFormatting.AQUA + "" + ChatFormatting.BOLD + "" + ChatFormatting.UNDERLINE + "" + toolkit.getCurrentBrushProperties().getName() + "-" + ChatFormatting.ITALIC + action.toString()));
                CompoundTag display = toolMaterial.getOrCreateTagElement("display");
                ListTag tag = display.getList("Lore", 8);
                tag.clear();
                tag.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(ChatFormatting.DARK_AQUA + "Size: " + toolkit.getProperties().getBrushSize()))));
                toolkit.getCurrentBrush().getSettings().forEach((setting, value) -> {
                    tag.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(ChatFormatting.DARK_AQUA + setting + ": " + value))));
                });
                tag.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(ChatFormatting.BLUE + "Material: " + toolkit.getProperties().getPattern().getPattern().toString()))));
                tag.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(ChatFormatting.BLUE + "Replacing: " + toolkit.getProperties().getReplacePattern().getPattern().toString()))));
                display.put("Lore", tag);

                if (index >= 0) {
                    this.getPlayer().getInventory().setItem(index, toolMaterial.copy());
                }

            }
        });
    }

    public int getItemSlot(ItemStack itemStack) {
        for (int i = 0; i < this.getPlayer().getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(itemStack.getItem()) && (player.getInventory().getItem(i).hasTag() && player.getInventory().getItem(i).getTag().contains("toolkit"))) {
                return i;
            }
        }
        return -1;
    }

    public void cleanToolkits() {
        this.toolkits.forEach(toolkit -> {
            if (toolkit.getToolActions().size() == 0) {
                this.removeToolkit(toolkit);
                this.player.sendSystemMessage(Component.literal("Removed toolkit " + toolkit.getToolkitName() + " because it has no actions."));
            }
        });
    }
}