package com.breakinblocks.animusnv.mixin;

import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InvWrapper.class, remap = false)
public abstract class InvWrapperFragmentLockMixin {

    @Shadow
    public abstract Container getInv();

    @Inject(method = "extractItem", at = @At("HEAD"), cancellable = true)
    private void animus$blockFragmentExtraction(int slot, int amount, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        if (isLockedFragmentSlot(slot)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "setStackInSlot", at = @At("HEAD"), cancellable = true)
    private void animus$blockFragmentOverwrite(int slot, ItemStack stack, CallbackInfo ci) {
        if (isLockedFragmentSlot(slot)) {
            ci.cancel();
        }
    }

    private boolean isLockedFragmentSlot(int slot) {
        if (!(this.getInv() instanceof Inventory)) {
            return false;
        }
        if (slot < 0 || slot >= this.getInv().getContainerSize()) {
            return false;
        }
        return this.getInv().getItem(slot).is(AnimusItems.FRAGMENT_HEALING.get());
    }
}
