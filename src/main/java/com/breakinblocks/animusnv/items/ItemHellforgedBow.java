package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.entities.EntityHellforgedArrow;
import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.animusnv.util.SpiritusTypeHelper;
import com.breakinblocks.animusnv.util.SpiritusWeaponStats;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
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

    public ItemHellforgedBow(Item.Properties props) {
        super(props
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
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.HELLFORGED_BOW_FLAVOUR)
            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));

        String ownerName = getBindingOwnerName(stack);
        if (ownerName != null) {
            tooltip.accept(Component.translatable("tooltip.animusnv.bound_to")
                .append(Component.literal(ownerName))
                .withStyle(ChatFormatting.DARK_RED));
        } else {
            tooltip.accept(Component.translatable("tooltip.animusnv.unbound_bind")
                .withStyle(ChatFormatting.GRAY));
        }

        SpiritusType type = getCurrentType(stack);
        if (type != SpiritusType.RAW) {
            tooltip.accept(Component.translatable("tooltip.animusnv.hellforged_bow.spiritus_type", type.name().toLowerCase())
                .withStyle(ChatFormatting.DARK_PURPLE));
        }

        tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.HELLFORGED_BOW_INFO)
            .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.HELLFORGED_BOW_EV_COST, getBaseEvCost())
            .withStyle(ChatFormatting.DARK_RED));
        tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.HELLFORGED_BOW_CHARGE)
            .withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.HELLFORGED_BOW_EXECUTE,
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
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        UUID ownerId = getBindingOwnerId(stack);
        if (ownerId == null) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(
                    Component.translatable("text.component.animusnv.hellforged_bow.not_bound")
                        .withStyle(ChatFormatting.RED));
            }
            return false;
        }

        int useDuration = this.getUseDuration(stack, entity) - timeLeft;
        float power = getPowerForTime(useDuration);

        if (power < 0.1F) {
            return false;
        }

        IAnima network = getNetworkForBinding(player, stack);
        if (network == null || network.getCurrentEV() < getBaseEvCost()) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(
                    Component.translatable("text.component.animusnv.hellforged_bow.no_ev")
                        .withStyle(ChatFormatting.RED));
            }
            return false;
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            AnimaTicket ticket = AnimaTicket.create(getBaseEvCost());
            network.syphonAndDamage(player, ticket);

            SpiritusType spiritusType = getCurrentType(stack);
            double spiritusAmount = NeoVitaeAPI.getInstance().getPlayerSpiritusHandler().getTotalSpiritus(spiritusType, player);
            int spiritusLevel = getLevel(spiritusAmount);

            float chargeMultiplier = 0.0f;
            if (useDuration > NORMAL_DRAW_TICKS) {
                int extraChargeTicks = Math.min(useDuration - NORMAL_DRAW_TICKS, getMaxChargeTicks() - NORMAL_DRAW_TICKS);
                chargeMultiplier = (float) extraChargeTicks / (getMaxChargeTicks() - NORMAL_DRAW_TICKS);
            }

            double baseDamage = 2.0 + (power * 2.0);
            double chargeBonusDamage = chargeMultiplier * (getMaxArrowDamage() - baseDamage);
            double totalDamage = baseDamage + chargeBonusDamage;

            int powerEnchant = stack.getEnchantmentLevel(serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.POWER));
            if (powerEnchant > 0) {
                totalDamage += (double) powerEnchant * 0.5D + 0.5D;
            }

            EntityHellforgedArrow arrow = new EntityHellforgedArrow(level, player);
            arrow.setSpiritusType(spiritusType);
            arrow.setSpiritusLevel(spiritusLevel);
            arrow.setBaseDamage(totalDamage);
            arrow.setChargeMultiplier(chargeMultiplier);
            arrow.setExecuteThreshold(chargeMultiplier >= 1.0f ? getExecuteThreshold() : 0.0);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

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
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        UUID ownerId = getBindingOwnerId(stack);
        if (ownerId == null) {
            if (!level.isClientSide()) {
                bindToPlayer(stack, player);
                player.sendOverlayMessage(
                    Component.translatable("text.component.animusnv.hellforged_bow.bound")
                        .withStyle(ChatFormatting.AQUA));
            }
            return InteractionResult.CONSUME;
        }

        IAnima network = getNetworkForBinding(player, stack);
        if (network == null || network.getCurrentEV() < getBaseEvCost()) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(
                    Component.translatable("text.component.animusnv.hellforged_bow.no_ev")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
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
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        if (entity instanceof Player player) {
            SpiritusType newType = SpiritusTypeHelper.findSpiritusType(player);
            if (newType != getCurrentType(stack)) {
                setCurrentType(stack, newType);
            }
        }
    }

    public static int getLevel(double spiritusAmount) {
        return SpiritusWeaponStats.getLevel(spiritusAmount);
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
        return super.isFoil(stack) || (getBindingOwnerId(stack) != null && getCurrentType(stack) != SpiritusType.RAW);
    }
}
