package com.teamdman.animus.items;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.entities.EntityHellforgedArrow;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.api.compat.IDemonWillWeapon;
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.core.data.Binding;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;
import wayoftime.bloodmagic.will.PlayerDemonWillHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Hellforged Bow - An advanced LP-powered bow with charged shot mechanics
 *
 * - Consumes LP instead of demon will to fire
 * - Base LP cost to fire, plus LP per tick while charging beyond normal draw
 * - Still empowered by demon will for type-based effects
 * - Extended charge time up to 3.5 seconds for maximum damage
 * - Execute effect at full charge: instant kill targets below threshold
 */
public class ItemHellforgedBow extends BowItem implements IDemonWillWeapon, IBindable {
    // Soul brackets for will level progression (same as sentient weapons)
    public static final double[] soulBracket = new double[]{16, 60, 200, 400, 1000};

    // Normal bow draw time in ticks (~20 ticks for full power)
    public static final int NORMAL_DRAW_TICKS = 20;

    // Default config values (used before config loads)
    public static final int DEFAULT_BASE_LP_COST = 5;
    public static final int DEFAULT_LP_PER_TICK = 5;
    public static final int DEFAULT_MAX_CHARGE_TICKS = 70;
    public static final double DEFAULT_MAX_DAMAGE = 40.0;
    public static final double DEFAULT_EXECUTE_THRESHOLD = 0.15;

    public ItemHellforgedBow() {
        super(new Item.Properties()
            .stacksTo(1)
            .durability(750)
            .rarity(Rarity.EPIC));
    }

    // Config accessors with fallbacks
    public static int getBaseLpCost() {
        try {
            return AnimusConfig.weapons.hellforgedBowBaseLpCost.get();
        } catch (IllegalStateException e) {
            return DEFAULT_BASE_LP_COST;
        }
    }

    public static int getLpPerTick() {
        try {
            return AnimusConfig.weapons.hellforgedBowLpPerTick.get();
        } catch (IllegalStateException e) {
            return DEFAULT_LP_PER_TICK;
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HELLFORGED_BOW_FLAVOUR)
            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));

        // Show binding status
        Binding binding = getBinding(stack);
        if (binding != null) {
            tooltip.add(Component.translatable("tooltip.animus.bound_to")
                .append(Component.literal(binding.getOwnerName()))
                .withStyle(ChatFormatting.DARK_RED));
        } else {
            tooltip.add(Component.translatable("tooltip.animus.unbound_bind")
                .withStyle(ChatFormatting.GRAY));
        }

        // Show will type if attuned
        EnumDemonWillType type = getCurrentType(stack);
        if (type != EnumDemonWillType.DEFAULT) {
            tooltip.add(Component.translatable("tooltip.animus.hellforged_bow.will_type", type.name().toLowerCase())
                .withStyle(ChatFormatting.DARK_PURPLE));
        }

        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HELLFORGED_BOW_INFO)
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HELLFORGED_BOW_LP_COST, getBaseLpCost())
            .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HELLFORGED_BOW_CHARGE)
            .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.HELLFORGED_BOW_EXECUTE,
            (int)(getExecuteThreshold() * 100))
            .withStyle(ChatFormatting.RED));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        // Allow extended use duration for charging
        return 72000; // Same as vanilla bow (effectively unlimited)
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (!(entity instanceof Player player)) {
            return;
        }

        int useDuration = this.getUseDuration(stack) - remainingTicks;

        // Server-side: drain LP after normal draw time
        if (!level.isClientSide() && useDuration > NORMAL_DRAW_TICKS && useDuration <= getMaxChargeTicks()) {
            // Drain LP per tick during extended charge
            if (!drainLP(player, getLpPerTick())) {
                // Not enough LP, release the bow
                player.releaseUsingItem();
            }
        }

        // Visual effects when fully charged (both client and server for particles)
        if (useDuration >= getMaxChargeTicks()) {
            // Spawn blood/soul particles around the bow
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                // Get position in front of player (where bow would be)
                double x = player.getX();
                double y = player.getY() + player.getEyeHeight() - 0.2;
                double z = player.getZ();

                // Soul fire flame particles
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                    x, y, z,
                    1,
                    0.3, 0.2, 0.3,
                    0.02
                );

                // Occasional crimson spore (blood-like) particles
                if (useDuration % 5 == 0) {
                    serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.CRIMSON_SPORE,
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

        // Check binding
        Binding binding = getBinding(stack);
        if (binding == null) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.animus.hellforged_bow.not_bound")
                        .withStyle(ChatFormatting.RED),
                    true
                );
            }
            return;
        }

        int useDuration = this.getUseDuration(stack) - timeLeft;
        float power = getPowerForTime(useDuration);

        if (power < 0.1F) {
            return;
        }

        // Check if player has enough LP for base cost
        SoulNetwork network = NetworkHelper.getSoulNetwork(binding.getOwnerId());
        if (network == null || network.getCurrentEssence() < getBaseLpCost()) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.animus.hellforged_bow.out_of_lp")
                        .withStyle(ChatFormatting.RED),
                    true
                );
            }
            return;
        }

        if (!level.isClientSide) {
            // Consume base LP cost
            network.syphon(new SoulTicket(
                Component.translatable(Constants.Localizations.Text.TICKET_HELLFORGED_BOW),
                getBaseLpCost()
            ), false);

            // Get will type for effects
            EnumDemonWillType willType = getCurrentType(stack);
            double willAmount = PlayerDemonWillHandler.getTotalDemonWill(willType, player);
            int willLevel = getLevel(willAmount);

            // Calculate charge multiplier (0.0 to 1.0 based on charge beyond normal draw)
            float chargeMultiplier = 0.0f;
            if (useDuration > NORMAL_DRAW_TICKS) {
                int extraChargeTicks = Math.min(useDuration - NORMAL_DRAW_TICKS, getMaxChargeTicks() - NORMAL_DRAW_TICKS);
                chargeMultiplier = (float) extraChargeTicks / (getMaxChargeTicks() - NORMAL_DRAW_TICKS);
            }

            // Calculate damage based on charge
            // Base arrow damage (2) + power scaling + charge bonus
            double baseDamage = 2.0 + (power * 2.0); // Normal bow damage
            double chargeBonusDamage = chargeMultiplier * (getMaxArrowDamage() - baseDamage);
            double totalDamage = baseDamage + chargeBonusDamage;

            // Create hellforged arrow
            EntityHellforgedArrow arrow = new EntityHellforgedArrow(level, player);
            arrow.setWillType(willType);
            arrow.setWillLevel(willLevel);
            arrow.setBaseDamage(totalDamage);
            arrow.setChargeMultiplier(chargeMultiplier);
            arrow.setExecuteThreshold(chargeMultiplier >= 1.0f ? getExecuteThreshold() : 0.0);
            arrow.setPierceLevel((byte) 127); // Pierce through all entities
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

            // Apply power enchantment bonus
            int powerEnchant = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
            if (powerEnchant > 0) {
                arrow.setBaseDamage(arrow.getBaseDamage() + (double) powerEnchant * 0.5D + 0.5D);
            }

            // Apply punch enchantment
            int punchEnchant = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
            if (punchEnchant > 0) {
                arrow.setKnockback(punchEnchant);
            }

            // Apply flame enchantment
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                arrow.setSecondsOnFire(100);
            }

            // Critical hit if fully charged
            if (power >= 1.0F) {
                arrow.setCritArrow(true);
            }

            // Virtual arrows don't get picked up
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

            level.addFreshEntity(arrow);

            // Play sound with pitch based on charge
            float pitch = 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F;
            if (chargeMultiplier > 0.5f) {
                // Higher pitched sound for charged shots
                pitch += chargeMultiplier * 0.3f;
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, pitch);

            // Damage the bow
            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));

            // LP-powered self-repair: 100 LP per damage point
            tryRepairWithLP(stack, network);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Check binding - bind on first use
        Binding binding = getBinding(stack);
        if (binding == null) {
            if (!level.isClientSide) {
                if (onBind(player, stack)) {
                    player.displayClientMessage(
                        Component.translatable("message.animus.hellforged_bow.bound")
                            .withStyle(ChatFormatting.AQUA),
                        true
                    );
                }
            }
            return InteractionResultHolder.consume(stack);
        }

        // Check if player has LP to shoot
        SoulNetwork network = NetworkHelper.getSoulNetwork(binding.getOwnerId());
        if (network == null || network.getCurrentEssence() < getBaseLpCost()) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.translatable("message.animus.hellforged_bow.out_of_lp")
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
        // Hellforged bow doesn't use regular arrows
        return (stack) -> false;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }

    // Will type management
    public EnumDemonWillType getCurrentType(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("demonWillType")) {
            try {
                return EnumDemonWillType.valueOf(stack.getTag().getString("demonWillType").toUpperCase());
            } catch (IllegalArgumentException e) {
                return EnumDemonWillType.DEFAULT;
            }
        }
        return EnumDemonWillType.DEFAULT;
    }

    public void setCurrentType(ItemStack stack, EnumDemonWillType type) {
        stack.getOrCreateTag().putString("demonWillType", type.toString());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (entity instanceof Player player) {
            // Update the will type based on the player's inventory
            EnumDemonWillType newType = findDemonWillType(player);
            if (newType != getCurrentType(stack)) {
                setCurrentType(stack, newType);
            }
        }
    }

    /**
     * Determines the demon will type based on will available from the player's soul network
     * Returns the type with the highest will amount
     */
    private static EnumDemonWillType findDemonWillType(Player player) {
        EnumDemonWillType highestType = EnumDemonWillType.DEFAULT;
        double highestAmount = 0;

        for (EnumDemonWillType type : EnumDemonWillType.values()) {
            double amount = PlayerDemonWillHandler.getTotalDemonWill(type, player);
            if (type != EnumDemonWillType.DEFAULT && amount > highestAmount) {
                highestType = type;
                highestAmount = amount;
            }
        }

        return highestType;
    }

    public static int getLevel(double willAmount) {
        for (int i = 0; i < soulBracket.length; i++) {
            if (willAmount < soulBracket[i]) {
                return i;
            }
        }
        return soulBracket.length;
    }

    private static final int LP_PER_REPAIR = 100;

    /**
     * Attempts to repair the bow using LP from the soul network
     * Costs 100 LP per damage point repaired
     */
    private void tryRepairWithLP(ItemStack stack, SoulNetwork network) {
        int damage = stack.getDamageValue();
        if (damage <= 0) {
            return; // No damage to repair
        }

        // Calculate how much we can repair based on available LP
        int availableLP = network.getCurrentEssence();
        int maxRepair = availableLP / LP_PER_REPAIR;
        int actualRepair = Math.min(damage, maxRepair);

        if (actualRepair > 0) {
            // Consume LP and repair
            int lpCost = actualRepair * LP_PER_REPAIR;
            network.syphon(new SoulTicket(
                Component.translatable("text.component.animus.ticket.hellforged_repair"),
                lpCost
            ), false);
            stack.setDamageValue(damage - actualRepair);
        }
    }

    private boolean drainLP(Player player, int amount) {
        Binding binding = getBinding(player.getUseItem());
        if (binding == null) {
            return false;
        }

        SoulNetwork network = NetworkHelper.getSoulNetwork(binding.getOwnerId());
        if (network == null || network.getCurrentEssence() < amount) {
            return false;
        }

        network.syphon(new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_HELLFORGED_BOW_CHARGE),
            amount
        ), false);
        return true;
    }

    // IBindable implementation
    @Override
    public Binding getBinding(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("binding")) {
            return Binding.fromStack(stack);
        }
        return null;
    }

    @Override
    public boolean onBind(Player player, ItemStack stack) {
        Binding binding = new Binding(player.getUUID(), player.getName().getString());
        stack.getOrCreateTag().put("binding", binding.serializeNBT());
        return true;
    }

    // IDemonWillWeapon implementation
    @Override
    public List<ItemStack> getRandomDemonWillDrop(LivingEntity killedEntity, LivingEntity attackingEntity,
                                                   ItemStack stack, int looting) {
        return new ArrayList<>();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Add enchantment glint when bound and has will type
        return super.isFoil(stack) || (getBinding(stack) != null && getCurrentType(stack) != EnumDemonWillType.DEFAULT);
    }
}
