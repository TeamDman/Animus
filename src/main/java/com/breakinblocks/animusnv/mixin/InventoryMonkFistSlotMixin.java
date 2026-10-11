package com.breakinblocks.animusnv.mixin;

import com.breakinblocks.animusnv.events.MonkSigilEventHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class InventoryMonkFistSlotMixin {

    @Shadow
    @Final
    private NonNullList<ItemStack> items;

    @Shadow
    private int selected;

    @Shadow
    @Final
    public Player player;

    @Inject(method = "getFreeSlot", at = @At("HEAD"), cancellable = true)
    private void animus$keepMonkFistSlotEmpty(CallbackInfoReturnable<Integer> cir) {
        if (!this.items.get(this.selected).isEmpty()) {
            return;
        }

        if (!MonkSigilEventHandler.hasActiveMonkSigil(this.player)) {
            return;
        }

        for (int i = 0; i < this.items.size(); i++) {
            if (i != this.selected && this.items.get(i).isEmpty()) {
                cir.setReturnValue(i);
                return;
            }
        }

        cir.setReturnValue(-1);
    }
}
