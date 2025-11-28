package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import com.teamdman.animus.entities.EntityThrownPilum;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.core.data.Binding;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.List;

/**
 * Bound Pilum - A soul-bound javelin that can be toggled between active/deactivated modes
 *
 * Deactivated Mode: Behaves like a diamond pilum but unbreakable
 * Active Mode: Has AOE attacks, sacrifices entities to altars, costs 50LP per attack/throw
 *
 * Shift+Right-Click: Bind (if unbound) or toggle active/deactivated (if bound)
 */
public class ItemPilumBound extends ItemPilum implements IBindable {
    private static final int LP_COST = 50;
    private static final String NBT_ACTIVATED = "activated";

    public ItemPilumBound() {
        super(Tiers.DIAMOND);
    }

    @Override
    public boolean isFireResistant() {
        return true; // Fireproof - won't burn in lava/fire
    }

    @Override
    public boolean canBeDepleted() {
        return false; // Unbreakable - never takes damage
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0; // No durability bar
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false; // Never damaged
    }

    /**
     * Check if this pilum is activated
     */
    public boolean isActivated(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(NBT_ACTIVATED);
    }

    /**
     * Set activation state
     */
    public void setActivated(ItemStack stack, boolean activated) {
        stack.getOrCreateTag().putBoolean(NBT_ACTIVATED, activated);
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
        SoulNetwork network = NetworkHelper.getSoulNetwork(player);
        SoulTicket ticket = new SoulTicket(
            Component.literal("Bound Pilum"),
            LP_COST
        );

        var result = network.syphonAndDamage(player, ticket);
        return result.isSuccess();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Shift+right-click handling
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                Binding binding = getBinding(stack);

                if (binding == null) {
                    // Not bound - bind it now
                    onBind(player, stack);
                    player.displayClientMessage(
                        Component.literal("Bound Pilum bound to you")
                            .withStyle(ChatFormatting.AQUA),
                        true
                    );
                } else {
                    // Already bound - toggle activation
                    boolean wasActivated = isActivated(stack);
                    setActivated(stack, !wasActivated);

                    if (!wasActivated) {
                        player.displayClientMessage(
                            Component.literal("Bound Pilum activated - 50LP per attack")
                                .withStyle(ChatFormatting.GREEN),
                            true
                        );
                    } else {
                        player.displayClientMessage(
                            Component.literal("Bound Pilum deactivated")
                                .withStyle(ChatFormatting.GRAY),
                            true
                        );
                    }
                }
            }
            return InteractionResultHolder.success(stack);
        }

        // Normal right-click - throw the pilum
        // Bound pilum is unbreakable, so skip durability check
        if (EnchantmentHelper.getRiptide(stack) > 0 && !player.isInWaterOrRain()) {
            return InteractionResultHolder.fail(stack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int useDuration = this.getUseDuration(stack) - timeLeft;
            if (useDuration >= 10) {
                int riptide = EnchantmentHelper.getRiptide(stack);

                // If activated, check for LP cost
                if (isActivated(stack) && !consumeLP(player, stack)) {
                    if (!level.isClientSide) {
                        player.displayClientMessage(
                            Component.literal("Not enough LP to throw!")
                                .withStyle(ChatFormatting.RED),
                            true
                        );
                    }
                    return;
                }

                if (riptide <= 0 || player.isInWaterOrRain()) {
                    if (!level.isClientSide) {
                        if (riptide == 0) {
                            // Spawn our custom pilum entity
                            EntityThrownPilum thrownPilum = new EntityThrownPilum(level, player, stack);
                            thrownPilum.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                            if (player.getAbilities().instabuild) {
                                thrownPilum.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            }

                            level.addFreshEntity(thrownPilum);
                            level.playSound(null, thrownPilum, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
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
                        player.startAutoSpinAttack(20);
                        if (player.onGround()) {
                            player.move(net.minecraft.world.entity.MoverType.SELF, new net.minecraft.world.phys.Vec3(0.0, 1.2, 0.0));
                        }

                        level.playSound(null, player, SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS, 1.0F, 1.0F);
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
                    Component.literal("Not enough LP to attack!")
                        .withStyle(ChatFormatting.RED),
                    true
                );
                return false;
            }
        }

        // Call parent to apply normal attack damage
        super.hurtEnemy(stack, target, attacker);

        Level level = target.level();

        if (level.isClientSide) {
            return false;
        }

        double x = target.getX();
        double y = target.getY();
        double z = target.getZ();

        // Try to sacrifice entities first
        if (checkAndKill(x, y, z, level, attacker, false)) {
            return false;
        }

        // If sacrifice failed, do normal AOE damage
        checkAndDamage(x, y, z, level, attacker);
        return false;
    }

    /**
     * Damages all entities in range
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

            boolean result = target.hurt(level.damageSources().genericKill(), damage);
            if (result) {
                hit = true;
            }
        }

        return hit;
    }

    /**
     * Kills low-health entities and fills nearby Blood Altars with their life essence
     */
    private boolean checkAndKill(double x, double y, double z, Level level, LivingEntity attacker, boolean efficient) {
        int range = 5;
        boolean killed = false;

        AABB region = new AABB(x - range, y - range, z - range, x + range, y + range, z + range);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, region);

        if (entities.isEmpty()) {
            return false;
        }

        for (LivingEntity target : entities) {
            if (target == null || attacker == null || attacker instanceof FakePlayer) {
                continue;
            }

            // Only sacrifice entities that are almost dead, non-boss, and not players
            if (target.isDeadOrDying() || target.getHealth() >= 0.5F || !target.canChangeDimensions() || target instanceof Player) {
                continue;
            }

            // Calculate life essence using entity-type based values
            // This mimics Blood Magic's sacrifice value system
            int lifeEssence = getEntitySacrificeValue(target);

            if (lifeEssence <= 0) {
                continue;
            }

            // Try to find and fill altar using Blood Magic's helper
            if (findAndFillAltar(level, attacker, lifeEssence, efficient)) {
                // Play sound effect
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

                // Kill the entity
                target.setHealth(-1);
                target.die(level.damageSources().genericKill());
                killed = true;
            }
        }

        return killed;
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

    /**
     * Finds a nearby Blood Altar and fills it with life essence
     * Uses Blood Magic's PlayerSacrificeHelper for proper integration
     */
    private boolean findAndFillAltar(Level level, LivingEntity sacrificingEntity, int amount, boolean efficient) {
        if (efficient) {
            // Efficient mode - direct altar check at entity position
            wayoftime.bloodmagic.altar.IBloodAltar altar =
                wayoftime.bloodmagic.util.helper.PlayerSacrificeHelper.getAltar(level, sacrificingEntity.blockPosition());

            if (altar == null) {
                return false;
            }

            // Check if altar has capacity
            if (altar.getCurrentBlood() + amount > altar.getCapacity()) {
                return false;
            }

            // Fill altar using Blood Magic's sacrificial dagger method
            altar.sacrificialDaggerCall(amount, true);
            altar.startCycle();
            return true;
        } else {
            // Standard mode - use Blood Magic's helper which searches nearby for altars
            // This will find the nearest altar within range and fill it
            return wayoftime.bloodmagic.util.helper.PlayerSacrificeHelper.findAndFillAltar(
                level,
                sacrificingEntity,
                amount,
                true // doFill = true
            );
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        Binding binding = getBinding(stack);
        boolean activated = isActivated(stack);

        if (binding != null) {
            tooltip.add(Component.literal("Bound to: " + binding.getOwnerName())
                .withStyle(ChatFormatting.AQUA));

            if (activated) {
                tooltip.add(Component.literal("Status: Activated")
                    .withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.literal("Costs 50LP per attack/throw")
                    .withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.PILUM_FIRST));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.PILUM_SECOND));
            } else {
                tooltip.add(Component.literal("Status: Deactivated")
                    .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Behaves like Diamond Pilum")
                    .withStyle(ChatFormatting.GRAY));
            }

            tooltip.add(Component.literal("Shift+Right-Click to toggle")
                .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.literal("Unbound")
                .withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.literal("Shift+Right-Click to bind")
                .withStyle(ChatFormatting.YELLOW));
        }

        // Don't call super.appendHoverText to avoid duplicate tooltips
    }

    @Override
    public int getEnchantmentValue() {
        return Tiers.GOLD.getEnchantmentValue();
    }
}
