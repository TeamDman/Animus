package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import com.teamdman.animus.entities.EntityThrownSpear;
import com.teamdman.animus.registry.AnimusDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.common.datacomponent.Binding;
import wayoftime.bloodmagic.common.datacomponent.SoulNetwork;
import wayoftime.bloodmagic.common.blockentity.BloodAltarTile;
import wayoftime.bloodmagic.util.SoulTicket;
import wayoftime.bloodmagic.util.helper.SoulNetworkHelper;

import java.util.List;

/**
 * Bound Spear - A soul-bound javelin that can be toggled between active/deactivated modes
 *
 * Deactivated Mode: Behaves like a diamond spear but unbreakable
 * Active Mode: Has AOE attacks, sacrifices entities to altars, costs 50LP per attack/throw
 *
 * Sneak + Right-Click: Bind (if unbound) or toggle active/deactivated (if bound)
 */
public class ItemSpearBound extends ItemSpear implements IBindable {
    private static final int LP_COST = 50;

    public ItemSpearBound() {
        super(Tiers.DIAMOND);
    }

    // Note: Fire resistance and durability are now set via Item.Properties in the constructor
    // These methods are kept for documentation but don't override anything in 1.21

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0; // No durability bar
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false; // Never damaged
    }

    /**
     * Check if this spear is activated
     */
    public boolean isActivated(ItemStack stack) {
        Boolean activated = stack.get(AnimusDataComponents.SPEAR_ACTIVATED.get());
        return activated != null && activated;
    }

    /**
     * Set activation state
     */
    public void setActivated(ItemStack stack, boolean activated) {
        stack.set(AnimusDataComponents.SPEAR_ACTIVATED.get(), activated);
    }

    /**
     * Consume LP from the bound player's soul network
     * @return true if LP was successfully consumed, false if insufficient LP
     */
    private boolean consumeLP(Player player, ItemStack stack) {
        if (player.getAbilities().instabuild) {
            return true; // Creative mode players don't need LP
        }

        Binding binding = getBinding(stack);
        if (binding == null) {
            return false; // Not bound, can't consume LP
        }

        // Get the soul network and consume LP
        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(player);
        SoulTicket ticket = SoulTicket.create(LP_COST);

        var result = network.syphonAndDamage(player, ticket);
        return result.isSuccess();
    }

    /**
     * Get riptide enchantment level from the stack
     */
    private int getRiptideLevel(ItemStack stack, Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return stack.getEnchantmentLevel(serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.RIPTIDE));
        }
        return 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Allow shield blocking when sneaking with a shield in the other hand
        // This overrides the toggle mechanic
        if (player.isShiftKeyDown()) {
            InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack otherStack = player.getItemInHand(otherHand);
            if (otherStack.getItem() instanceof net.minecraft.world.item.ShieldItem) {
                return InteractionResultHolder.pass(stack);
            }
        }

        // Sneak + right-click handling (only if not blocking with shield)
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                Binding binding = getBinding(stack);

                if (binding == null) {
                    // Not bound - bind it now
                    onBind(player, stack);
                    player.displayClientMessage(
                        Component.translatable(Constants.Localizations.Text.SPEAR_BOUND_SUCCESS)
                            .withStyle(ChatFormatting.AQUA),
                        true
                    );
                } else {
                    // Already bound - toggle activation
                    boolean wasActivated = isActivated(stack);
                    setActivated(stack, !wasActivated);

                    if (!wasActivated) {
                        player.displayClientMessage(
                            Component.translatable(Constants.Localizations.Text.SPEAR_ACTIVATED)
                                .withStyle(ChatFormatting.GREEN),
                            true
                        );
                    } else {
                        player.displayClientMessage(
                            Component.translatable(Constants.Localizations.Text.SPEAR_DEACTIVATED)
                                .withStyle(ChatFormatting.GRAY),
                            true
                        );
                    }
                }
            }
            return InteractionResultHolder.success(stack);
        }

        // Normal right-click - throw the spear
        // Bound spear is unbreakable, so skip durability check
        if (getRiptideLevel(stack, level) > 0 && !player.isInWaterOrRain()) {
            return InteractionResultHolder.fail(stack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int useDuration = this.getUseDuration(stack, entity) - timeLeft;
            if (useDuration >= 10) {
                int riptide = getRiptideLevel(stack, level);

                // If activated, check for LP cost (server-side only)
                if (isActivated(stack) && !level.isClientSide && !consumeLP(player, stack)) {
                    player.displayClientMessage(
                        Component.translatable(Constants.Localizations.Text.SPEAR_NO_LP_THROW)
                            .withStyle(ChatFormatting.RED),
                        true
                    );
                    return;
                }

                if (riptide <= 0 || player.isInWaterOrRain()) {
                    if (!level.isClientSide) {
                        if (riptide == 0) {
                            // Spawn our custom spear entity
                            EntityThrownSpear thrownSpear = new EntityThrownSpear(level, player, stack);
                            thrownSpear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                            if (player.getAbilities().instabuild) {
                                thrownSpear.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            }

                            level.addFreshEntity(thrownSpear);
                            level.playSound(null, thrownSpear.getX(), thrownSpear.getY(), thrownSpear.getZ(),
                                SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                            if (!player.getAbilities().instabuild) {
                                player.getInventory().removeItem(stack);
                            }
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                    if (riptide > 0) {
                        float yaw = player.getYRot();
                        float pitch = player.getXRot();
                        float xSpeed = -net.minecraft.util.Mth.sin(yaw * ((float)Math.PI / 180F)) * net.minecraft.util.Mth.cos(pitch * ((float)Math.PI / 180F));
                        float ySpeed = -net.minecraft.util.Mth.sin(pitch * ((float)Math.PI / 180F));
                        float zSpeed = net.minecraft.util.Mth.cos(yaw * ((float)Math.PI / 180F)) * net.minecraft.util.Mth.cos(pitch * ((float)Math.PI / 180F));
                        float length = net.minecraft.util.Mth.sqrt(xSpeed * xSpeed + ySpeed * ySpeed + zSpeed * zSpeed);
                        float multiplier = 3.0F * ((1.0F + (float)riptide) / 4.0F);
                        xSpeed = xSpeed * (multiplier / length);
                        ySpeed = ySpeed * (multiplier / length);
                        zSpeed = zSpeed * (multiplier / length);
                        player.push((double)xSpeed, (double)ySpeed, (double)zSpeed);
                        player.startAutoSpinAttack(20, 8.0F + (float)riptide * 2.0F, stack);
                        if (player.onGround()) {
                            player.move(net.minecraft.world.entity.MoverType.SELF, new net.minecraft.world.phys.Vec3(0.0, 1.2, 0.0));
                        }

                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            hurtEnemy(stack, livingEntity, player);
            return true;
        }
        return false;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // If deactivated, just do normal single-target damage
        if (!isActivated(stack)) {
            return super.hurtEnemy(stack, target, attacker);
        }

        // If activated, check for LP cost
        if (attacker instanceof Player player) {
            if (!consumeLP(player, stack)) {
                player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.SPEAR_NO_LP_ATTACK)
                        .withStyle(ChatFormatting.RED),
                    true
                );
                return false;
            }
        }

        // Call parent to apply normal attack damage to the main target
        super.hurtEnemy(stack, target, attacker);

        Level level = target.level();

        if (level.isClientSide) {
            return false;
        }

        double x = target.getX();
        double y = target.getY();
        double z = target.getZ();

        // Apply AOE damage to nearby entities
        // If a Blood Altar is nearby, all enemies are sacrificed to it (instant kill)
        // If no altar is nearby, normal AOE damage is dealt
        checkAndDamage(x, y, z, level, attacker);

        return false;
    }

    /**
     * Damages all entities in range, sacrificing them to nearby altars if possible
     */
    private boolean checkAndDamage(double x, double y, double z, Level level, LivingEntity attacker) {
        int range = 5;
        boolean hit = false;

        AABB region = new AABB(x - range, y - range, z - range, x + range, y + range, z + range);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, region);

        if (entities.isEmpty()) {
            return false;
        }

        float damage = 6.0F + getTier().getAttackDamageBonus();

        for (LivingEntity target : entities) {
            if (target == null || target.isDeadOrDying() || !(attacker instanceof Player) || attacker == target) {
                continue;
            }

            // Try to sacrifice this entity to a nearby altar (regardless of health)
            // Skip players, non-sacrificeable entities, and entities with the disallow_sacrifice tag
            if (target.canChangeDimensions(level, level) && !(target instanceof Player)) {
                // Check if entity is tagged as too powerful to sacrifice
                if (target.getType().is(Constants.Tags.DISALLOW_SACRIFICE)) {
                    // Show message to player that this enemy is too powerful
                    if (attacker instanceof Player playerAttacker) {
                        playerAttacker.displayClientMessage(
                            Component.translatable(Constants.Localizations.Text.SACRIFICE_TOO_POWERFUL),
                            true
                        );
                    }
                    // Fall through to normal damage
                } else {
                    // Calculate life essence value
                    int lifeEssence = getEntitySacrificeValue(target);

                    // Try to find and fill altar
                    if (lifeEssence > 0 && findAndFillAltar(level, target, lifeEssence)) {
                        // Sacrifice successful - kill the entity outright and play effect
                        level.playSound(
                            null,
                            target.getX(),
                            target.getY(),
                            target.getZ(),
                            SoundEvents.FIRE_EXTINGUISH,
                            SoundSource.BLOCKS,
                            0.5F,
                            2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F
                        );
                        target.setHealth(-1);
                        target.die(level.damageSources().genericKill());
                        hit = true;
                        continue; // Skip normal damage for this entity
                    }
                }
            }

            // Normal damage if sacrifice didn't happen (no altar nearby)
            boolean result = target.hurt(level.damageSources().genericKill(), damage);
            if (result) {
                hit = true;
            }
        }

        return hit;
    }


    /**
     * Calculates entity sacrifice value based on entity type
     * Values are based on Blood Magic's standard sacrifice values
     */
    private int getEntitySacrificeValue(LivingEntity entity) {
        // Boss entities - very high value
        if (entity instanceof WitherBoss) {
            return 2000;
        }
        if (entity instanceof EnderDragon) {
            return 3000;
        }

        // Special cases - low value
        if (entity instanceof Silverfish || entity instanceof Endermite) {
            return 25;
        }

        // Fire entities - higher value
        if (entity instanceof Blaze) {
            return 250;
        }

        // Default values based on entity attributes
        int baseValue = 500;

        // Baby entities give half value
        if (entity.isBaby()) {
            baseValue /= 2;
        }

        // Scale by max health (entities with more health give more LP)
        float healthMultiplier = Math.min(entity.getMaxHealth() / 20.0F, 2.0F);
        baseValue = (int) (baseValue * healthMultiplier);

        return Math.max(baseValue, 50); // Minimum 50 LP
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Binding binding = getBinding(stack);
        boolean activated = isActivated(stack);

        if (binding != null) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SPEAR_BOUND_TO, binding.name())
                .withStyle(ChatFormatting.AQUA));

            if (activated) {
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SPEAR_STATUS_ACTIVATED)
                    .withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SPEAR_COST)
                    .withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SPEAR_FIRST));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SPEAR_SECOND));
            } else {
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SPEAR_STATUS_DEACTIVATED)
                    .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SPEAR_BEHAVES_DIAMOND)
                    .withStyle(ChatFormatting.GRAY));
            }

            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SPEAR_TOGGLE)
                .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SPEAR_UNBOUND)
                .withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SPEAR_BIND)
                .withStyle(ChatFormatting.YELLOW));
        }

        // Don't call super.appendHoverText to avoid duplicate tooltips
    }

    @Override
    public int getEnchantmentValue() {
        return Tiers.GOLD.getEnchantmentValue();
    }

    // Note: canApplyAtEnchantingTable was removed in 1.21
    // Enchantment compatibility is now handled through enchantment tags

    /**
     * Find a nearby Blood Altar and fill it with LP
     * @param level The level to search in
     * @param entity The entity being sacrificed (used for position)
     * @param lifeEssence The amount of LP to add
     * @return true if an altar was found and filled
     */
    private boolean findAndFillAltar(Level level, LivingEntity entity, int lifeEssence) {
        BlockPos center = entity.blockPosition();
        int searchRadius = 8;

        for (BlockPos pos : BlockPos.betweenClosed(
            center.offset(-searchRadius, -searchRadius, -searchRadius),
            center.offset(searchRadius, searchRadius, searchRadius)
        )) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BloodAltarTile altar) {
                // Check if altar has space
                int currentBlood = altar.mainTank;
                int capacity = altar.getMainCapacity();
                if (currentBlood < capacity) {
                    // Add LP using the standard method
                    altar.sacrificialDaggerCall(lifeEssence, true);
                    return true;
                }
            }
        }
        return false;
    }
}
