package com.jayemceekay.TerraSniper.command;

import com.jayemceekay.TerraSniper.TerraSniper;
import com.jayemceekay.TerraSniper.brush.Brush;
import com.jayemceekay.TerraSniper.brush.property.BrushPattern;
import com.jayemceekay.TerraSniper.brush.property.BrushProperties;
import com.jayemceekay.TerraSniper.brush.type.performer.AbstractPerformerBrush;
import com.jayemceekay.TerraSniper.sniper.Sniper;
import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolAction;
import com.jayemceekay.TerraSniper.sniper.ToolKit.Toolkit;
import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolkitProperties;
import com.jayemceekay.TerraSniper.sniper.snipe.Snipe;
import com.jayemceekay.TerraSniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.TerraSniper.util.PlatformAdapter;
import com.jayemceekay.TerraSniper.util.message.MessageSender;
import com.jayemceekay.TerraSniper.util.message.Messenger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.enginehub.piston.converter.SuggestionHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class TerraSniperCommandHandler {
    public TerraSniperCommandHandler() {
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ts")
                .then(Commands.literal("b").requires((commandSource) -> {
                            try {
                                return (commandSource.getPlayerOrException().isCreative());

                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                            }
                            return false;
                        })
                        .then((Commands.argument("brush/size", StringArgumentType.string()).suggests((context, builder) -> {
                            try {
                                String argument = StringArgumentType.getString(context, "brush/size");
                                if (context.getArgument("brush/size", String.class).matches("\\d+")) {
                                    for (int i = 0; i < 10; ++i) {
                                        builder.suggest(argument + i);
                                    }
                                } else {
                                    SuggestionHelper.limitByPrefix(TerraSniper.brushRegistry.getBrushProperties().keySet().stream(), context.getArgument("brush/size", String.class)).forEach(builder::suggest);
                                }
                            } catch (Exception e) {
                                SuggestionHelper.limitByPrefix(TerraSniper.brushRegistry.getBrushProperties().keySet().stream(), "").forEach(builder::suggest);
                            }

                            return builder.buildFuture();
                        }).executes((context) -> {
                            String brushName = StringArgumentType.getString(context, "brush/size");
                            Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                            if (sniper.getCurrentToolkit() != null) {
                                if (brushName.matches("\\d+")) {
                                    try {
                                        sniper.getCurrentToolkit().getProperties().setBrushSize(Integer.parseInt(brushName));
                                        new MessageSender(sniper.getPlayer()).message(ChatFormatting.AQUA + "Brush size set to " + brushName).send();
                                    } catch (Exception e) {
                                        (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "Invalid brush size!").send();
                                    }
                                } else {
                                    try {
                                        BrushProperties brushProperties = TerraSniper.brushRegistry.getBrushProperties().get(brushName);
                                        sniper.getCurrentToolkit().useBrush(brushProperties);
                                        sniper.sendInfo(sniper.getPlayer());
                                    } catch (Exception e) {
                                        (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "Invalid brush name!").send();
                                    }
                                }
                                sniper.updateItemStackInfo(sniper.getCurrentToolkit());
                            } else {
                                (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "You must have a toolkit selected!").send();
                            }
                            return 0;
                        }))
                                .then(Commands.argument("args", StringArgumentType.greedyString()).suggests((context, builder) -> {
                                    try {
                                        Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                                        BrushProperties properties = TerraSniper.brushRegistry.getBrushProperties(StringArgumentType.getString(context, "brush/size"));
                                        if (properties == null) {
                                            return builder.buildFuture();
                                        }

                                        Brush brush = properties.getCreator().create();
                                        Snipe snipe = new Snipe(sniper, sniper.getCurrentToolkit(), sniper.getCurrentToolkit().getProperties(), brush.getProperties(), brush);
                                        try {
                                            String[] args = StringUtils.splitByWholeSeparatorPreserveAllTokens(StringArgumentType.getString(context, "args"), " ");
                                            brush.handleCompletions(args, snipe).stream().filter((s) -> Arrays.stream(args).noneMatch((s1) -> StringUtils.startsWith(s1, s))).forEach((s) -> {
                                                builder.suggest(context.getArgument("args", String.class) + s.replaceFirst(args[args.length - 1], ""));
                                            });
                                        } catch (Exception var5) {
                                            brush.handleCompletions(new String[0], snipe).forEach(builder::suggest);
                                        }
                                    } catch (Exception ignored) {
                                    }

                                    return builder.buildFuture();
                                }).executes((ctx) -> {
                                    String brushName = StringArgumentType.getString(ctx, "brush/size");
                                    Sniper sniper = TerraSniper.sniperRegistry.getSniper(ctx.getSource().getPlayerOrException().getUUID());
                                    if (sniper.getCurrentToolkit() != null) {
                                        try {
                                            BrushProperties brushProperties = TerraSniper.brushRegistry.getBrushProperties().get(brushName);
                                            String[] args = StringUtils.splitByWholeSeparatorPreserveAllTokens(StringArgumentType.getString(ctx, "args"), " ");
                                            sniper.getCurrentToolkit().useBrush(brushProperties);
                                            Brush brush = sniper.getCurrentToolkit().getCurrentBrush();
                                            Snipe snipe = new Snipe(sniper, sniper.getCurrentToolkit(), sniper.getCurrentToolkit().getProperties(), brushProperties, brush);
                                            if (brush instanceof AbstractPerformerBrush) {
                                                AbstractPerformerBrush performerBrush = (AbstractPerformerBrush) brush;
                                                performerBrush.handlePerformerCommand(args, snipe, TerraSniper.performerRegistry);
                                            } else {
                                                brush.handleCommand(args, snipe);
                                            }

                                        } catch (Exception e) {
                                            (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "Invalid brush name!").send();
                                        }
                                        sniper.updateItemStackInfo(sniper.getCurrentToolkit());
                                    } else {
                                        (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "You must have a toolkit selected!").send();
                                    }
                                    return 0;
                                }))))
                .then(Commands.literal("v").requires((commandSource) -> {
                            try {
                                return (commandSource.getPlayerOrException().isCreative());
                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                            }
                            return false;
                        })
                        .then(Commands.argument("voxel", StringArgumentType.greedyString()).suggests((context, builder) -> {
                            TerraSniper.TerraSniperPatternParser.getSuggestions(context.getArgument("voxel", String.class)).forEach(builder::suggest);
                            return builder.buildFuture();
                        }).executes((context) -> {
                            Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                            if (sniper != null && sniper.getCurrentToolkit() != null) {
                                try {
                                    ParserContext parserContext = new ParserContext();
                                    parserContext.setActor(PlatformAdapter.adaptPlayer(context.getSource().getPlayerOrException()));
                                    parserContext.setRestricted(false);
                                    parserContext.setPreferringWildcard(true);
                                    parserContext.setWorld(PlatformAdapter.adaptPlayer(context.getSource().getPlayerOrException()).getWorld());
                                    Pattern pattern = TerraSniper.TerraSniperPatternParser.parseFromInput(context.getArgument("voxel", String.class), parserContext);
                                    sniper.getCurrentToolkit().getProperties().setPattern(new BrushPattern(pattern, context.getArgument("voxel", String.class)));
                                    (new MessageSender(sniper.getPlayer())).patternMessage(sniper.getCurrentToolkit().getProperties().getPattern()).send();
                                } catch (InputParseException e) {
                                    (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "Invalid Item!").send();
                                }
                                sniper.updateItemStackInfo(sniper.getCurrentToolkit());
                            } else {
                                (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "You must have a toolkit selected!").send();
                            }
                            return 0;
                        })).executes((context) -> {
                            Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                            if (sniper != null && sniper.getCurrentToolkit() != null) {
                                try {
                                    Player player = sniper.getPlayer();
                                    BlockState state = PlatformAdapter.adapt(player.getServer().getLevel(player.level.dimension())).getBlock(PlatformAdapter.adapt(player.pick(128, 1, false).getLocation()).toBlockPoint());
                                    sniper.getCurrentToolkit().getProperties().setPattern(new BrushPattern(state));
                                    (new MessageSender(sniper.getPlayer())).patternMessage(new BrushPattern(state)).send();
                                } catch (Exception e) {
                                    (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "Invalid Item!").send();
                                }
                                sniper.updateItemStackInfo(sniper.getCurrentToolkit());
                            } else {
                                (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "You must have a toolkit selected!").send();
                            }

                            return 0;
                        }))
                .then(Commands.literal("vr").requires((commandSource) -> {
                            try {
                                return (commandSource.getPlayerOrException().isCreative());
                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                            }
                            return false;
                        })
                        .then(Commands.argument("voxelReplace", StringArgumentType.greedyString()).suggests((context, builder) -> {
                            if (builder.getInput().contains("[")) {
                                String newInput = context.getArgument("voxelReplace", String.class);
                                String typeString = newInput.substring(0, newInput.indexOf("["));
                                String dataString = newInput.substring(newInput.indexOf("[") + 1);
                                if (BlockType.REGISTRY.get(typeString) != null) {
                                    BlockType type = BlockType.REGISTRY.get(typeString);
                                    HashMap<String, String> dataMap = new HashMap();
                                    String[] data = dataString.split("(,+)");
                                    int var9 = data.length;

                                    for (String s : data) {
                                        String[] keyValue = s.split("=");
                                        if (keyValue.length == 2) {
                                            dataMap.put(keyValue[0], keyValue[1]);
                                        } else {
                                            dataMap.put(keyValue[0], "");
                                        }
                                    }

                                    if (dataString.trim().endsWith(",")) {
                                        type.getPropertyMap().keySet().stream().filter((sx) -> {
                                            return !dataMap.containsKey(sx);
                                        }).forEach((sx) -> {
                                            builder.suggest(newInput.trim() + " " + sx + "=");
                                        });
                                    } else if (data[data.length - 1].contains("=")) {
                                        String currentKey = data[data.length - 1].substring(0, data[data.length - 1].indexOf("="));
                                        (type.getPropertyMap().get(currentKey)).getValues().stream().map(Object::toString).filter((sx) -> {
                                            return StringUtils.startsWith(sx, dataMap.get(currentKey));
                                        }).forEach((sx) -> {
                                            builder.suggest(newInput + sx.replaceFirst(dataMap.get(currentKey), ""));
                                        });
                                    } else {
                                        type.getPropertyMap().keySet().stream().filter((sx) -> {
                                            return StringUtils.startsWith(sx, data[data.length - 1]);
                                        }).forEach((sx) -> {
                                            builder.suggest(newInput + sx.replaceFirst(data[data.length - 1], "") + "=");
                                        });
                                    }
                                }
                            } else {
                                BlockType.REGISTRY.keySet().forEach(builder::suggest);
                            }

                            return builder.buildFuture();
                        }).executes((context) -> {
                            Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                            if (sniper != null && sniper.getCurrentToolkit() != null) {
                                try {
                                    ParserContext parserContext = new ParserContext();
                                    parserContext.setActor(PlatformAdapter.adaptPlayer((ServerPlayer) sniper.getPlayer()));
                                    BlockState state = WorldEdit.getInstance().getBlockFactory().parseFromInput(context.getArgument("voxelReplace", String.class), parserContext).toImmutableState();
                                    sniper.getCurrentToolkit().getProperties().setReplacePattern(new BrushPattern(state));
                                    (new MessageSender(sniper.getPlayer())).replacePatternMessage(new BrushPattern(state)).send();
                                } catch (InputParseException var4) {
                                    (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "Invalid Item!").send();
                                }
                                sniper.updateItemStackInfo(sniper.getCurrentToolkit());
                            } else {
                                (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "You must have a toolkit selected!").send();
                            }

                            return 0;
                        })).executes((context) -> {
                            Sniper sniper = TerraSniper.sniperRegistry.getSniper((context.getSource()).getPlayerOrException().getUUID());
                            if (sniper != null && sniper.getCurrentToolkit() != null) {
                                try {
                                    Player player = sniper.getPlayer();
                                    BlockState state = PlatformAdapter.adapt(player.getServer().getLevel(player.level.dimension())).getBlock(PlatformAdapter.adapt(player.pick(128, 1, false).getLocation()).toBlockPoint());
                                    sniper.getCurrentToolkit().getProperties().setReplacePattern(new BrushPattern(state));
                                    (new MessageSender(sniper.getPlayer())).replacePatternMessage(new BrushPattern(state)).send();
                                } catch (Exception var5) {
                                    (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "Invalid Item!").send();
                                }
                                sniper.updateItemStackInfo(sniper.getCurrentToolkit());
                            } else {
                                (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "You must have a toolkit selected!").send();
                            }

                            return 0;
                        }))
                .then(Commands.literal("vi").requires((commandSource) -> {
                            try {
                                return (commandSource.getPlayerOrException().isCreative());
                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                            }
                            return false;
                        })
                        .then(Commands.argument("voxelCombo", StringArgumentType.greedyString()).suggests((context, builder) -> builder.buildFuture()).executes((context) -> {
                            Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                            if (sniper != null && sniper.getCurrentToolkit() != null) {
                                try {
                                    ParserContext parserContext = new ParserContext();
                                    parserContext.setActor(PlatformAdapter.adaptPlayer((ServerPlayer) sniper.getPlayer()));
                                    BlockState state = WorldEdit.getInstance().getBlockFactory().parseFromInput(context.getArgument("voxelCombo", String.class), parserContext).toImmutableState();
                                    sniper.getCurrentToolkit().getProperties().setPattern(new BrushPattern(state));
                                    (new MessageSender(sniper.getPlayer())).patternMessage(new BrushPattern(state)).send();
                                } catch (InputParseException var4) {
                                    (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "Invalid Item!").send();
                                }
                                sniper.updateItemStackInfo(sniper.getCurrentToolkit());
                            } else {
                                (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "You must have a toolkit selected!").send();
                            }

                            return 0;
                        })).executes((context) -> {
                            Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                            if (sniper != null && sniper.getCurrentToolkit() != null) {
                                try {
                                    Player player = sniper.getPlayer();
                                    BlockState state = PlatformAdapter.adapt(player.getServer().getLevel(player.level.dimension())).getBlock(PlatformAdapter.adapt(player.pick(128, 1, false).getLocation()).toBlockPoint());
                                    sniper.getCurrentToolkit().getProperties().setPattern(new BrushPattern(state));
                                    (new MessageSender(sniper.getPlayer())).patternMessage(new BrushPattern(state)).send();
                                } catch (Exception var5) {
                                    (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "Invalid Item!").send();
                                }
                                sniper.updateItemStackInfo(sniper.getCurrentToolkit());
                            } else {
                                (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "You must have a toolkit selected!").send();
                            }

                            return 0;
                        }))
                .then(Commands.literal("vir").requires((commandSource) -> {
                            try {
                                return (commandSource.getPlayerOrException().isCreative());
                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                            }
                            return false;
                        })
                        .then(Commands.argument("voxelComboReplace", StringArgumentType.greedyString()).suggests((context, builder) -> {
                            return builder.buildFuture();
                        }).executes((context) -> {
                            Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                            if (sniper != null && sniper.getCurrentToolkit() != null) {
                                try {
                                    ParserContext parserContext = new ParserContext();
                                    parserContext.setActor(PlatformAdapter.adaptPlayer((ServerPlayer) sniper.getPlayer()));
                                    BlockState state = WorldEdit.getInstance().getBlockFactory().parseFromInput(context.getArgument("voxelComboReplace", String.class), parserContext).toImmutableState();
                                    sniper.getCurrentToolkit().getProperties().setReplacePattern(new BrushPattern(state));
                                    (new MessageSender(sniper.getPlayer())).replacePatternMessage(new BrushPattern(state)).send();
                                } catch (InputParseException var4) {
                                    (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "Invalid Item!").send();
                                }

                                sniper.updateItemStackInfo(sniper.getCurrentToolkit());
                            } else {
                                (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "You must have a toolkit selected!").send();
                            }

                            return 0;
                        })).executes((context) -> {
                            Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                            if (sniper != null && sniper.getCurrentToolkit() != null) {
                                try {
                                    Player player = sniper.getPlayer();
                                    BlockState state = PlatformAdapter.adapt(player.getServer().getLevel(player.level.dimension())).getBlock(PlatformAdapter.adapt(player.pick(128, 1, false).getLocation()).toBlockPoint());
                                    sniper.getCurrentToolkit().getProperties().setReplacePattern(new BrushPattern(state));
                                    (new MessageSender(sniper.getPlayer())).replacePatternMessage(new BrushPattern(state)).send();
                                } catch (Exception var5) {
                                    (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "Invalid Item!").send();
                                }
                                sniper.updateItemStackInfo(sniper.getCurrentToolkit());
                            } else {
                                (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "You must have a toolkit selected!").send();
                            }

                            return 0;
                        }))
                .then((Commands.literal("d").requires((commandSource) -> {
                    try {
                        return (commandSource.getPlayerOrException().isCreative());
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                    }
                    return false;
                })).executes((context) -> {
                    Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                    if (sniper != null && sniper.getCurrentToolkit() != null) {
                        sniper.getCurrentToolkit().reset();
                        sniper.updateItemStackInfo(sniper.getCurrentToolkit());
                        context.getSource().sendSuccess(new TextComponent(ChatFormatting.AQUA + "Brush settings reset to their default values."), false);
                    } else {
                        (new MessageSender(sniper.getPlayer())).message(ChatFormatting.RED + "You must have a toolkit selected!").send();
                    }

                    return 0;
                }))

                .then(Commands.literal("vc").then(Commands.argument("center", IntegerArgumentType.integer()).executes((context) -> {
                    Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                    SnipeMessenger sender = new SnipeMessenger(sniper.getCurrentToolkit().getProperties(), sniper.getCurrentToolkit().getCurrentBrushProperties(), sniper.getPlayer());
                    Toolkit toolkit = sniper.getCurrentToolkit();
                    if (toolkit == null) {
                        return 0;
                    } else {
                        ToolkitProperties toolkitProperties = toolkit.getProperties();
                        if (toolkitProperties == null) {
                            return 0;
                        } else {
                            int center;
                            try {
                                center = IntegerArgumentType.getInteger(context, "center");
                            } catch (ArrayIndexOutOfBoundsException | NumberFormatException var7) {
                                sender.sendMessage(ChatFormatting.RED + "Invalid input. Must be a number.");
                                return 0;
                            }

                            toolkitProperties.setCylinderCenter(center);
                            Messenger messenger = new Messenger(sniper.getPlayer());
                            messenger.sendCylinderCenterMessage(center);
                            return 0;
                        }
                    }
                })))
                .then(Commands.literal("vh").then(Commands.argument("height", IntegerArgumentType.integer()).executes((context) -> {
                    Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                    SnipeMessenger sender = new SnipeMessenger(sniper.getCurrentToolkit().getProperties(), sniper.getCurrentToolkit().getCurrentBrushProperties(), sniper.getPlayer());
                    if (sniper == null) {
                        return 0;
                    } else {
                        Toolkit toolkit = sniper.getCurrentToolkit();
                        if (toolkit == null) {
                            return 0;
                        } else {
                            ToolkitProperties toolkitProperties = toolkit.getProperties();
                            if (toolkitProperties == null) {
                                return 0;
                            } else {
                                int height;
                                try {
                                    height = IntegerArgumentType.getInteger(context, "height");
                                } catch (ArrayIndexOutOfBoundsException | NumberFormatException var7) {
                                    sender.sendMessage(ChatFormatting.RED + "Invalid input. Must be a number.");
                                    return 0;
                                }

                                toolkitProperties.setVoxelHeight(height);
                                Messenger messenger = new Messenger(sniper.getPlayer());
                                messenger.sendVoxelHeightMessage(height);
                                return 0;
                            }
                        }
                    }
                })))

                .then(Commands.literal("toolkit")
                        .then(Commands.argument("toolkit_name", StringArgumentType.string())
                                .then(Commands.literal("add")
                                        .then(Commands.literal("arrow").executes((context) -> {
                                            Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                                            ItemStack itemType = sniper.getPlayer().getMainHandItem();
                                            Messenger messenger = new Messenger(sniper.getPlayer());
                                            String toolkitName = StringArgumentType.getString(context, "toolkit_name");
                                            if (sniper.getToolkit(itemType) == null) {
                                                CompoundTag tag = new CompoundTag();
                                                tag.putString("toolkit", toolkitName);
                                                itemType.setTag(tag);
                                                sniper.addToolkit(new Toolkit(toolkitName));
                                                sniper.getToolkit(toolkitName).addToolAction(itemType, ToolAction.ARROW);
                                                sniper.updateItemStackInfo(sniper.getToolkit(toolkitName));
                                                messenger.sendMessage(ChatFormatting.GREEN + "Added toolkit " + toolkitName + " with " + itemType.getItem() + " bound to Arrow.");
                                            } else {
                                                messenger.sendMessage(ChatFormatting.RED + "Toolkit " + toolkitName + " already exists with " + sniper.getToolkit(itemType).getToolAction(itemType).toString() + " bound to this item.");
                                            }
                                            return 0;
                                        }))
                                        .then(Commands.literal("wand").executes((context) -> {
                                            Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                                            ItemStack itemType = sniper.getPlayer().getMainHandItem();
                                            Messenger messenger = new Messenger(sniper.getPlayer());
                                            String toolkitName = StringArgumentType.getString(context, "toolkit_name");
                                            if (sniper.getToolkit(itemType) == null) {
                                                sniper.addToolkit(new Toolkit(toolkitName));
                                                sniper.getToolkit(toolkitName).addToolAction(itemType, ToolAction.WAND);
                                                sniper.updateItemStackInfo(sniper.getToolkit(toolkitName));
                                                messenger.sendMessage(ChatFormatting.GREEN + "Added toolkit " + toolkitName + " with " + itemType.getItem() + " bound to Wand.");
                                            } else {
                                                messenger.sendMessage(ChatFormatting.RED + "Toolkit " + toolkitName + " already exists with " + sniper.getToolkit(itemType).getToolAction(itemType).toString() + " bound to this item.");
                                            }
                                            return 0;
                                        })))

                                .then(Commands.literal("remove").executes((context) -> {
                                    Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                                    ItemStack itemType = sniper.getPlayer().getMainHandItem();
                                    Messenger messenger = new Messenger(sniper.getPlayer());
                                    String toolkitName = StringArgumentType.getString(context, "toolkit_name");
                                    if (sniper.getToolkit(toolkitName) != null) {
                                        if (sniper.getToolkit(toolkitName).getToolAction(itemType) != null) {
                                            sniper.getToolkit(toolkitName).removeToolAction(itemType);
                                            sniper.getPlayer().getInventory().removeItem(itemType);
                                            sniper.cleanToolkits();
                                        } else {
                                            messenger.sendMessage(ChatFormatting.RED + "Toolkit " + toolkitName + " does not have a tool bound to " + itemType.getItem().getName(itemType) + ".");
                                        }
                                    } else {
                                        messenger.sendMessage(ChatFormatting.RED + "Toolkit " + toolkitName + " does not exist.");
                                    }

                                    return 0;
                                }))
                                .then(Commands.literal("gift").then(Commands.argument("recipient_name", StringArgumentType.string()).executes((context) -> {
                                            Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                                            Messenger messenger = new Messenger(sniper.getPlayer());
                                            try {
                                                Sniper recipient = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getServer().getPlayerList().getPlayerByName(StringArgumentType.getString(context, "recipient_name")).getUUID());
                                                if (recipient != null) {
                                                    Messenger recipientMessenger = new Messenger(recipient.getPlayer());
                                                    if (!recipient.getToolkits().contains(sniper.getToolkit(StringArgumentType.getString(context, "toolkit_name")))) {
                                                        recipient.addToolkit(sniper.getToolkit(StringArgumentType.getString(context, "toolkit_name")));
                                                        messenger.sendMessage(ChatFormatting.GREEN + "Gave " + recipient.getPlayer().getName() + " the toolkit " + StringArgumentType.getString(context, "toolkit_name") + ".");
                                                        recipientMessenger.sendMessage(ChatFormatting.GREEN + "You were given the toolkit " + StringArgumentType.getString(context, "toolkit_name") + " by " + sniper.getPlayer().getName() + ".");
                                                    }
                                                }
                                            } catch (NullPointerException e) {
                                                messenger.sendMessage(ChatFormatting.RED + "Player " + context.getArgument("recipient_name", String.class) + " does not exist.");
                                            }
                                            return 0;
                                        }))
                                )))
                .then(Commands.literal("tools").then(Commands.argument("toolkit_name", StringArgumentType.string()).executes((context) -> {
                    Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                    String toolkitName = StringArgumentType.getString(context, "toolkit_name");
                    Messenger messenger = new Messenger(sniper.getPlayer());
                    if (sniper.getToolkit(toolkitName) != null) {
                        for (ItemStack item : sniper.getToolkit(toolkitName).getToolActions().keySet()) {
                            sniper.getPlayer().getInventory().add(-1, item.copy());
                        }
                        sniper.updateItemStackInfo(sniper.getToolkit(toolkitName));

                        messenger.sendMessage(ChatFormatting.GREEN + "Added all items in toolkit " + ChatFormatting.GOLD + toolkitName + " to inventory.");
                    }
                    return 0;
                })))
                .then(Commands.literal("safety").executes((context) -> {
                    Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                    Messenger messenger = new Messenger(sniper.getPlayer());
                    if (sniper.isEnabled()) {
                        sniper.setEnabled(false);
                        messenger.sendMessage(ChatFormatting.RED + "TerraSniper is now disabled.");
                    } else if (!sniper.isEnabled()) {
                        sniper.setEnabled(true);
                        messenger.sendMessage(ChatFormatting.GREEN + "TerraSniper is now enabled.");
                    }
                    return 0;
                }))
                .then(Commands.literal("range").then(Commands.argument("rangeValue", StringArgumentType.string()).executes((context) -> {
                    Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                    Messenger messenger = new Messenger(sniper.getPlayer());
                    if (sniper.getCurrentToolkit() != null) {
                        int range = Integer.parseInt(StringArgumentType.getString(context, "rangeValue"));
                        if (range > 0) {
                            sniper.getCurrentToolkit().getProperties().setBlockTracerRange(range);
                            messenger.sendMessage(ChatFormatting.GREEN + "Set block tracer range to " + ChatFormatting.GOLD + range + ChatFormatting.GREEN + ".");
                        }
                    } else {
                        messenger.sendMessage(ChatFormatting.RED + "You must have a toolkit selected.");
                    }
                    return 0;
                })))
                .then(Commands.literal("brushes").executes((context) -> {
                    Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());

                    if (sniper.getCurrentToolkit() != null) {
                        SnipeMessenger sender = new SnipeMessenger(sniper.getCurrentToolkit().getProperties(), sniper.getCurrentToolkit().getCurrentBrushProperties(), sniper.getPlayer());
                        Toolkit toolkit;
                        toolkit = sniper.getCurrentToolkit();
                        BrushProperties brushProperties = toolkit == null ? null : toolkit.getCurrentBrushProperties();
                        sender.sendMessage(TerraSniper.brushRegistry.getBrushProperties().entrySet().stream().map((entry) -> {
                            return (entry.getValue() == brushProperties ? ChatFormatting.GOLD : ChatFormatting.GRAY) + entry.getKey();
                        }).sorted().collect(Collectors.joining(ChatFormatting.WHITE + ", ", ChatFormatting.AQUA + "Available brushes: ", "")));
                    } else {
                        new Messenger(sniper.getPlayer()).sendMessage(ChatFormatting.RED + "No toolkit selected.");
                    }
                    return 0;
                }))
                .executes((context) -> {
                    Sniper sniper = TerraSniper.sniperRegistry.getSniper(context.getSource().getPlayerOrException().getUUID());
                    if (sniper.getCurrentToolkit() != null) {
                        SnipeMessenger sender = new SnipeMessenger(sniper.getCurrentToolkit().getProperties(), sniper.getCurrentToolkit().getCurrentBrushProperties(), sniper.getPlayer());
                        sender.sendMessage(ChatFormatting.DARK_RED + "TerraSniper - Current Brush Settings:");
                        sniper.sendInfo(sniper.getPlayer());
                    } else {
                        new Messenger(sniper.getPlayer()).sendMessage(ChatFormatting.RED + "No toolkit selected.");
                    }
                    return 0;
                }));
    }
}
