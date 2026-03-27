package com.breakinblocks.animusnv.rituals;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.EnumRuneType;

import java.util.function.Consumer;

/**
 * Ritual of Reparare - Repairs items in an inventory above the ritual
 * Looks for a chest/inventory above the master ritual stone and repairs damaged items within
 * Activation Cost: 5000 EV
 * Refresh Cost: Configurable (default: 50 EV per damage point repaired)
 * Refresh Time: Configurable (default: 100 ticks / 5 seconds)
 * Repair Amount: 20% of item's max durability per cycle
 */
public class RitualReparare extends Ritual {
    public static final String CHEST_RANGE = "chest";

    public RitualReparare() {
        super(Constants.Rituals.REPARARE, 0, 5000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.REPARARE);

        addBlockRange(CHEST_RANGE, RitualAreaDescriptors.singleBlockAbove());
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        IAnima network = AnimusRitualHelper.getOwnerNetwork(mrs);
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide) {
            return;
        }

        if (network == null) {
            return;
        }

        AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(masterPos).get(0);
        BlockEntity chestTile = level.getBlockEntity(chestPos);

        if (chestTile == null) {
            return;
        }

        IItemHandler handler = AnimusRitualHelper.getItemHandler(level, chestPos);
        if (handler == null) {
            return;
        }

        int evPerDamage = AnimusConfig.rituals.reparareRitualEVPerDamage.get();

        int totalEVCost = 0;
        int itemsRepaired = 0;

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (!stack.isDamageableItem()) {
                continue;
            }

            if (!stack.isDamaged()) {
                continue;
            }

            if (stack.is(Constants.Tags.DISALLOW_REPAIR)) {
                continue;
            }

            int maxDurability = stack.getMaxDamage();
            int repairAmount = (int) Math.ceil(maxDurability * 0.20);
            int currentDamage = stack.getDamageValue();
            int actualRepairAmount = Math.min(repairAmount, currentDamage);

            int evCost = actualRepairAmount * evPerDamage;

            if (network.getCurrentEV() < totalEVCost + evCost) {
                continue;
            }

            stack.setDamageValue(currentDamage - actualRepairAmount);

            totalEVCost += evCost;
            itemsRepaired++;
        }

        if (totalEVCost > 0 && itemsRepaired > 0) {
            AnimaTicket ticket = AnimaTicket.create(totalEVCost);
            network.syphon(ticket);
        }
    }

    @Override
    public int getRefreshCost() {
        return 0;
    }

    @Override
    public int getRefreshTime() {
        return AnimusConfig.rituals.reparareRitualInterval.get();
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, -2, -1, 0, EnumRuneType.WATER);
        addRune(components, 0, -1, -2, EnumRuneType.WATER);
        addRune(components, 0, -1, 2, EnumRuneType.WATER);
        addRune(components, 2, -1, 0, EnumRuneType.WATER);
        addRune(components, -2, 0, 0, EnumRuneType.WATER);
        addRune(components, -1, 0, -1, EnumRuneType.EARTH);
        addRune(components, -1, 0, 0, EnumRuneType.WATER);
        addRune(components, -1, 0, 1, EnumRuneType.FIRE);
        addRune(components, 0, 0, -2, EnumRuneType.WATER);
        addRune(components, 0, 0, -1, EnumRuneType.WATER);
        addRune(components, 0, 0, 1, EnumRuneType.WATER);
        addRune(components, 0, 0, 2, EnumRuneType.WATER);
        addRune(components, 1, 0, -1, EnumRuneType.AIR);
        addRune(components, 1, 0, 0, EnumRuneType.WATER);
        addRune(components, 1, 0, 1, EnumRuneType.EARTH);
        addRune(components, 2, 0, 0, EnumRuneType.WATER);
        addRune(components, -2, 2, 0, EnumRuneType.WATER);
        addRune(components, 0, 2, -2, EnumRuneType.AIR);
        addRune(components, 0, 2, 2, EnumRuneType.FIRE);
        addRune(components, 2, 2, 0, EnumRuneType.EARTH);
    }



    @Override
    public Ritual getNewCopy() {
        return new RitualReparare();
    }
}
