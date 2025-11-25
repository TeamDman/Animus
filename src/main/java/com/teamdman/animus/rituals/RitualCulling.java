package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.util.AnimusUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.common.tile.TileAltar;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.types.RitualType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.*;
import java.util.function.Consumer;

/**
 * Ritual of Culling - Kills entities in range
 * Powerful ritual that kills non-boss entities and can kill bosses with demon will
 * Also destroys primed TNT if configured
 * Activation Cost: 50000 LP
 * Refresh Cost: 75 LP per entity
 * Refresh Time: 25 ticks
 */
@RitualRegister(Constants.Rituals.CULLING)
public class RitualCulling extends Ritual {
    public static final String ALTAR_RANGE = "altar";
    public static final String EFFECT_RANGE = "effect";
    public static final int amount = 200;

    static final DamageSource culled = new DamageSource(
        Level.damageSources().genericKill().typeHolder()
    );

    public final int maxWill = 100;
    public final Random rand = new Random();
    public BlockPos altarOffsetPos = BlockPos.ZERO;
    public double crystalBuffer = 0;
    public int reagentDrain = 2;
    public boolean result = false;
    public double willBuffer = 0;
    public HashMap<String, Double> willMap = new HashMap<>();

    public RitualCulling() {
        super(new RitualType(Constants.Rituals.CULLING, 0, 50000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.CULLING));

        addBlockRange(ALTAR_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-5, -10, -5), 11, 21, 11));
        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-10, -10, -10), 21));

        setMaximumVolumeAndDistanceOfRange(ALTAR_RANGE, 0, 10, 15);
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 15, 15);
    }

    public double smallGauss(double d) {
        Random myRand = new Random();
        return (myRand.nextFloat() - 0.5D) * d;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        willBuffer = tag.getDouble(Constants.NBT.CULLING_BUFFER_WILL);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putDouble(Constants.NBT.CULLING_BUFFER_WILL, willBuffer);
    }

    @Override
    public boolean activateRitual(IMasterRitualStone ritualStone, Player player, UUID owner) {
        double xCoord = ritualStone.getBlockPos().getX();
        double yCoord = ritualStone.getBlockPos().getY();
        double zCoord = ritualStone.getBlockPos().getZ();

        if (player != null && player.level() instanceof ServerLevel serverLevel) {
            // Spawn lightning effect at ritual
            serverLevel.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                xCoord + 0.5,
                yCoord + 1,
                zCoord + 0.5,
                20,
                0.5, 1.0, 0.5,
                0.1
            );
            player.level().playSound(null, ritualStone.getBlockPos(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        return true;
    }

    @Override
    public void performRitual(IMasterRitualStone ritualStone) {
        SoulNetwork network = NetworkHelper.getSoulNetwork(ritualStone.getOwner());
        if (network == null) {
            return;
        }

        int currentEssence = network.getCurrentEssence();
        Level level = ritualStone.getWorldObj();
        BlockPos pos = ritualStone.getBlockPos();

        if (level.isClientSide) {
            return;
        }

        // TODO: Integrate with demon will system when available
        // EnumDemonWillType type = EnumDemonWillType.DESTRUCTIVE;
        // double currentAmount = WorldDemonWillHandler.getCurrentWill(level, pos, type);
        double currentAmount = 0; // Placeholder

        // Find nearby altar
        TileAltar tileAltar = AnimusUtil.getNearbyAltar(level, getBlockRange(ALTAR_RANGE), pos, altarOffsetPos);
        if (tileAltar == null) {
            if (AnimusConfig.rituals.cullingDebug.get()) {
                System.out.println("Animus: [Ritual of Culling Debug]: No valid altar found within altar range for MRS at " + ritualStone.getBlockPos());
            }
            return;
        }
        altarOffsetPos = tileAltar.getBlockPos();

        AreaDescriptor damageRange = getBlockRange(EFFECT_RANGE);
        AABB range = damageRange.getAABB(pos);

        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, range);

        if (AnimusConfig.rituals.cullingDebug.get()) {
            System.out.println("Animus: [Ritual of Culling Debug]: Starting Ritual perform for MRS at " + ritualStone.getBlockPos());
        }

        // Kill primed TNT if configured
        if (AnimusConfig.rituals.cullingKillsTnT.get()) {
            List<PrimedTnt> tntList = level.getEntitiesOfClass(PrimedTnt.class, range);
            for (PrimedTnt tnt : tntList) {
                tnt.setFuse(1000);
                tnt.discard();
                if (AnimusConfig.rituals.cullingDebug.get()) {
                    System.out.println("Animus: [Ritual of Culling Debug]: Found TNT entity, killing");
                }
            }
        }

        int entityCount = 0;

        if (currentEssence < getRefreshCost() * list.size()) {
            network.causeNausea();
            if (AnimusConfig.rituals.cullingDebug.get()) {
                System.out.println("Animus: [Ritual of Culling Debug]: Culling MRS at " + ritualStone.getBlockPos() + " does not have sufficient LP from the owner");
            }
        } else {
            if (AnimusConfig.rituals.cullingDebug.get()) {
                System.out.println("Animus: [Ritual of Culling Debug]: Starting culling for loop for MRS at " + ritualStone.getBlockPos());
            }

            for (LivingEntity livingEntity : list) {
                // Skip players with more than 4 health
                if (livingEntity instanceof Player && livingEntity.getHealth() > 4) {
                    continue;
                }

                // Check for potion effects (cursed earth spawned mobs have effects)
                Collection<MobEffectInstance> effects = livingEntity.getActiveEffects();

                if (effects.isEmpty() || AnimusConfig.general.canKillBuffedMobs.get()) {
                    BlockPos at = livingEntity.blockPosition();
                    boolean isNonBoss = !livingEntity.canChangeDimensions();

                    // Skip Gaia Guardian (Botania boss)
                    if (livingEntity.getName().getString().contains("Gaia")) {
                        continue;
                    }

                    // Silence entity
                    livingEntity.setSilent(true);

                    if (AnimusConfig.rituals.cullingDebug.get()) {
                        System.out.println("Animus: [Ritual of Culling Debug]: MRS at " + ritualStone.getBlockPos() + " Found entity, killing");
                    }

                    float damage = Float.MAX_VALUE;

                    // Special handling for bosses
                    if (AnimusConfig.rituals.killWither.get() && !isNonBoss && currentAmount > 99
                        && (currentEssence >= AnimusConfig.rituals.witherCost.get() + (getRefreshCost() * list.size()))) {

                        livingEntity.setInvulnerable(false);

                        if (livingEntity instanceof WitherBoss wither) {
                            wither.setInvulTime(0);
                        }
                    }

                    result = livingEntity.hurt(culled, damage);

                    if (result) {
                        entityCount++;
                        tileAltar.sacrificialDaggerCall(amount, true);

                        if (!isNonBoss) {
                            // Boss kill - high LP cost
                            network.syphon(new SoulTicket(
                                Component.translatable(Constants.Localizations.Text.TICKET_CULLING),
                                AnimusConfig.rituals.witherCost.get()
                            ), false);
                        } else {
                            // Regular mob - add to will buffer
                            double modifier = 0.5;
                            if (livingEntity instanceof Animal) {
                                modifier = 2.0;
                            }
                            willBuffer += modifier * Math.min(15.0, livingEntity.getMaxHealth());
                        }

                        // Spawn particles
                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(
                                ParticleTypes.PORTAL,
                                at.getX() + 0.5,
                                at.getY() + 0.5,
                                at.getZ() + 0.5,
                                rand.nextInt(4),
                                (rand.nextDouble() - 0.5D) * 2.0D,
                                rand.nextDouble(),
                                (rand.nextDouble() - 0.5D) * 2.0D,
                                0.1
                            );
                        }

                        level.playSound(null, at, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }

            // Consume LP for kills
            network.syphon(new SoulTicket(
                Component.translatable(Constants.Localizations.Text.TICKET_CULLING),
                getRefreshCost() * entityCount
            ), false);

            // TODO: Integrate demon will generation when available
            // double drainAmount = Math.min(maxWill - currentAmount, Math.min(entityCount / 2, 10));
            // if (rand.nextInt(30) == 0) { // 3% chance per cycle to generate destructive will
            //     double filled = WorldDemonWillHandler.fillWillToMaximum(level, pos, type, drainAmount, maxWill, false);
            //     if (filled > 0) {
            //         WorldDemonWillHandler.fillWillToMaximum(level, pos, type, filled, maxWill, true);
            //     }
            // }
        }
    }

    @Override
    public int getRefreshCost() {
        return 75;
    }

    @Override
    public int getRefreshTime() {
        return 25;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 1, 0, 1, RitualType.EnumRuneType.FIRE);
        addRune(components, -1, 0, 1, RitualType.EnumRuneType.FIRE);
        addRune(components, 1, 0, -1, RitualType.EnumRuneType.FIRE);
        addRune(components, -1, 0, -1, RitualType.EnumRuneType.FIRE);
        addRune(components, 2, -1, 2, RitualType.EnumRuneType.DUSK);
        addRune(components, 2, -1, -2, RitualType.EnumRuneType.DUSK);
        addRune(components, -2, -1, 2, RitualType.EnumRuneType.DUSK);
        addRune(components, -2, -1, -2, RitualType.EnumRuneType.DUSK);
        addRune(components, 0, -1, 2, RitualType.EnumRuneType.DUSK);
        addRune(components, 2, -1, 0, RitualType.EnumRuneType.DUSK);
        addRune(components, 0, -1, -2, RitualType.EnumRuneType.DUSK);
        addRune(components, -2, -1, 0, RitualType.EnumRuneType.DUSK);
        addRune(components, -3, -1, -3, RitualType.EnumRuneType.DUSK);
        addRune(components, 3, -1, -3, RitualType.EnumRuneType.DUSK);
        addRune(components, -3, -1, 3, RitualType.EnumRuneType.DUSK);
        addRune(components, 3, -1, 3, RitualType.EnumRuneType.DUSK);
        addRune(components, 2, -1, 4, RitualType.EnumRuneType.DUSK);
        addRune(components, 4, -1, 2, RitualType.EnumRuneType.DUSK);
        addRune(components, -2, -1, 4, RitualType.EnumRuneType.DUSK);
        addRune(components, 4, -1, -2, RitualType.EnumRuneType.DUSK);
        addRune(components, 2, -1, -4, RitualType.EnumRuneType.DUSK);
        addRune(components, -4, -1, 2, RitualType.EnumRuneType.DUSK);
        addRune(components, -2, -1, -4, RitualType.EnumRuneType.DUSK);
        addRune(components, -4, -1, -2, RitualType.EnumRuneType.DUSK);
        addRune(components, 1, 0, 4, RitualType.EnumRuneType.DUSK);
        addRune(components, 4, 0, 1, RitualType.EnumRuneType.DUSK);
        addRune(components, 1, 0, -4, RitualType.EnumRuneType.DUSK);
        addRune(components, -4, 0, 1, RitualType.EnumRuneType.DUSK);
        addRune(components, -1, 0, 4, RitualType.EnumRuneType.DUSK);
        addRune(components, 4, 0, -1, RitualType.EnumRuneType.DUSK);
        addRune(components, -1, 0, -4, RitualType.EnumRuneType.DUSK);
        addRune(components, -4, 0, -1, RitualType.EnumRuneType.DUSK);
        addRune(components, 4, 1, 0, RitualType.EnumRuneType.DUSK);
        addRune(components, 0, 1, 4, RitualType.EnumRuneType.DUSK);
        addRune(components, -4, 1, 0, RitualType.EnumRuneType.DUSK);
        addRune(components, 0, 1, -4, RitualType.EnumRuneType.DUSK);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualCulling();
    }
}
