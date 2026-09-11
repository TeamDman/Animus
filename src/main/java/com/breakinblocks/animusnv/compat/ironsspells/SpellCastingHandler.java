package com.breakinblocks.animusnv.compat.ironsspells;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.animusnv.util.InventorySearchHelper;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.common.item.BloodOrbItem;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public final class SpellCastingHandler {
    private SpellCastingHandler() {}

    public static boolean canPayWithEV(Player player, int manaCost) {
        return payment(player, manaCost) != null;
    }

    public static boolean payAndSupplyMana(Player player, int manaCost) {
        MagicData data = MagicData.getPlayerMagicData(player);
        if (data.getMana() >= manaCost) {
            return true;
        }
        Payment payment = payment(player, manaCost);
        if (payment == null || player.level().isClientSide()) {
            return false;
        }
        int drained = payment.network.syphon(AnimaTicket.create(payment.ev));
        if (drained != payment.ev) {
            if (drained > 0) {
                payment.network.add(AnimaTicket.create(drained), Integer.MAX_VALUE);
            }
            return false;
        }
        data.setMana(data.getMana() + payment.mana);
        return true;
    }

    @Nullable
    private static Payment payment(Player player, int manaCost) {
        if (!AnimusConfig.ironsSpells.enableEVCasting.get()) {
            return null;
        }
        float currentMana = MagicData.getPlayerMagicData(player).getMana();
        if (currentMana >= manaCost) {
            return null;
        }
        ItemStack orb = findItem(player, stack -> stack.getItem() instanceof BloodOrbItem);
        if (orb.isEmpty() && AnimusConfig.ironsSpells.requireBloodOrb.get()) {
            return null;
        }
        IAnima network = orb.isEmpty() ? NeoVitaeAPI.getInstance().getAnima(player.getUUID())
            : AnimusRitualHelper.getNetworkForBoundItem(player, orb);
        if (network == null) {
            return null;
        }
        float suppliedMana = AnimusConfig.ironsSpells.allowHybridCasting.get()
            ? manaCost - currentMana : manaCost;
        ItemStack spellbook = findItem(player, stack -> stack.getItem() instanceof ItemBloodInfusedSpellbook);
        double reduction = spellbook.isEmpty() ? 0 : ItemBloodInfusedSpellbook.getEVCostReduction(spellbook);
        double cost = Math.ceil(suppliedMana * AnimusConfig.ironsSpells.evPerMana.get() * Math.max(0, 1 - reduction));
        if (cost > Integer.MAX_VALUE) {
            return null;
        }
        int ev = (int) Math.max(1, cost);
        return network.getCurrentEV() >= ev ? new Payment(network, ev, suppliedMana) : null;
    }

    private static ItemStack findItem(Player player, Predicate<ItemStack> predicate) {
        var inventory = InventorySearchHelper.findFirst(player, predicate);
        if (inventory.isPresent()) {
            return inventory.get();
        }
        return CuriosApi.getCuriosInventory(player)
            .map(inv -> inv.findFirstCurio(predicate)).orElse(Optional.empty())
            .map(result -> result.stack()).orElse(ItemStack.EMPTY);
    }

    private record Payment(IAnima network, int ev, float mana) {}
}
