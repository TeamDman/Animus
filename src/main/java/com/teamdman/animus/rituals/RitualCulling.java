package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.AnimusStartupConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.util.AnimusFakePlayer;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import com.breakinblocks.neovitae.common.datacomponent.EnumWillType;
import com.breakinblocks.neovitae.common.blockentity.BloodAltarTile;
import com.breakinblocks.neovitae.common.datacomponent.SoulNetwork;
import com.breakinblocks.neovitae.api.soul.SoulTicket;
import com.breakinblocks.neovitae.will.WorldDemonWillHandler;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.common.datamap.EntitySacrificeHelper;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.EnumRuneType;
import com.breakinblocks.neovitae.util.helper.SoulNetworkHelper;

import java.util.*;
import java.util.function.Consumer;

/**
 * Ritual of Culling - Kills entities in range
 * Powerful ritual that kills non-boss entities and can kill bosses with demon will
 * Also destroys primed TNT if configured
 * Activation Cost: 50000 LP
 * Refresh Cost: 75 LP per entity
 * Refresh Time: 25 ticks
 * Range: Configurable (default 10 blocks horizontal, 10 blocks vertical above AND below stone)
 * LP per Kill: Determined by NeoVitae's entity_sacrifice_value datamap (entity's full health worth)
 */
public class RitualCulling extends Ritual {
    public static final String ALTAR_RANGE = "altar";
    public static final String EFFECT_RANGE = "effect";

    public final int maxWill = 100;
    public final Random rand = new Random();
    public BlockPos altarOffsetPos = BlockPos.ZERO;
    public double crystalBuffer = 0;
    public int reagentDrain = 2;
    public boolean result = false;
    public double willBuffer = 0;
    public HashMap<String, Double> willMap = new HashMap<>();

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
        willBuffer = tag.getDouble(Constants.NBT.CULLING_BUFFER_WILL);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putDouble(Constants.NBT.CULLING_BUFFER_WILL, willBuffer);
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
        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(ritualStone.getOwner());
        if (network == null) {
            return;
        }

        int currentEssence = network.getCurrentEssence();
        Level level = ritualStone.getWorldObj();
        BlockPos pos = ritualStone.getMasterBlockPos();

        if (level.isClientSide) {
            return;
        }

        EnumWillType type = EnumWillType.DESTRUCTIVE;
        double currentAmount = WorldDemonWillHandler.getCurrentWill(level, pos, type);

        // Raw demon will enables player-like kills (mob drops as if killed by player)
        double rawWillAmount = WorldDemonWillHandler.getCurrentWill(level, pos, EnumWillType.DEFAULT);
        boolean usePlayerKill = AnimusConfig.rituals.cullingPlayerKillDrops.get() && rawWillAmount >= 1.0;

        BloodAltarTile tileAltar = AnimusUtil.getNearbyAltar(level, getBlockRange(ALTAR_RANGE), pos, altarOffsetPos);
        if (tileAltar == null) {
            if (AnimusConfig.rituals.cullingDebug.get()) {
                System.out.println("Animus: [Ritual of Culling Debug]: No valid altar found within altar range for MRS at " + ritualStone.getMasterBlockPos());
            }
            return;
        }
        altarOffsetPos = tileAltar.getBlockPos();

        AreaDescriptor damageRange = getBlockRange(EFFECT_RANGE);
        AABB range = damageRange.getAABB(pos);

        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, range);

        if (AnimusConfig.rituals.cullingDebug.get()) {
            System.out.println("Animus: [Ritual of Culling Debug]: Starting Ritual perform for MRS at " + ritualStone.getMasterBlockPos());
            System.out.println("Animus: [Ritual of Culling Debug]: Range AABB: " + range);
            System.out.println("Animus: [Ritual of Culling Debug]: Found " + list.size() + " entities in range");
        }

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
            if (AnimusConfig.rituals.cullingDebug.get()) {
                System.out.println("Animus: [Ritual of Culling Debug]: Culling MRS at " + ritualStone.getMasterBlockPos() + " does not have sufficient LP from the owner");
            }
        } else {
            if (AnimusConfig.rituals.cullingDebug.get()) {
                System.out.println("Animus: [Ritual of Culling Debug]: Starting culling for loop for MRS at " + ritualStone.getMasterBlockPos());
            }

            for (LivingEntity livingEntity : list) {
                if (AnimusConfig.rituals.cullingDebug.get()) {
                    System.out.println("Animus: [Ritual of Culling Debug]: Processing entity: " + livingEntity.getName().getString() +
                        " at " + livingEntity.blockPosition() + " (Type: " + livingEntity.getType() + ")");
                    System.out.println("Animus: [Ritual of Culling Debug]:   Health: " + livingEntity.getHealth() + "/" + livingEntity.getMaxHealth());
                }

                if (livingEntity instanceof Player && livingEntity.getHealth() > 4) {
                    if (AnimusConfig.rituals.cullingDebug.get()) {
                        System.out.println("Animus: [Ritual of Culling Debug]:   SKIPPED - Player with health > 4");
                    }
                    continue;
                }

                // Mobs with potion effects (e.g. from cursed earth spawners) are skipped unless config allows
                Collection<MobEffectInstance> effects = livingEntity.getActiveEffects();

                if (AnimusConfig.rituals.cullingDebug.get()) {
                    System.out.println("Animus: [Ritual of Culling Debug]:   Active effects: " + effects.size());
                    if (!effects.isEmpty()) {
                        for (MobEffectInstance effect : effects) {
                            System.out.println("Animus: [Ritual of Culling Debug]:     - " + effect.getEffect().value().getDescriptionId());
                        }
                    }
                    System.out.println("Animus: [Ritual of Culling Debug]:   canKillBuffedMobs config: " + AnimusConfig.general.canKillBuffedMobs.get());
                }

                if (effects.isEmpty() || AnimusConfig.general.canKillBuffedMobs.get()) {
                    BlockPos at = livingEntity.blockPosition();
                    boolean isBoss = livingEntity.isInvulnerable()
                                     || livingEntity.getType() == net.minecraft.world.entity.EntityType.WITHER
                                     || livingEntity.getType() == net.minecraft.world.entity.EntityType.ENDER_DRAGON
                                     || livingEntity.getType().is(net.minecraft.tags.EntityTypeTags.RAIDERS);

                    if (AnimusConfig.rituals.cullingDebug.get()) {
                        System.out.println("Animus: [Ritual of Culling Debug]:   Is boss: " + isBoss);
                        System.out.println("Animus: [Ritual of Culling Debug]:   Is invulnerable: " + livingEntity.isInvulnerable());
                    }

                    if (livingEntity.getType().is(Constants.Tags.DISALLOW_CULLING)) {
                        if (AnimusConfig.rituals.cullingDebug.get()) {
                            System.out.println("Animus: [Ritual of Culling Debug]:   SKIPPED - Entity in disallow_culling tag");
                        }
                        continue;
                    }

                    livingEntity.setSilent(true);

                    if (AnimusConfig.rituals.cullingDebug.get()) {
                        System.out.println("Animus: [Ritual of Culling Debug]:   ATTEMPTING TO KILL entity");
                    }

                    float damage = Float.MAX_VALUE;

                    // Bosses require 100+ destructive demon will and extra LP to kill
                    if (AnimusConfig.rituals.killBoss.get() && isBoss && currentAmount > 99
                        && (currentEssence >= AnimusConfig.rituals.bossCost.get() + (getRefreshCost() * list.size()))) {

                        if (AnimusConfig.rituals.cullingDebug.get()) {
                            System.out.println("Animus: [Ritual of Culling Debug]:   Boss kill conditions met - making vulnerable");
                            System.out.println("Animus: [Ritual of Culling Debug]:     Current demon will: " + currentAmount);
                            System.out.println("Animus: [Ritual of Culling Debug]:     Boss cost: " + AnimusConfig.rituals.bossCost.get());
                        }

                        livingEntity.setInvulnerable(false);
                    } else if (isBoss) {
                        if (AnimusConfig.rituals.cullingDebug.get()) {
                            System.out.println("Animus: [Ritual of Culling Debug]:   Boss kill conditions NOT met:");
                            System.out.println("Animus: [Ritual of Culling Debug]:     killBoss config: " + AnimusConfig.rituals.killBoss.get());
                            System.out.println("Animus: [Ritual of Culling Debug]:     Current demon will: " + currentAmount + " (need > 99)");
                            System.out.println("Animus: [Ritual of Culling Debug]:     Current essence: " + currentEssence);
                            System.out.println("Animus: [Ritual of Culling Debug]:     Required essence: " + (AnimusConfig.rituals.bossCost.get() + (getRefreshCost() * list.size())));
                        }
                    }

                    if (AnimusConfig.rituals.cullingDebug.get()) {
                        System.out.println("Animus: [Ritual of Culling Debug]:   Applying damage: " + damage);
                        System.out.println("Animus: [Ritual of Culling Debug]:   Using player kill: " + usePlayerKill);
                    }

                    if (usePlayerKill && level instanceof ServerLevel serverLevel) {
                        AnimusFakePlayer fakePlayer = AnimusFakePlayer.get(serverLevel, ritualStone.getOwner(), null);

                        ItemStack lootingSword = AnimusFakePlayer.createLootingSword(serverLevel, pos);
                        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, lootingSword);
                        fakePlayer.setPos(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());

                        // Set lastHurtByPlayer so loot tables treat this as a player kill (enables player-only drops like blaze rods)
                        try {
                            java.lang.reflect.Field lastHurtByPlayerField = LivingEntity.class.getDeclaredField("lastHurtByPlayer");
                            lastHurtByPlayerField.setAccessible(true);
                            lastHurtByPlayerField.set(livingEntity, fakePlayer);

                            java.lang.reflect.Field lastHurtByPlayerTimeField = LivingEntity.class.getDeclaredField("lastHurtByPlayerTime");
                            lastHurtByPlayerTimeField.setAccessible(true);
                            lastHurtByPlayerTimeField.setInt(livingEntity, 100);
                        } catch (Exception e) {
                            // Fall back to obfuscated field names
                            try {
                                java.lang.reflect.Field lastHurtByPlayerField = LivingEntity.class.getDeclaredField("f_20889_");
                                lastHurtByPlayerField.setAccessible(true);
                                lastHurtByPlayerField.set(livingEntity, fakePlayer);

                                java.lang.reflect.Field lastHurtByPlayerTimeField = LivingEntity.class.getDeclaredField("f_20890_");
                                lastHurtByPlayerTimeField.setAccessible(true);
                                lastHurtByPlayerTimeField.setInt(livingEntity, 100);
                            } catch (Exception e2) {
                                if (AnimusConfig.rituals.cullingDebug.get()) {
                                    System.out.println("Animus: [Ritual of Culling Debug]: Failed to set lastHurtByPlayer fields: " + e2.getMessage());
                                }
                            }
                        }

                        DamageSource playerDamage = level.damageSources().playerAttack(fakePlayer);
                        result = livingEntity.hurt(playerDamage, damage);

                        if (result && rand.nextDouble() < AnimusConfig.rituals.cullingWillConsumeChance.get()) {
                            WorldDemonWillHandler.drainWillFromChunk(level, pos, EnumWillType.DEFAULT, 1.0);
                            if (AnimusConfig.rituals.cullingDebug.get()) {
                                System.out.println("Animus: [Ritual of Culling Debug]:   Consumed 1 raw demon will");
                            }
                        }
                    } else {
                        result = livingEntity.hurt(level.damageSources().genericKill(), damage);
                    }

                    if (AnimusConfig.rituals.cullingDebug.get()) {
                        System.out.println("Animus: [Ritual of Culling Debug]:   Damage result: " + result);
                        System.out.println("Animus: [Ritual of Culling Debug]:   Entity alive after damage: " + livingEntity.isAlive());
                        System.out.println("Animus: [Ritual of Culling Debug]:   Entity removed: " + livingEntity.isRemoved());
                    }

                    if (result) {
                        entityCount++;
                        int lpPerKill = EntitySacrificeHelper.calculateLP(livingEntity, livingEntity.getMaxHealth());
                        tileAltar.addSacrificeLP(lpPerKill, true);

                        if (AnimusConfig.rituals.cullingDebug.get()) {
                            System.out.println("Animus: [Ritual of Culling Debug]:   LP generated: " + lpPerKill +
                                " (LP/dmg: " + EntitySacrificeHelper.getLpPerDamage(livingEntity) +
                                ", maxHP: " + livingEntity.getMaxHealth() + ")");
                        }

                        if (isBoss) {
                            network.syphon(SoulTicket.create(AnimusConfig.rituals.bossCost.get()));
                        } else {
                            // Animals generate more will than other mobs
                            double modifier = 0.5;
                            if (livingEntity instanceof Animal) {
                                modifier = 2.0;
                            }
                            willBuffer += modifier * Math.min(15.0, livingEntity.getMaxHealth());
                        }

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

                        level.playSound(null, at, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                } else {
                    if (AnimusConfig.rituals.cullingDebug.get()) {
                        System.out.println("Animus: [Ritual of Culling Debug]:   SKIPPED - Entity has potion effects and canKillBuffedMobs is false");
                    }
                }
            }

            if (AnimusConfig.rituals.cullingDebug.get()) {
                System.out.println("Animus: [Ritual of Culling Debug]: Finished culling loop - killed " + entityCount + " entities");
            }

            network.syphon(SoulTicket.create(getRefreshCost() * entityCount));

            // ~3% chance per cycle to generate destructive demon will
            double addAmount = Math.min(maxWill - currentAmount, Math.min(entityCount / 2.0, 10));
            if (rand.nextInt(30) == 0 && addAmount > 0) {
                WorldDemonWillHandler.addWillToChunk(level, pos, type, addAmount);
            }
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
        addRune(components, 1, 0, 1, EnumRuneType.FIRE);
        addRune(components, -1, 0, 1, EnumRuneType.FIRE);
        addRune(components, 1, 0, -1, EnumRuneType.FIRE);
        addRune(components, -1, 0, -1, EnumRuneType.FIRE);
        addRune(components, 2, -1, 2, EnumRuneType.DUSK);
        addRune(components, 2, -1, -2, EnumRuneType.DUSK);
        addRune(components, -2, -1, 2, EnumRuneType.DUSK);
        addRune(components, -2, -1, -2, EnumRuneType.DUSK);
        addRune(components, 0, -1, 2, EnumRuneType.DUSK);
        addRune(components, 2, -1, 0, EnumRuneType.DUSK);
        addRune(components, 0, -1, -2, EnumRuneType.DUSK);
        addRune(components, -2, -1, 0, EnumRuneType.DUSK);
        addRune(components, -3, -1, -3, EnumRuneType.DUSK);
        addRune(components, 3, -1, -3, EnumRuneType.DUSK);
        addRune(components, -3, -1, 3, EnumRuneType.DUSK);
        addRune(components, 3, -1, 3, EnumRuneType.DUSK);
        addRune(components, 2, -1, 4, EnumRuneType.DUSK);
        addRune(components, 4, -1, 2, EnumRuneType.DUSK);
        addRune(components, -2, -1, 4, EnumRuneType.DUSK);
        addRune(components, 4, -1, -2, EnumRuneType.DUSK);
        addRune(components, 2, -1, -4, EnumRuneType.DUSK);
        addRune(components, -4, -1, 2, EnumRuneType.DUSK);
        addRune(components, -2, -1, -4, EnumRuneType.DUSK);
        addRune(components, -4, -1, -2, EnumRuneType.DUSK);
        addRune(components, 1, 0, 4, EnumRuneType.DUSK);
        addRune(components, 4, 0, 1, EnumRuneType.DUSK);
        addRune(components, 1, 0, -4, EnumRuneType.DUSK);
        addRune(components, -4, 0, 1, EnumRuneType.DUSK);
        addRune(components, -1, 0, 4, EnumRuneType.DUSK);
        addRune(components, 4, 0, -1, EnumRuneType.DUSK);
        addRune(components, -1, 0, -4, EnumRuneType.DUSK);
        addRune(components, -4, 0, -1, EnumRuneType.DUSK);
        addRune(components, 4, 1, 0, EnumRuneType.DUSK);
        addRune(components, 0, 1, 4, EnumRuneType.DUSK);
        addRune(components, -4, 1, 0, EnumRuneType.DUSK);
        addRune(components, 0, 1, -4, EnumRuneType.DUSK);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualCulling();
    }
}
