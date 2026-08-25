package com.breakinblocks.animusnv.rituals;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.AnimusStartupConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.util.AnimusUtil;
import com.breakinblocks.animusnv.util.CullingHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.api.spiritus.ISpiritusHandler;
import com.breakinblocks.neovitae.common.datamap.EntitySacrificeHelper;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.EnumRuneType;

import java.util.*;
import java.util.function.Consumer;

/**
 * Ritual of Culling - Kills entities in range
 * Powerful ritual that kills non-boss entities and can kill bosses with Spiritus
 * Also destroys primed TNT if configured
 * Activation Cost: 50000 EV
 * Refresh Cost: 75 EV per entity
 * Refresh Time: 25 ticks
 * Range: Configurable (default 10 blocks horizontal, 10 blocks vertical above AND below stone)
 * EV per Kill: Determined by NeoVitae's entity_sacrifice_value datamap (entity's full health worth)
 */
public class RitualCulling extends Ritual {
    public static final String ALTAR_RANGE = "altar";
    public static final String EFFECT_RANGE = "effect";

    public final int maxSpiritus = 100;
    public final Random rand = new Random();
    public BlockPos altarOffsetPos = BlockPos.ZERO;
    public double crystalBuffer = 0;
    public int reagentDrain = 2;
    public boolean result = false;
    public double spiritusBuffer = 0;
    public HashMap<String, Double> spiritusMap = new HashMap<>();

    public RitualCulling() {
        super(Constants.Rituals.CULLING, 0, 50000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.CULLING);

        int hRange = AnimusStartupConfig.ritualRanges.cullingHorizontalRange.get();
        int vRange = AnimusStartupConfig.ritualRanges.cullingVerticalRange.get();

        addBlockRange(ALTAR_RANGE, RitualAreaDescriptors.horizontalArea(5, 10));
        addBlockRange(EFFECT_RANGE, RitualAreaDescriptors.horizontalArea(hRange, vRange));

        setMaximumVolumeAndDistanceOfRange(ALTAR_RANGE, 0, 10, 15);
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, hRange + 5, vRange + 5);
    }

    public double smallGauss(double d) {
        Random myRand = new Random();
        return (myRand.nextFloat() - 0.5D) * d;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        spiritusBuffer = tag.getDoubleOr(Constants.NBT.CULLING_BUFFER_WILL, 0.0);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putDouble(Constants.NBT.CULLING_BUFFER_WILL, spiritusBuffer);
    }

    @Override
    public boolean activateRitual(IMasterRitualStone ritualStone, Player player, UUID owner) {
        double xCoord = ritualStone.getMasterBlockPos().getX();
        double yCoord = ritualStone.getMasterBlockPos().getY();
        double zCoord = ritualStone.getMasterBlockPos().getZ();

        if (player != null && player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                xCoord + 0.5,
                yCoord + 1,
                zCoord + 0.5,
                20,
                0.5, 1.0, 0.5,
                0.1
            );
            player.level().playSound(null, ritualStone.getMasterBlockPos(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        return true;
    }

    @Override
    public void performRitual(IMasterRitualStone ritualStone) {
        IAnima network = AnimusRitualHelper.getOwnerNetwork(ritualStone);
        if (network == null) {
            return;
        }

        int currentEV = network.getCurrentEV();
        Level level = ritualStone.getWorldObj();
        BlockPos pos = ritualStone.getMasterBlockPos();

        if (level.isClientSide()) {
            return;
        }

        ISpiritusHandler spiritusHandler = NeoVitaeAPI.getInstance().getSpiritusHandler();
        SpiritusType type = SpiritusType.NIHILUM;
        double currentAmount = spiritusHandler.getCurrentSpiritus(level, pos, type);

        // Raw Spiritus enables player-like kills (mob drops as if killed by player)
        double rawSpiritusAmount = spiritusHandler.getCurrentSpiritus(level, pos, SpiritusType.RAW);
        boolean usePlayerKill = AnimusConfig.rituals.cullingPlayerKillDrops.get() && rawSpiritusAmount >= 1.0;

        AraVitaeTile tileAltar = AnimusUtil.getNearbyAltar(level, getBlockRange(ALTAR_RANGE), pos, altarOffsetPos);
        if (tileAltar == null) {
            if (AnimusConfig.rituals.cullingDebug.get()) {
                Animus.LOGGER.debug("[Ritual of Culling Debug]: No valid altar found within altar range for MRS at {}", ritualStone.getMasterBlockPos());
            }
            return;
        }
        altarOffsetPos = tileAltar.getBlockPos();

        AreaDescriptor damageRange = getBlockRange(EFFECT_RANGE);
        AABB range = damageRange.getAABB(pos);

        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, range);

        if (AnimusConfig.rituals.cullingDebug.get()) {
            Animus.LOGGER.debug("[Ritual of Culling Debug]: Starting Ritual perform for MRS at {}", ritualStone.getMasterBlockPos());
            Animus.LOGGER.debug("[Ritual of Culling Debug]: Range AABB: {}", range);
            Animus.LOGGER.debug("[Ritual of Culling Debug]: Found {} entities in range", list.size());
        }

        CullingHelper.removePrimedTnt(level, range);

        int entityCount = 0;

        if (currentEV < getRefreshCost() * list.size()) {
            if (AnimusConfig.rituals.cullingDebug.get()) {
                Animus.LOGGER.debug("[Ritual of Culling Debug]: Culling MRS at {} does not have sufficient EV from the owner", ritualStone.getMasterBlockPos());
            }
        } else {
            if (AnimusConfig.rituals.cullingDebug.get()) {
                Animus.LOGGER.debug("[Ritual of Culling Debug]: Starting culling for loop for MRS at {}", ritualStone.getMasterBlockPos());
            }

            for (LivingEntity livingEntity : list) {
                CullingHelper.debugLogEntityProcessing(livingEntity);

                if (CullingHelper.shouldSkipEntity(livingEntity)) {
                    continue;
                }

                BlockPos at = livingEntity.blockPosition();
                boolean isBoss = CullingHelper.isBoss(livingEntity);
                float damage = Float.MAX_VALUE;

                CullingHelper.debugLogKillAttempt(livingEntity, isBoss, damage, usePlayerKill);

                livingEntity.setSilent(true);

                // Bosses require 100+ Nihilum Spiritus and extra EV to kill
                if (isBoss) {
                    int requiredEssence = AnimusConfig.rituals.bossCost.get() + (getRefreshCost() * list.size());
                    CullingHelper.tryMakeBossVulnerable(livingEntity, currentAmount, currentEV, requiredEssence);
                }

                if (usePlayerKill && level instanceof ServerLevel serverLevel) {
                    result = CullingHelper.applyPlayerKillDamage(livingEntity, serverLevel, ritualStone.getOwner(), pos, damage);

                    if (result && rand.nextDouble() < AnimusConfig.rituals.cullingSpiritusConsumeChance.get()) {
                        spiritusHandler.drainSpiritus(level, pos, SpiritusType.RAW, 1.0);
                        if (AnimusConfig.rituals.cullingDebug.get()) {
                            Animus.LOGGER.debug("[Ritual of Culling Debug]:   Consumed 1 raw Spiritus");
                        }
                    }
                } else {
                    result = livingEntity.hurtOrSimulate(level.damageSources().genericKill(), damage);
                }

                CullingHelper.debugLogKillResult(livingEntity, result);

                if (result) {
                    entityCount++;
                    int evPerKill = EntitySacrificeHelper.calculateEV(livingEntity, livingEntity.getMaxHealth());
                    tileAltar.addSacrificeEV(evPerKill, true);

                    if (AnimusConfig.rituals.cullingDebug.get()) {
                        Animus.LOGGER.debug("[Ritual of Culling Debug]:   EV generated: {} (EV/dmg: {}, maxHP: {})", evPerKill, EntitySacrificeHelper.getEvPerDamage(livingEntity), livingEntity.getMaxHealth());
                    }

                    if (isBoss) {
                        network.syphon(AnimaTicket.create(AnimusConfig.rituals.bossCost.get()));
                    } else {
                        spiritusBuffer += CullingHelper.calculateSpiritusGain(livingEntity);
                    }

                    CullingHelper.spawnKillEffects(level, at, rand);
                }
            }

            if (AnimusConfig.rituals.cullingDebug.get()) {
                Animus.LOGGER.debug("[Ritual of Culling Debug]: Finished culling loop - killed {} entities", entityCount);
            }

            network.syphon(AnimaTicket.create(getRefreshCost() * entityCount));

            // ~3% chance per cycle to generate Nihilum Spiritus
            double addAmount = Math.min(maxSpiritus - currentAmount, Math.min(entityCount / 2.0, 10));
            if (rand.nextInt(30) == 0 && addAmount > 0) {
                spiritusHandler.addSpiritus(level, pos, type, addAmount);
            }
        }
    }

    @Override
    public int getRefreshCost() {
        return AnimusConfig.rituals.cullingRefreshCost.get();
    }

    @Override
    public int getRefreshTime() {
        return AnimusConfig.rituals.cullingRefreshTime.get();
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 1, 0, 1, EnumRuneType.FIRE);
        addRune(components, -1, 0, 1, EnumRuneType.FIRE);
        addRune(components, 1, 0, -1, EnumRuneType.FIRE);
        addRune(components, -1, 0, -1, EnumRuneType.FIRE);
        addRune(components, 2, -1, 2, EnumRuneType.TENEBRAE);
        addRune(components, 2, -1, -2, EnumRuneType.TENEBRAE);
        addRune(components, -2, -1, 2, EnumRuneType.TENEBRAE);
        addRune(components, -2, -1, -2, EnumRuneType.TENEBRAE);
        addRune(components, 0, -1, 2, EnumRuneType.TENEBRAE);
        addRune(components, 2, -1, 0, EnumRuneType.TENEBRAE);
        addRune(components, 0, -1, -2, EnumRuneType.TENEBRAE);
        addRune(components, -2, -1, 0, EnumRuneType.TENEBRAE);
        addRune(components, -3, -1, -3, EnumRuneType.TENEBRAE);
        addRune(components, 3, -1, -3, EnumRuneType.TENEBRAE);
        addRune(components, -3, -1, 3, EnumRuneType.TENEBRAE);
        addRune(components, 3, -1, 3, EnumRuneType.TENEBRAE);
        addRune(components, 2, -1, 4, EnumRuneType.TENEBRAE);
        addRune(components, 4, -1, 2, EnumRuneType.TENEBRAE);
        addRune(components, -2, -1, 4, EnumRuneType.TENEBRAE);
        addRune(components, 4, -1, -2, EnumRuneType.TENEBRAE);
        addRune(components, 2, -1, -4, EnumRuneType.TENEBRAE);
        addRune(components, -4, -1, 2, EnumRuneType.TENEBRAE);
        addRune(components, -2, -1, -4, EnumRuneType.TENEBRAE);
        addRune(components, -4, -1, -2, EnumRuneType.TENEBRAE);
        addRune(components, 1, 0, 4, EnumRuneType.TENEBRAE);
        addRune(components, 4, 0, 1, EnumRuneType.TENEBRAE);
        addRune(components, 1, 0, -4, EnumRuneType.TENEBRAE);
        addRune(components, -4, 0, 1, EnumRuneType.TENEBRAE);
        addRune(components, -1, 0, 4, EnumRuneType.TENEBRAE);
        addRune(components, 4, 0, -1, EnumRuneType.TENEBRAE);
        addRune(components, -1, 0, -4, EnumRuneType.TENEBRAE);
        addRune(components, -4, 0, -1, EnumRuneType.TENEBRAE);
        addRune(components, 4, 1, 0, EnumRuneType.TENEBRAE);
        addRune(components, 0, 1, 4, EnumRuneType.TENEBRAE);
        addRune(components, -4, 1, 0, EnumRuneType.TENEBRAE);
        addRune(components, 0, 1, -4, EnumRuneType.TENEBRAE);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualCulling();
    }
}
