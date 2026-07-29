package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Mob Soul - stores a captured entity
 * Right-click on a block to release the captured mob
 */
public class ItemMobSoul extends Item {
    public ItemMobSoul(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (level.isClientSide()) {
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

        Entity entity = entityType.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (entity == null) {
            return InteractionResult.FAIL;
        }

        CompoundTag entityData = stack.get(AnimusDataComponents.SOUL_DATA.get());
        if (entityData != null) {
            entity.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), entityData));
        }

        entity.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
            entity.getYRot(), entity.getXRot());

        if (!level.addFreshEntity(entity)) {
            return InteractionResult.FAIL;
        }

        if (entity instanceof Mob mob && level instanceof ServerLevel serverLevel) {
            mob.finalizeSpawn(
                serverLevel,
                serverLevel.getCurrentDifficultyAt(pos),
                EntitySpawnReason.MOB_SUMMONED,
                null
            );
        }

        stack.shrink(1);

        return InteractionResult.CONSUME;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        String entityName = stack.get(AnimusDataComponents.SOUL_ENTITY_NAME.get());
        if (entityName != null) {
            EntityType.byString(entityName).ifPresent(entityType -> {
                tooltip.accept(Component.translatable("tooltip.animusnv.mob_soul.type")
                    .append(": ")
                    .append(entityType.getDescription()));
            });
        }

        String soulName = stack.get(AnimusDataComponents.SOUL_NAME.get());
        if (soulName != null) {
            tooltip.accept(Component.literal(soulName));
        }

        tooltip.accept(Component.translatable("tooltip.animusnv.mob_soul.info"));
        super.appendHoverText(stack, context, display, tooltip, flag);
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
