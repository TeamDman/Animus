package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.advancements.AnimusCriteriaTriggers;
import com.breakinblocks.animusnv.gametest.base.AnimusTestRegistrar;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;

import java.util.List;
import java.util.Optional;

public final class AnimusDataTests {

    private AnimusDataTests() {
    }

    public static void register(AnimusTestRegistrar r) {
        r.add("animus_advancements_load", AnimusDataTests::animusAdvancementsLoad);
        r.add("altar_tier_trigger_registered", AnimusDataTests::altarTierTriggerIsRegistered);
    }

    private static void animusAdvancementsLoad(GameTestHelper helper) {
        ServerAdvancementManager manager = helper.getLevel().getServer().getAdvancements();

        List<AdvancementHolder> ours = manager.getAllAdvancements().stream()
                .filter(a -> a.id().getNamespace().equals(Constants.Mod.MODID))
                .toList();

        if (ours.size() < 14) {
            helper.fail("only " + ours.size() + " Animus advancements loaded; check the data directory name and the criteria format");
            return;
        }

        for (AdvancementHolder holder : ours) {
            if (holder.value().criteria().isEmpty()) {
                helper.fail(holder.id() + " has no criteria, so it would be granted immediately");
                return;
            }

            // A dangling parent drops the advancement out of the tree, so it never shows in game
            // even though the file itself loaded fine.
            Optional<Identifier> parent = holder.value().parent();
            if (parent.isPresent() && manager.get(parent.get()) == null) {
                helper.fail(holder.id() + " has parent " + parent.get() + " which does not exist, so the tree cannot attach it");
                return;
            }
        }

        helper.succeed();
    }

    private static void altarTierTriggerIsRegistered(GameTestHelper helper) {
        if (AnimusCriteriaTriggers.ALTAR_TIER.get() == null) {
            helper.fail("the altar_tier criterion is not registered, so Tier 6 Ascension can never fire");
            return;
        }

        helper.succeed();
    }
}
