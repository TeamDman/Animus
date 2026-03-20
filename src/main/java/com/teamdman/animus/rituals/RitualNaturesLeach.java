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
import com.breakinblocks.neovitae.common.blockentity.BloodAltarTile;
import com.breakinblocks.neovitae.common.datacomponent.EnumWillType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.ISoulNetwork;
import com.breakinblocks.neovitae.api.soul.SoulTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.api.will.IDemonWillHandler;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.EnumRuneType;

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
public class RitualNaturesLeach extends Ritual {
    public static final String ALTAR_RANGE = "altar";
    public static final String EFFECT_RANGE = "effect";
    public static final int ALTAR_RECHECK_INTERVAL = 100;
    public final int maxWill = 100;

    public BlockPos cachedAltarPos = null;
    public BloodAltarTile cachedAltar = null;
    public int ticksSinceAltarCheck = 0;

    public double will = 100;

    public RitualNaturesLeach() {
        super(Constants.Rituals.LEACH, 0, 3000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.LEACH);

        int range = 32;
        int rangeSize = range * 2 + 4;

        addBlockRange(ALTAR_RANGE, RitualAreaDescriptors.horizontalArea(32, 10));
        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-range, -range, -range), rangeSize, rangeSize, rangeSize));
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, range + 10, range + 10, range + 10);
        setMaximumVolumeAndDistanceOfRange(ALTAR_RANGE, 0, 32, 32);
    }

    public static boolean isBlacklisted(Block block) {
        return block.defaultBlockState().is(Constants.Tags.DISALLOW_LEACH);
    }

    public void performRitual(IMasterRitualStone ritualStone) {
        Level level = ritualStone.getWorldObj();
        net.minecraft.util.RandomSource randomSource = level.random;
        Random random = new Random(randomSource.nextLong());
        BlockPos pos = ritualStone.getMasterBlockPos();

        IDemonWillHandler willHandler = NeoVitaeAPI.getInstance().getDemonWillHandler();
        EnumWillType type = EnumWillType.CORROSIVE;
        will = willHandler.getCurrentWill(level, pos, type);

        ISoulNetwork network = NeoVitaeAPI.getInstance().getSoulNetwork(ritualStone.getOwner());
        if (network == null) {
            return;
        }

        int currentEssence = network.getCurrentEssence();

        if (level.isClientSide) {
            return;
        }

        if (currentEssence < getRefreshCost()) {
            return;
        }

        network.syphon(SoulTicket.create(getRefreshCost()));

        BloodAltarTile tileAltar = null;
        ticksSinceAltarCheck++;

        if (cachedAltar != null && cachedAltarPos != null && ticksSinceAltarCheck < ALTAR_RECHECK_INTERVAL) {
            if (level.getBlockEntity(cachedAltarPos) instanceof BloodAltarTile altar) {
                tileAltar = altar;
            } else {
                cachedAltar = null;
                cachedAltarPos = null;
            }
        }

        if (tileAltar == null || ticksSinceAltarCheck >= ALTAR_RECHECK_INTERVAL) {
            BlockPos hintPos = cachedAltarPos != null ? cachedAltarPos : BlockPos.ZERO;
            tileAltar = AnimusUtil.getNearbyAltar(level, getBlockRange(ALTAR_RANGE), pos, hintPos);

            if (tileAltar != null) {
                cachedAltar = tileAltar;
                cachedAltarPos = tileAltar.getBlockPos();
                ticksSinceAltarCheck = 0;
            } else {
                cachedAltar = null;
                cachedAltarPos = null;
                ticksSinceAltarCheck = 0;
                return;
            }
        }

        if (tileAltar == null) {
            return;
        }

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

                level.playSound(null, eatPos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 0.4F, 1.0F);
                level.removeBlock(eatPos, false);
                eaten++;
            }
        }

        int lpPerBlock = AnimusConfig.rituals.naturesLeachLpPerBlock.get();
        tileAltar.addSacrificeLP(eaten * lpPerBlock, true);

        // Each consumed block generates 0.5-1.5 corrosive will
        if (eaten > 0) {
            double willPerBlock = 0.5 + random.nextDouble();
            double totalWillToAdd = eaten * willPerBlock;
            double currentWill = willHandler.getCurrentWill(level, pos, type);
            double actualAdd = Math.min(totalWillToAdd, maxWill - currentWill);
            if (actualAdd > 0) {
                willHandler.addWill(level, pos, type, actualAdd);
            }
        }
    }

    public static boolean isConsumable(Block block) {
        if (block == null || block == Blocks.AIR) {
            return false;
        }

        String blockName = block.getDescriptionId().toLowerCase();

        if (blockName.contains("specialflower") || blockName.contains("shinyflower")) {
            return false;
        }

        if (isBlacklisted(block)) {
            return false;
        }

        if (block.defaultBlockState().is(BlockTags.LOGS)) {
            return true;
        }

        if (block.defaultBlockState().is(BlockTags.FLOWERS) ||
            block.defaultBlockState().is(BlockTags.CROPS) ||
            block.defaultBlockState().is(BlockTags.SAPLINGS)) {
            return true;
        }

        if (block instanceof BonemealableBlock) {
            return true;
        }

        if (block == Blocks.SHORT_GRASS ||
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
