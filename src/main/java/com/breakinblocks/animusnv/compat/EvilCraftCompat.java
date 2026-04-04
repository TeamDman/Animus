package com.breakinblocks.animusnv.compat;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.evilcraft.BlockEntitySanguineRectifier;
import com.breakinblocks.animusnv.compat.evilcraft.BlockSanguineRectifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class EvilCraftCompat implements ICompatModule {

    private static EvilCraftCompat INSTANCE;

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(Constants.Mod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(Constants.Mod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.Mod.MODID);

    public static final DeferredHolder<Block, BlockSanguineRectifier> SANGUINE_RECTIFIER =
        BLOCKS.register("sanguine_rectifier", BlockSanguineRectifier::new);

    public static final DeferredHolder<Item, BlockItem> SANGUINE_RECTIFIER_ITEM =
        ITEMS.register("sanguine_rectifier", () -> new BlockItem(SANGUINE_RECTIFIER.get(), new Item.Properties()) {
            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable("tooltip.animusnv.sanguine_rectifier.desc")
                    .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("tooltip.animusnv.sanguine_rectifier.ev_to_blood")
                    .withStyle(ChatFormatting.DARK_RED));
                tooltip.add(Component.translatable("tooltip.animusnv.sanguine_rectifier.blood_to_altar")
                    .withStyle(ChatFormatting.DARK_RED));
                tooltip.add(Component.translatable("tooltip.animusnv.sanguine_rectifier.rate")
                    .withStyle(ChatFormatting.DARK_AQUA));
                if (!BlockEntitySanguineRectifier.isEvToBloodEnabled()) {
                    tooltip.add(Component.translatable("tooltip.animusnv.sanguine_rectifier.ev_disabled")
                        .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                }
                super.appendHoverText(stack, context, tooltip, flag);
            }
        });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntitySanguineRectifier>> SANGUINE_RECTIFIER_BE =
        BLOCK_ENTITIES.register("sanguine_rectifier", () -> BlockEntityType.Builder.of(
            BlockEntitySanguineRectifier::new,
            SANGUINE_RECTIFIER.get()
        ).build(null));

    public EvilCraftCompat() {
        INSTANCE = this;
    }

    public static EvilCraftCompat getInstance() {
        return INSTANCE;
    }

    public static void registerDeferred(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        Animus.LOGGER.debug("Registered EvilCraft compatibility registries");
    }

    @Override
    public void init() {
        Animus.LOGGER.debug("Initializing EvilCraft compatibility");
    }

    @Override
    public String getModId() {
        return "evilcraft";
    }
}
