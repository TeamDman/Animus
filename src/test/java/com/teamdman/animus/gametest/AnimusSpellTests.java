package com.teamdman.animus.gametest;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.compat.ironsspells.SpellCastingHandler;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wayoftime.bloodmagic.common.item.BloodMagicItems;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.function.Consumer;

@GameTestHolder("animus")
@PrefixGameTestTemplate(false)
public class AnimusSpellTests {
    private record Caster(ServerPlayer player, MagicData data, SoulNetwork network, AbstractSpell spell) {}

    private static Caster caster(GameTestHelper helper, float mana) {
        ServerPlayer player = AnimusRegressionTests.player(helper.getLevel());
        player.getInventory().add(new ItemStack(BloodMagicItems.WEAK_BLOOD_ORB.get()));
        MagicData data = MagicData.getPlayerMagicData(player);
        data.setSyncedData(new SyncedSpellData(player));
        data.setMana(mana);
        SoulNetwork network = NetworkHelper.getSoulNetwork(player);
        network.setCurrentEssence(1000000);
        return new Caster(player, data, network, SpellRegistry.FIREBOLT_SPELL.get());
    }

    private static SpellPreCastEvent pre(Caster caster, CastSource source) {
        return new SpellPreCastEvent(caster.player(), caster.spell().getSpellId(), 1, caster.spell().getSchoolType(), source);
    }

    private static SpellOnCastEvent cast(Caster caster) {
        return new SpellOnCastEvent(caster.player(), caster.spell().getSpellId(), 1, caster.spell().getManaCost(1),
            caster.spell().getSchoolType(), CastSource.SPELLBOOK);
    }

    @GameTest(template = "empty")
    public static void lpCastCheckIsReadOnlyAndCompletedCastPaysOnce(GameTestHelper helper) {
        Caster caster = caster(helper, 0);
        helper.assertTrue(caster.spell().canBeCastedBy(1, CastSource.SPELLBOOK, caster.data(), caster.player()).isSuccess(),
            "LP should satisfy the actual spell eligibility check");
        helper.assertTrue(caster.data().getMana() == 0 && caster.network().getCurrentEssence() == 1000000,
            "Eligibility check changed mana or LP");
        int expectedLP = caster.spell().getManaCost(1) * AnimusConfig.ironsSpells.lpPerMana.get();
        helper.assertTrue(!MinecraftForge.EVENT_BUS.post(pre(caster, CastSource.SPELLBOOK)), "Funded spell was rejected");
        helper.assertTrue(caster.network().getCurrentEssence() == 1000000 - expectedLP, "LP was not reserved");
        helper.assertTrue(caster.data().getMana() == 0, "Reservation created temporary mana");
        SpellOnCastEvent event = cast(caster);
        MinecraftForge.EVENT_BUS.post(event);
        helper.assertTrue(event.getManaCost() == 0, "Pure LP cast also consumed mana");
        MinecraftForge.EVENT_BUS.post(cast(caster));
        helper.assertTrue(caster.network().getCurrentEssence() == 1000000 - expectedLP, "Cast was charged twice");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hybridCastingPaysOnlyForMissingMana(GameTestHelper helper) {
        Caster caster = caster(helper, 0);
        int cost = caster.spell().getManaCost(1);
        int mana = cost / 2;
        caster.data().setMana(mana);
        int covered = AnimusConfig.ironsSpells.allowHybridCasting.get() ? cost - mana : cost;
        MinecraftForge.EVENT_BUS.post(pre(caster, CastSource.SPELLBOOK));
        SpellOnCastEvent event = cast(caster);
        MinecraftForge.EVENT_BUS.post(event);
        helper.assertTrue(event.getManaCost() == cost - covered, "Incorrect remaining mana charge");
        helper.assertTrue(caster.network().getCurrentEssence() == 1000000 - covered * AnimusConfig.ironsSpells.lpPerMana.get(),
            "Incorrect LP charge for the mana deficit");
        helper.assertTrue(caster.data().getMana() == mana, "Animus changed mana before Iron's Spells consumes it");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cancelledCastRefundsReservation(GameTestHelper helper) {
        Caster caster = caster(helper, 0);
        Consumer<SpellPreCastEvent> cancel = event -> {
            if (event.getEntity() == caster.player()) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, SpellPreCastEvent.class, cancel);
        try {
            helper.assertTrue(MinecraftForge.EVENT_BUS.post(pre(caster, CastSource.SPELLBOOK)), "Cancellation listener did not run");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(cancel);
        }
        helper.assertTrue(caster.network().getCurrentEssence() == 1000000 && caster.data().getMana() == 0,
            "Cancelled cast retained LP or created mana");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void interruptedCastAndLogoutRefundReservation(GameTestHelper helper) {
        Caster caster = caster(helper, 0);
        MinecraftForge.EVENT_BUS.post(pre(caster, CastSource.SPELLBOOK));
        caster.data().initiateCast(caster.spell(), 1, 20, CastSource.SPELLBOOK, "mainhand");
        SpellCastingHandler.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, caster.player()));
        helper.assertTrue(caster.network().getCurrentEssence() < 1000000, "Active cast was refunded early");
        caster.data().resetCastingState();
        SpellCastingHandler.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, caster.player()));
        helper.assertTrue(caster.network().getCurrentEssence() == 1000000, "Interrupted cast was not refunded");
        MinecraftForge.EVENT_BUS.post(pre(caster, CastSource.SPELLBOOK));
        SpellCastingHandler.onPlayerLogout(new PlayerEvent.PlayerLoggedOutEvent(caster.player()));
        helper.assertTrue(caster.network().getCurrentEssence() == 1000000, "Logout lost reserved LP");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void normalManaAndScrollCastsDoNotChargeLP(GameTestHelper helper) {
        Caster caster = caster(helper, 10000);
        MinecraftForge.EVENT_BUS.post(pre(caster, CastSource.SPELLBOOK));
        SpellOnCastEvent event = cast(caster);
        MinecraftForge.EVENT_BUS.post(event);
        helper.assertTrue(event.getManaCost() == caster.spell().getManaCost(1), "Normal mana casting was modified");
        caster.data().setMana(0);
        MinecraftForge.EVENT_BUS.post(pre(caster, CastSource.SCROLL));
        helper.assertTrue(caster.network().getCurrentEssence() == 1000000, "Normal mana or scroll cast consumed LP");
        helper.assertTrue(caster.data().getMana() == 0, "Scroll cast generated mana");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void insufficientLpCannotPassEligibilityOrReserve(GameTestHelper helper) {
        Caster caster = caster(helper, 0);
        caster.network().setCurrentEssence(0);
        helper.assertTrue(!caster.spell().canBeCastedBy(1, CastSource.SPELLBOOK, caster.data(), caster.player()).isSuccess(),
            "Unfunded cast passed eligibility");
        helper.assertTrue(MinecraftForge.EVENT_BUS.post(pre(caster, CastSource.SPELLBOOK)), "Unfunded reservation was accepted");
        helper.assertTrue(caster.data().getMana() == 0 && caster.network().getCurrentEssence() == 0, "Failed cast changed resources");
        helper.succeed();
    }
}
