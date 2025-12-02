package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandler;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.common.item.ItemBloodOrb;
import wayoftime.bloodmagic.core.data.Binding;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.demonaura.WorldDemonWillHandler;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.EnumRuneType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.*;
import java.util.function.Consumer;

/**
 * Ritual of the Steadfast Heart - Grants Absorption to players
 * Provides increasingly powerful absorption effect to players in range
 * Can also buff players remotely via bound blood orbs in a chest above the ritual
 * Also generates Steadfast demon will
 * Activation Cost: 20000 LP
 * Refresh Cost: 100 LP per player (nearby or remote)
 * Refresh Time: Configurable (default 60 ticks = 3 seconds)
 * Range: Configurable (default 128 blocks)
 */
@RitualRegister(Constants.Rituals.STEADFAST)
public class RitualSteadfastHeart extends Ritual {
    public static final String EFFECT_RANGE = "effect";
    public final int maxWill = 100;
    public double willBuffer = 0;

    public RitualSteadfastHeart() {
        super(Constants.Rituals.STEADFAST, 0, 20000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.STEADFAST);

        // Use config value for range (default 128 blocks)
        int range = AnimusConfig.rituals.steadfastHeartRange.get();
        int halfRange = range / 2;
        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-halfRange, -halfRange, -halfRange), range));
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, range, range);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        willBuffer = tag.getDouble("willBuffer");
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putDouble("willBuffer", willBuffer);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        SoulNetwork network = NetworkHelper.getSoulNetwork(mrs.getOwner());
        if (network == null) {
            return;
        }

        Level level = mrs.getWorldObj();
        if (level.isClientSide) {
            return;
        }

        BlockPos pos = mrs.getMasterBlockPos();

        // Get current steadfast demon will
        EnumDemonWillType type = EnumDemonWillType.STEADFAST;
        double currentAmount = WorldDemonWillHandler.getCurrentWill(level, pos, type);

        // Track players who have been buffed to avoid duplicates
        Set<UUID> buffedPlayers = new HashSet<>();

        // Get all players in range
        AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
        AABB range = effectRange.getAABB(pos);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, range);

        int entityCount = 0;
        MobEffect absorbEffect = MobEffects.ABSORPTION;

        // Buff nearby players
        for (LivingEntity entity : entities) {
            // Only affect real players
            if (!(entity instanceof Player) || entity instanceof FakePlayer) {
                continue;
            }

            Player player = (Player) entity;
            UUID playerUUID = player.getUUID();

            // Skip if already buffed
            if (buffedPlayers.contains(playerUUID)) {
                continue;
            }

            applyAbsorptionBuff(player, absorbEffect);
            buffedPlayers.add(playerUUID);
            entityCount++;
        }

        // Check for chest above ritual stone with bound blood orbs
        BlockPos chestPos = pos.above();
        BlockEntity chestTile = level.getBlockEntity(chestPos);

        if (chestTile != null) {
            IItemHandler handler = chestTile.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
            if (handler != null) {
                // Scan chest for bound blood orbs
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);

                    if (stack.isEmpty() || !(stack.getItem() instanceof ItemBloodOrb)) {
                        continue;
                    }

                    // Get the player UUID bound to this orb
                    IBindable bindable = (IBindable) stack.getItem();
                    Binding binding = bindable.getBinding(stack);
                    if (binding == null) {
                        continue;
                    }

                    UUID orbOwner = binding.getOwnerId();
                    if (orbOwner == null) {
                        continue;
                    }

                    // Skip if this player was already buffed
                    if (buffedPlayers.contains(orbOwner)) {
                        continue;
                    }

                    // Check if player is online
                    ServerPlayer targetPlayer = level.getServer().getPlayerList().getPlayer(orbOwner);
                    if (targetPlayer == null || targetPlayer instanceof FakePlayer) {
                        continue;
                    }

                    // Apply buff remotely
                    applyAbsorptionBuff(targetPlayer, absorbEffect);
                    buffedPlayers.add(orbOwner);
                    entityCount++;
                }
            }
        }

        // Consume LP based on number of players affected
        SoulTicket ticket = new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_STEADFAST),
            getRefreshCost() * entityCount
        );
        network.syphon(ticket, false);

        // Generate steadfast demon will
        double drainAmount = 2 * Math.min((maxWill - currentAmount) + 1, Math.min(entityCount / 2, 10));
        double filled = WorldDemonWillHandler.fillWillToMaximum(level, pos, type, drainAmount, maxWill, false);
        if (filled > 0) {
            WorldDemonWillHandler.fillWillToMaximum(level, pos, type, filled, maxWill, true);
        }
    }

    /**
     * Apply absorption buff to a player
     */
    private void applyAbsorptionBuff(Player player, MobEffect absorbEffect) {
        // Get existing absorption effect
        MobEffectInstance existingEffect = player.getEffect(absorbEffect);
        int currentDuration = 0;

        if (existingEffect != null) {
            currentDuration = existingEffect.getDuration();
            player.removeEffect(absorbEffect);
        }

        // Calculate new duration and amplifier
        int newDuration = Math.min(((currentDuration + 800) * 2), 30000);
        int maxAmplifier = AnimusConfig.rituals.steadfastHeartMaxAmplifier.get();
        int amplifier = Math.min((5 * (1 + (newDuration + 60)) / 36000), maxAmplifier);

        // Apply new absorption effect
        player.addEffect(new MobEffectInstance(
            absorbEffect,
            newDuration,
            amplifier,
            true,
            false
        ));
    }

    @Override
    public int getRefreshCost() {
        return 100;
    }

    @Override
    public int getRefreshTime() {
        return AnimusConfig.rituals.steadfastHeartRefreshTime.get();
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 1, 0, 1, EnumRuneType.EARTH);
        addRune(components, -1, 0, 1, EnumRuneType.WATER);
        addRune(components, 1, 0, -1, EnumRuneType.EARTH);
        addRune(components, -1, 0, -1, EnumRuneType.WATER);
        addRune(components, 0, -1, 0, EnumRuneType.AIR);
        addRune(components, 2, -1, 2, EnumRuneType.EARTH);
        addRune(components, 2, -1, -2, EnumRuneType.EARTH);
        addRune(components, -2, -1, 2, EnumRuneType.WATER);
        addRune(components, -2, -1, -2, EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualSteadfastHeart();
    }
}
