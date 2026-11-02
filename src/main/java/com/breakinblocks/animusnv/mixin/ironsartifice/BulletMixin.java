package com.breakinblocks.animusnv.mixin.ironsartifice;

import com.breakinblocks.animusnv.compat.ironsartifice.VengeanceBulletModifier;
import io.redspace.irons_artifice.entity.Bullet;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(value = Bullet.class, remap = false)
public abstract class BulletMixin {

    @Shadow
    @Final
    private Set<Integer> piercedEntities;

    @Inject(method = "canHitEntity", at = @At("RETURN"), cancellable = true)
    private void animus$hitVengeanceSpirits(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()
                && !piercedEntities.contains(entity.getId())
                && VengeanceBulletModifier.canStrike((Bullet) (Object) this, entity)) {
            cir.setReturnValue(true);
        }
    }
}
