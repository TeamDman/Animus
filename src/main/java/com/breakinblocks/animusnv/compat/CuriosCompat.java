package com.breakinblocks.animusnv.compat;

import com.breakinblocks.animusnv.registry.AnimusItems;
import com.breakinblocks.neovitae.common.item.IActivatable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;

/** Only accessed after verifying that the optional Curios mod is loaded. */
public final class CuriosCompat {

    private CuriosCompat() {
    }

    @Nullable
    public static ItemStack findActiveMonkSigil(Player player) {
        return CuriosApi.getCuriosInventory(player)
            .flatMap(inventory -> inventory.findFirstCurio(stack ->
                stack.is(AnimusItems.SIGIL_MONK.get())
                    && stack.getItem() instanceof IActivatable activatable
                    && activatable.getActivated(stack)))
            .map(result -> result.stack())
            .orElse(null);
    }
}
