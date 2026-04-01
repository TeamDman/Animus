package com.breakinblocks.animusnv.mixin.evilcraft;

import com.breakinblocks.neovitae.common.fluid.NVFluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.cyclops.evilcraft.item.ItemCondensedBloodConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin for ItemCondensedBloodConfig's inner class that returns blood FluidStacks.
 * When unification is enabled, condensed blood items should report Essentia Vitae.
 */
@Mixin(ItemCondensedBloodConfig.class)
public abstract class ItemCondensedBloodMixin {

    /**
     * Redirect the fill() call when condensed blood is used, to fill with
     * essentia_vitae instead of blood.
     */
    @Redirect(method = "*",
        at = @At(value = "INVOKE",
                 target = "Lnet/neoforged/neoforge/fluids/capability/IFluidHandler;fill(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)I"),
        require = 0)
    private static int animus$redirectCondensedBloodFill(IFluidHandler handler, FluidStack resource, IFluidHandler.FluidAction action) {
        if (EvilCraftBloodHelper.isUnificationEnabled() && EvilCraftBloodHelper.isEvilCraftBlood(resource.getFluid())) {
            resource = new FluidStack(NVFluids.ESSENTIA_VITAE_SOURCE, resource.getAmount());
        }
        return handler.fill(resource, action);
    }
}
