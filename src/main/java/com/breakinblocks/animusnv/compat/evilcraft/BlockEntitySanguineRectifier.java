package com.breakinblocks.animusnv.compat.evilcraft;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.compat.EvilCraftCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;
import com.breakinblocks.neovitae.common.datacomponent.Binding;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.item.BloodOrbItem;
import com.breakinblocks.neovitae.api.stream.StreamPresets;

import javax.annotation.Nullable;

public class BlockEntitySanguineRectifier extends BlockEntity {

    @Nullable
    private BlockPos altarPos;
    private ItemStack orbStack = ItemStack.EMPTY;
    private final FluidTank bloodTank;

    public BlockEntitySanguineRectifier(BlockPos pos, BlockState state) {
        super(EvilCraftCompat.SANGUINE_RECTIFIER_BE.get(), pos, state);
        this.bloodTank = new FluidTank(AnimusConfig.sanguineRectifier.tankCapacity.get()) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                return isEvilCraftBlood(stack);
            }
        };
    }

    private static boolean isEvilCraftBlood(FluidStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        return fluidId.equals(ResourceLocation.parse("evilcraft:blood"));
    }

    public FluidTank getBloodTank() {
        return bloodTank;
    }

    public ItemStack getOrbStack() {
        return orbStack;
    }

    public void setOrbStack(ItemStack stack) {
        this.orbStack = stack;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Nullable
    public BlockPos getAltarPos() {
        return altarPos;
    }

    public void setAltarPos(@Nullable BlockPos pos) {
        this.altarPos = pos;
        setChanged();
    }

    /**
     * Scan for an AraVitae in a cube around this block.
     * @return the altar position if found, null otherwise
     */
    @Nullable
    public BlockPos scanForAltar() {
        if (level == null) return null;
        int range = AnimusConfig.sanguineRectifier.searchRange.get();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    mutable.set(worldPosition.getX() + x, worldPosition.getY() + y, worldPosition.getZ() + z);
                    if (level.getBlockEntity(mutable) instanceof AraVitaeTile) {
                        return mutable.immutable();
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private AraVitaeTile getLinkedAltar() {
        if (level == null || altarPos == null) return null;
        BlockEntity be = level.getBlockEntity(altarPos);
        if (be instanceof AraVitaeTile altar) return altar;
        // Altar was removed
        altarPos = null;
        setChanged();
        return null;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        AraVitaeTile altar = getLinkedAltar();

        // Mode 1: EV → Blood (bound orb drains EV, fills adjacent tanks)
        if (!orbStack.isEmpty() && altar != null) {
            tickEvToBlood(altar);
        }

        // Mode 2: Blood → Altar (internal tank drains into altar)
        if (altar != null && bloodTank.getFluidAmount() > 0) {
            tickBloodToAltar(altar);
        }
    }

    public static boolean isEvToBloodEnabled() {
        return AnimusConfig.sanguineRectifier.evPerBlood.get() > 0;
    }

    private void tickEvToBlood(AraVitaeTile altar) {
        if (!isEvToBloodEnabled()) return;

        Binding binding = orbStack.getOrDefault(NVDataComponents.BINDING.get(), Binding.EMPTY);
        if (binding.isEmpty()) return;

        IAnima network = NeoVitaeAPI.getInstance().getAnima(binding.uuid());
        if (network == null) return;

        int baseRate = AnimusConfig.sanguineRectifier.baseTransferRate.get();
        int evPerBlood = AnimusConfig.sanguineRectifier.evPerBlood.get();
        float speedBonus = altar.getSpeedBonus();
        int fillRate = Math.max(1, (int) (baseRate * (1 + speedBonus)));

        int evCost = fillRate * evPerBlood;
        int availableEV = network.getCurrentEV();
        if (availableEV <= 0) return;

        // Limit by available EV
        int actualBlood = Math.min(fillRate, availableEV / Math.max(1, evPerBlood));
        if (actualBlood <= 0) return;

        // Try to fill adjacent tanks
        FluidStack bloodFluid = createBloodStack(actualBlood);
        if (bloodFluid.isEmpty()) return;

        int totalFilled = 0;
        for (Direction dir : Direction.values()) {
            if (totalFilled >= actualBlood) break;
            BlockPos adjacent = worldPosition.relative(dir);
            IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, adjacent, dir.getOpposite());
            if (handler != null) {
                FluidStack toFill = createBloodStack(actualBlood - totalFilled);
                int filled = handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                totalFilled += filled;
            }
        }

        if (totalFilled > 0) {
            int actualEvCost = totalFilled * evPerBlood;
            network.syphon(AnimaTicket.create(actualEvCost));
        }
    }

    private void tickBloodToAltar(AraVitaeTile altar) {
        int baseRate = AnimusConfig.sanguineRectifier.baseTransferRate.get();
        float speedBonus = altar.getSpeedBonus();
        int transferRate = Math.max(1, (int) (baseRate * 4 * (1 + speedBonus)));

        int altarSpace = altar.getCapacity() - altar.getCurrentBlood();
        if (altarSpace <= 0) return;

        int toTransfer = Math.min(transferRate, Math.min(bloodTank.getFluidAmount(), altarSpace));
        if (toTransfer <= 0) return;

        FluidStack drained = bloodTank.drain(toTransfer, IFluidHandler.FluidAction.EXECUTE);
        if (!drained.isEmpty()) {
            altar.addSacrificeEV(drained.getAmount(), false);

            // Send blood stream effect occasionally (every ~40 ticks)
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel
                    && level.getGameTime() % 40 == 0) {
                StreamPresets.bloodTendril(worldPosition, altarPos)
                    .build()
                    .sendToNearby(serverLevel, worldPosition, 64);
            }
        }
    }

    private FluidStack createBloodStack(int amount) {
        var fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse("evilcraft:blood"));
        if (fluid == null) return FluidStack.EMPTY;
        return new FluidStack(fluid, amount);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (altarPos != null) {
            tag.putInt("AltarX", altarPos.getX());
            tag.putInt("AltarY", altarPos.getY());
            tag.putInt("AltarZ", altarPos.getZ());
        }

        if (!orbStack.isEmpty()) {
            tag.put("OrbStack", orbStack.save(registries));
        }

        bloodTank.writeToNBT(registries, tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("AltarX")) {
            altarPos = new BlockPos(tag.getInt("AltarX"), tag.getInt("AltarY"), tag.getInt("AltarZ"));
        } else {
            altarPos = null;
        }

        if (tag.contains("OrbStack")) {
            orbStack = ItemStack.parse(registries, tag.getCompound("OrbStack")).orElse(ItemStack.EMPTY);
        } else {
            orbStack = ItemStack.EMPTY;
        }

        bloodTank.readFromNBT(registries, tag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    public void dropContents(Level level, BlockPos pos) {
        if (!orbStack.isEmpty()) {
            net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), orbStack);
            orbStack = ItemStack.EMPTY;
        }
    }

    public boolean isValidOrb(ItemStack stack) {
        return stack.getItem() instanceof BloodOrbItem;
    }

    public boolean isOrbBound(ItemStack stack) {
        Binding binding = stack.getOrDefault(NVDataComponents.BINDING.get(), Binding.EMPTY);
        return !binding.isEmpty();
    }
}
