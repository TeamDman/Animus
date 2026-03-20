package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.items.IItemHandler;
import com.breakinblocks.neovitae.common.datacomponent.EnumWillType;
import com.breakinblocks.neovitae.common.item.IBindable;
import com.breakinblocks.neovitae.common.item.BloodOrbItem;
import com.breakinblocks.neovitae.common.datacomponent.Binding;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.ISoulNetwork;
import com.breakinblocks.neovitae.api.soul.SoulTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.api.will.IDemonWillHandler;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.EnumRuneType;

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
public class RitualSteadfastHeart extends Ritual {
    public static final String EFFECT_RANGE = "effect";
    public final int maxWill = 100;
    public double willBuffer = 0;

    public RitualSteadfastHeart() {
        super(Constants.Rituals.STEADFAST, 0, 20000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.STEADFAST);

        int range = 128;
        int halfRange = range / 2;
        addBlockRange(EFFECT_RANGE, RitualAreaDescriptors.symmetricCube(halfRange));
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
        ISoulNetwork network = NeoVitaeAPI.getInstance().getSoulNetwork(mrs.getOwner());
        if (network == null) {
            return;
        }

        Level level = mrs.getWorldObj();
        if (level.isClientSide) {
            return;
        }

        BlockPos pos = mrs.getMasterBlockPos();

        IDemonWillHandler willHandler = NeoVitaeAPI.getInstance().getDemonWillHandler();
        EnumWillType type = EnumWillType.STEADFAST;
        double currentAmount = willHandler.getCurrentWill(level, pos, type);

        Set<UUID> buffedPlayers = new HashSet<>();

        AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
        AABB range = effectRange.getAABB(pos);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, range);

        int entityCount = 0;
        Holder<net.minecraft.world.effect.MobEffect> absorbEffect = MobEffects.ABSORPTION;

        for (LivingEntity entity : entities) {
            if (!(entity instanceof Player) || entity instanceof FakePlayer) {
                continue;
            }

            Player player = (Player) entity;
            UUID playerUUID = player.getUUID();

            if (buffedPlayers.contains(playerUUID)) {
                continue;
            }

            applyAbsorptionBuff(player, absorbEffect);
            buffedPlayers.add(playerUUID);
            entityCount++;
        }

        // Buff remote players via bound blood orbs in chest above ritual
        BlockPos chestPos = pos.above();
        BlockEntity chestTile = level.getBlockEntity(chestPos);

        if (chestTile != null) {
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, chestPos, null);
            if (handler != null) {
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);

                    if (stack.isEmpty() || !(stack.getItem() instanceof BloodOrbItem)) {
                        continue;
                    }

                    IBindable bindable = (IBindable) stack.getItem();
                    Binding binding = bindable.getBinding(stack);
                    if (binding == null) {
                        continue;
                    }

                    UUID orbOwner = binding.uuid();
                    if (orbOwner == null) {
                        continue;
                    }

                    if (buffedPlayers.contains(orbOwner)) {
                        continue;
                    }

                    ServerPlayer targetPlayer = level.getServer().getPlayerList().getPlayer(orbOwner);
                    if (targetPlayer == null || targetPlayer instanceof FakePlayer) {
                        continue;
                    }

                    applyAbsorptionBuff(targetPlayer, absorbEffect);
                    buffedPlayers.add(orbOwner);
                    entityCount++;
                }
            }
        }

        SoulTicket ticket = SoulTicket.create(getRefreshCost() * entityCount);
        network.syphon(ticket);

        double addAmount = 2 * Math.min((maxWill - currentAmount) + 1, Math.min(entityCount / 2.0, 10));
        if (addAmount > 0) {
            willHandler.addWill(level, pos, type, addAmount);
        }
    }

    private void applyAbsorptionBuff(Player player, Holder<net.minecraft.world.effect.MobEffect> absorbEffect) {
        MobEffectInstance existingEffect = player.getEffect(absorbEffect);
        int currentDuration = 0;

        if (existingEffect != null) {
            currentDuration = existingEffect.getDuration();
            player.removeEffect(absorbEffect);
        }

        // Duration and amplifier scale up with continued exposure
        int newDuration = Math.min(((currentDuration + 800) * 2), 30000);
        int maxAmplifier = AnimusConfig.rituals.steadfastHeartMaxAmplifier.get();
        int amplifier = Math.min((5 * (1 + (newDuration + 60)) / 36000), maxAmplifier);

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
