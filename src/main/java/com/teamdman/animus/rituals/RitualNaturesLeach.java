package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.util.AnimusUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import wayoftime.bloodmagic.common.tile.TileAltar;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.demonaura.WorldDemonWillHandler;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.EnumRuneType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.Random;
import java.util.function.Consumer;

/**
 * Ritual of Nature's Leach - Consumes plants to fill blood altar
 * Scans area for consumable plant matter and destroys it to fill the altar
 * Activation Cost: 3000 LP
 * Refresh Cost: 10 LP
 * Refresh Time: Configurable (default 80 ticks, varies with demon will)
 * Range: Configurable (default 32 blocks)
 * Altar Search Range: 32 blocks horizontally, ±10 blocks vertically (cached for performance)
 * LP per Block: Configurable (default 50 LP)
 */
@RitualRegister(Constants.Rituals.LEACH)
public class RitualNaturesLeach extends Ritual {
    public static final String ALTAR_RANGE = "altar";
    public static final String EFFECT_RANGE = "effect";
    public static final int ALTAR_RECHECK_INTERVAL = 100; // Recheck altar every 100 ticks (5 seconds)
    public final int maxWill = 100;

    // Altar caching for performance
    public BlockPos cachedAltarPos = null;
    public TileAltar cachedAltar = null;
    public int ticksSinceAltarCheck = 0;

    public double will = 100;

    public RitualNaturesLeach() {
        super(Constants.Rituals.LEACH, 0, 3000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.LEACH);

        // Use config value for range (default 32 blocks)
        int range = AnimusConfig.rituals.naturesLeachRange.get();
        int rangeSize = range * 2 + 4; // Convert to full size

        // Altar range: 32 blocks horizontally, 10 blocks down, 10 blocks up
        addBlockRange(ALTAR_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-32, -10, -32), 65, 21, 65));
        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-range, -range, -range), rangeSize));
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, range + 10, range + 10, range + 10);
        setMaximumVolumeAndDistanceOfRange(ALTAR_RANGE, 0, 32, 32);
    }

    /**
     * Check if a block is blacklisted from being consumed by Nature's Leach
     * @param block The block to check
     * @return true if the block is in the disallow_leach tag
     */
    public static boolean isBlacklisted(Block block) {
        return block.defaultBlockState().is(Constants.Tags.DISALLOW_LEACH);
    }

    public void performRitual(IMasterRitualStone ritualStone) {
        Level level = ritualStone.getWorldObj();
        // RandomSource in 1.20.1
        net.minecraft.util.RandomSource randomSource = level.random;
        Random random = new Random(randomSource.nextLong());
        BlockPos pos = ritualStone.getMasterBlockPos();

        // Get current corrosive demon will
        EnumDemonWillType type = EnumDemonWillType.CORROSIVE;
        will = WorldDemonWillHandler.getCurrentWill(level, pos, type);

        SoulNetwork network = NetworkHelper.getSoulNetwork(ritualStone.getOwner());
        if (network == null) {
            return;
        }

        int currentEssence = network.getCurrentEssence();

        if (level.isClientSide) {
            return;
        }

        if (currentEssence < getRefreshCost()) {
            network.causeNausea();
            return;
        }

        network.syphon(new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_LEACH),
            getRefreshCost()
        ), false);

        // Find nearby altar with caching for performance
        TileAltar tileAltar = null;
        ticksSinceAltarCheck++;

        // Check if we have a valid cached altar
        if (cachedAltar != null && cachedAltarPos != null && ticksSinceAltarCheck < ALTAR_RECHECK_INTERVAL) {
            // Verify the cached altar is still valid
            if (level.getBlockEntity(cachedAltarPos) instanceof TileAltar altar) {
                tileAltar = altar;
            } else {
                // Cached altar is no longer valid, clear cache
                cachedAltar = null;
                cachedAltarPos = null;
            }
        }

        // If we don't have a cached altar or it's time to recheck, search for one
        if (tileAltar == null || ticksSinceAltarCheck >= ALTAR_RECHECK_INTERVAL) {
            BlockPos hintPos = cachedAltarPos != null ? cachedAltarPos : BlockPos.ZERO;
            tileAltar = AnimusUtil.getNearbyAltar(level, getBlockRange(ALTAR_RANGE), pos, hintPos);

            if (tileAltar != null) {
                // Update cache
                cachedAltar = tileAltar;
                cachedAltarPos = tileAltar.getBlockPos();
                ticksSinceAltarCheck = 0;
            } else {
                // No altar found, clear cache
                cachedAltar = null;
                cachedAltarPos = null;
                ticksSinceAltarCheck = 0;
                return;
            }
        }

        if (tileAltar == null) {
            return;
        }

        // Scan for consumable plants
        AreaDescriptor eatRange = getBlockRange(EFFECT_RANGE);
        int randFood = 1 + random.nextInt(3);
        int eaten = 0;

        for (BlockPos eatPos : eatRange.getContainedPositions(pos)) {
            if (eaten >= randFood) {
                break;
            }

            Block eatBlock = level.getBlockState(eatPos).getBlock();
            if (eatBlock == Blocks.AIR) {
                continue;
            }

            if (random.nextInt(100) < 20 && isConsumable(eatBlock)) {
                // Spawn particles
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        eatPos.getX() + 0.5,
                        eatPos.getY() + 0.5,
                        eatPos.getZ() + 0.5,
                        5,
                        (random.nextDouble() - 0.5D) * 2.0D,
                        random.nextDouble(),
                        (random.nextDouble() - 0.5D) * 2.0D,
                        0.1
                    );
                }

                // Play sound
                level.playSound(null, eatPos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 0.4F, 1.0F);

                // Remove block
                level.removeBlock(eatPos, false);
                eaten++;
            }
        }

        // Add blood to altar (use config value)
        int lpPerBlock = AnimusConfig.rituals.naturesLeachLpPerBlock.get();
        tileAltar.sacrificialDaggerCall(eaten * lpPerBlock, true);

        // Generate corrosive demon will based on blocks consumed
        // Each consumed block generates 0.5-1.5 corrosive will
        if (eaten > 0) {
            double willPerBlock = 0.5 + random.nextDouble(); // 0.5-1.5 per block
            double totalWillToAdd = eaten * willPerBlock;
            double filled = WorldDemonWillHandler.fillWillToMaximum(level, pos, type, totalWillToAdd, maxWill, false);
            if (filled > 0) {
                WorldDemonWillHandler.fillWillToMaximum(level, pos, type, filled, maxWill, true);
            }
        }
    }

    public static boolean isConsumable(Block block) {
        if (block == null || block == Blocks.AIR) {
            return false;
        }

        String blockName = block.getDescriptionId().toLowerCase();

        // Skip Botania special flowers
        if (blockName.contains("specialflower") || blockName.contains("shinyflower")) {
            return false;
        }

        // Check if block is blacklisted using the tag
        if (isBlacklisted(block)) {
            return false;
        }

        // Check if block is a log
        if (block.defaultBlockState().is(BlockTags.LOGS)) {
            return true;
        }

        // Check if block is a plant (flowers, crops, etc)
        if (block.defaultBlockState().is(BlockTags.FLOWERS) ||
            block.defaultBlockState().is(BlockTags.CROPS) ||
            block.defaultBlockState().is(BlockTags.SAPLINGS)) {
            return true;
        }

        // Check if block is bonemealable (growable)
        if (block instanceof BonemealableBlock) {
            return true;
        }

        // Check for common plant blocks
        if (block == Blocks.GRASS ||
            block == Blocks.TALL_GRASS ||
            block == Blocks.FERN ||
            block == Blocks.LARGE_FERN ||
            block == Blocks.DEAD_BUSH ||
            block == Blocks.SEAGRASS ||
            block == Blocks.TALL_SEAGRASS ||
            block == Blocks.KELP ||
            block == Blocks.KELP_PLANT ||
            block == Blocks.VINE ||
            block == Blocks.MOSS_CARPET ||
            block == Blocks.MOSS_BLOCK) {
            return true;
        }

        return false;
    }

    @Override
    public int getRefreshCost() {
        return 10;
    }

    @Override
    public int getRefreshTime() {
        int baseSpeed = AnimusConfig.rituals.naturesLeachBaseSpeed.get();
        return (int) Math.min(baseSpeed, (100 * (100 / (Math.max(1, will) * 6))));
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, -2, 1, -2, EnumRuneType.WATER);
        addRune(components, -2, 1, 0, EnumRuneType.AIR);
        addRune(components, -2, 1, 2, EnumRuneType.WATER);
        addRune(components, -1, 0, -1, EnumRuneType.EARTH);
        addRune(components, -1, 0, 1, EnumRuneType.WATER);
        addRune(components, 0, 1, -2, EnumRuneType.AIR);
        addRune(components, 0, 1, 2, EnumRuneType.AIR);
        addRune(components, 1, 0, -1, EnumRuneType.WATER);
        addRune(components, 1, 0, 1, EnumRuneType.AIR);
        addRune(components, 2, 1, -2, EnumRuneType.WATER);
        addRune(components, 2, 1, 0, EnumRuneType.AIR);
        addRune(components, 2, 1, 2, EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualNaturesLeach();
    }
}
