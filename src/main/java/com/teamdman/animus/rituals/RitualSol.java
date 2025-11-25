package com.teamdman.animus.rituals;

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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.EnumRuneType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Ritual of Sol - Places light sources in dark areas
 * Takes blocks from chest above ritual and places them in dark spots (light level < 8)
 * with solid ground below
 * Supports Blood Magic's Sigil of Blood Light for placing blood lights without consuming the sigil
 * Activation Cost: 1000 LP
 * Refresh Cost: 1 LP (regular blocks) or 1 LP (blood light)
 * Refresh Time: 5 ticks
 */
@RitualRegister(Constants.Rituals.SOL)
public class RitualSol extends Ritual {
    public static final String CHEST_RANGE = "chest";
    public static final String EFFECT_RANGE = "effect";
    private static final ResourceLocation BLOOD_LIGHT_SIGIL = ResourceLocation.fromNamespaceAndPath("bloodmagic", "bloodlightsigil");
    private static final ResourceLocation BLOOD_LIGHT_BLOCK = ResourceLocation.fromNamespaceAndPath("bloodmagic", "bloodlight");

    public RitualSol() {
        super(Constants.Rituals.SOL, 0, 1000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.SOL);

        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-32, -32, -32), 65));
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 128, 128);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        SoulNetwork network = NetworkHelper.getSoulNetwork(mrs.getOwner());
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide) {
            return;
        }

        // Get chest
        AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(masterPos).get(0);
        BlockEntity chestTile = level.getBlockEntity(chestPos);

        if (chestTile == null) {
            return;
        }

        // Get item handler from chest
        IItemHandler handler = chestTile.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
        if (handler == null) {
            return;
        }

        // Find a non-empty slot with a valid block item
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

        // Find a dark spot to place the block
        Optional<BlockPos> placePos = getBlockRange(EFFECT_RANGE)
            .getContainedPositions(masterPos)
            .stream()
            .filter(level::isEmptyBlock)
            .filter(pos -> level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos) < 8)
            .filter(pos -> level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP))
            .findFirst();

        if (!placePos.isPresent()) {
            return;
        }

        // Get the block state to place
        BlockState stateToPlace = getStateToUse(stack);

        // Place the block
        level.setBlock(placePos.get(), stateToPlace, 3);

        // Check if this is a blood light sigil (doesn't consume)
        boolean isBloodLightSigil = isBloodLightSigil(stack);

        // Extract item from chest (unless it's the blood light sigil)
        if (!isBloodLightSigil && stack.getItem() instanceof BlockItem) {
            handler.extractItem(slot, 1, false);
        }

        // Consume LP
        SoulTicket ticket = new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_SOL),
            getRefreshCost()
        );
        network.syphon(ticket, false);
    }

    /**
     * Check if an item is the Blood Light sigil
     */
    private boolean isBloodLightSigil(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return BLOOD_LIGHT_SIGIL.equals(itemId);
    }

    /**
     * Check if an item can be used by this ritual
     */
    private boolean isOkayToUse(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        // Check for Blood Magic's Sigil of Blood Light
        if (isBloodLightSigil(stack)) {
            return true;
        }

        // Check if it's a block item
        return stack.getItem() instanceof BlockItem;
    }

    /**
     * Get the block state to place
     */
    private BlockState getStateToUse(ItemStack stack) {
        // Check for Blood Magic's Sigil of Blood Light
        if (isBloodLightSigil(stack)) {
            Block bloodLight = ForgeRegistries.BLOCKS.getValue(BLOOD_LIGHT_BLOCK);
            if (bloodLight != null && bloodLight != Blocks.AIR) {
                return bloodLight.defaultBlockState();
            }
        }

        // Get the block from the item
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
            addRune(components, 2, layer, 2, EnumRuneType.AIR);
            addRune(components, -2, layer, 2, EnumRuneType.AIR);
            addRune(components, 2, layer, -2, EnumRuneType.AIR);
            addRune(components, -2, layer, -2, EnumRuneType.AIR);
        }
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualSol();
    }
}
