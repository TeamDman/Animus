package com.breakinblocks.animusnv.mixin;

import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VanillaContainerWrapper.class, remap = false)
public abstract class ContainerWrapperFragmentLockMixin {

    @Shadow
    @Final
    private Container container;

    @Inject(
        method = "extract(ILnet/neoforged/neoforge/transfer/item/ItemResource;ILnet/neoforged/neoforge/transfer/transaction/TransactionContext;)I",
        at = @At("HEAD"),
        cancellable = true)
    private void animus$blockFragmentExtraction(int index, ItemResource resource, int amount, TransactionContext transaction, CallbackInfoReturnable<Integer> cir) {
        if (this.container instanceof Inventory && resource.is(AnimusItems.FRAGMENT_HEALING.get())) {
            cir.setReturnValue(0);
        }
    }
}
