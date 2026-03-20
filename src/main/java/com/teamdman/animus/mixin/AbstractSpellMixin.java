package com.teamdman.animus.mixin;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.compat.IronsSpellsCompat;
import com.teamdman.animus.compat.ironsspells.ItemBloodInfusedSpellbook;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import com.breakinblocks.neovitae.common.item.BloodOrbItem;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.ISoulNetwork;

/**
 * Mixin for Iron's Spells AbstractSpell to enable LP-powered spell casting.
 *
 * This mixin intercepts the canBeCastedBy check and adds temporary mana
 * when the player has sufficient LP but insufficient mana, allowing the
 * spell cast to proceed.
 */
@Mixin(value = AbstractSpell.class, remap = false)
public class AbstractSpellMixin {

    /**
     * Inject at the HEAD of canBeCastedBy to add temporary mana before the mana check.
     * This allows LP casting to work by ensuring mana is sufficient before the check runs.
     */
    @Inject(
        method = "canBeCastedBy",
        at = @At("HEAD")
    )
    private void animus$addTemporaryManaForLPCasting(
        int spellLevel,
        CastSource castSource,
        MagicData magicData,
        Player player,
        CallbackInfoReturnable<CastResult> cir
    ) {
        try {
            boolean lpCastingEnabled;
            boolean requireBloodOrb = true;
            int lpPerMana = 100;
            boolean allowHybridCasting = true;
            try {
                if (AnimusConfig.ironsSpells != null) {
                    lpCastingEnabled = AnimusConfig.ironsSpells.enableLPCasting.get();
                    requireBloodOrb = AnimusConfig.ironsSpells.requireBloodOrb.get();
                    lpPerMana = AnimusConfig.ironsSpells.lpPerMana.get();
                    allowHybridCasting = AnimusConfig.ironsSpells.allowHybridCasting.get();
                } else {
                    lpCastingEnabled = true;
                }
            } catch (Exception e) {
                lpCastingEnabled = true;
            }

            if (!lpCastingEnabled) {
                return;
            }

            AbstractSpell spell = (AbstractSpell) (Object) this;
            int manaCost = spell.getManaCost(spellLevel);
            int currentMana = (int) magicData.getMana();

            if (currentMana >= manaCost) {
                return;
            }

            int manaDeficit = manaCost - currentMana;

            if (requireBloodOrb && !hasBloodOrb(player)) {
                return;
            }

            ISoulNetwork network = NeoVitaeAPI.getInstance().getSoulNetwork(player.getUUID());
            if (network == null) {
                return;
            }

            ItemStack spellbook = findBloodInfusedSpellbook(player);

            int lpCost;
            int manaToAdd;

            if (allowHybridCasting && currentMana > 0) {
                manaToAdd = manaDeficit;
                lpCost = manaDeficit * lpPerMana;
            } else {
                manaToAdd = manaCost;
                lpCost = manaCost * lpPerMana;
            }

            if (!spellbook.isEmpty()) {
                double lpReduction = ItemBloodInfusedSpellbook.getLPCostReduction(spellbook);
                if (lpReduction > 0) {
                    lpCost = (int) Math.max(1, lpCost * (1.0 - lpReduction));
                }
            }

            if (network.getCurrentEssence() < lpCost) {
                return;
            }

            // SpellCastingHandler handles the actual LP consumption in SpellPreCastEvent
            magicData.setMana(currentMana + manaToAdd);
        } catch (Exception e) {
            // Ignore - don't break spell casting
        }
    }

    private static ItemStack findBloodInfusedSpellbook(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() == IronsSpellsCompat.BLOOD_INFUSED_SPELLBOOK.get()) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() == IronsSpellsCompat.BLOOD_INFUSED_SPELLBOOK.get()) {
            return offHand;
        }

        var curiosOpt = CuriosApi.getCuriosInventory(player);
        if (curiosOpt.isPresent()) {
            var curios = curiosOpt.get();
            var handler = curios.getEquippedCurios();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack.getItem() == IronsSpellsCompat.BLOOD_INFUSED_SPELLBOOK.get()) {
                    return stack;
                }
            }
        }

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == IronsSpellsCompat.BLOOD_INFUSED_SPELLBOOK.get()) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean hasBloodOrb(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BloodOrbItem) {
                return true;
            }
        }

        for (ItemStack stack : player.getInventory().armor) {
            if (stack.getItem() instanceof BloodOrbItem) {
                return true;
            }
        }

        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof BloodOrbItem) {
                return true;
            }
        }

        var curiosOpt = CuriosApi.getCuriosInventory(player);
        if (curiosOpt.isPresent()) {
            var curios = curiosOpt.get();
            var handler = curios.getEquippedCurios();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack.getItem() instanceof BloodOrbItem) {
                    return true;
                }
            }
        }

        return false;
    }
}
