package com.breakinblocks.animusnv.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.util.InventorySearchHelper;
import com.breakinblocks.animusnv.util.SigilStateTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;

import java.util.List;
import java.util.UUID;

public record RepareSigilEffect() implements ISigilEffect {
    public static final MapCodec<RepareSigilEffect> CODEC = MapCodec.unit(RepareSigilEffect::new);

    private static final SigilStateTracker TRACKER = new SigilStateTracker("reparare");

    @Override
    public MapCodec<? extends ISigilEffect> codec() {
        return CODEC;
    }

    @Override
    public boolean isToggleable() {
        return true;
    }

    @Override
    public void activeTick(Level level, Player player, ItemStack stack, int itemSlot, boolean isSelected) {
        if (level.isClientSide()) {
            return;
        }

        // Check if enough time has passed using the tracker
        long currentTime = level.getGameTime();
        int repairInterval = AnimusConfig.sigils.reparareInterval.get();
        if (!TRACKER.isReady(player.getUUID(), currentTime, repairInterval)) {
            return;
        }

        List<ItemStack> repairableItems = InventorySearchHelper.findAll(player, RepareSigilEffect::canRepair);

        if (repairableItems.isEmpty()) {
            return;
        }

        int maxRepairPerItem = AnimusConfig.sigils.reparareRepairAmount.get();
        for (ItemStack repairStack : repairableItems) {
            int damage = repairStack.getDamageValue();
            int toRepair = Math.min(damage, maxRepairPerItem);
            if (toRepair > 0) {
                repairStack.setDamageValue(damage - toRepair);
            }
        }

        TRACKER.updateTime(player.getUUID(), currentTime);
    }

    @Override
    public void onPlayerLogout(UUID playerId, MinecraftServer server) {
        TRACKER.cleanup(playerId);
    }

    private static boolean canRepair(ItemStack stack) {
        return !stack.isEmpty()
            && stack.isDamageableItem()
            && stack.getDamageValue() > 0
            && !stack.is(Constants.Tags.DISALLOW_REPAIR);
    }
}
