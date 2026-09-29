package com.breakinblocks.animusnv.mixin;

import com.breakinblocks.animusnv.compat.CompatHandler;
import com.breakinblocks.animusnv.compat.malum.SpiritHarvestHelper;
import com.breakinblocks.animusnv.items.ItemRunicSentientScythe;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Player.class)
public abstract class PlayerScytheSweepMixin {

    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), index = 0)
    private DamageSource animus$useMalumSweepDamage(DamageSource source) {
        Player player = (Player) (Object) this;
        if (CompatHandler.isMalumLoaded() && player.getMainHandItem().getItem() instanceof ItemRunicSentientScythe) {
            return SpiritHarvestHelper.scytheSweepSource(player);
        }
        return source;
    }
}
