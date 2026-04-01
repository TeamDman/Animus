package com.breakinblocks.animusnv.mixin.evilcraft;

import com.breakinblocks.neovitae.common.fluid.NVFluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.cyclops.evilcraft.loot.modifier.LootModifierInjectBroom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin for LootModifierInjectBroom to fill broom loot items with Essentia Vitae
 * instead of blood when unification is enabled.
 */
@Mixin(LootModifierInjectBroom.class)
public abstract class LootModifierInjectBroomMixin {

    @Redirect(method = "doApply",
        at = @At(value = "INVOKE",
                 target = "Lnet/neoforged/neoforge/fluids/capability/IFluidHandler;fill(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)I"),
        require = 0)
    private int animus$redirectBroomFill(IFluidHandler handler, FluidStack resource, IFluidHandler.FluidAction action) {
        if (EvilCraftBloodHelper.isUnificationEnabled() && EvilCraftBloodHelper.isEvilCraftBlood(resource.getFluid())) {
            resource = new FluidStack(NVFluids.ESSENTIA_VITAE_SOURCE, resource.getAmount());
        }
        return handler.fill(resource, action);
    }
}
