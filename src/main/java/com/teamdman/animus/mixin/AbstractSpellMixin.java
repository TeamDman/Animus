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
import com.breakinblocks.neovitae.common.datacomponent.SoulNetwork;
import com.breakinblocks.neovitae.util.helper.SoulNetworkHelper;

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
            // Check if LP casting is enabled
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

            // If player already has enough mana, no need for LP
            if (currentMana >= manaCost) {
                return;
            }

            // Player doesn't have enough mana - check if we can use LP
            int manaDeficit = manaCost - currentMana;

            // Check if Blood Orb is required
            if (requireBloodOrb && !hasBloodOrb(player)) {
                return;
            }

            // Get the player's soul network
            SoulNetwork network = SoulNetworkHelper.getSoulNetwork(player);
            if (network == null) {
                return;
            }

            // Find Blood Infused Spellbook for LP reduction bonus
            ItemStack spellbook = findBloodInfusedSpellbook(player);

            // Calculate LP cost
            int lpCost;
            int manaToAdd;

            if (allowHybridCasting && currentMana > 0) {
                // Hybrid: only cover deficit
                manaToAdd = manaDeficit;
                lpCost = manaDeficit * lpPerMana;
            } else {
                // Pure LP: cover full cost
                manaToAdd = manaCost;
                lpCost = manaCost * lpPerMana;
            }

            // Apply LP reduction from spellbook
            if (!spellbook.isEmpty()) {
                double lpReduction = ItemBloodInfusedSpellbook.getLPCostReduction(spellbook);
                if (lpReduction > 0) {
                    lpCost = (int) Math.max(1, lpCost * (1.0 - lpReduction));
                }
            }

            // Check if player has enough LP
            if (network.getCurrentEssence() < lpCost) {
                return;
            }

            // Add temporary mana to pass the check
            // The SpellCastingHandler will handle LP consumption in SpellPreCastEvent
            magicData.setMana(currentMana + manaToAdd);
        } catch (Exception e) {
            // Silently fail - don't break spell casting if mixin has issues
        }
    }

    /**
     * Find the Blood Infused Spellbook in player's hands, inventory, or curios slots
     */
    private static ItemStack findBloodInfusedSpellbook(Player player) {
        // Check main hand
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() == IronsSpellsCompat.BLOOD_INFUSED_SPELLBOOK.get()) {
            return mainHand;
        }

        // Check off hand
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() == IronsSpellsCompat.BLOOD_INFUSED_SPELLBOOK.get()) {
            return offHand;
        }

        // Check curios slots
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

        // Check main inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == IronsSpellsCompat.BLOOD_INFUSED_SPELLBOOK.get()) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Check if player has a Blood Orb in inventory or curios
     */
    private static boolean hasBloodOrb(Player player) {
        // Check main inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BloodOrbItem) {
                return true;
            }
        }

        // Check armor slots
        for (ItemStack stack : player.getInventory().armor) {
            if (stack.getItem() instanceof BloodOrbItem) {
                return true;
            }
        }

        // Check offhand
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof BloodOrbItem) {
                return true;
            }
        }

        // Check curios slots
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
