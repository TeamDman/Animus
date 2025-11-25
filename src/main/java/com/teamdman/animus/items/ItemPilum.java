package com.teamdman.animus.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Pilum - A Roman-style javelin with AOE melee capabilities
 * Can be thrown like a trident but also damages nearby entities when used in melee
 */
public class ItemPilum extends TridentItem {
    protected final Tier tier;

    public ItemPilum(Tier tier) {
        super(new Properties()
            .durability(tier.getUses())
        );
        this.tier = tier;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Call parent for standard trident behavior
        super.hurtEnemy(stack, target, attacker);

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
            return true;
        }

        // Get damage from tier
        float damage = 6.0F + tier.getAttackDamageBonus();

        for (LivingEntity entity : entities) {
            // Skip players
            if (entity instanceof Player) {
                continue;
            }

            // Skip null, dead, or same entity
            if (entity == null || entity == attacker || entity == target) {
                continue;
            }

            // Damage the entity
            entity.hurt(level.damageSources().mobAttack(attacker), damage);

            // Damage the pilum
            stack.hurtAndBreak(1, attacker, (e) -> e.broadcastBreakEvent(attacker.getUsedItemHand()));
        }

        return true;
    }

    /**
     * Get the tier of this pilum
     */
    public Tier getTier() {
        return tier;
    }
}
