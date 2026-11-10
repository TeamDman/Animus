package com.breakinblocks.animusnv.mixin;

import com.breakinblocks.animusnv.compat.CompatHandler;
import com.breakinblocks.animusnv.compat.MalumScytheCurios;
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

    @Inject(
            method = "performAreaAttack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/LivingEntity;Lcom/breakinblocks/neovitae/common/datacomponent/SpiritusType;I)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0)
    private void animus$suppressSweepForNarrowEdge(Player player, LivingEntity target, SpiritusType type, int level, CallbackInfo ci) {
        animus$suppressSweep(player, ci);
    }

    @Inject(
            method = "performAreaAttack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/LivingEntity;Lcom/breakinblocks/neovitae/common/datacomponent/SpiritusType;ID)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0)
    private void animus$suppressSweepForNarrowEdgeScaled(Player player, LivingEntity target, SpiritusType type, int level, double scale, CallbackInfo ci) {
        animus$suppressSweep(player, ci);
    }

    private static void animus$suppressSweep(Player player, CallbackInfo ci) {
        if (!(player.getMainHandItem().getItem() instanceof ItemRunicSentientScythe)) {
            return;
        }

        if (CompatHandler.isMalumLoaded() && MalumScytheCurios.isSweepSuppressed(player)) {
            ci.cancel();
        }
    }
}
