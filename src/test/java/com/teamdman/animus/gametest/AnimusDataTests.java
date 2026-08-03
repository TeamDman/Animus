package com.teamdman.animus.gametest;

import com.teamdman.animus.advancements.AltarTierTrigger;
import com.teamdman.animus.recipes.ImperfectRitualRecipe;
import com.teamdman.animus.registry.AnimusRecipeTypes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder("animus")
@PrefixGameTestTemplate(false)
public class AnimusDataTests {

    private static final String EMPTY = "empty";

    @GameTest(template = EMPTY)
    public static void imperfectRitualRecipesLoad(GameTestHelper helper) {
        List<ImperfectRitualRecipe> recipes = helper.getLevel().getRecipeManager()
            .getAllRecipesFor(AnimusRecipeTypes.IMPERFECT_RITUAL_TYPE.get());

        if (recipes.isEmpty()) {
            helper.fail("no imperfect ritual recipes loaded; the Imperfect Ritual Stone would find nothing");
            return;
        }

        boolean hunger = recipes.stream().anyMatch(recipe -> recipe.matches(Blocks.BONE_BLOCK.defaultBlockState()));
        if (!hunger) {
            helper.fail("no imperfect ritual matches a Bone Block");
            return;
        }

        boolean matchesPlainStone = recipes.stream().anyMatch(recipe -> recipe.matches(Blocks.STONE.defaultBlockState()));
        if (matchesPlainStone) {
            helper.fail("an imperfect ritual matches plain Stone, so the stone would fire on the wrong block");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void imperfectRitualCostsArePositive(GameTestHelper helper) {
        List<ImperfectRitualRecipe> recipes = helper.getLevel().getRecipeManager()
            .getAllRecipesFor(AnimusRecipeTypes.IMPERFECT_RITUAL_TYPE.get());

        for (ImperfectRitualRecipe recipe : recipes) {
            if (recipe.getLpCost() < 0) {
                helper.fail(recipe.getRitualKey() + " has a negative LP cost");
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void altarTierTriggerIsRegistered(GameTestHelper helper) {
        if (CriteriaTriggers.getCriterion(AltarTierTrigger.ID) == null) {
            helper.fail("the " + AltarTierTrigger.ID + " criterion is not registered, so Tier 6 Ascension can never fire");
            return;
        }

        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void animusAdvancementsLoad(GameTestHelper helper) {
        ResourceLocation tier6 = ResourceLocation.fromNamespaceAndPath("animus", "tier6_ascension");
        Advancement advancement = helper.getLevel().getServer().getAdvancements().getAdvancement(tier6);

        if (advancement == null) {
            helper.fail(tier6 + " failed to load; check its criteria for malformed conditions");
            return;
        }

        if (advancement.getCriteria().isEmpty()) {
            helper.fail(tier6 + " has no criteria, so it would be granted immediately");
            return;
        }

        helper.succeed();
    }
}
