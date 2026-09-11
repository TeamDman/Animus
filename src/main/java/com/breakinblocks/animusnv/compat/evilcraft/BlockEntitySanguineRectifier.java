package com.breakinblocks.animusnv.compat.evilcraft;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.compat.EvilCraftCompat;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;
import com.breakinblocks.neovitae.common.datacomponent.Binding;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.item.BloodOrbItem;
import com.breakinblocks.neovitae.api.stream.StreamPresets;

import javax.annotation.Nullable;
import java.util.Optional;

public class BlockEntitySanguineRectifier extends BlockEntity {

    private static final Identifier EVILCRAFT_BLOOD = Identifier.parse("evilcraft:blood");

    @Nullable
    private BlockPos altarPos;
    private ItemStack orbStack = ItemStack.EMPTY;
    private final BloodTank bloodTank;

    public BlockEntitySanguineRectifier(BlockPos pos, BlockState state) {
        super(EvilCraftCompat.SANGUINE_RECTIFIER_BE.get(), pos, state);
        this.bloodTank = new BloodTank(AnimusConfig.sanguineRectifier.tankCapacity.get(), this::setChanged);
    }

    private static class BloodTank extends FluidStacksResourceHandler {
        private final Runnable onChanged;

        BloodTank(int capacity, Runnable onChanged) {
            super(1, capacity);
            this.onChanged = onChanged;
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return isEvilCraftBlood(resource);
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousContents) {
            onChanged.run();
        }
    }

    private static boolean isEvilCraftBlood(FluidResource resource) {
        if (resource.isEmpty()) return false;
        Identifier fluidId = BuiltInRegistries.FLUID.getKey(resource.getFluid());
        return fluidId != null && fluidId.equals(EVILCRAFT_BLOOD);
    }

    public ResourceHandler<FluidResource> getBloodTank() {
        return bloodTank;
    }

    public ItemStack getOrbStack() {
        return orbStack;
    }

    public void setOrbStack(ItemStack stack) {
        this.orbStack = stack;
        setChanged();
        if (level != null && !level.isClientSide()) {
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
        if (level == null || level.isClientSide()) return;

        AraVitaeTile altar = getLinkedAltar();

        // Mode 1: EV → Blood (bound orb drains EV, fills adjacent tanks)
        if (!orbStack.isEmpty() && altar != null) {
            tickEvToBlood(altar);
        }

        // Mode 2: Blood → Altar (internal tank drains into altar)
        if (altar != null && bloodTank.getAmountAsInt(0) > 0) {
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

        int availableEV = network.getCurrentEV();
        if (availableEV <= 0) return;

        // Limit by available EV
        int actualBlood = Math.min(fillRate, availableEV / Math.max(1, evPerBlood));
        if (actualBlood <= 0) return;

        FluidResource blood = bloodResource();
        if (blood == null) return;

        int totalFilled = 0;
        for (Direction dir : Direction.values()) {
            if (totalFilled >= actualBlood) break;
            BlockPos adjacent = worldPosition.relative(dir);
            ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, adjacent, dir.getOpposite());
            if (handler != null) {
                try (Transaction tx = Transaction.openRoot()) {
                    int filled = handler.insert(blood, actualBlood - totalFilled, tx);
                    if (filled > 0) {
                        tx.commit();
                        totalFilled += filled;
                    }
                }
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

        int toTransfer = Math.min(transferRate, Math.min(bloodTank.getAmountAsInt(0), altarSpace));
        if (toTransfer <= 0) return;

        FluidResource stored = bloodTank.getResource(0);
        if (stored.isEmpty()) return;

        int drained;
        try (Transaction tx = Transaction.openRoot()) {
            drained = bloodTank.extract(0, stored, toTransfer, tx);
            if (drained > 0) tx.commit();
        }

        if (drained > 0) {
            altar.addSacrificeEV(drained, false);

            // Send blood stream effect occasionally (every ~40 ticks)
            if (level instanceof ServerLevel serverLevel
                    && level.getGameTime() % 40 == 0) {
                StreamPresets.bloodTendril(worldPosition, altarPos)
                    .build()
                    .sendToNearby(serverLevel, worldPosition, 64);
            }
        }
    }

    @Nullable
    private static FluidResource bloodResource() {
        Fluid fluid = BuiltInRegistries.FLUID.getValue(EVILCRAFT_BLOOD);
        if (fluid == null || fluid == Fluids.EMPTY) return null;
        return FluidResource.of(fluid);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        if (altarPos != null) {
            output.putInt("AltarX", altarPos.getX());
            output.putInt("AltarY", altarPos.getY());
            output.putInt("AltarZ", altarPos.getZ());
        }

        if (!orbStack.isEmpty()) {
            output.store("OrbStack", ItemStack.CODEC, orbStack);
        }

        bloodTank.serialize(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        Optional<Integer> altarX = input.getInt("AltarX");
        if (altarX.isPresent()) {
            altarPos = new BlockPos(altarX.get(), input.getIntOr("AltarY", 0), input.getIntOr("AltarZ", 0));
        } else {
            altarPos = null;
        }

        orbStack = input.read("OrbStack", ItemStack.CODEC).orElse(ItemStack.EMPTY);

        bloodTank.deserialize(input);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector reporter =
                     new ProblemReporter.ScopedCollector(LogUtils.getLogger())) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            return output.buildResult();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    public void dropContents(Level level, BlockPos pos) {
        if (!orbStack.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), orbStack);
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
