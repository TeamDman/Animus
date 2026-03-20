package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.ISoulNetwork;
import com.breakinblocks.neovitae.api.soul.SoulTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.EnumRuneType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Ritual of Sol - Places light sources in dark areas
 * Takes blocks from chest above ritual and places them in dark spots (light level < 8)
 * with solid ground below
 * Uses center-outward search algorithm to prioritize nearby positions
 * Supports NeoVitae's Sigil of Blood Light for placing blood lights without consuming the sigil
 * Activation Cost: 1000 LP
 * Refresh Cost: 1 LP (regular blocks) or 1 LP (blood light)
 * Refresh Time: 5 ticks
 */
public class RitualSol extends Ritual {
    public static final String CHEST_RANGE = "chest";
    public static final String EFFECT_RANGE = "effect";
    private static final ResourceLocation BLOOD_LIGHT_SIGIL = ResourceLocation.fromNamespaceAndPath("neovitae", "bloodlightsigil");
    private static final ResourceLocation BLOOD_LIGHT_BLOCK = ResourceLocation.fromNamespaceAndPath("neovitae", "bloodlight");

    private static final Map<BlockPos, SearchState> searchStates = new HashMap<>();

    public RitualSol() {
        super(Constants.Rituals.SOL, 0, 1000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.SOL);

        addBlockRange(EFFECT_RANGE, RitualAreaDescriptors.largeCube65());
        addBlockRange(CHEST_RANGE, RitualAreaDescriptors.singleBlockAbove());

        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 128, 128);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        ISoulNetwork network = NeoVitaeAPI.getInstance().getSoulNetwork(mrs.getOwner());
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide) {
            return;
        }

        int currentEssence = network.getCurrentEssence();
        if (currentEssence < getRefreshCost()) {
            return;
        }

        AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(masterPos).get(0);
        BlockEntity chestTile = level.getBlockEntity(chestPos);

        if (chestTile == null) {
            return;
        }

        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, chestPos, null);
        if (handler == null) {
            return;
        }

        Optional<Integer> slotOpt = IntStream.range(0, handler.getSlots())
            .filter(i -> !handler.getStackInSlot(i).isEmpty())
            .filter(i -> isOkayToUse(handler.getStackInSlot(i)))
            .boxed()
            .findAny();

        if (!slotOpt.isPresent()) {
            return;
        }

        int slot = slotOpt.get();
        ItemStack stack = handler.getStackInSlot(slot);

        AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
        BlockPos placePos = findDarkSpot(level, masterPos, effectRange);

        if (placePos == null) {
            return;
        }

        BlockState stateToPlace = getStateToUse(stack);

        level.setBlock(placePos, stateToPlace, 3);

        // Blood light sigils are not consumed when used
        boolean isBloodLightSigil = isBloodLightSigil(stack);
        if (!isBloodLightSigil && stack.getItem() instanceof BlockItem) {
            handler.extractItem(slot, 1, false);
        }

        SoulTicket ticket = SoulTicket.create(getRefreshCost());
        network.syphon(ticket);
    }

    private boolean isBloodLightSigil(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return BLOOD_LIGHT_SIGIL.equals(itemId);
    }

    private boolean isOkayToUse(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (isBloodLightSigil(stack)) {
            return true;
        }

        return stack.getItem() instanceof BlockItem;
    }

    private BlockState getStateToUse(ItemStack stack) {
        if (isBloodLightSigil(stack)) {
            Block bloodLight = BuiltInRegistries.BLOCK.getOptional(BLOOD_LIGHT_BLOCK).orElse(null);
            if (bloodLight != null && bloodLight != Blocks.AIR) {
                return bloodLight.defaultBlockState();
            }
        }

        if (stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState();
        }

        return Blocks.AIR.defaultBlockState();
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
            addCornerRunes(components, 2, layer, EnumRuneType.AIR);
        }
    }

    private BlockPos findDarkSpot(Level level, BlockPos masterPos, AreaDescriptor effectRange) {
        SearchState state = searchStates.computeIfAbsent(masterPos.immutable(), k -> new SearchState());

        int maxChecksPerTick = 4096;
        int checksThisTick = 0;

        net.minecraft.world.phys.AABB aabb = effectRange.getAABB(masterPos);
        int horizontalRadius = (int) Math.max(
            Math.max(Math.abs(aabb.minX - masterPos.getX()), Math.abs(aabb.maxX - masterPos.getX())),
            Math.max(Math.abs(aabb.minZ - masterPos.getZ()), Math.abs(aabb.maxZ - masterPos.getZ()))
        );
        int verticalRadius = (int) Math.max(
            Math.abs(aabb.minY - masterPos.getY()),
            Math.abs(aabb.maxY - masterPos.getY())
        );

        for (int radius = state.currentRadius; radius <= horizontalRadius && checksThisTick < maxChecksPerTick; radius++) {
            for (int x = -radius; x <= radius && checksThisTick < maxChecksPerTick; x++) {
                if (radius == state.currentRadius && x < state.currentX) continue;

                for (int z = -radius; z <= radius && checksThisTick < maxChecksPerTick; z++) {
                    if (radius == state.currentRadius && x == state.currentX && z < state.currentZ) continue;

                    if (radius > 0 && Math.abs(x) != radius && Math.abs(z) != radius) {
                        continue;
                    }

                    int startY = (radius == state.currentRadius && x == state.currentX && z == state.currentZ) ? state.currentY : 0;
                    for (int y = startY; y >= -verticalRadius && checksThisTick < maxChecksPerTick; y--) {
                        BlockPos checkPos = masterPos.offset(x, y, z);
                        checksThisTick++;

                        state.currentRadius = radius;
                        state.currentX = x;
                        state.currentZ = z;
                        state.currentY = y;

                        if (level.isEmptyBlock(checkPos) &&
                            level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, checkPos) < 8 &&
                            level.getBlockState(checkPos.below()).isFaceSturdy(level, checkPos.below(), Direction.UP)) {

                            state.currentY--;
                            if (state.currentY < -verticalRadius) {
                                state.currentY = 0;
                                state.currentZ++;
                                if (state.currentZ > radius) {
                                    state.currentZ = -radius;
                                    state.currentX++;
                                    if (state.currentX > radius) {
                                        state.currentX = -radius;
                                        state.currentZ = -radius;
                                        state.currentY = 0;
                                        state.currentRadius++;
                                    }
                                }
                            }
                            return checkPos;
                        }
                    }
                    state.currentY = 0;
                }
                state.currentZ = -radius;
            }
        }

        if (state.currentRadius > horizontalRadius) {
            searchStates.remove(masterPos);
        }

        return null;
    }

    private static class SearchState {
        int currentRadius = 0;
        int currentX = Integer.MIN_VALUE;
        int currentZ = Integer.MIN_VALUE;
        int currentY = Integer.MAX_VALUE;
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualSol();
    }
}
