package com.breakinblocks.animusnv.mixin;

import com.breakinblocks.animusnv.compat.CompatHandler;
import com.breakinblocks.animusnv.compat.malum.SpiritHarvestHelper;
import com.breakinblocks.animusnv.items.ItemRunicSentientScythe;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.item.soul.SentientScytheItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Malum's scythe necklaces trade sweeping for single target damage. NeoVitae's area attack is
 * private, so it is cancelled here for our scythes only; NeoVitae's own scythes are untouched.
 */
@Mixin(value = SentientScytheItem.class, remap = false)
public class SentientScytheSweepMixin {

    @Inject(method = "performAreaAttack", at = @At("HEAD"), cancellable = true)
    private void animus$suppressSweepForNarrowEdge(Player player, LivingEntity target, SpiritusType type, int level, CallbackInfo ci) {
        if (!(player.getMainHandItem().getItem() instanceof ItemRunicSentientScythe)) {
            return;
        }

        if (CompatHandler.isMalumLoaded() && SpiritHarvestHelper.isSweepSuppressed(player)) {
            ci.cancel();
        }
    }
}
