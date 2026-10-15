package com.breakinblocks.animusnv.blockentities;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.blocks.BlockSanguineDynamo;
import com.breakinblocks.animusnv.recipes.SanguineDynamoRecipe;
import com.breakinblocks.animusnv.registry.AnimusBlockEntities;
import com.breakinblocks.animusnv.registry.AnimusRecipeSerializers;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.stream.StreamEffect;
import com.breakinblocks.neovitae.common.datacomponent.Binding;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Optional;

public class BlockEntitySanguineDynamo extends BlockEntity implements GeoBlockEntity {

    private static final int ZAP_CHANCE = 6;
    private static final double ORB_HEIGHT = 12.5 / 16.0;
    private static final double POST_TIP_HEIGHT = 15.5 / 16.0;
    private static final double POST_OFFSET = 6.5 / 16.0;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WORKING = RawAnimation.begin().thenLoop("working");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ItemStack orbStack = ItemStack.EMPTY;
    private final DynamoEnergyStorage energy;
    private int lastEvPerTick;

    public BlockEntitySanguineDynamo(BlockPos pos, BlockState state) {
        super(AnimusBlockEntities.SANGUINE_DYNAMO.get(), pos, state);
        this.energy = new DynamoEnergyStorage(AnimusConfig.sanguineDynamo.bufferCapacity.get());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 8, state -> {
            BlockState blockState = getBlockState();
            boolean active = blockState.hasProperty(BlockSanguineDynamo.ACTIVE) && blockState.getValue(BlockSanguineDynamo.ACTIVE);
            return state.setAndContinue(active ? WORKING : IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public IEnergyStorage getEnergyStorage() {
        return energy;
    }

    public ItemStack getOrbStack() {
        return orbStack;
    }

    public int getLastEvPerTick() {
        return lastEvPerTick;
    }

    public void setOrbStack(ItemStack stack) {
        this.orbStack = stack;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public Optional<SanguineDynamoRecipe> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) return Optional.empty();
        return level.getRecipeManager()
            .getRecipeFor(AnimusRecipeSerializers.SANGUINE_DYNAMO_TYPE.get(), new SingleRecipeInput(stack), level)
            .map(RecipeHolder::value);
    }

    @Nullable
    public IAnima getOwnerNetwork() {
        Binding binding = orbStack.getOrDefault(NVDataComponents.BINDING.get(), Binding.EMPTY);
        if (binding.isEmpty()) return null;
        return NeoVitaeAPI.getInstance().getAnima(binding.uuid());
    }

    public int getAllowedEvPerTick(SanguineDynamoRecipe recipe) {
        if (level == null) return 0;
        int signal = level.getBestNeighborSignal(worldPosition);
        if (signal <= 0) return recipe.maxEvPerTick();
        return recipe.maxEvPerTick() * signal / 15;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        lastEvPerTick = generate();
        pushEnergy();

        boolean active = lastEvPerTick > 0;
        BlockState state = getBlockState();
        if (state.getValue(BlockSanguineDynamo.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(BlockSanguineDynamo.ACTIVE, active), Block.UPDATE_ALL);
        }

        if (active && level instanceof ServerLevel serverLevel && level.random.nextInt(ZAP_CHANCE) == 0) {
            sendZap(serverLevel);
        }
    }

    private void sendZap(ServerLevel serverLevel) {
        double sx = level.random.nextBoolean() ? POST_OFFSET : -POST_OFFSET;
        double sz = level.random.nextBoolean() ? POST_OFFSET : -POST_OFFSET;
        StreamEffect.builder(worldPosition.getX() + 0.5, worldPosition.getY() + ORB_HEIGHT, worldPosition.getZ() + 0.5)
            .to(worldPosition.getX() + 0.5 + sx, worldPosition.getY() + POST_TIP_HEIGHT, worldPosition.getZ() + 0.5 + sz)
            .color(0xFF2A36)
            .endColor(0xFF9A9A)
            .scale(0.012f)
            .speed(9.0f)
            .gravity(0.0f)
            .wobble(0.035f)
            .wobbleFrequency(6.0f)
            .spiralInto(false)
            .approachHeight(0.0f)
            .alpha(0.9f)
            .glow(true)
            .drainSpeed(6.0f)
            .trailDensity(1)
            .build()
            .sendToNearby(serverLevel, worldPosition, 32);
    }

    private int generate() {
        if (orbStack.isEmpty()) return 0;

        Optional<SanguineDynamoRecipe> found = findRecipe(orbStack);
        if (found.isEmpty()) return 0;
        SanguineDynamoRecipe recipe = found.get();

        IAnima network = getOwnerNetwork();
        if (network == null) return 0;

        int space = energy.getMaxEnergyStored() - energy.getEnergyStored();
        int evWanted = Math.min(getAllowedEvPerTick(recipe), space / recipe.fePerEv());
        evWanted = Math.min(evWanted, network.getCurrentEV());
        if (evWanted <= 0) return 0;

        int drained = network.syphon(AnimaTicket.create(evWanted));
        if (drained <= 0) return 0;

        energy.generate(drained * recipe.fePerEv());
        return drained;
    }

    private void pushEnergy() {
        if (level == null) return;
        for (Direction dir : Direction.values()) {
            if (energy.getEnergyStored() <= 0) return;
            IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, worldPosition.relative(dir), dir.getOpposite());
            if (target == null || !target.canReceive()) continue;
            int accepted = target.receiveEnergy(energy.getEnergyStored(), false);
            if (accepted > 0) {
                energy.extractEnergy(accepted, false);
            }
        }
    }

    public int getComparatorOutput() {
        if (energy.getEnergyStored() <= 0) return 0;
        return Math.max(1, (int) ((long) energy.getEnergyStored() * 15 / energy.getMaxEnergyStored()));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("OrbStack", orbStack.saveOptional(registries));
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        orbStack = ItemStack.parseOptional(registries, tag.getCompound("OrbStack"));
        energy.setEnergy(tag.getInt("Energy"));
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
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), orbStack);
            orbStack = ItemStack.EMPTY;
        }
    }

    public boolean isOrbBound(ItemStack stack) {
        Binding binding = stack.getOrDefault(NVDataComponents.BINDING.get(), Binding.EMPTY);
        return !binding.isEmpty();
    }

    private class DynamoEnergyStorage extends EnergyStorage {
        DynamoEnergyStorage(int capacity) {
            super(capacity, 0, Integer.MAX_VALUE);
        }

        void generate(int amount) {
            energy = Math.min(capacity, energy + amount);
            setChanged();
        }

        void setEnergy(int amount) {
            energy = Math.max(0, Math.min(capacity, amount));
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            int extracted = super.extractEnergy(toExtract, simulate);
            if (extracted > 0 && !simulate) {
                setChanged();
            }
            return extracted;
        }
    }
}
