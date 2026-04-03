package com.breakinblocks.animusnv.compat.ironsspells;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Sigil effect for the Sigil of Crimson Will.
 * <p>
 * This is a toggleable sigil that empowers spell casting when active.
 * The actual spell power boost logic is handled by {@link CrimsonWillSpellHandler},
 * which detects active instances of this sigil in the player's inventory.
 * <p>
 * Features:
 * - Toggle active/inactive with right-click (handled by SigilItem)
 * - While active: Boosts spell power and summon power by 30-50%
 * - Base 30% bonus, scales up to 50% with Spiritus (0-4096 will)
 * - Consumes EV per spell cast (scales with spell cost)
 * - Consumes Spiritus per spell cast
 */
public record CrimsonWillSigilEffect() implements ISigilEffect {
    public static final MapCodec<CrimsonWillSigilEffect> CODEC = MapCodec.unit(CrimsonWillSigilEffect::new);

    @Override
    public MapCodec<? extends ISigilEffect> codec() {
        return CODEC;
    }

    @Override
    public boolean isToggleable() {
        return true;
    }

    @Override
    public void activeTick(Level level, Player player, ItemStack stack, int itemSlot, boolean isSelected) {
        // No passive effect - spell power boost is handled by CrimsonWillSpellHandler
        // which applies attribute modifiers during spell casting events
    }
}
