package com.teamdman.animus.items;

import com.teamdman.animus.registry.AnimusDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/**
 * Mob Soul - stores a captured entity
 * Right-click on a block to release the captured mob
 */
public class ItemMobSoul extends Item {
    public ItemMobSoul() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();

        String entityName = stack.get(AnimusDataComponents.SOUL_ENTITY_NAME.get());
        if (entityName == null) {
            return InteractionResult.FAIL;
        }

        Optional<EntityType<?>> entityTypeOpt = EntityType.byString(entityName);
        if (entityTypeOpt.isEmpty()) {
            return InteractionResult.FAIL;
        }

        EntityType<?> entityType = entityTypeOpt.get();

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

        Entity entity = entityType.create(level);
        if (entity == null) {
            return InteractionResult.FAIL;
        }

        CompoundTag entityData = stack.get(AnimusDataComponents.SOUL_DATA.get());
        if (entityData != null) {
            entity.load(entityData);
        }

        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
            entity.getYRot(), entity.getXRot());

        if (!level.addFreshEntity(entity)) {
            return InteractionResult.FAIL;
        }

        if (entity instanceof net.minecraft.world.entity.Mob mob) {
            mob.finalizeSpawn(
                (net.minecraft.server.level.ServerLevel) level,
                level.getCurrentDifficultyAt(pos),
                MobSpawnType.MOB_SUMMONED,
                null
            );
        }

        stack.shrink(1);

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String entityName = stack.get(AnimusDataComponents.SOUL_ENTITY_NAME.get());
        if (entityName != null) {
            EntityType.byString(entityName).ifPresent(entityType -> {
                tooltip.add(Component.translatable("tooltip.animus.mob_soul.type")
                    .append(": ")
                    .append(entityType.getDescription()));
            });
        }

        String soulName = stack.get(AnimusDataComponents.SOUL_NAME.get());
        if (soulName != null) {
            tooltip.add(Component.literal(soulName));
        }

        tooltip.add(Component.translatable("tooltip.animus.mob_soul.info"));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        String soulName = stack.get(AnimusDataComponents.SOUL_NAME.get());
        if (soulName != null) {
            return Component.literal(soulName);
        }
        return super.getName(stack);
    }
}
