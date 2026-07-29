package com.breakinblocks.animusnv.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.spiritus.ISpiritusGem;
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

    private static final String[] WILL_TYPE_NAMES = Stream.of(SpiritusType.values())
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
                Commands.literal("animusnv")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("getlp")
                                .executes(AnimusCommands::getEV))
                        .then(Commands.literal("setlp")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(AnimusCommands::setEV)))
                        .then(Commands.literal("fillwill")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests(WILL_TYPE_SUGGESTIONS)
                                        .executes(AnimusCommands::fillWill)))
                        .then(Commands.literal("fillgem")
                                .executes(AnimusCommands::fillGem))
        );
    }

    private static int getEV(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        if (network == null) {
            context.getSource().sendFailure(Component.literal("Could not access Anima"));
            return 0;
        }
        int currentEV = network.getCurrentEV();
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.animusnv.getlp.success", String.format("%,d", currentEV)), false);
        return 1;
    }

    private static int setEV(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        if (network == null) {
            context.getSource().sendFailure(Component.literal("Could not access Anima"));
            return 0;
        }
        network.set(AnimaTicket.create(amount), Integer.MAX_VALUE);
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.animusnv.setlp.success", String.format("%,d", amount)), true);
        return 1;
    }

    private static int fillWill(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String typeStr = StringArgumentType.getString(context, "type");
        SpiritusType type;
        try {
            type = SpiritusType.valueOf(typeStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("Invalid spiritus type: " + typeStr
                    + ". Valid types: " + String.join(", ", WILL_TYPE_NAMES)));
            return 0;
        }
        NeoVitaeAPI.getInstance().getSpiritusHandler().fillSpiritusToAmount(
                player.level(), player.blockPosition(), type, 100.0);
        String displayName = type.name().toLowerCase(Locale.ROOT);
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.animusnv.fillwill.success", displayName), true);
        return 1;
    }

    private static int fillGem(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ISpiritusGem gem)) {
            context.getSource().sendFailure(Component.translatable("commands.animusnv.fillgem.not_gem"));
            return 0;
        }

        // Fill all will types to max
        for (SpiritusType type : SpiritusType.values()) {
            int maxSpiritus = gem.getMaxSpiritus(type, stack);
            gem.setSpiritus(type, stack, maxSpiritus);
        }

        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.animusnv.fillgem.success"), true);
        return 1;
    }
}
