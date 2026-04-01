package com.breakinblocks.animusnv.mixin.evilcraft;

import com.breakinblocks.neovitae.common.fluid.NVFluids;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.cyclops.cyclopscore.inventory.PlayerInventoryIterator;
import org.cyclops.evilcraft.item.ItemBloodExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for ItemBloodExtractor to produce Essentia Vitae from mob kills
 * instead of EvilCraft blood when unification is enabled.
 */
@Mixin(ItemBloodExtractor.class)
public abstract class ItemBloodExtractorMixin {

    @Inject(method = "fillForAllBloodExtractors", at = @At("HEAD"), cancellable = true)
    private static void animus$fillWithEssentiaVitae(Player player, int minimumMB, int maximumMB, CallbackInfo ci) {
        if (!EvilCraftBloodHelper.isUnificationEnabled()) return;
        ci.cancel();

        int toFill = minimumMB + player.getRandom().nextInt(Math.max(1, maximumMB - minimumMB));
        PlayerInventoryIterator it = new PlayerInventoryIterator(player);
        while (it.hasNext() && toFill > 0) {
            ItemStack itemStack = it.next();
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemBloodExtractor) {
                IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(itemStack).orElse(null);
                if (fluidHandler != null) {
                    toFill -= fluidHandler.fill(new FluidStack(NVFluids.ESSENTIA_VITAE_SOURCE, toFill), IFluidHandler.FluidAction.EXECUTE);
                    it.replace(fluidHandler.getContainer());
                }
            }
        }
    }
}
