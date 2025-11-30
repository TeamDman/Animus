package com.teamdman.animus.blockentities;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.registry.AnimusBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.demonaura.WorldDemonWillHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Diabolical Fungi (Devil's Tooth Mushroom)
 *
 * A Botania mana-generating flower that consumes demon will from the chunk to produce mana.
 *
 * Features:
 * - Consumes up to 100 demon will every 2 seconds (40 ticks)
 * - Conversion rate: 1 will = 250 mana (configurable)
 * - Bonus: More varied will types consumed = more mana produced
 * - If placed on Botania Overgrowth Soil, doubles the conversion rate
 *
 * Requires both Blood Magic and Botania to be installed.
 */
public class BlockEntityDiabolicalFungi extends GeneratingFlowerBlockEntity {
    private static final int TICK_RATE = 40; // 2 seconds
    private static final int MAX_WILL_PER_CYCLE = 100;

    private int tickCounter = 0;

    public BlockEntityDiabolicalFungi(BlockPos pos, BlockState state) {
        super(AnimusBlockEntities.DIABOLICAL_FUNGI.get(), pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (level == null || level.isClientSide) {
            return;
        }

        tickCounter++;
        if (tickCounter >= TICK_RATE) {
            tickCounter = 0;
            consumeWillAndGenerateMana();
        }
    }

    /**
     * Attempts to consume demon will from the chunk and convert it to mana
     * Consumes proportionally from all available will types
     */
    private void consumeWillAndGenerateMana() {
        if (!ModList.get().isLoaded("bloodmagic")) {
            return;
        }

        int conversionRate = AnimusConfig.botania.willToManaConversionRate.get();

        // First pass: Calculate available will for each type and total
        Map<EnumDemonWillType, Double> availableWill = new HashMap<>();
        double totalAvailableWill = 0;

        for (EnumDemonWillType willType : EnumDemonWillType.values()) {
            double available = WorldDemonWillHandler.getCurrentWill(level, worldPosition, willType);
            if (available > 0) {
                availableWill.put(willType, available);
                totalAvailableWill += available;
            }
        }

        // No will available
        if (totalAvailableWill == 0) {
            return;
        }

        // Determine how much total will we want to consume (up to MAX_WILL_PER_CYCLE)
        int targetWillConsumption = (int) Math.min(MAX_WILL_PER_CYCLE, totalAvailableWill);

        // Second pass: Consume proportionally from each type
        Set<EnumDemonWillType> typesConsumed = new HashSet<>();
        int totalWillConsumed = 0;

        for (Map.Entry<EnumDemonWillType, Double> entry : availableWill.entrySet()) {
            EnumDemonWillType willType = entry.getKey();
            double available = entry.getValue();

            // Calculate proportion of this type relative to total
            double proportion = available / totalAvailableWill;

            // Calculate how much of this type to consume based on proportion
            int willToConsume = (int) Math.ceil(targetWillConsumption * proportion);

            // Make sure we don't consume more than available
            willToConsume = (int) Math.min(willToConsume, available);

            // Make sure we don't exceed our target
            if (totalWillConsumed + willToConsume > targetWillConsumption) {
                willToConsume = targetWillConsumption - totalWillConsumed;
            }

            if (willToConsume > 0) {
                // Drain the will from the chunk
                WorldDemonWillHandler.drainWill(level, worldPosition, willType, willToConsume, true);
                totalWillConsumed += willToConsume;
                typesConsumed.add(willType);
            }
        }

        if (totalWillConsumed > 0) {
            // Calculate base mana from consumed will
            int baseMana = totalWillConsumed * conversionRate;

            // Apply variety bonus: 10% bonus per additional will type after the first
            // 1 type = 100%, 2 types = 110%, 3 types = 120%, 4 types = 130%, 5 types = 140%
            double varietyMultiplier = 1.0 + ((typesConsumed.size() - 1) * 0.1);
            int manaWithBonus = (int) (baseMana * varietyMultiplier);

            // Check if on overgrowth soil and double if so
            if (isOnOvergrowthSoil()) {
                manaWithBonus *= 2;
            }

            addMana(manaWithBonus);

            // Play sound and spawn particles
            playConsumptionEffects();
        }
    }

    /**
     * Plays sound and particle effects when consuming demon will
     */
    private void playConsumptionEffects() {
        if (level == null || level.isClientSide) {
            return;
        }

        // Play sound at 20% volume
        level.playSound(null, worldPosition,
            com.teamdman.animus.registry.AnimusSounds.FUNGAL_SLURP.get(),
            net.minecraft.sounds.SoundSource.BLOCKS,
            0.2f, // volume
            1.0f  // pitch
        );

        // Spawn 2-3 soul fire flame particles
        net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) level;
        int particleCount = 2 + level.random.nextInt(2); // 2-3 particles

        for (int i = 0; i < particleCount; i++) {
            // Randomize position slightly around the block center
            double x = worldPosition.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.5;
            double y = worldPosition.getY() + 0.5;
            double z = worldPosition.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.5;

            // Upward velocity for rising particles
            double velocityX = (level.random.nextDouble() - 0.5) * 0.05;
            double velocityY = 0.05 + level.random.nextDouble() * 0.05; // Rise upward
            double velocityZ = (level.random.nextDouble() - 0.5) * 0.05;

            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                x, y, z,
                1, // count
                velocityX, velocityY, velocityZ,
                0.01 // speed
            );
        }
    }

    /**
     * Checks if the flower is placed on Botania's Overgrowth Soil
     */
    private boolean isOnOvergrowthSoil() {
        if (level == null) {
            return false;
        }

        BlockPos below = worldPosition.below();
        BlockState belowState = level.getBlockState(below);

        // Check for Botania's enchanted soil (overgrowth seed applied)
        // The enchanted soil block has a property ENCHANTED that is true when overgrowth seed is applied
        try {
            return belowState.getBlock().getDescriptionId().contains("enchanted_soil");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public RadiusDescriptor getRadius() {
        // The flower doesn't have a visible effect radius - it consumes from the entire chunk
        return RadiusDescriptor.Rectangle.square(worldPosition, 0);
    }

    @Override
    public int getColor() {
        // Dark red/purple color for the mushroom
        return 0x8B0000;
    }

    @Override
    public int getMaxMana() {
        return 5000;
    }

    @Override
    public ItemStack getDefaultHudIcon() {
        return new ItemStack(getBlockState().getBlock());
    }
}
