package com.breakinblocks.animusnv.rituals;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.animusnv.util.ChebyshevSearcher;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.EnumRuneType;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Ritual of Sol - Places light sources in dark areas
 * Takes blocks from chest above ritual and places them in dark spots (light level < 8)
 * with solid ground below
 * Uses center-outward search algorithm to prioritize nearby positions
 * Supports NeoVitae's Sigil of Blood Light for placing blood lights without consuming the sigil
 * Activation Cost: 1000 EV
 * Refresh Cost: 1 EV (regular blocks) or 1 EV (blood light)
 * Refresh Time: 5 ticks
 */
public class RitualSol extends Ritual {
    public static final String CHEST_RANGE = "chest";
    public static final String EFFECT_RANGE = "effect";
    private static final ResourceLocation BLOOD_LIGHT_SIGIL = ResourceLocation.fromNamespaceAndPath("neovitae", "bloodlightsigil");
    private static final ResourceLocation BLOOD_LIGHT_BLOCK = ResourceLocation.fromNamespaceAndPath("neovitae", "bloodlight");

    private static final ChebyshevSearcher SEARCHER = new ChebyshevSearcher();

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
        IAnima network = AnimusRitualHelper.getOwnerNetwork(mrs);
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide) {
            return;
        }

        int currentEV = network.getCurrentEV();
        if (currentEV < getRefreshCost()) {
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

        AnimaTicket ticket = AnimaTicket.create(getRefreshCost());
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
        return AnimusConfig.rituals.solRefreshCost.get();
    }

    @Override
    public int getRefreshTime() {
        return AnimusConfig.rituals.solRefreshTime.get();
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        for (int layer = 0; layer < 3; layer++) {
            addCornerRunes(components, 2, layer, EnumRuneType.AIR);
        }
    }

    private BlockPos findDarkSpot(Level level, BlockPos masterPos, AreaDescriptor effectRange) {
        AABB aabb = effectRange.getAABB(masterPos);

        return SEARCHER.search(
            masterPos,
            masterPos,
            ChebyshevSearcher.horizontalRadiusOf(aabb, masterPos),
            ChebyshevSearcher.downwardDepthOf(aabb, masterPos),
            true,
            4096,
            checkPos -> level.isEmptyBlock(checkPos)
                && level.getBrightness(LightLayer.BLOCK, checkPos) < 8
                && level.getBlockState(checkPos.below()).isFaceSturdy(level, checkPos.below(), Direction.UP)
        );
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualSol();
    }
}
