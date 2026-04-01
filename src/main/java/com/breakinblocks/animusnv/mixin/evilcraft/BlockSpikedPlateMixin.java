package com.breakinblocks.animusnv.mixin.evilcraft;

import com.breakinblocks.neovitae.common.fluid.NVFluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.cyclops.evilcraft.block.BlockSpikedPlate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin for BlockSpikedPlate to produce Essentia Vitae instead of blood
 * when entities are damaged on the plate and blood flows to the pedestal below.
 */
@Mixin(BlockSpikedPlate.class)
public abstract class BlockSpikedPlateMixin {

    @Redirect(method = "damageEntity",
        at = @At(value = "INVOKE",
                 target = "Lnet/neoforged/neoforge/fluids/capability/IFluidHandler;fill(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)I"))
    private int animus$redirectSpikedPlateFill(IFluidHandler handler, FluidStack resource, IFluidHandler.FluidAction action) {
        if (EvilCraftBloodHelper.isUnificationEnabled()) {
            resource = new FluidStack(NVFluids.ESSENTIA_VITAE_SOURCE, resource.getAmount());
        }
        return handler.fill(resource, action);
    }
}
