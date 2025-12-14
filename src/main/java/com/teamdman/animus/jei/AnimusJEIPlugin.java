package com.teamdman.animus.jei;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import wayoftime.bloodmagic.common.block.BMBlocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JEI Plugin for Animus mod
 * Shows imperfect rituals and special transformations
 */
@JeiPlugin
public class AnimusJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        // Register the Imperfect Ritual category
        registration.addRecipeCategories(new ImperfectRitualCategory(guiHelper));

        // Register the Altar Infusion category (for Iron's Spells compat)
        if (ModList.get().isLoaded("irons_spellbooks")) {
            registration.addRecipeCategories(new AltarInfusionCategory(guiHelper));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Register all imperfect ritual displays
        List<ImperfectRitualDisplay> ritualDisplays = ImperfectRitualDisplayFactory.createAllDisplays();
        registration.addRecipes(ImperfectRitualCategory.RECIPE_TYPE, ritualDisplays);

        // Add info for AntiLife Bucket - explains the lightning transformation
        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(com.teamdman.animus.registry.AnimusItems.ANTILIFE_BUCKET.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.antilife.info")
        );

        // Add info for AntiLife block
        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(AnimusBlocks.BLOCK_ANTILIFE.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.antilife_block.info")
        );

        // Sanguine Scrolls and Blood-Infused Spellbook JEI (requires Iron's Spells)
        if (ModList.get().isLoaded("irons_spellbooks")) {
            registerSanguineScrollsJEI(registration);
            registerAltarInfusionRecipes(registration);
        }
    }

    /**
     * Register JEI info for Sanguine Scrolls
     */
    private void registerSanguineScrollsJEI(IRecipeRegistration registration) {
        try {
            // Get scroll items from registry to avoid class loading issues
            List<ItemStack> scrollStacks = new ArrayList<>();
            String[] scrollNames = {
                "sanguine_scroll_blank",
                "sanguine_scroll_reinforced",
                "sanguine_scroll_imbued",
                "sanguine_scroll_demon",
                "sanguine_scroll_ethereal"
            };
            for (String name : scrollNames) {
                Item item = BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, name));
                if (item != null && item != Items.AIR) {
                    scrollStacks.add(new ItemStack(item));
                }
            }

            if (!scrollStacks.isEmpty()) {
                registration.addIngredientInfo(
                    scrollStacks,
                    VanillaTypes.ITEM_STACK,
                    Component.translatable("jei.animus.sanguine_scroll.info")
                );
            }
        } catch (Exception e) {
            // Iron's Spells compat not loaded or items not registered
        }
    }

    /**
     * Helper to get an Animus item from the registry by name
     */
    private Item getAnimusItem(String name) {
        return BuiltInRegistries.ITEM.get(
            ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, name));
    }

    /**
     * Register visual recipes for Altar Infusion category
     * Shows Blood-Infused Spellbook and Sanguine Scroll creation
     */
    private void registerAltarInfusionRecipes(IRecipeRegistration registration) {
        try {
            List<AltarInfusionDisplay> displays = new ArrayList<>();

            // Blood-Infused Spellbook recipe
            Item leatherSpellbook = BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "leather_spellbook"));
            Item bloodInfusedSpellbook = getAnimusItem("blood_infused_spellbook");
            if (leatherSpellbook != null && leatherSpellbook != Items.AIR
                && bloodInfusedSpellbook != null && bloodInfusedSpellbook != Items.AIR) {
                displays.add(AltarInfusionDisplay.createSpellbookInfusion(
                    new ItemStack(leatherSpellbook),
                    new ItemStack(bloodInfusedSpellbook),
                    10000,
                    Component.translatable("jei.animus.blood_infused_spellbook.title"),
                    Component.translatable("jei.animus.blood_infused_spellbook.desc")
                ));
            }

            // Sanguine Scroll recipes - one per slate tier
            // Get the generic scroll item from Iron's Spells
            Item ironsScroll = BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "scroll"));

            if (ironsScroll != null && ironsScroll != Items.AIR) {
                ItemStack scrollStack = new ItemStack(ironsScroll);

                // Define scroll tiers: slate name, output item name, lang key suffix
                String[][] scrollTiers = {
                    {"blankslate", "sanguine_scroll_blank", "blank"},
                    {"reinforcedslate", "sanguine_scroll_reinforced", "reinforced"},
                    {"imbuedslate", "sanguine_scroll_imbued", "imbued"},
                    {"demonslate", "sanguine_scroll_demon", "demon"},
                    {"etherealslate", "sanguine_scroll_ethereal", "ethereal"}
                };

                for (String[] tier : scrollTiers) {
                    Item slate = BuiltInRegistries.ITEM.get(
                        ResourceLocation.fromNamespaceAndPath("bloodmagic", tier[0]));
                    Item outputScroll = getAnimusItem(tier[1]);

                    if (slate != null && slate != Items.AIR
                        && outputScroll != null && outputScroll != Items.AIR) {
                        displays.add(AltarInfusionDisplay.createSanguineScrollInfusion(
                            scrollStack,
                            new ItemStack(slate),
                            List.of(new ItemStack(outputScroll)),
                            2000,
                            Component.translatable("jei.animus.sanguine_scroll.title." + tier[2]),
                            Component.translatable("jei.animus.sanguine_scroll.desc")
                        ));
                    }
                }
            }

            // Register all displays
            if (!displays.isEmpty()) {
                registration.addRecipes(AltarInfusionCategory.RECIPE_TYPE, displays);
            }

        } catch (Exception e) {
            // Iron's Spells compat not fully loaded
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Blood Magic's Imperfect Ritual Stone is the main catalyst - clicking it shows ALL rituals
        registration.addRecipeCatalyst(
            new ItemStack(BMBlocks.IMPERFECT_RITUAL_STONE.block().get()),
            ImperfectRitualCategory.RECIPE_TYPE
        );

        // Also register each trigger block as a catalyst so clicking them shows their ritual
        // Vanilla blocks
        registration.addRecipeCatalyst(new ItemStack(Items.BOOKSHELF), ImperfectRitualCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.BONE_BLOCK), ImperfectRitualCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.AMETHYST_BLOCK), ImperfectRitualCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.ANCIENT_DEBRIS), ImperfectRitualCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.GLOWSTONE), ImperfectRitualCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.PRISMARINE), ImperfectRitualCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.SCULK), ImperfectRitualCategory.RECIPE_TYPE);

        // Mod-dependent catalyst blocks
        if (ModList.get().isLoaded("botania")) {
            Item manasteelBlock = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("botania", "manasteel_block"));
            if (manasteelBlock != null && manasteelBlock != Items.AIR) {
                registration.addRecipeCatalyst(new ItemStack(manasteelBlock), ImperfectRitualCategory.RECIPE_TYPE);
            }
        }

        if (ModList.get().isLoaded("malum")) {
            Item hallowedGoldBlock = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("malum", "block_of_hallowed_gold"));
            if (hallowedGoldBlock != null && hallowedGoldBlock != Items.AIR) {
                registration.addRecipeCatalyst(new ItemStack(hallowedGoldBlock), ImperfectRitualCategory.RECIPE_TYPE);
            }
        }

        if (ModList.get().isLoaded("ars_nouveau")) {
            Item sourceGemBlock = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("ars_nouveau", "source_gem_block"));
            if (sourceGemBlock != null && sourceGemBlock != Items.AIR) {
                registration.addRecipeCatalyst(new ItemStack(sourceGemBlock), ImperfectRitualCategory.RECIPE_TYPE);
            }
        }

        if (ModList.get().isLoaded("irons_spellbooks")) {
            Item arcaneAnvil = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "arcane_anvil"));
            if (arcaneAnvil != null && arcaneAnvil != Items.AIR) {
                registration.addRecipeCatalyst(new ItemStack(arcaneAnvil), ImperfectRitualCategory.RECIPE_TYPE);
            }

            // Blood Altar is the catalyst for Altar Infusion recipes (Iron's Spells compat)
            registration.addRecipeCatalyst(
                new ItemStack(BMBlocks.BLOOD_ALTAR.block().get()),
                AltarInfusionCategory.RECIPE_TYPE
            );

            // Register output items as catalysts so clicking them shows how to craft them
            // Use registry lookup to avoid class loading issues with IronsSpellsCompat
            String[] animusItems = {
                "blood_infused_spellbook",
                "sanguine_scroll_blank",
                "sanguine_scroll_reinforced",
                "sanguine_scroll_imbued",
                "sanguine_scroll_demon",
                "sanguine_scroll_ethereal"
            };
            for (String itemName : animusItems) {
                Item item = BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, itemName));
                if (item != null && item != Items.AIR) {
                    registration.addRecipeCatalyst(new ItemStack(item), AltarInfusionCategory.RECIPE_TYPE);
                }
            }
        }
    }

}
