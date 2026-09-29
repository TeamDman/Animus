package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.advancements.AltarTierTrigger;
import com.breakinblocks.animusnv.advancements.AnimusCriteriaTriggers;
import com.breakinblocks.animusnv.gametest.base.AnimusTestRegistrar;
import com.klikli_dev.modonomicon.data.BookDataManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class AnimusDataTests {

    private AnimusDataTests() {
    }

    public static void register(AnimusTestRegistrar r) {
        r.add("animus_advancements_load", AnimusDataTests::animusAdvancementsLoad);
        r.add("altar_tier_trigger_registered", AnimusDataTests::altarTierTriggerIsRegistered);
        r.add("altar_tier_trigger_tracks_listening_players", AnimusDataTests::altarTierTriggerTracksListeningPlayers);
        r.add("altar_tier_trigger_awards_and_stops_checking", AnimusDataTests::altarTierTriggerAwardsAndStopsChecking);
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

    private static void altarTierTriggerTracksListeningPlayers(GameTestHelper helper) {
        AltarTierTrigger trigger = AnimusCriteriaTriggers.ALTAR_TIER.get();
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "AltarListener"));
        PlayerAdvancements advancements = player.getAdvancements();
        CriterionTrigger.Listener<AltarTierTrigger.Instance> first = new CriterionTrigger.Listener<>(
            new AltarTierTrigger.Instance(Optional.empty(), 5), null, "first");
        CriterionTrigger.Listener<AltarTierTrigger.Instance> second = new CriterionTrigger.Listener<>(
            new AltarTierTrigger.Instance(Optional.empty(), 3), null, "second");

        trigger.removePlayerListeners(advancements);
        helper.assertFalse(trigger.hasListeners(player), "a player with no altar criteria is skipped");

        trigger.addPlayerListener(advancements, first);
        trigger.addPlayerListener(advancements, second);
        helper.assertTrue(trigger.hasListeners(player), "a player working toward an altar advancement is checked");

        trigger.removePlayerListener(advancements, first);
        helper.assertTrue(trigger.hasListeners(player), "one remaining criterion keeps the player checked");

        trigger.removePlayerListener(advancements, second);
        helper.assertFalse(trigger.hasListeners(player), "a player who finished every altar criterion is skipped");

        trigger.addPlayerListener(advancements, first);
        trigger.removePlayerListeners(advancements);
        helper.assertFalse(trigger.hasListeners(player), "logging out clears the player");

        helper.succeed();
    }

    private static void altarTierTriggerAwardsAndStopsChecking(GameTestHelper helper) {
        BookDataManager.get().tryBuildBooks(helper.getLevel());
        AltarTierTrigger trigger = AnimusCriteriaTriggers.ALTAR_TIER.get();
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
            new GameProfile(UUID.randomUUID(), "AltarAwardee"), ClientInformation.createDefault());
        player.connection = FakePlayerFactory.getMinecraft(helper.getLevel()).connection;
        PlayerAdvancements advancements = player.getAdvancements();
        AdvancementHolder ascension = helper.getLevel().getServer().getAdvancements()
            .get(Identifier.fromNamespaceAndPath(Constants.Mod.MODID, "tier6_ascension"));

        try {
            helper.assertTrue(ascension != null, "tier6_ascension advancement loaded");
            helper.assertTrue(trigger.hasListeners(player), "a player without Tier 6 Ascension is checked");

            trigger.trigger(player, 4);
            helper.assertFalse(advancements.getOrStartProgress(ascension).isDone(), "a tier below the minimum does not award");

            trigger.trigger(player, 5);
            helper.assertTrue(advancements.getOrStartProgress(ascension).isDone(), "reaching the minimum tier awards the advancement");
            helper.assertFalse(trigger.hasListeners(player), "a player who has Tier 6 Ascension is no longer checked");
        } finally {
            advancements.stopListening();
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
