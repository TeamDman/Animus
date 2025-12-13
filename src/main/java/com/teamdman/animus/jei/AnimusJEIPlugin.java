package com.teamdman.animus.jei;

import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.IronsSpellsCompat;
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

        // Sanguine Scrolls (only if Iron's Spellbooks is loaded)
        if (ModList.get().isLoaded("irons_spellbooks")) {
            registerSanguineScrollsJEI(registration);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Imperfect Ritual Stone is the main catalyst - clicking it shows ALL rituals
        registration.addRecipeCatalyst(
            new ItemStack(AnimusBlocks.BLOCK_IMPERFECT_RITUAL_STONE.get()),
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
}
