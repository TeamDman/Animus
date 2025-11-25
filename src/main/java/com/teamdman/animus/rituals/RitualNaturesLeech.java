package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.util.AnimusUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraftforge.registries.ForgeRegistries;
import wayoftime.bloodmagic.common.tile.TileAltar;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.demonaura.WorldDemonWillHandler;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.types.RitualType;
import wayoftime.bloodmagic.soul.EnumDemonWillType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Ritual of Nature's Leech - Consumes plants to fill blood altar
 * Scans area for consumable plant matter and destroys it to fill the altar
 * Activation Cost: 3000 LP
 * Refresh Cost: 10 LP
 * Refresh Time: 80 ticks (varies with demon will)
 */
@RitualRegister(Constants.Rituals.LEECH)
public class RitualNaturesLeech extends Ritual {
    public static final String ALTAR_RANGE = "altar";
    public static final String EFFECT_RANGE = "effect";
    public final int maxWill = 100;
    public BlockPos altarOffsetPos = BlockPos.ZERO;
    public double will = 100;

    public RitualNaturesLeech() {
        super(new RitualType(Constants.Rituals.LEECH, 0, 3000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.LEECH));

        addBlockRange(ALTAR_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-5, -10, -5), 11, 21, 11));
        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-10, -10, -10), 24));
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 20, 20, 20);
        setMaximumVolumeAndDistanceOfRange(ALTAR_RANGE, 0, 10, 15);
    }

    public static boolean isBlacklisted(ResourceLocation resourceLocation) {
        return resourceLocation != null && isBlacklisted(resourceLocation.toString());
    }

    public static boolean isBlacklisted(String registryName) {
        for (String entry : AnimusConfig.sigils.leechBlacklist.get()) {
            if (Objects.equals(entry, registryName)) {
                return true;
            }
        }
        return false;
    }

    public void performRitual(IMasterRitualStone ritualStone) {
        Level level = ritualStone.getWorldObj();
        Random random = level.random;
        BlockPos pos = ritualStone.getBlockPos();

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
            Component.translatable(Constants.Localizations.Text.TICKET_LEECH),
            getRefreshCost()
        ), false);

        // Find nearby altar
        TileAltar tileAltar = AnimusUtil.getNearbyAltar(level, getBlockRange(ALTAR_RANGE), pos, altarOffsetPos);
        if (tileAltar == null) {
            return;
        }
        altarOffsetPos = tileAltar.getBlockPos();

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

        // Add blood to altar
        tileAltar.sacrificialDaggerCall(eaten * 50, true);

        // Generate corrosive demon will
        int drainAmount = 1 + random.nextInt(5);
        double filled = WorldDemonWillHandler.fillWillToMaximum(level, pos, type, drainAmount, maxWill, false);
        if (filled > 0) {
            WorldDemonWillHandler.fillWillToMaximum(level, pos, type, filled, maxWill, true);
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

        // Check if block is blacklisted
        ResourceLocation registryName = ForgeRegistries.BLOCKS.getKey(block);
        if (isBlacklisted(registryName)) {
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
        return (int) Math.min(80, (100 * (100 / (Math.max(1, will) * 6))));
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, -2, 1, -2, RitualType.EnumRuneType.WATER);
        addRune(components, -2, 1, 0, RitualType.EnumRuneType.AIR);
        addRune(components, -2, 1, 2, RitualType.EnumRuneType.WATER);
        addRune(components, -1, 0, -1, RitualType.EnumRuneType.EARTH);
        addRune(components, -1, 0, 1, RitualType.EnumRuneType.WATER);
        addRune(components, 0, 1, -2, RitualType.EnumRuneType.AIR);
        addRune(components, 0, 1, 2, RitualType.EnumRuneType.AIR);
        addRune(components, 1, 0, -1, RitualType.EnumRuneType.WATER);
        addRune(components, 1, 0, 1, RitualType.EnumRuneType.AIR);
        addRune(components, 2, 1, -2, RitualType.EnumRuneType.WATER);
        addRune(components, 2, 1, 0, RitualType.EnumRuneType.AIR);
        addRune(components, 2, 1, 2, RitualType.EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualNaturesLeech();
    }
}
