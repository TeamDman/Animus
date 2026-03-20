package com.teamdman.animus.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.teamdman.animus.Constants;
import com.breakinblocks.neovitae.common.datacomponent.EnumWillType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.ISoulNetwork;
import com.breakinblocks.neovitae.will.IDemonWillGem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;
import java.util.stream.Stream;

@EventBusSubscriber(modid = Constants.Mod.MODID)
public class AnimusCommands {

    private static final String[] WILL_TYPE_NAMES = Stream.of(EnumWillType.values())
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
                        .then(Commands.literal("fillgem")
                                .executes(AnimusCommands::fillGem))
        );
    }

    private static int getLP(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ISoulNetwork network = NeoVitaeAPI.getInstance().getSoulNetwork(player.getUUID());
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
        ISoulNetwork network = NeoVitaeAPI.getInstance().getSoulNetwork(player.getUUID());
        if (network == null) {
            context.getSource().sendFailure(Component.literal("Could not access soul network"));
            return 0;
        }
        network.set(com.breakinblocks.neovitae.api.soul.SoulTicket.create(amount), Integer.MAX_VALUE);
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.animus.setlp.success", String.format("%,d", amount)), true);
        return 1;
    }

    private static int fillWill(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String typeStr = StringArgumentType.getString(context, "type");
        EnumWillType type;
        try {
            type = EnumWillType.valueOf(typeStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("Invalid will type: " + typeStr
                    + ". Valid types: default, corrosive, destructive, vengeful, steadfast"));
            return 0;
        }
        NeoVitaeAPI.getInstance().getDemonWillHandler().fillWillToAmount(
                player.serverLevel(), player.blockPosition(), type, 100.0);
        String displayName = type.name().toLowerCase(Locale.ROOT);
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.animus.fillwill.success", displayName), true);
        return 1;
    }

    private static int fillGem(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof IDemonWillGem gem)) {
            context.getSource().sendFailure(Component.translatable("commands.animus.fillgem.not_gem"));
            return 0;
        }

        // Fill all will types to max
        for (EnumWillType type : EnumWillType.values()) {
            int maxWill = gem.getMaxWill(type, stack);
            gem.setWill(type, stack, maxWill);
        }

        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.animus.fillgem.success"), true);
        return 1;
    }
}
