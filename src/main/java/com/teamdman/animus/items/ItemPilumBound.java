package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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

            // Calculate life essence
            // TODO: Use Blood Magic's entity sacrifice values when API is available
            // For now, use default of 500 (half for babies)
            int lifeEssence = 500;

            if (target.isBaby()) {
                lifeEssence /= 2;
            }

            if (lifeEssence <= 0) {
                continue;
            }

            // Try to find and fill altar
            // TODO: Integrate with Blood Magic's PlayerSacrificeHelper
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
     * Finds a nearby Blood Altar and fills it with life essence
     */
    private boolean findAndFillAltar(Level level, LivingEntity sacrificingEntity, int amount, boolean efficient) {
        if (efficient) {
            // Efficient mode - direct altar check
            wayoftime.bloodmagic.altar.IBloodAltar altar =
                wayoftime.bloodmagic.util.helper.PlayerSacrificeHelper.getAltar(level, sacrificingEntity.blockPosition());

            if (altar == null) {
                return false;
            }

            if (altar.getCurrentBlood() + amount > altar.getCapacity()) {
                return false;
            }

            altar.sacrificialDaggerCall(amount, true);
            altar.startCycle();
            return true;
        } else {
            // Standard mode - use Blood Magic's helper which searches for altars
            return wayoftime.bloodmagic.util.helper.PlayerSacrificeHelper.findAndFillAltar(
                level,
                sacrificingEntity,
                amount,
                true
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
