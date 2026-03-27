package com.breakinblocks.animusnv;

import com.breakinblocks.animusnv.client.models.AnimusModelLayers;
import com.breakinblocks.animusnv.client.models.SpearModel;
import com.breakinblocks.animusnv.client.renderers.ThrownSpearRenderer;
import com.breakinblocks.animusnv.registry.AnimusEntityTypes;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.breakinblocks.animusnv.client.renderers.AnimusArrowRenderer;
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

@EventBusSubscriber(modid = Constants.Mod.MODID, value = Dist.CLIENT)
public class AnimusClientSetup {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Note: In NeoForge 1.21+, render layers are defined in block/fluid model JSON files
            // using "render_type": "cutout" or "render_type": "translucent"
            // No need to call ItemBlockRenderTypes.setRenderLayer() anymore

            registerToggleableSigilProperty(AnimusItems.SIGIL_BUILDER.get());
            registerToggleableSigilProperty(AnimusItems.SIGIL_LEACH.get());
            registerToggleableSigilProperty(AnimusItems.SIGIL_TRANSPOSITION.get());
            registerToggleableSigilProperty(AnimusItems.SIGIL_MONK.get());

            registerActiveSigilProperty(AnimusItems.SIGIL_REMEDIUM.get());
            registerActiveSigilProperty(AnimusItems.SIGIL_REPARARE.get());
            registerActiveSigilProperty(AnimusItems.SIGIL_HEAVENLY_WRATH.get());
            // TODO: ItemSigilBoundlessNature needs to be ported from 1.20.1
            // registerActiveSigilProperty(AnimusItems.SIGIL_BOUNDLESS_NATURE.get());

            registerBoundSpearProperty(AnimusItems.SPEAR_BOUND.get());

            registerKeyBindingProperty(AnimusItems.KEY_BINDING.get());

            registerSpearThrowingProperty(AnimusItems.SPEAR_IRON.get());
            registerSpearThrowingProperty(AnimusItems.SPEAR_DIAMOND.get());
            registerSpearThrowingProperty(AnimusItems.SPEAR_BOUND.get());
            registerSpearThrowingProperty(AnimusItems.SPEAR_SENTIENT.get());

            if (ModList.get().isLoaded("irons_spellbooks")) {
                registerCrimsonWillSigilProperty();
            }
        });
    }

    /**
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
        event.registerEntityRenderer(AnimusEntityTypes.THROWN_PILUM.get(), ThrownSpearRenderer::new);

        event.registerEntityRenderer(AnimusEntityTypes.SENTIENT_ARROW.get(), AnimusArrowRenderer::new);
        event.registerEntityRenderer(AnimusEntityTypes.HELLFORGED_ARROW.get(), AnimusArrowRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AnimusModelLayers.PILUM, SpearModel::createBodyLayer);
    }

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

    private static void registerBoundSpearProperty(net.minecraft.world.item.Item item) {
        ItemProperties.register(item,
            ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "activated"),
            (stack, level, entity, seed) -> {
                if (item instanceof com.breakinblocks.animusnv.items.ItemSpearBound spear) {
                    return spear.isActivated(stack) ? 1.0F : 0.0F;
                }
                return 0.0F;
            }
        );
    }

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

    private static void registerActiveSigilProperty(net.minecraft.world.item.Item item) {
        ItemProperties.register(item,
            ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "active"),
            (stack, level, entity, seed) -> {
                // NeoVitae still uses custom data for the "Active" tag
                var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                if (customData != null && customData.copyTag().getBoolean("Active")) {
                    return 1.0F;
                }
                return 0.0F;
            }
        );
    }

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
