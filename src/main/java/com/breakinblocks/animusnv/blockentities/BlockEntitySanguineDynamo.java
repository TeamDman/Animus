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
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;
import java.util.Optional;

public class BlockEntitySanguineDynamo extends BlockEntity implements GeoBlockEntity {

    private static final int ZAP_CHANCE = 6;
    private static final double ORB_HEIGHT = 12.5 / 16.0;
    private static final double POST_TIP_HEIGHT = 15.5 / 16.0;
    private static final double POST_OFFSET = 6.5 / 16.0;
    private static final double ZAP_OVERSHOOT = 0.5;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WORKING = RawAnimation.begin().thenLoop("working");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final DynamoEnergyHandler energy;
    private ItemStack orbStack = ItemStack.EMPTY;
    private int lastEvPerTick;

    public BlockEntitySanguineDynamo(BlockPos pos, BlockState state) {
        super(AnimusBlockEntities.SANGUINE_DYNAMO.get(), pos, state);
        this.energy = new DynamoEnergyHandler(AnimusConfig.sanguineDynamo.bufferCapacity.get());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<BlockEntitySanguineDynamo>("main", 8, test -> {
            BlockState blockState = test.animatable().getBlockState();
            boolean active = blockState.hasProperty(BlockSanguineDynamo.ACTIVE) && blockState.getValue(BlockSanguineDynamo.ACTIVE);
            return test.setAndContinue(active ? WORKING : IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public EnergyHandler getEnergyHandler() {
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
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public Optional<SanguineDynamoRecipe> findRecipe(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel) || stack.isEmpty()) return Optional.empty();
        return serverLevel.recipeAccess()
            .getRecipeFor(AnimusRecipeSerializers.SANGUINE_DYNAMO_TYPE.get(), new SingleRecipeInput(stack), serverLevel)
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
        if (level == null || level.isClientSide()) return;

        lastEvPerTick = generate();
        pushEnergy();

        boolean active = lastEvPerTick > 0;
        BlockState state = getBlockState();
        if (state.getValue(BlockSanguineDynamo.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(BlockSanguineDynamo.ACTIVE, active), Block.UPDATE_ALL);
        }

        if (active && level instanceof ServerLevel serverLevel && level.getRandom().nextInt(ZAP_CHANCE) == 0) {
            sendZap(serverLevel);
        }
    }

    private void sendZap(ServerLevel serverLevel) {
        double sx = level.getRandom().nextBoolean() ? POST_OFFSET : -POST_OFFSET;
        double sz = level.getRandom().nextBoolean() ? POST_OFFSET : -POST_OFFSET;
        double ox = worldPosition.getX() + 0.5;
        double oy = worldPosition.getY() + ORB_HEIGHT;
        double oz = worldPosition.getZ() + 0.5;
        double dx = sx;
        double dy = POST_TIP_HEIGHT - ORB_HEIGHT;
        double dz = sz;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double reach = (length + ZAP_OVERSHOOT) / length;
        StreamEffect.builder(ox, oy, oz)
            .to(ox + dx * reach, oy + dy * reach, oz + dz * reach)
            .color(0xFF2A36)
            .endColor(0xFF9A9A)
            .scale(0.03f)
            .speed(3.0f)
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

        int space = energy.getCapacityAsInt() - energy.getAmountAsInt();
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
            if (energy.getAmountAsInt() <= 0) return;
            EnergyHandler target = level.getCapability(Capabilities.Energy.BLOCK, worldPosition.relative(dir), dir.getOpposite());
            if (target == null) continue;
            try (Transaction tx = Transaction.openRoot()) {
                int moved = EnergyHandlerUtil.move(energy, target, energy.getAmountAsInt(), tx);
                if (moved > 0) {
                    tx.commit();
                }
            }
        }
    }

    public int getComparatorOutput() {
        return EnergyHandlerUtil.getRedstoneSignalFromEnergyHandler(energy);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!orbStack.isEmpty()) {
            output.store("OrbStack", ItemStack.CODEC, orbStack);
        }
        output.putInt("Energy", energy.getAmountAsInt());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        orbStack = input.read("OrbStack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        energy.load(input.getIntOr("Energy", 0));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LogUtils.getLogger())) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            return output.buildResult();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !orbStack.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), orbStack);
            orbStack = ItemStack.EMPTY;
        }
    }

    public boolean isOrbBound(ItemStack stack) {
        Binding binding = stack.getOrDefault(NVDataComponents.BINDING.get(), Binding.EMPTY);
        return !binding.isEmpty();
    }

    private class DynamoEnergyHandler extends SimpleEnergyHandler {
        DynamoEnergyHandler(int capacity) {
            super(capacity, 0, Integer.MAX_VALUE);
        }

        void generate(int amount) {
            set(Math.min(capacity, energy + amount));
        }

        void load(int amount) {
            energy = Math.max(0, Math.min(capacity, amount));
        }

        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    }
}
