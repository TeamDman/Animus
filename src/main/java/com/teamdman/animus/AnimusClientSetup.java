package com.teamdman.animus;

import com.teamdman.animus.client.models.AnimusModelLayers;
import com.teamdman.animus.client.models.SpearModel;
import com.teamdman.animus.client.renderers.ThrownSpearRenderer;
import com.teamdman.animus.registry.AnimusEntityTypes;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.client.renderers.AnimusArrowRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import com.breakinblocks.neovitae.common.item.IActivatable;

/**
 * Client-side setup for Animus mod
 * Handles render layers and other client-only initialization
 */
@EventBusSubscriber(modid = Constants.Mod.MODID, value = Dist.CLIENT)
public class AnimusClientSetup {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Note: In NeoForge 1.21+, render layers are defined in block/fluid model JSON files
            // using "render_type": "cutout" or "render_type": "translucent"
            // No need to call ItemBlockRenderTypes.setRenderLayer() anymore

            // Register item properties for toggleable sigils (use "activated" NBT tag)
            registerToggleableSigilProperty(AnimusItems.SIGIL_BUILDER.get());
            registerToggleableSigilProperty(AnimusItems.SIGIL_LEACH.get());
            registerToggleableSigilProperty(AnimusItems.SIGIL_TRANSPOSITION.get());
            registerToggleableSigilProperty(AnimusItems.SIGIL_MONK.get());

            // Register item properties for active-state sigils (Remedium, Reparare, Heavenly Wrath)
            // These use "Active" NBT tag
            registerActiveSigilProperty(AnimusItems.SIGIL_REMEDIUM.get());
            registerActiveSigilProperty(AnimusItems.SIGIL_REPARARE.get());
            registerActiveSigilProperty(AnimusItems.SIGIL_HEAVENLY_WRATH.get());
            // TODO: ItemSigilBoundlessNature needs to be ported from 1.20.1
            // registerActiveSigilProperty(AnimusItems.SIGIL_BOUNDLESS_NATURE.get());

            // Register item property for Bound Spear activation state
            registerBoundSpearProperty(AnimusItems.SPEAR_BOUND.get());

            // Register item property for Key of Binding bound state
            registerKeyBindingProperty(AnimusItems.KEY_BINDING.get());

            // Register "throwing" property for all spears (like trident)
            registerSpearThrowingProperty(AnimusItems.SPEAR_IRON.get());
            registerSpearThrowingProperty(AnimusItems.SPEAR_DIAMOND.get());
            registerSpearThrowingProperty(AnimusItems.SPEAR_BOUND.get());
            registerSpearThrowingProperty(AnimusItems.SPEAR_SENTIENT.get());

            // Register Iron's Spells compat item properties (if mod is loaded)
            if (ModList.get().isLoaded("irons_spellbooks")) {
                registerCrimsonWillSigilProperty();
            }
        });
    }

    /**
     * Registers the "active" item property for Sigil of Crimson Will
     * This allows the model to switch between active/inactive textures
     * Uses registry lookup to avoid class loading issues with IronsSpellsCompat
     */
    private static void registerCrimsonWillSigilProperty() {
        try {
            Item sigilCrimsonWill = BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "sigil_crimson_will"));
            if (sigilCrimsonWill != null && sigilCrimsonWill != Items.AIR) {
                ItemProperties.register(sigilCrimsonWill,
                    ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "active"),
                    (stack, level, entity, seed) -> {
                        // Check for Active state in custom data
                        var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                        if (customData != null && customData.copyTag().getBoolean("Active")) {
                            return 1.0F;
                        }
                        return 0.0F;
                    }
                );
            }
        } catch (Exception e) {
            // Item not registered yet or compat not loaded
        }
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register custom renderer for thrown spear
        event.registerEntityRenderer(AnimusEntityTypes.THROWN_PILUM.get(), ThrownSpearRenderer::new);

        // Register arrow renderers for custom bow projectiles
        event.registerEntityRenderer(AnimusEntityTypes.SENTIENT_ARROW.get(), AnimusArrowRenderer::new);
        event.registerEntityRenderer(AnimusEntityTypes.HELLFORGED_ARROW.get(), AnimusArrowRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Register custom model layer for spear
        event.registerLayerDefinition(AnimusModelLayers.PILUM, SpearModel::createBodyLayer);
    }

    /**
     * Registers the "activated" item property for toggleable sigils
     * This allows models to switch textures based on activation state
     */
    private static void registerToggleableSigilProperty(net.minecraft.world.item.Item item) {
        ItemProperties.register(item,
            ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "activated"),
            (stack, level, entity, seed) -> {
                if (item instanceof IActivatable activatable) {
                    return activatable.getActivated(stack) ? 1.0F : 0.0F;
                }
                return 0.0F;
            }
        );
    }

    /**
     * Registers the "activated" item property for Bound Spear
     * This allows models to switch between activated/deactivated textures
     */
    private static void registerBoundSpearProperty(net.minecraft.world.item.Item item) {
        ItemProperties.register(item,
            ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "activated"),
            (stack, level, entity, seed) -> {
                if (item instanceof com.teamdman.animus.items.ItemSpearBound spear) {
                    return spear.isActivated(stack) ? 1.0F : 0.0F;
                }
                return 0.0F;
            }
        );
    }

    /**
     * Registers the "bound" item property for Key of Binding
     * This allows models to switch between bound/unbound textures
     */
    private static void registerKeyBindingProperty(net.minecraft.world.item.Item item) {
        ItemProperties.register(item,
            ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "bound"),
            (stack, level, entity, seed) -> {
                if (item instanceof com.breakinblocks.neovitae.common.item.IBindable bindable) {
                    return bindable.getBinding(stack) != null ? 1.0F : 0.0F;
                }
                return 0.0F;
            }
        );
    }

    /**
     * Registers the "active" item property for sigils with active/inactive states
     * This allows models to switch textures based on the "Active" data component
     */
    private static void registerActiveSigilProperty(net.minecraft.world.item.Item item) {
        ItemProperties.register(item,
            ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "active"),
            (stack, level, entity, seed) -> {
                // In 1.21+, NBT is replaced by data components
                // For now, check for the old NBT tag pattern - Blood Magic may still use custom data
                var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                if (customData != null && customData.copyTag().getBoolean("Active")) {
                    return 1.0F;
                }
                return 0.0F;
            }
        );
    }

    /**
     * Registers the "throwing" item property for spears (like vanilla trident)
     * Returns 1.0 when the player is charging to throw the spear
     */
    private static void registerSpearThrowingProperty(net.minecraft.world.item.Item item) {
        ItemProperties.register(item,
            ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "throwing"),
            (stack, level, entity, seed) -> {
                if (entity != null && entity.isUsingItem() && entity.getUseItem() == stack) {
                    return 1.0F;
                }
                return 0.0F;
            }
        );
    }
}
