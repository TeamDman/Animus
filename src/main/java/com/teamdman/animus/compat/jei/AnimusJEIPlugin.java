package com.teamdman.animus.compat.jei;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.ironsspells.ItemBloodInfusedSpellbook;
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
import net.minecraft.core.registries.BuiltInRegistries;
import wayoftime.bloodmagic.common.block.BMBlocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JEI Plugin for Animus mod
 * Shows item info and Iron's Spells altar infusion recipes
 *
 * Note: Imperfect Ritual JEI integration is handled by Blood Magic itself.
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

        // Register the Altar Infusion category (for Iron's Spells compat)
        if (ModList.get().isLoaded("irons_spellbooks")) {
            registration.addRecipeCategories(new AltarInfusionCategory(guiHelper));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
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
     * Register visual recipes for Altar Infusion category.
     *
     * Note: Initial Blood-Infused Spellbook infusion is now a data-driven Blood Altar
     * recipe (with copyInputComponents for preserving spell data) and shows in Blood
     * Magic's native JEI category. This only registers:
     * - Spellbook tier upgrades (1->2, 2->3, etc.) which require right-click interaction
     * - Sanguine Scroll creation which requires right-click interaction
     */
    private void registerAltarInfusionRecipes(IRecipeRegistration registration) {
        try {
            List<AltarInfusionDisplay> displays = new ArrayList<>();

            // Blood-Infused Spellbook tier upgrades (right-click on altar)
            // Initial infusion is now a data-driven recipe shown in Blood Magic's JEI
            Item bloodInfusedSpellbook = getAnimusItem("blood_infused_spellbook");
            if (bloodInfusedSpellbook != null && bloodInfusedSpellbook != Items.AIR) {

                // Upgrade tiers: Tier 1->2, 2->3, 3->4, 4->5, 5->6
                String[] orbNames = {
                    "Apprentice Blood Orb",    // Tier 1 -> 2
                    "Magician's Blood Orb",    // Tier 2 -> 3
                    "Master Blood Orb",        // Tier 3 -> 4
                    "Archmage's Blood Orb",    // Tier 4 -> 5
                    "Transcendent Blood Orb"   // Tier 5 -> 6
                };

                int[] lpCosts = {
                    AnimusConfig.ironsSpells.bloodSpellbookTier2LP.get(),
                    AnimusConfig.ironsSpells.bloodSpellbookTier3LP.get(),
                    AnimusConfig.ironsSpells.bloodSpellbookTier4LP.get(),
                    AnimusConfig.ironsSpells.bloodSpellbookTier5LP.get(),
                    AnimusConfig.ironsSpells.bloodSpellbookTier6LP.get()
                };

                for (int tier = 1; tier <= 5; tier++) {
                    ItemStack tierInput = new ItemStack(bloodInfusedSpellbook);
                    ItemBloodInfusedSpellbook.setInfusionTier(tierInput, tier);

                    ItemStack tierOutput = new ItemStack(bloodInfusedSpellbook);
                    ItemBloodInfusedSpellbook.setInfusionTier(tierOutput, tier + 1);

                    displays.add(AltarInfusionDisplay.createSpellbookUpgrade(
                        tierInput,
                        tierOutput,
                        lpCosts[tier - 1],
                        tier,
                        tier + 1,
                        orbNames[tier - 1],
                        Component.translatable("jei.animus.blood_infused_spellbook.upgrade_title"),
                        Component.translatable("jei.animus.blood_infused_spellbook.upgrade_desc")
                    ));
                }
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
        // Iron's Spells Altar Infusion catalysts
        if (ModList.get().isLoaded("irons_spellbooks")) {
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
