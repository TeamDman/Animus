package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.MinecraftForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

import java.util.List;

/**
 * Compatibility module for Ars Nouveau
 * Handles Living Armor integration with Ars Nouveau spell system
 *
 * Features:
 * - Living Armor gains XP from casting glyphs
 * - Source Attunement upgrade tree for Ars Nouveau spellcasters
 * - Arcane Rune block that consumes Source for altar bonuses
 */
public class ArsNouveauCompat implements ICompatModule {

    private static ArsNouveauCompat INSTANCE;

    // DeferredRegister for Ars Nouveau compatibility blocks
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.Mod.MODID);

    // DeferredRegister for Ars Nouveau compatibility items
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, Constants.Mod.MODID);

    // DeferredRegister for Ars Nouveau compatibility block entities
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.Mod.MODID);

    // Blocks
    public static final RegistryObject<Block> BLOCK_ARCANE_RUNE = BLOCKS.register("arcane_rune",
        com.teamdman.animus.compat.arsnouveau.BlockArcaneRune::new);

    // Block Items
    public static final RegistryObject<Item> ITEM_ARCANE_RUNE = ITEMS.register("arcane_rune",
        () -> new BlockItem(BLOCK_ARCANE_RUNE.get(), new Item.Properties()) {
            @Override
            public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable("tooltip.animus.arcane_rune.flavour"));
                tooltip.add(Component.translatable("tooltip.animus.arcane_rune.info"));
                tooltip.add(Component.translatable("tooltip.animus.arcane_rune.powered"));
                tooltip.add(Component.translatable("tooltip.animus.arcane_rune.unpowered"));
                super.appendHoverText(stack, level, tooltip, flag);
            }
        });

    // Block Entities
    @SuppressWarnings("unchecked")
    public static final RegistryObject<BlockEntityType<com.teamdman.animus.compat.arsnouveau.BlockEntityArcaneRune>> ARCANE_RUNE_BE =
        BLOCK_ENTITIES.register("arcane_rune",
            () -> BlockEntityType.Builder.of(
                com.teamdman.animus.compat.arsnouveau.BlockEntityArcaneRune::new,
                BLOCK_ARCANE_RUNE.get()
            ).build(null));

    public ArsNouveauCompat() {
        INSTANCE = this;
    }

    public static ArsNouveauCompat getInstance() {
        return INSTANCE;
    }

    /**
     * Register the DeferredRegisters to the mod event bus
     * This must be called early, during mod construction
     */
    public static void registerDeferred(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        Animus.LOGGER.info("Registered Ars Nouveau compatibility registries");
    }

    @Override
    public void init() {
        Animus.LOGGER.info("Initializing Ars Nouveau compatibility");

        // Register event listeners for Living Armor integration
        registerEventListeners();

        // Register Source Attunement upgrade
        registerSourceAttunementUpgrade();

        // Register Arcane Rune as a valid BLOODRUNE component for Blood Magic altars
        registerAltarComponent();

        Animus.LOGGER.info("Ars Nouveau compatibility initialized successfully");
    }

    /**
     * Register Arcane Rune as a valid altar component
     */
    private void registerAltarComponent() {
        try {
            wayoftime.bloodmagic.impl.BloodMagicAPI.INSTANCE.registerAltarComponent(
                BLOCK_ARCANE_RUNE.get().defaultBlockState(),
                wayoftime.bloodmagic.altar.ComponentType.BLOODRUNE.name()
            );
            Animus.LOGGER.info("Registered Arcane Rune as BLOODRUNE component for Blood Magic altars");
        } catch (Exception e) {
            Animus.LOGGER.error("Failed to register Arcane Rune with Blood Magic", e);
        }
    }

    @Override
    public String getModId() {
        return "ars_nouveau";
    }

    /**
     * Register Forge event listeners for Ars Nouveau integration
     */
    private void registerEventListeners() {
        // Living Armor XP from spell casting
        MinecraftForge.EVENT_BUS.register(com.teamdman.animus.compat.arsnouveau.LivingArmorGlyphHandler.class);

        Animus.LOGGER.info("Registered Ars Nouveau event listeners");
    }

    /**
     * Register the Source Attunement Living Armor upgrade
     */
    private void registerSourceAttunementUpgrade() {
        try {
            com.teamdman.animus.compat.arsnouveau.UpgradeSourceAttunement upgrade =
                new com.teamdman.animus.compat.arsnouveau.UpgradeSourceAttunement();

            // Register with Blood Magic's Living Armor system
            wayoftime.bloodmagic.core.LivingArmorRegistrar.registerUpgrade(upgrade);

            // Set the upgrade reference in the handler
            com.teamdman.animus.compat.arsnouveau.LivingArmorGlyphHandler.setUpgrade(upgrade);

            Animus.LOGGER.info("Registered Source Attunement Living Armor upgrade");
        } catch (Exception e) {
            Animus.LOGGER.error("Failed to register Source Attunement upgrade", e);
        }
    }
}
