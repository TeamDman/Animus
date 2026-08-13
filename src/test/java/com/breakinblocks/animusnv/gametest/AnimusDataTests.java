package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.advancements.AnimusCriteriaTriggers;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

@GameTestHolder(Constants.Mod.MODID)
@PrefixGameTestTemplate(false)
public class AnimusDataTests {

    private static final String TEMPLATE = "empty_5x5x7";

    @GameTest(template = TEMPLATE)
    public void animus_advancements_load(GameTestHelper helper) {
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

            Optional<ResourceLocation> parent = holder.value().parent();
            if (parent.isPresent() && manager.get(parent.get()) == null) {
                helper.fail(holder.id() + " has parent " + parent.get() + " which does not exist, so the tree cannot attach it");
                return;
            }
        }

        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void altar_tier_trigger_registered(GameTestHelper helper) {
        if (AnimusCriteriaTriggers.ALTAR_TIER.get() == null) {
            helper.fail("the altar_tier criterion is not registered, so Tier 6 Ascension can never fire");
            return;
        }

        helper.succeed();
    }
}
