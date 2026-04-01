package com.breakinblocks.animusnv.mixin.evilcraft;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.cyclops.evilcraft.blockentity.BlockEntityBloodStain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

/**
 * Mixin for BlockEntityBloodStain's inner FluidHandler class.
 * Makes blood stains report and accept Essentia Vitae when unification is enabled.
 */
@Mixin(targets = "org.cyclops.evilcraft.blockentity.BlockEntityBloodStain$FluidHandler")
public abstract class BlockEntityBloodStainFluidHandlerMixin {

    @Shadow
    private BlockEntityBloodStain tile;

    @Inject(method = "getFluidInTank", at = @At("HEAD"), cancellable = true)
    private void animus$getFluidInTank(int tank, CallbackInfoReturnable<FluidStack> cir) {
        if (EvilCraftBloodHelper.isUnificationEnabled()) {
            cir.setReturnValue(new FluidStack(EvilCraftBloodHelper.getEffectiveBloodHolder(), tile.getAmount()));
        }
    }

    @Inject(method = "isFluidValid", at = @At("HEAD"), cancellable = true)
    private void animus$isFluidValid(int tank, @Nonnull FluidStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (EvilCraftBloodHelper.isUnificationEnabled()) {
            cir.setReturnValue(tank == 0 && EvilCraftBloodHelper.isBloodOrUnified(stack.getFluid()));
        }
    }

    @Inject(method = "drain(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;",
            at = @At("HEAD"), cancellable = true)
    private void animus$drainFluidStack(@Nonnull FluidStack resource, IFluidHandler.FluidAction action,
            CallbackInfoReturnable<FluidStack> cir) {
        if (EvilCraftBloodHelper.isUnificationEnabled() && EvilCraftBloodHelper.isBloodOrUnified(resource.getFluid())) {
            int maxDrain = Math.min(tile.getAmount(), resource.getAmount());
            FluidStack drained = new FluidStack(EvilCraftBloodHelper.getEffectiveBloodHolder(), maxDrain);
            if (action.execute()) {
                tile.addAmount(-maxDrain);
            }
            cir.setReturnValue(drained);
        }
    }

    @Inject(method = "drain(ILnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;",
            at = @At("HEAD"), cancellable = true)
    private void animus$drainAmount(int maxDrain, IFluidHandler.FluidAction action,
            CallbackInfoReturnable<FluidStack> cir) {
        if (EvilCraftBloodHelper.isUnificationEnabled()) {
            maxDrain = Math.min(tile.getAmount(), maxDrain);
            FluidStack drained = new FluidStack(EvilCraftBloodHelper.getEffectiveBloodHolder(), maxDrain);
            if (action.execute()) {
                tile.addAmount(-maxDrain);
            }
            cir.setReturnValue(drained);
        }
    }
}
