package com.breakinblocks.animusnv.rituals;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.EnumRuneType;

import com.breakinblocks.animusnv.util.ChebyshevSearcher;

import java.util.function.Consumer;

/**
 * Ritual of Luna - Harvests light-emitting blocks
 * Scans the effect range for blocks with light level > 0, harvests them, and stores in chest above ritual
 * Uses center-outward search algorithm to prioritize nearby positions
 * Activation Cost: 1000 EV
 * Refresh Cost: 1 EV
 * Refresh Time: 5 ticks
 */
public class RitualLuna extends Ritual {
    private static final Logger LOGGER = LoggerFactory.getLogger(RitualLuna.class);

    public static final String CHEST_RANGE = "chest";
    public static final String EFFECT_RANGE = "effect";

    private static final ChebyshevSearcher SEARCHER = new ChebyshevSearcher();

    public RitualLuna() {
        super(Constants.Rituals.LUNA, 0, 1000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.LUNA);

        addBlockRange(EFFECT_RANGE, RitualAreaDescriptors.largeCube65());
        addBlockRange(CHEST_RANGE, RitualAreaDescriptors.singleBlockAbove());

        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 128, 128);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        IAnima network = AnimusRitualHelper.getOwnerNetwork(mrs);
        int currentEV = network.getCurrentEV();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide) {
            return;
        }

        if (currentEV < getRefreshCost()) {
            return;
        }

        AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(masterPos).get(0);
        BlockEntity chestTile = level.getBlockEntity(chestPos);

        AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
        BlockPos lightPos = findLightEmittingBlock(level, masterPos, effectRange);

        if (lightPos == null) {
            return;
        }

        BlockState state = level.getBlockState(lightPos);
        Block block = state.getBlock();

        ItemStack stack = block.getCloneItemStack(level, lightPos, state);

        boolean shouldRemoveBlock = true;
        if (!stack.isEmpty()) {
            if (chestTile != null) {
                IItemHandler handler = AnimusRitualHelper.getItemHandler(level, chestPos);
                if (handler != null) {
                    ItemStack remainder = ItemHandlerHelper.insertItem(handler, stack, true);
                    if (remainder.isEmpty()) {
                        ItemHandlerHelper.insertItem(handler, stack, false);
                    } else {
                        shouldRemoveBlock = false;
                    }
                } else {
                    shouldRemoveBlock = false;
                }
            } else {
                ItemEntity itemEntity = new ItemEntity(
                    level,
                    masterPos.getX() + 0.5,
                    masterPos.getY() + 1,
                    masterPos.getZ() + 0.5,
                    stack
                );
                level.addFreshEntity(itemEntity);
            }
        }

        if (shouldRemoveBlock) {
            level.removeBlock(lightPos, false);

            AnimaTicket ticket = AnimaTicket.create(getRefreshCost());
            network.syphon(ticket);
        }
    }

    @Override
    public int getRefreshCost() {
        return 1;
    }

    @Override
    public int getRefreshTime() {
        return 5;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        for (int layer = 0; layer < 3; layer++) {
            addCornerRunes(components, 2, layer, EnumRuneType.EARTH);
        }
    }

    private BlockPos findLightEmittingBlock(Level level, BlockPos masterPos, AreaDescriptor effectRange) {
        AABB aabb = effectRange.getAABB(masterPos);
        int horizontalRadius = (int) Math.max(
            Math.max(Math.abs(aabb.minX - masterPos.getX()), Math.abs(aabb.maxX - masterPos.getX())),
            Math.max(Math.abs(aabb.minZ - masterPos.getZ()), Math.abs(aabb.maxZ - masterPos.getZ()))
        );
        int verticalRadius = (int) Math.max(
            Math.abs(aabb.minY - masterPos.getY()),
            Math.abs(aabb.maxY - masterPos.getY())
        );

        return SEARCHER.search(masterPos, masterPos.below(), horizontalRadius, verticalRadius,
                true, 4096,
                checkPos -> level.getBlockState(checkPos).getLightEmission() > 0);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualLuna();
    }
}
