package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.IronsSpellsCompat;
import com.breakinblocks.animusnv.compat.ironsspells.CrimsonWillSpellHandler;
import com.breakinblocks.animusnv.compat.ironsspells.ItemSanguineScroll;
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
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.spells.fire.FireboltSpell;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;

@GameTestHolder(Constants.Mod.MODID)
@PrefixGameTestTemplate(false)
public class AnimusSpellTests {
    private static final String TEMPLATE = "empty_5x5x7";

    private IAnima fund(ServerPlayer player) {
        ItemStack orb = new ItemStack(AnimusItems.BLOOD_ORB_TRANSCENDENT.get());
        orb.set(NVDataComponents.BINDING.get(), new Binding(player.getUUID(), "Regression"));
        player.getInventory().add(orb);
        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(1000000), 1000000);
        return network;
    }

    @GameTest(template = TEMPLATE)
    public void completed_ev_cast_pays_deficit_once(GameTestHelper h) {
        var player = RegressionTestSupport.player(h.getLevel());
        var network = fund(player);
        var data = MagicData.getPlayerMagicData(player);
        var spell = SpellRegistry.getSpell("irons_spellbooks:firebolt");
        int mana = spell.getManaCost(1);
        data.setMana(mana / 2f);
        int cost = (int) Math.ceil(mana / 2f * AnimusConfig.ironsSpells.evPerMana.get());
        spell.castSpell(h.getLevel(), 1, player, CastSource.SPELLBOOK, false);
        h.assertTrue(network.getCurrentEV() == 1000000 - cost, "only deficit paid");
        h.assertTrue(data.getMana() == 0, "no synthetic mana left");
        spell.castSpell(h.getLevel(), 1, player, CastSource.SPELLBOOK, false);
        h.assertTrue(network.getCurrentEV() == 1000000 - cost - mana * AnimusConfig.ironsSpells.evPerMana.get(), "next cast paid once");
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void ev_cast_uses_event_adjusted_cost(GameTestHelper h) {
        var player = RegressionTestSupport.player(h.getLevel());
        var network = fund(player);
        var data = MagicData.getPlayerMagicData(player);
        data.setMana(0);
        var spell = SpellRegistry.getSpell("irons_spellbooks:firebolt");
        Consumer<SpellOnCastEvent> discount = event -> { if (event.getEntity() == player) event.setManaCost(7); };
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

    @GameTest(template = TEMPLATE)
    public void crimson_will_lasts_through_spell_execution(GameTestHelper h) {
        var player = RegressionTestSupport.player(h.getLevel());
        fund(player);
        var sigil = new ItemStack(IronsSpellsCompat.SIGIL_CRIMSON_WILL.get());
        ((IActivatable) sigil.getItem()).setActivatedState(sigil, true);
        player.getInventory().add(sigil);
        double baseline = player.getAttributeValue(AttributeRegistry.SPELL_POWER);
        var spell = new FireboltSpell() {
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

    @GameTest(template = TEMPLATE)
    public void rejected_sanguine_scroll_refunds_ev_and_preserves_durability(GameTestHelper h) {
        var player = RegressionTestSupport.player(h.getLevel());
        var network = fund(player);
        var scroll = new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_RASA.get());
        ItemSanguineScroll.setSpell(scroll, "irons_spellbooks:firebolt", 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, scroll);
        Consumer<SpellPreCastEvent> deny = event -> { if (event.getEntity() == player) event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, SpellPreCastEvent.class, deny);
        try {
            scroll.getItem().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
            h.assertTrue(network.getCurrentEV() == 1000000, "rejected cast refunded");
            h.assertTrue(scroll.getDamageValue() == 0, "rejected cast does not wear scroll");
            h.assertTrue(!MagicData.getPlayerMagicData(player).getPlayerCooldowns().isOnCooldown(SpellRegistry.getSpell("irons_spellbooks:firebolt")), "no rejection cooldown");
        } finally {
            NeoForge.EVENT_BUS.unregister(deny);
        }
        h.succeed();
    }
}
