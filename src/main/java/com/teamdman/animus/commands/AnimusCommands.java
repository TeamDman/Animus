package com.teamdman.animus.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.teamdman.animus.Constants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.api.compat.IDemonWillGem;
import wayoftime.bloodmagic.api.compat.IMultiWillTool;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.demonaura.WorldDemonWillHandler;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.Locale;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = Constants.Mod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnimusCommands {

    private static final String[] WILL_TYPE_NAMES = Stream.of(EnumDemonWillType.values())
            .map(t -> t.name().toLowerCase(Locale.ROOT))
            .toArray(String[]::new);

    private static final SuggestionProvider<CommandSourceStack> WILL_TYPE_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggest(WILL_TYPE_NAMES, builder);

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("animus")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("getlp")
                                .executes(AnimusCommands::getLP))
                        .then(Commands.literal("setlp")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(AnimusCommands::setLP)))
                        .then(Commands.literal("fillwill")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests(WILL_TYPE_SUGGESTIONS)
                                        .executes(AnimusCommands::fillWill)))
                        .then(Commands.literal("gemtype")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests(WILL_TYPE_SUGGESTIONS)
                                        .executes(AnimusCommands::gemType)))
                        .then(Commands.literal("fillgem")
                                .executes(AnimusCommands::fillGem))
        );
    }

    private static int getLP(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SoulNetwork network = NetworkHelper.getSoulNetwork(player);
        if (network == null) {
            context.getSource().sendFailure(Component.literal("Could not access soul network"));
            return 0;
        }
        int currentLP = network.getCurrentEssence();
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.animus.getlp.success", String.format("%,d", currentLP)), false);
        return 1;
    }

    private static int setLP(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        SoulNetwork network = NetworkHelper.getSoulNetwork(player);
        if (network == null) {
            context.getSource().sendFailure(Component.literal("Could not access soul network"));
            return 0;
        }
        network.setCurrentEssence(amount);
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.animus.setlp.success", String.format("%,d", amount)), true);
        return 1;
    }

    private static int fillWill(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String typeStr = StringArgumentType.getString(context, "type");
        EnumDemonWillType type = EnumDemonWillType.getType(typeStr);
        if (type == null) {
            context.getSource().sendFailure(Component.literal("Invalid will type: " + typeStr
                    + ". Valid types: default, corrosive, destructive, vengeful, steadfast"));
            return 0;
        }
        WorldDemonWillHandler.fillWillToMaximum(
                player.serverLevel(), player.blockPosition(), type, 100.0, 100.0, true);
        String displayName = type.name().toLowerCase(Locale.ROOT);
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.animus.fillwill.success", displayName), true);
        return 1;
    }

    private static int gemType(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String typeStr = StringArgumentType.getString(context, "type");
        EnumDemonWillType newType = EnumDemonWillType.getType(typeStr);
        if (newType == null) {
            context.getSource().sendFailure(Component.literal("Invalid will type: " + typeStr
                    + ". Valid types: default, corrosive, destructive, vengeful, steadfast"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof IDemonWillGem gem)) {
            context.getSource().sendFailure(Component.translatable("commands.animus.gemtype.not_gem"));
            return 0;
        }

        // Get current type and will amount, then transfer to new type
        EnumDemonWillType currentType = stack.getItem() instanceof IMultiWillTool tool
                ? tool.getCurrentType(stack) : EnumDemonWillType.DEFAULT;
        double currentWill = gem.getWill(currentType, stack);
        gem.setWill(newType, stack, currentWill);

        String displayName = newType.name().toLowerCase(Locale.ROOT);
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.animus.gemtype.success", displayName), true);
        return 1;
    }

    private static int fillGem(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof IDemonWillGem gem)) {
            context.getSource().sendFailure(Component.translatable("commands.animus.fillgem.not_gem"));
            return 0;
        }

        EnumDemonWillType currentType = stack.getItem() instanceof IMultiWillTool tool
                ? tool.getCurrentType(stack) : EnumDemonWillType.DEFAULT;
        int maxWill = gem.getMaxWill(currentType, stack);
        gem.setWill(currentType, stack, maxWill);

        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.animus.fillgem.success", String.format("%,d", maxWill)), true);
        return 1;
    }
}
