package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;

/**
 * Bound Pilum of Feasting - A Roman javelin that sacrifices low-health entities to fill nearby Blood Altars
 * Kills entities below 0.5 health and adds their life essence to nearby altars
 * If no altars are found or entities can't be killed, does AOE damage instead
 */
public class ItemPilumBound extends ItemPilum {

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

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // Bound pilum is unbreakable, so skip durability check
        if (net.minecraft.world.item.enchantment.EnchantmentHelper.getRiptide(stack) > 0 && !player.isInWaterOrRain()) {
            return net.minecraft.world.InteractionResultHolder.fail(stack);
        } else {
            player.startUsingItem(hand);
            return net.minecraft.world.InteractionResultHolder.consume(stack);
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
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.PILUM_FIRST));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.PILUM_SECOND));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public int getEnchantmentValue() {
        return Tiers.GOLD.getEnchantmentValue();
    }
}
