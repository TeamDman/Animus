package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
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
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains(Constants.NBT.SOUL_ENTITY_NAME)) {
            return InteractionResult.FAIL;
        }

        // Get entity type from NBT
        String entityName = tag.getString(Constants.NBT.SOUL_ENTITY_NAME);
        Optional<EntityType<?>> entityTypeOpt = EntityType.byString(entityName);

        if (entityTypeOpt.isEmpty()) {
            return InteractionResult.FAIL;
        }

        EntityType<?> entityType = entityTypeOpt.get();

        // Get spawn position (on top of clicked block)
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

        // Create entity
        Entity entity = entityType.create(level);
        if (entity == null) {
            return InteractionResult.FAIL;
        }

        // Load entity data from NBT
        if (tag.contains(Constants.NBT.SOUL_DATA)) {
            CompoundTag entityData = tag.getCompound(Constants.NBT.SOUL_DATA);
            entity.load(entityData);
        }

        // Set position
        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
            entity.getYRot(), entity.getXRot());

        // Spawn entity
        if (!level.addFreshEntity(entity)) {
            return InteractionResult.FAIL;
        }

        // Trigger spawn event for mobs
        if (entity instanceof net.minecraft.world.entity.Mob mob) {
            mob.finalizeSpawn(
                (net.minecraft.server.level.ServerLevel) level,
                level.getCurrentDifficultyAt(pos),
                MobSpawnType.MOB_SUMMONED,
                null,
                null
            );
        }

        // Consume the item
        stack.shrink(1);

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            // Show entity type
            if (tag.contains(Constants.NBT.SOUL_ENTITY_NAME)) {
                String entityName = tag.getString(Constants.NBT.SOUL_ENTITY_NAME);
                EntityType.byString(entityName).ifPresent(entityType -> {
                    tooltip.add(Component.translatable("tooltip.animus.mob_soul.type")
                        .append(": ")
                        .append(entityType.getDescription()));
                });
            }

            // Show custom name if present
            if (tag.contains(Constants.NBT.SOUL_NAME)) {
                tooltip.add(Component.literal(tag.getString(Constants.NBT.SOUL_NAME)));
            }
        }

        tooltip.add(Component.translatable("tooltip.animus.mob_soul.info"));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(Constants.NBT.SOUL_NAME)) {
            return Component.literal(tag.getString(Constants.NBT.SOUL_NAME));
        }
        return super.getName(stack);
    }
}
