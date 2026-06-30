package com.teamdman.animus.mixin;

import com.teamdman.animus.registry.AnimusAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import wayoftime.bloodmagic.api.compat.IDemonWill;
import wayoftime.bloodmagic.api.compat.IDemonWillWeapon;
import wayoftime.bloodmagic.util.handler.event.WillHandler;

import java.util.List;

/**
 * Mixin for Blood Magic's WillHandler to apply the bonus_demon_will attribute
 * to demon will drops from Blood Magic's own sentient weapons (Sentient Sword, etc.).
 */
@Mixin(value = WillHandler.class, remap = false)
public class WillHandlerMixin {

    /**
     * Redirect the getRandomDemonWillDrop call in onLivingDrops to multiply
     * will amounts by the player's bonus_demon_will attribute.
     */
    @Redirect(
        method = "onLivingDrops",
        at = @At(value = "INVOKE", target = "Lwayoftime/bloodmagic/api/compat/IDemonWillWeapon;getRandomDemonWillDrop(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)Ljava/util/List;")
    )
    private List<ItemStack> animus$applyBonusDemonWill(IDemonWillWeapon weapon, LivingEntity killedEntity, LivingEntity playerEntity, ItemStack heldStack, int looting) {
        List<ItemStack> drops = weapon.getRandomDemonWillDrop(killedEntity, playerEntity, heldStack, looting);

        if (!(playerEntity instanceof Player player)) return drops;

        double bonusFraction = player.getAttributeValue(AnimusAttributes.BONUS_DEMON_WILL.get());
        if (bonusFraction > 0) {
            double multiplier = 1 + bonusFraction;
            for (ItemStack willStack : drops) {
                if (willStack.getItem() instanceof IDemonWill demonWill) {
                    double currentWill = demonWill.getWill(demonWill.getType(willStack), willStack);
                    demonWill.setWill(demonWill.getType(willStack), willStack, currentWill * multiplier);
                }
            }
        }

        return drops;
    }
}
