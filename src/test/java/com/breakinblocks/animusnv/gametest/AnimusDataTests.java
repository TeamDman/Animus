package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.advancements.AnimusCriteriaTriggers;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("animusnv")
@PrefixGameTestTemplate(false)
public class AnimusDataTests {

    private static final String EMPTY = "empty";

    @GameTest(template = EMPTY)
    public static void animusAdvancementsLoad(GameTestHelper helper) {
        ResourceLocation root = ResourceLocation.fromNamespaceAndPath("animusnv", "root");
        ResourceLocation tier6 = ResourceLocation.fromNamespaceAndPath("animusnv", "tier6_ascension");

        for (ResourceLocation id : new ResourceLocation[]{root, tier6}) {
            AdvancementHolder holder = helper.getLevel().getServer().getAdvancements().get(id);
            if (holder == null) {
                helper.fail(id + " failed to load; check the data directory name and the criteria format");
                return;
            }
            if (holder.value().criteria().isEmpty()) {
                helper.fail(id + " has no criteria, so it would be granted immediately");
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void altarTierTriggerIsRegistered(GameTestHelper helper) {
        if (AnimusCriteriaTriggers.ALTAR_TIER.get() == null) {
            helper.fail("the altar_tier criterion is not registered, so Tier 6 Ascension can never fire");
            return;
        }

        helper.succeed();
    }
}
