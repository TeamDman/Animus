package com.breakinblocks.animusnv.compat.jei;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.ironsspells.ItemBloodInfusedSpellbook;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.compat.IronsSpellsCompat;
import com.breakinblocks.animusnv.registry.AnimusItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.minecraft.core.registries.BuiltInRegistries;
import com.breakinblocks.neovitae.common.block.NVBlocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JEI Plugin for Animus mod
 * Shows item info and Iron's Spells altar infusion recipes
 *
 * Note: Imperfect Ritual JEI integration is handled by NeoVitae itself.
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

        if (ModList.get().isLoaded("irons_spellbooks")) {
            registration.addRecipeCategories(new AltarInfusionCategory(guiHelper));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(AnimusItems.ANTILIFE_BUCKET.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animusnv.antilife.info")
        );

        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(AnimusItems.LIVING_TERRA_BUCKET.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animusnv.living_terra.info")
        );

        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(AnimusItems.BLOOD_APPLE.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animusnv.blood_apple.info")
        );

        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(AnimusBlocks.BLOCK_ANTILIFE.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animusnv.antilife_block.info")
        );

        if (ModList.get().isLoaded("irons_spellbooks")) {
            registerSanguineScrollsJEI(registration);
            registerAltarInfusionRecipes(registration);
        }

        if (ModList.get().isLoaded("evilcraft")) {
            Item rectifier = BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "sanguine_rectifier"));
            if (rectifier != null && rectifier != Items.AIR) {
                registration.addIngredientInfo(
                    Arrays.asList(new ItemStack(rectifier)),
                    VanillaTypes.ITEM_STACK,
                    Component.translatable("jei.animusnv.sanguine_rectifier.info")
                );
            }
        }
    }

    private void registerSanguineScrollsJEI(IRecipeRegistration registration) {
        try {
            List<ItemStack> scrollStacks = new ArrayList<>();
            String[] scrollNames = {
                "sanguine_scroll_rasa",
                "sanguine_scroll_robur",
                "sanguine_scroll_animata",
                "sanguine_scroll_spiritus",
                "sanguine_scroll_aetherea"
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
                    Component.translatable("jei.animusnv.sanguine_scroll.info")
                );
            }
        } catch (Exception e) {
            // Iron's Spells compat not loaded or items not registered
        }
    }

    private Item getAnimusItem(String name) {
        return BuiltInRegistries.ITEM.get(
            ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, name));
    }

    /**
     * Register visual recipes for Altar Infusion category.
     *
     * Note: Initial Blood-Infused Spellbook infusion is now a data-driven Ara Vitae
     * recipe (with copyInputComponents for preserving spell data) and shows in Blood
     * Magic's native JEI category. This only registers:
     * - Spellbook tier upgrades (1->2, 2->3, etc.) which require right-click interaction
     * - Sanguine Scroll creation which requires right-click interaction
     */
    private void registerAltarInfusionRecipes(IRecipeRegistration registration) {
        try {
            List<AltarInfusionDisplay> displays = new ArrayList<>();

            // Tier upgrades only; initial infusion is a data-driven recipe in NeoVitae's JEI
            Item bloodInfusedSpellbook = getAnimusItem("blood_infused_spellbook");
            if (bloodInfusedSpellbook != null && bloodInfusedSpellbook != Items.AIR) {

                String[] orbNames = {
                    "Apprentice Orb of Vitae",
                    "Magician's Orb of Vitae",
                    "Master Orb of Vitae",
                    "Archmage's Orb of Vitae",
                    "Transcendent Orb of Vitae"
                };

                int[] evCosts = {
                    AnimusConfig.ironsSpells.bloodSpellbookTier2EV.get(),
                    AnimusConfig.ironsSpells.bloodSpellbookTier3EV.get(),
                    AnimusConfig.ironsSpells.bloodSpellbookTier4EV.get(),
                    AnimusConfig.ironsSpells.bloodSpellbookTier5EV.get(),
                    AnimusConfig.ironsSpells.bloodSpellbookTier6EV.get()
                };

                for (int tier = 1; tier <= 5; tier++) {
                    ItemStack tierInput = new ItemStack(bloodInfusedSpellbook);
                    ItemBloodInfusedSpellbook.setInfusionTier(tierInput, tier);

                    ItemStack tierOutput = new ItemStack(bloodInfusedSpellbook);
                    ItemBloodInfusedSpellbook.setInfusionTier(tierOutput, tier + 1);

                    displays.add(AltarInfusionDisplay.createSpellbookUpgrade(
                        tierInput,
                        tierOutput,
                        evCosts[tier - 1],
                        tier,
                        tier + 1,
                        orbNames[tier - 1],
                        Component.translatable("jei.animusnv.blood_infused_spellbook.upgrade_title"),
                        Component.translatable("jei.animusnv.blood_infused_spellbook.upgrade_desc")
                    ));
                }
            }

            // Sanguine Scroll recipes - one per slate tier
            Item ironsScroll = BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "scroll"));

            if (ironsScroll != null && ironsScroll != Items.AIR) {
                ItemStack scrollStack = new ItemStack(ironsScroll);

                String[][] scrollTiers = {
                    {"tabula_rasa", "sanguine_scroll_rasa", "rasa"},
                    {"tabula_robur", "sanguine_scroll_robur", "robur"},
                    {"tabula_animata", "sanguine_scroll_animata", "animata"},
                    {"tabula_spiritus", "sanguine_scroll_spiritus", "spiritus"},
                    {"tabula_aetherea", "sanguine_scroll_aetherea", "aetherea"}
                };

                for (String[] tier : scrollTiers) {
                    Item slate = BuiltInRegistries.ITEM.get(
                        ResourceLocation.fromNamespaceAndPath("neovitae", tier[0]));
                    Item outputScroll = getAnimusItem(tier[1]);

                    if (slate != null && slate != Items.AIR
                        && outputScroll != null && outputScroll != Items.AIR) {
                        displays.add(AltarInfusionDisplay.createSanguineScrollInfusion(
                            scrollStack,
                            new ItemStack(slate),
                            List.of(new ItemStack(outputScroll)),
                            2000,
                            Component.translatable("jei.animusnv.sanguine_scroll.title." + tier[2]),
                            Component.translatable("jei.animusnv.sanguine_scroll.desc")
                        ));
                    }
                }
            }

            if (!displays.isEmpty()) {
                registration.addRecipes(AltarInfusionCategory.RECIPE_TYPE, displays);
            }

        } catch (Exception e) {
            // Iron's Spells compat not fully loaded
        }
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        registration.addAliases(
            VanillaTypes.ITEM_STACK,
            List.of(new ItemStack(AnimusItems.GUIDE_BOOK.get())),
            List.of(
                "jei.animusnv.alias.guide",
                "jei.animusnv.alias.book",
                "jei.animusnv.alias.manual"
            )
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (ModList.get().isLoaded("irons_spellbooks")) {
            registration.addRecipeCatalyst(
                new ItemStack(NVBlocks.ARA_VITAE.block().get()),
                AltarInfusionCategory.RECIPE_TYPE
            );

            // Use registry lookup to avoid class loading issues with IronsSpellsCompat
            String[] animusItems = {
                "blood_infused_spellbook",
                "sanguine_scroll_rasa",
                "sanguine_scroll_robur",
                "sanguine_scroll_animata",
                "sanguine_scroll_spiritus",
                "sanguine_scroll_aetherea"
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

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        if (!ModList.get().isLoaded("malum")) {
            var ingredientManager = jeiRuntime.getIngredientManager();

            ingredientManager.removeIngredientsAtRuntime(
                VanillaTypes.ITEM_STACK,
                List.of(
                    new ItemStack(AnimusItems.RUNIC_SENTIENT_SCYTHE.get()),
                    new ItemStack(AnimusItems.HAND_OF_DEATH.get())
                )
            );

            Animus.LOGGER.debug("JEI: Hidden Malum-dependent items (Malum not loaded)");
        }

        if (!ModList.get().isLoaded("irons_spellbooks")) {
            var ingredientManager = jeiRuntime.getIngredientManager();

            ingredientManager.removeIngredientsAtRuntime(
                VanillaTypes.ITEM_STACK,
                List.of(
                    new ItemStack(IronsSpellsCompat.BLOOD_INFUSED_SPELLBOOK.get()),
                    new ItemStack(IronsSpellsCompat.SIGIL_CRIMSON_WILL.get()),
                    new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_RASA.get()),
                    new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_ROBUR.get()),
                    new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_ANIMATA.get()),
                    new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_SPIRITUS.get()),
                    new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_AETHEREA.get())
                )
            );

            Animus.LOGGER.debug("JEI: Hidden Iron's Spells-dependent items (irons_spellbooks not loaded)");
        }
    }
}
