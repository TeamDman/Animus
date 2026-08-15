package com.breakinblocks.animusnv.mixin;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class ContainerMenuFragmentLockMixin {

    @Shadow
    @Final
    public NonNullList<Slot> slots;

    @Shadow
    public abstract ItemStack getCarried();

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void animus$lockHealingFragment(int slotId, int button, ContainerInput input, Player player, CallbackInfo ci) {
        if (player.getAbilities().instabuild) {
            return;
        }

        if (isFragment(this.getCarried())) {
            reject(player, ci);
            return;
        }

        if (input == ContainerInput.SWAP && isFragment(player.getInventory().getItem(button))) {
            reject(player, ci);
            return;
        }

        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot.container instanceof Inventory && isFragment(slot.getItem())) {
                reject(player, ci);
            }
        }
    }

    private static boolean isFragment(ItemStack stack) {
        return !stack.isEmpty() && stack.is(AnimusItems.FRAGMENT_HEALING.get());
    }

    private static void reject(Player player, CallbackInfo ci) {
        ci.cancel();
        if (!player.level().isClientSide()) {
            player.sendOverlayMessage(
                Component.translatable(Constants.Localizations.Text.HEALING_CANNOT_DROP)
                    .withStyle(ChatFormatting.RED));
        }
    }
}
