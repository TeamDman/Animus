package com.teamdman.animus.jei;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.IronsSpellsCompat;
import com.teamdman.animus.compat.ironsspells.ItemBloodInfusedSpellbook;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import wayoftime.bloodmagic.common.block.BloodMagicBlocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * JEI Plugin for Animus mod
 * Shows imperfect rituals and special transformations
 */
@JeiPlugin
public class AnimusJEIPlugin implements IModPlugin {

    private List<ImperfectRitualDisplay> cachedRitualDisplays;

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        Animus.LOGGER.info("JEI: Registering recipe categories");

        // Register the Imperfect Ritual category
        registration.addRecipeCategories(new ImperfectRitualCategory(guiHelper));
        Animus.LOGGER.info("JEI: Registered ImperfectRitualCategory");

        // Register the Altar Infusion category (for Iron's Spells compat)
        if (ModList.get().isLoaded("irons_spellbooks")) {
            registration.addRecipeCategories(new AltarInfusionCategory(guiHelper));
            Animus.LOGGER.info("JEI: Registered AltarInfusionCategory (irons_spellbooks loaded)");
        } else {
            Animus.LOGGER.info("JEI: Skipping AltarInfusionCategory (irons_spellbooks not loaded)");
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Get recipe manager and build displays from loaded recipes
        RecipeManager recipeManager = Minecraft.getInstance().getConnection().getRecipeManager();
        cachedRitualDisplays = ImperfectRitualDisplayFactory.createAllDisplays(recipeManager);
        registration.addRecipes(ImperfectRitualCategory.RECIPE_TYPE, cachedRitualDisplays);

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

        // Sanguine Scrolls and Altar Infusion recipes (only if Iron's Spellbooks is loaded)
        if (ModList.get().isLoaded("irons_spellbooks")) {
            registerSanguineScrollsJEI(registration);
            registerAltarInfusionRecipes(registration);
        }
    }

    /**
     * Register visual recipes for Altar Infusion category
     * Shows Blood-Infused Spellbook and Sanguine Scroll creation
     */
    private void registerAltarInfusionRecipes(IRecipeRegistration registration) {
        try {
            List<AltarInfusionDisplay> displays = new ArrayList<>();

            Animus.LOGGER.info("JEI: Starting altar infusion recipe registration");

            // Blood-Infused Spellbook recipe - use direct registry reference
            Item bloodInfusedSpellbook = IronsSpellsCompat.BLOOD_INFUSED_SPELLBOOK.get();
            Animus.LOGGER.info("JEI: Blood-Infused Spellbook item: {}", bloodInfusedSpellbook);

            // Try to get any spellbook from Iron's Spells as input
            // Iron's Spellbooks uses "spell_book" naming convention
            String[] spellbookNames = {
                "copper_spell_book", "iron_spell_book", "gold_spell_book",
                "diamond_spell_book", "netherite_spell_book",
                "copper_spellbook", "iron_spellbook", "gold_spellbook",
                "diamond_spellbook", "netherite_spellbook",
                "leather_spell_book", "leather_spellbook"
            };
            Item inputSpellbook = null;
            for (String name : spellbookNames) {
                inputSpellbook = ForgeRegistries.ITEMS.getValue(
                    ResourceLocation.fromNamespaceAndPath("irons_spellbooks", name));
                if (inputSpellbook != null && inputSpellbook != Items.AIR) {
                    Animus.LOGGER.info("JEI: Found input spellbook: irons_spellbooks:{}", name);
                    break;
                }
            }
            if (inputSpellbook == null || inputSpellbook == Items.AIR) {
                Animus.LOGGER.warn("JEI: Could not find any Iron's Spellbooks spellbook item");
            }

            if (bloodInfusedSpellbook != null) {
                // Use found spellbook or fall back to showing the output as a self-reference
                ItemStack inputStack;
                if (inputSpellbook != null && inputSpellbook != Items.AIR) {
                    inputStack = new ItemStack(inputSpellbook);
                } else {
                    // Fallback: use enchanted book as placeholder to indicate "any spellbook"
                    inputStack = new ItemStack(Items.ENCHANTED_BOOK);
                    Animus.LOGGER.info("JEI: Using enchanted book as placeholder for input spellbook");
                }

                // Create tier 1 output (initial infusion result)
                ItemStack tier1Output = new ItemStack(bloodInfusedSpellbook);
                ItemBloodInfusedSpellbook.setInfusionTier(tier1Output, 1);

                displays.add(AltarInfusionDisplay.createSpellbookInfusion(
                    inputStack,
                    tier1Output,
                    AnimusConfig.ironsSpells.bloodSpellbookTier1LP.get(),
                    Component.translatable("jei.animus.blood_infused_spellbook.title"),
                    Component.translatable("jei.animus.blood_infused_spellbook.desc")
                ));
                Animus.LOGGER.info("JEI: Added Blood-Infused Spellbook creation recipe display");

                // Add upgrade tier displays (Tier 1→2, 2→3, 3→4, 4→5, 5→6)
                String[] orbNames = {
                    "Apprentice Blood Orb",    // Tier 2
                    "Magician's Blood Orb",    // Tier 3
                    "Master Blood Orb",        // Tier 4
                    "Archmage's Blood Orb",    // Tier 5
                    "Transcendent Blood Orb"   // Tier 6
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
                Animus.LOGGER.info("JEI: Added 5 Blood-Infused Spellbook upgrade displays");

            } else {
                Animus.LOGGER.error("JEI: Blood-Infused Spellbook item is null! Cannot create recipe display.");
            }

            // Sanguine Scroll recipes - one per slate tier
            Item ironsScroll = ForgeRegistries.ITEMS.getValue(
                ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "scroll"));

            if (ironsScroll != null && ironsScroll != Items.AIR) {
                ItemStack scrollStack = new ItemStack(ironsScroll);

                // Define scroll tiers: slate name, output item getter, lang key suffix
                Object[][] scrollTiers = {
                    {"blankslate", IronsSpellsCompat.SANGUINE_SCROLL_BLANK, "blank"},
                    {"reinforcedslate", IronsSpellsCompat.SANGUINE_SCROLL_REINFORCED, "reinforced"},
                    {"imbuedslate", IronsSpellsCompat.SANGUINE_SCROLL_IMBUED, "imbued"},
                    {"demonslate", IronsSpellsCompat.SANGUINE_SCROLL_DEMON, "demon"},
                    {"etherealslate", IronsSpellsCompat.SANGUINE_SCROLL_ETHEREAL, "ethereal"}
                };

                for (Object[] tier : scrollTiers) {
                    Item slate = ForgeRegistries.ITEMS.getValue(
                        ResourceLocation.fromNamespaceAndPath("bloodmagic", (String) tier[0]));
                    @SuppressWarnings("unchecked")
                    Item outputScroll = ((net.minecraftforge.registries.RegistryObject<Item>) tier[1]).get();

                    if (slate != null && slate != Items.AIR && outputScroll != null) {
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
                Animus.LOGGER.info("Registered {} altar infusion recipes for JEI", displays.size());
            } else {
                Animus.LOGGER.warn("No altar infusion recipes to register for JEI");
            }

        } catch (Exception e) {
            Animus.LOGGER.error("Failed to register altar infusion recipes for JEI", e);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Imperfect Ritual Stone is the main catalyst - clicking it shows ALL rituals
        registration.addRecipeCatalyst(
            new ItemStack(AnimusBlocks.BLOCK_IMPERFECT_RITUAL_STONE.get()),
            ImperfectRitualCategory.RECIPE_TYPE
        );

        // Register each unique trigger block from the cached displays as a catalyst
        if (cachedRitualDisplays != null) {
            Set<Item> registeredItems = new LinkedHashSet<>();
            for (ImperfectRitualDisplay display : cachedRitualDisplays) {
                Item triggerItem = display.getTriggerBlockType().asItem();
                if (triggerItem != Items.AIR && registeredItems.add(triggerItem)) {
                    registration.addRecipeCatalyst(new ItemStack(triggerItem), ImperfectRitualCategory.RECIPE_TYPE);
                }
            }
        }

        // Altar Infusion catalysts (Iron's Spellbooks compat)
        if (ModList.get().isLoaded("irons_spellbooks")) {
            registration.addRecipeCatalyst(
                new ItemStack(BloodMagicBlocks.BLOOD_ALTAR.get()),
                AltarInfusionCategory.RECIPE_TYPE
            );

            Item bloodInfusedSpellbook = IronsSpellsCompat.BLOOD_INFUSED_SPELLBOOK.get();
            if (bloodInfusedSpellbook != null) {
                registration.addRecipeCatalyst(
                    new ItemStack(bloodInfusedSpellbook),
                    AltarInfusionCategory.RECIPE_TYPE
                );
            }
        }
    }

    /**
     * Register JEI info for Sanguine Scrolls
     * Called only when Iron's Spellbooks is present
     */
    private void registerSanguineScrollsJEI(IRecipeRegistration registration) {
        // Add info for all Sanguine Scroll tiers - this makes them searchable in JEI
        registration.addIngredientInfo(
            Arrays.asList(
                new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_BLANK.get()),
                new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_REINFORCED.get()),
                new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_IMBUED.get()),
                new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_DEMON.get()),
                new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_ETHEREAL.get())
            ),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.sanguine_scroll.info")
        );

        // Individual tier info for crafting costs
        registration.addIngredientInfo(
            Arrays.asList(new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_BLANK.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.sanguine_scroll.blank")
        );

        registration.addIngredientInfo(
            Arrays.asList(new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_REINFORCED.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.sanguine_scroll.reinforced")
        );

        registration.addIngredientInfo(
            Arrays.asList(new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_IMBUED.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.sanguine_scroll.imbued")
        );

        registration.addIngredientInfo(
            Arrays.asList(new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_DEMON.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.sanguine_scroll.demon")
        );

        registration.addIngredientInfo(
            Arrays.asList(new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_ETHEREAL.get())),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animus.sanguine_scroll.ethereal")
        );
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        // Hide Malum-dependent items when Malum is not loaded
        if (!ModList.get().isLoaded("malum")) {
            var ingredientManager = jeiRuntime.getIngredientManager();

            // Hide Runic Sentient Scythe - requires Malum
            ingredientManager.removeIngredientsAtRuntime(
                VanillaTypes.ITEM_STACK,
                List.of(new ItemStack(AnimusItems.RUNIC_SENTIENT_SCYTHE.get()))
            );

            // Hide Hand of Death - requires Malum
            ingredientManager.removeIngredientsAtRuntime(
                VanillaTypes.ITEM_STACK,
                List.of(new ItemStack(AnimusItems.HAND_OF_DEATH.get()))
            );

            Animus.LOGGER.info("JEI: Hidden Malum-dependent items (Malum not loaded)");
        }

        // Hide Iron's Spells-dependent items when Iron's Spells is not loaded
        if (!ModList.get().isLoaded("irons_spellbooks")) {
            var ingredientManager = jeiRuntime.getIngredientManager();

            // Hide all Iron's Spells compat items
            ingredientManager.removeIngredientsAtRuntime(
                VanillaTypes.ITEM_STACK,
                List.of(
                    new ItemStack(IronsSpellsCompat.BLOOD_INFUSED_SPELLBOOK.get()),
                    new ItemStack(IronsSpellsCompat.SIGIL_CRIMSON_WILL.get()),
                    new ItemStack(IronsSpellsCompat.REAGENT_CRIMSON_WILL.get()),
                    new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_BLANK.get()),
                    new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_REINFORCED.get()),
                    new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_IMBUED.get()),
                    new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_DEMON.get()),
                    new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_ETHEREAL.get())
                )
            );

            Animus.LOGGER.info("JEI: Hidden Iron's Spells-dependent items (irons_spellbooks not loaded)");
        }
    }
}
