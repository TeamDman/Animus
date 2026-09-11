package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.compat.IronsSpellsCompat;
import com.breakinblocks.animusnv.compat.ironsspells.CrimsonWillSpellHandler;
import com.breakinblocks.animusnv.compat.ironsspells.ItemSanguineScroll;
import com.breakinblocks.animusnv.compat.ironsspells.SpellCastingHandler;
import com.breakinblocks.animusnv.gametest.base.AnimusTestRegistrar;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.common.datacomponent.Binding;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.item.IActivatable;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.spells.fire.FireboltSpell;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;

public final class AnimusSpellTests {

    private AnimusSpellTests() {
    }

    public static void register(AnimusTestRegistrar r) {
        r.add("ev_casting_eligibility_is_read_only", AnimusSpellTests::evCastingChecksAreReadOnlyAndPaymentIsExact);
        r.add("completed_ev_cast_pays_deficit_once", AnimusSpellTests::completedEvCastPaysDeficitOnce);
        r.add("ev_cast_uses_event_adjusted_cost", AnimusSpellTests::evCastUsesEventAdjustedCost);
        r.add("crimson_will_lasts_through_execution", AnimusSpellTests::crimsonWillLastsThroughSpellExecution);
        r.add("rejected_scroll_refunds_ev", AnimusSpellTests::rejectedSanguineScrollRefundsEvAndPreservesDurability);
    }

    private static IAnima fund(ServerPlayer player, int amount) {
        ItemStack orb = new ItemStack(AnimusItems.BLOOD_ORB_TRANSCENDENT.get());
        orb.set(NVDataComponents.BINDING.get(), new Binding(player.getUUID(), "Regression"));
        player.getInventory().add(orb);
        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(amount), amount);
        return network;
    }

    private static void evCastingChecksAreReadOnlyAndPaymentIsExact(GameTestHelper h) {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        IAnima network = fund(player, 100000);
        AbstractSpell spell = SpellRegistry.getSpell("irons_spellbooks:firebolt");
        MagicData magic = MagicData.getPlayerMagicData(player);
        magic.setMana(0);
        spell.canBeCastedBy(1, CastSource.SPELLBOOK, magic, player);
        h.assertTrue(magic.getMana() == 0, "eligibility must not grant mana");
        h.assertTrue(SpellCastingHandler.payAndSupplyMana(player, spell.getManaCost(1)), "funded payment succeeds");
        int expected = 100000 - spell.getManaCost(1) * AnimusConfig.ironsSpells.evPerMana.get();
        h.assertTrue(network.getCurrentEV() == expected, "actual mana deficit is paid");
        h.succeed();
    }

    private static void completedEvCastPaysDeficitOnce(GameTestHelper h) {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        IAnima network = fund(player, 1000000);
        MagicData data = MagicData.getPlayerMagicData(player);
        AbstractSpell spell = SpellRegistry.getSpell("irons_spellbooks:firebolt");
        int mana = spell.getManaCost(1);
        data.setMana(mana / 2f);
        int cost = (int) Math.ceil(mana / 2f * AnimusConfig.ironsSpells.evPerMana.get());
        spell.castSpell(h.getLevel(), 1, player, CastSource.SPELLBOOK, false);
        h.assertTrue(network.getCurrentEV() == 1000000 - cost, "only the deficit is paid");
        h.assertTrue(data.getMana() == 0, "no synthetic mana left");
        spell.castSpell(h.getLevel(), 1, player, CastSource.SPELLBOOK, false);
        h.assertTrue(network.getCurrentEV() == 1000000 - cost - mana * AnimusConfig.ironsSpells.evPerMana.get(), "next cast paid once");
        h.succeed();
    }

    private static void evCastUsesEventAdjustedCost(GameTestHelper h) {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        IAnima network = fund(player, 1000000);
        MagicData data = MagicData.getPlayerMagicData(player);
        data.setMana(0);
        AbstractSpell spell = SpellRegistry.getSpell("irons_spellbooks:firebolt");
        Consumer<SpellOnCastEvent> discount = event -> {
            if (event.getEntity() == player) {
                event.setManaCost(7);
            }
        };
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, SpellOnCastEvent.class, discount);
        try {
            spell.castSpell(h.getLevel(), 1, player, CastSource.SPELLBOOK, false);
            h.assertTrue(network.getCurrentEV() == 1000000 - 7 * AnimusConfig.ironsSpells.evPerMana.get(), "final event cost paid");
            h.assertTrue(data.getMana() == 0, "mana fully consumed");
        } finally {
            NeoForge.EVENT_BUS.unregister(discount);
        }
        h.succeed();
    }

    private static void crimsonWillLastsThroughSpellExecution(GameTestHelper h) {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        fund(player, 1000000);
        ItemStack sigil = new ItemStack(IronsSpellsCompat.SIGIL_CRIMSON_WILL.get());
        ((IActivatable) sigil.getItem()).setActivatedState(sigil, true);
        player.getInventory().add(sigil);
        double baseline = player.getAttributeValue(AttributeRegistry.SPELL_POWER);
        FireboltSpell spell = new FireboltSpell() {
            @Override
            public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData data) {
                h.assertTrue(caster.getAttributeValue(AttributeRegistry.SPELL_POWER) > baseline, "power modifier present inside onCast");
            }
        };
        new CrimsonWillSpellHandler().onSpellCast(new SpellPreCastEvent(player, spell.getSpellId(), 1, spell.getSchoolType(), CastSource.SPELLBOOK));
        h.assertTrue(player.getAttributeValue(AttributeRegistry.SPELL_POWER) > baseline, "bonus applied at initiation");
        MagicData.getPlayerMagicData(player).setMana(1000);
        spell.castSpell(h.getLevel(), 1, player, CastSource.SPELLBOOK, false);
        h.assertTrue(player.getAttributeValue(AttributeRegistry.SPELL_POWER) == baseline, "bonus removed after execution");
        h.succeed();
    }

    private static void rejectedSanguineScrollRefundsEvAndPreservesDurability(GameTestHelper h) {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        IAnima network = fund(player, 1000000);
        ItemStack scroll = new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_RASA.get());
        ItemSanguineScroll.setSpell(scroll, "irons_spellbooks:firebolt", 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, scroll);
        Consumer<SpellPreCastEvent> deny = event -> {
            if (event.getEntity() == player) {
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, SpellPreCastEvent.class, deny);
        try {
            scroll.getItem().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
            h.assertTrue(network.getCurrentEV() == 1000000, "rejected cast refunded");
            h.assertTrue(scroll.getDamageValue() == 0, "rejected cast does not wear the scroll");
            AbstractSpell firebolt = SpellRegistry.getSpell("irons_spellbooks:firebolt");
            h.assertTrue(!MagicData.getPlayerMagicData(player).getPlayerCooldowns().isOnCooldown(firebolt), "no rejection cooldown");
        } finally {
            NeoForge.EVENT_BUS.unregister(deny);
        }
        h.succeed();
    }
}
