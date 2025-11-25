package com.teamdman.animus.items;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Kama - An AOE melee weapon
 * Attacks all entities in an area based on the tool material's harvest level
 */
public class ItemKama extends SwordItem {
    protected final Tier tier;

    public ItemKama(Tier tier) {
        super(tier, 3, -2.4F, new Properties());
        this.tier = tier;
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
        Level level = target.level();

        if (level.isClientSide) {
            return false;
        }

        double x = target.getX();
        double y = target.getY();
        double z = target.getZ();

        // Calculate range based on tier (attack damage as proxy for tier level)
        int range = ((int) tier.getAttackDamageBonus() + 1) * 2;

        // Create bounding box for AOE
        AABB region = new AABB(x - range, y - range, z - range, x + range, y + range, z + range);

        // Get all living entities in range
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, region);

        if (entities.isEmpty()) {
            return false;
        }

        float damage = getDamage();

        for (LivingEntity entity : entities) {
            // Skip players
            if (entity instanceof Player) {
                continue;
            }

            // Skip null or same entity
            if (entity == null || entity == attacker) {
                continue;
            }

            // Damage the entity
            entity.hurt(level.damageSources().mobAttack(attacker), damage);

            // Damage the kama
            stack.hurtAndBreak(1, attacker, (e) -> e.broadcastBreakEvent(attacker.getUsedItemHand()));
        }

        return false;
    }
}
