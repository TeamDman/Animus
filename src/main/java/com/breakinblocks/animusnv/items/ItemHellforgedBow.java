package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.entities.EntityHellforgedArrow;
import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.animusnv.util.SpiritusTypeHelper;
import com.breakinblocks.animusnv.util.WillWeaponStats;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Hellforged Bow - An advanced EV-powered bow with charged shot mechanics
 *
 * - Consumes EV instead of Spiritus to fire
 * - Base EV cost to fire, plus EV per tick while charging beyond normal draw
 * - Still empowered by Spiritus for type-based effects
 * - Extended charge time up to 3.5 seconds for maximum damage
 * - Execute effect at full charge: instant kill targets below threshold
 * - Piercing arrows that go through all targets
 */
public class ItemHellforgedBow extends BowItem {
    // Normal bow draw time in ticks (~20 ticks for full power)
    public static final int NORMAL_DRAW_TICKS = 20;

    // Default config values (used before config loads)
    public static final int DEFAULT_BASE_EV_COST = 5;
    public static final int DEFAULT_EV_PER_TICK = 50;
    public static final int DEFAULT_MAX_CHARGE_TICKS = 70;
    public static final double DEFAULT_MAX_DAMAGE = 40.0;
    public static final double DEFAULT_EXECUTE_THRESHOLD = 0.15;
    public static final int EV_PER_REPAIR = 100;

    public ItemHellforgedBow() {
        super(new Item.Properties()
            .stacksTo(1)
            .durability(750)
            .rarity(Rarity.EPIC));
    }

    public static int getBaseEvCost() {
        try {
            return AnimusConfig.weapons.hellforgedBowBaseLpCost.get();
        } catch (IllegalStateException e) {
            return DEFAULT_BASE_EV_COST;
        }
    }

    public static int getEvPerTick() {
        try {
            return AnimusConfig.weapons.hellforgedBowLpPerTick.get();
        } catch (IllegalStateException e) {
            return DEFAULT_EV_PER_TICK;
        }
    }

    public static int getMaxChargeTicks() {
        try {
            return AnimusConfig.weapons.hellforgedBowMaxChargeTicks.get();
        } catch (IllegalStateException e) {
            return DEFAULT_MAX_CHARGE_TICKS;
        }
    }

    public static double getMaxArrowDamage() {
        try {
            return AnimusConfig.weapons.hellforgedBowMaxDamage.get();
        } catch (IllegalStateException e) {
            return DEFAULT_MAX_DAMAGE;
        }
    }

    public static double getExecuteThreshold() {
        try {
            return AnimusConfig.weapons.hellforgedBowExecuteThreshold.get();
        } catch (IllegalStateException e) {
            return DEFAULT_EXECUTE_THRESHOLD;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HELLFORGED_BOW_FLAVOUR)
            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));

        String ownerName = getBindingOwnerName(stack);
        if (ownerName != null) {
            tooltip.add(Component.translatable("tooltip.animusnv.bound_to")
                .append(Component.literal(ownerName))
                .withStyle(ChatFormatting.DARK_RED));
        } else {
            tooltip.add(Component.translatable("tooltip.animusnv.unbound_bind")
                .withStyle(ChatFormatting.GRAY));
        }

        SpiritusType type = getCurrentType(stack);
        if (type != SpiritusType.DEFAULT) {
            tooltip.add(Component.translatable("tooltip.animusnv.hellforged_bow.will_type", type.name().toLowerCase())
                .withStyle(ChatFormatting.DARK_PURPLE));
        }

        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HELLFORGED_BOW_INFO)
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HELLFORGED_BOW_EV_COST, getBaseEvCost())
            .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HELLFORGED_BOW_CHARGE)
            .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HELLFORGED_BOW_EXECUTE,
            (int)(getExecuteThreshold() * 100))
            .withStyle(ChatFormatting.RED));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (!(entity instanceof Player player)) {
            return;
        }

        int useDuration = this.getUseDuration(stack, entity) - remainingTicks;

        if (!level.isClientSide() && useDuration > NORMAL_DRAW_TICKS && useDuration <= getMaxChargeTicks()) {
            if (!drainEV(player, stack, getEvPerTick())) {
                player.releaseUsingItem();
            }
        }

        if (useDuration >= getMaxChargeTicks()) {
            if (level instanceof ServerLevel serverLevel) {
                double x = player.getX();
                double y = player.getY() + player.getEyeHeight() - 0.2;
                double z = player.getZ();

                // Soul fire flame particles
                serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    x, y, z,
                    1,
                    0.3, 0.2, 0.3,
                    0.02
                );

                // Occasional crimson spore (blood-like) particles
                if (useDuration % 5 == 0) {
                    serverLevel.sendParticles(
                        ParticleTypes.CRIMSON_SPORE,
                        x, y, z,
                        3,
                        0.4, 0.3, 0.4,
                        0.01
                    );
                }
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        UUID ownerId = getBindingOwnerId(stack);
        if (ownerId == null) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.animus.hellforged_bow.not_bound")
                        .withStyle(ChatFormatting.RED),
                    true
                );
            }
            return;
        }

        int useDuration = this.getUseDuration(stack, entity) - timeLeft;
        float power = getPowerForTime(useDuration);

        if (power < 0.1F) {
            return;
        }

        IAnima network = getNetworkForBinding(player, stack);
        if (network == null || network.getCurrentEV() < getBaseEvCost()) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.animus.hellforged_bow.out_of_ev")
                        .withStyle(ChatFormatting.RED),
                    true
                );
            }
            return;
        }

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            AnimaTicket ticket = AnimaTicket.create(getBaseEvCost());
            network.syphonAndDamage(player, ticket);

            SpiritusType willType = getCurrentType(stack);
            double willAmount = NeoVitaeAPI.getInstance().getPlayerWillHandler().getTotalSpiritus(willType, player);
            int willLevel = getLevel(willAmount);

            float chargeMultiplier = 0.0f;
            if (useDuration > NORMAL_DRAW_TICKS) {
                int extraChargeTicks = Math.min(useDuration - NORMAL_DRAW_TICKS, getMaxChargeTicks() - NORMAL_DRAW_TICKS);
                chargeMultiplier = (float) extraChargeTicks / (getMaxChargeTicks() - NORMAL_DRAW_TICKS);
            }

            double baseDamage = 2.0 + (power * 2.0);
            double chargeBonusDamage = chargeMultiplier * (getMaxArrowDamage() - baseDamage);
            double totalDamage = baseDamage + chargeBonusDamage;

            EntityHellforgedArrow arrow = new EntityHellforgedArrow(level, player);
            arrow.setWillType(willType);
            arrow.setWillLevel(willLevel);
            arrow.setBaseDamage(totalDamage);
            arrow.setChargeMultiplier(chargeMultiplier);
            arrow.setExecuteThreshold(chargeMultiplier >= 1.0f ? getExecuteThreshold() : 0.0);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

            int powerEnchant = stack.getEnchantmentLevel(serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.POWER));
            if (powerEnchant > 0) {
                arrow.setBaseDamage(arrow.getBaseDamage() + (double) powerEnchant * 0.5D + 0.5D);
            }

            int flameEnchant = stack.getEnchantmentLevel(serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.FLAME));
            if (flameEnchant > 0) {
                arrow.setRemainingFireTicks(100);
            }

            if (power >= 1.0F) {
                arrow.setCritArrow(true);
            }

            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

            level.addFreshEntity(arrow);

            float pitch = 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F;
            if (chargeMultiplier > 0.5f) {
                pitch += chargeMultiplier * 0.3f;
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, pitch);

            stack.hurtAndBreak(1, serverLevel, player, (item) -> {});
            tryRepairWithEV(stack, network, player);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        UUID ownerId = getBindingOwnerId(stack);
        if (ownerId == null) {
            if (!level.isClientSide) {
                bindToPlayer(stack, player);
                player.displayClientMessage(
                    Component.translatable("message.animus.hellforged_bow.bound")
                        .withStyle(ChatFormatting.AQUA),
                    true
                );
            }
            return InteractionResultHolder.consume(stack);
        }

        IAnima network = getNetworkForBinding(player, stack);
        if (network == null || network.getCurrentEV() < getBaseEvCost()) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.animus.hellforged_bow.out_of_ev")
                        .withStyle(ChatFormatting.RED),
                    true
                );
            }
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return (stack) -> false;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }

    public void bindToPlayer(ItemStack stack, Player player) {
        stack.set(AnimusDataComponents.BINDING_OWNER_UUID.get(), player.getUUID().toString());
        stack.set(AnimusDataComponents.BINDING_OWNER_NAME.get(), player.getName().getString());
    }

    public UUID getBindingOwnerId(ItemStack stack) {
        String uuidStr = stack.get(AnimusDataComponents.BINDING_OWNER_UUID.get());
        if (uuidStr != null) {
            try {
                return UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    public String getBindingOwnerName(ItemStack stack) {
        return stack.get(AnimusDataComponents.BINDING_OWNER_NAME.get());
    }

    public SpiritusType getCurrentType(ItemStack stack) {
        return SpiritusTypeHelper.getCurrentType(stack);
    }

    public void setCurrentType(ItemStack stack, SpiritusType type) {
        SpiritusTypeHelper.setCurrentType(stack, type);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (entity instanceof Player player) {
            SpiritusType newType = SpiritusTypeHelper.findSpiritusType(player);
            if (newType != getCurrentType(stack)) {
                setCurrentType(stack, newType);
            }
        }
    }

    public static int getLevel(double willAmount) {
        return WillWeaponStats.getLevel(willAmount);
    }

    private IAnima getNetworkForBinding(Player player, ItemStack stack) {
        UUID ownerId = getBindingOwnerId(stack);
        if (ownerId == null) {
            return null;
        }
        return NeoVitaeAPI.getInstance().getAnima(ownerId);
    }

    private void tryRepairWithEV(ItemStack stack, IAnima network, Player player) {
        int damage = stack.getDamageValue();
        if (damage <= 0) {
            return;
        }

        int availableEV = network.getCurrentEV();
        int maxRepair = availableEV / EV_PER_REPAIR;
        int actualRepair = Math.min(damage, maxRepair);

        if (actualRepair > 0) {
            int evCost = actualRepair * EV_PER_REPAIR;
            AnimaTicket ticket = AnimaTicket.create(evCost);
            network.syphonAndDamage(player, ticket);
            stack.setDamageValue(damage - actualRepair);
        }
    }

    private boolean drainEV(Player player, ItemStack stack, int amount) {
        UUID ownerId = getBindingOwnerId(stack);
        if (ownerId == null) {
            return false;
        }

        return AnimusRitualHelper.drainEV(player, ownerId, amount);
    }

    public List<ItemStack> getRandomSpiritusDrop(LivingEntity killedEntity, LivingEntity attackingEntity,
                                                   ItemStack stack, int looting) {
        return new ArrayList<>();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || (getBindingOwnerId(stack) != null && getCurrentType(stack) != SpiritusType.DEFAULT);
    }
}
