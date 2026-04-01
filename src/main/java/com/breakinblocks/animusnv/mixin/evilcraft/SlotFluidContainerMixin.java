package com.breakinblocks.animusnv.mixin.evilcraft;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.cyclops.cyclopscore.inventory.slot.SlotFluidContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for CyclopsCore's {@link SlotFluidContainer} to accept Essentia Vitae
 * containers in EvilCraft machine GUI slots that normally only accept blood containers.
 *
 * When blood unification is enabled and a slot check for EvilCraft blood fails,
 * this mixin re-checks for Essentia Vitae in the container.
 */
@Mixin(SlotFluidContainer.class)
public abstract class SlotFluidContainerMixin {

    @Inject(method = "checkIsItemValid", at = @At("RETURN"), cancellable = true)
    private static void animus$alsoAcceptEssentiaVitae(ItemStack stack, Fluid fluid,
            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && EvilCraftBloodHelper.isEvilCraftBlood(fluid)) {
            IFluidHandlerItem handler = stack.getCapability(
                net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
            if (handler != null) {
                for (int i = 0; i < handler.getTanks(); i++) {
                    FluidStack fluidInTank = handler.getFluidInTank(i);
                    if (!fluidInTank.isEmpty() && EvilCraftBloodHelper.isEssentiaVitae(fluidInTank.getFluid())) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        }
    }
}
