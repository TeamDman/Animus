package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.entities.EntityThrownSpear;
import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.breakinblocks.neovitae.common.item.IBindable;
import com.breakinblocks.neovitae.common.datacomponent.Binding;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.common.datamap.EntitySacrificeHelper;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;

import java.util.List;
import java.util.function.Consumer;

/**
 * Bound Spear - A soul-bound javelin that can be toggled between active/deactivated modes
 *
 * Deactivated Mode: Behaves like a diamond spear but unbreakable
 * Active Mode: Has AOE attacks, sacrifices entities to altars, costs 50LP per attack/throw
 *
 * Sneak + Right-Click: Bind (if unbound) or toggle active/deactivated (if bound)
 */
public class ItemSpearBound extends ItemSpear implements IBindable {
    private static final int EV_COST = 50;

    public ItemSpearBound(Properties props) {
        super(ToolMaterial.DIAMOND, props.enchantable(ToolMaterial.GOLD.enchantmentValue()));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

    public boolean isActivated(ItemStack stack) {
        Boolean activated = stack.get(AnimusDataComponents.SPEAR_ACTIVATED.get());
        return activated != null && activated;
    }

    public void setActivated(ItemStack stack, boolean activated) {
        stack.set(AnimusDataComponents.SPEAR_ACTIVATED.get(), activated);
    }

    private boolean consumeEV(Player player, ItemStack stack) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        Binding binding = getBinding(stack);
        if (binding == null) {
            return false;
        }

        // Use the binding owner's network, not the using player's
        IAnima network = NeoVitaeAPI.getInstance().getAnima(binding.uuid());
        AnimaTicket ticket = AnimaTicket.create(EV_COST);

        var result = network.syphonAndDamage(player, ticket);
        return result.success();
    }

    private int getRiptideLevel(ItemStack stack, Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return stack.getEnchantmentLevel(serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.RIPTIDE));
        }
        return 0;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Shield blocking overrides the toggle mechanic
        if (player.isShiftKeyDown()) {
            InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack otherStack = player.getItemInHand(otherHand);
            if (otherStack.getItem() instanceof ShieldItem) {
                return InteractionResult.PASS;
            }
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                Binding binding = getBinding(stack);

                if (binding == null) {
                    onBind(player, stack);
                    player.sendOverlayMessage(
                        Component.translatable(Constants.Localizations.Text.SPEAR_BOUND_SUCCESS)
                            .withStyle(ChatFormatting.AQUA));
                } else {
                    boolean wasActivated = isActivated(stack);
                    setActivated(stack, !wasActivated);

                    if (!wasActivated) {
                        player.sendOverlayMessage(
                            Component.translatable(Constants.Localizations.Text.SPEAR_ACTIVATED)
                                .withStyle(ChatFormatting.GREEN));
                    } else {
                        player.sendOverlayMessage(
                            Component.translatable(Constants.Localizations.Text.SPEAR_DEACTIVATED)
                                .withStyle(ChatFormatting.GRAY));
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (getRiptideLevel(stack, level) > 0 && !player.isInWaterOrRain()) {
            return InteractionResult.FAIL;
        } else {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int useDuration = this.getUseDuration(stack, entity) - timeLeft;
            if (useDuration >= 10) {
                int riptide = getRiptideLevel(stack, level);

                // If activated, check for EV cost (server-side only)
                if (isActivated(stack) && !level.isClientSide() && !consumeEV(player, stack)) {
                    player.sendOverlayMessage(
                        Component.translatable(Constants.Localizations.Text.SPEAR_NO_EV_THROW)
                            .withStyle(ChatFormatting.RED));
                    return false;
                }

                if (riptide <= 0 || player.isInWaterOrRain()) {
                    if (!level.isClientSide()) {
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
                        float xSpeed = -Mth.sin(yaw * ((float)Math.PI / 180F)) * Mth.cos(pitch * ((float)Math.PI / 180F));
                        float ySpeed = -Mth.sin(pitch * ((float)Math.PI / 180F));
                        float zSpeed = Mth.cos(yaw * ((float)Math.PI / 180F)) * Mth.cos(pitch * ((float)Math.PI / 180F));
                        float length = Mth.sqrt(xSpeed * xSpeed + ySpeed * ySpeed + zSpeed * zSpeed);
                        float multiplier = 3.0F * ((1.0F + (float)riptide) / 4.0F);
                        xSpeed = xSpeed * (multiplier / length);
                        ySpeed = ySpeed * (multiplier / length);
                        zSpeed = zSpeed * (multiplier / length);
                        player.push((double)xSpeed, (double)ySpeed, (double)zSpeed);
                        player.startAutoSpinAttack(20, 8.0F + (float)riptide * 2.0F, stack);
                        if (player.onGround()) {
                            player.move(MoverType.SELF, new Vec3(0.0, 1.2, 0.0));
                        }

                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }

                    return true;
                }
            }
        }

        return false;
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
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!isActivated(stack)) {
            super.hurtEnemy(stack, target, attacker);
            return;
        }

        if (attacker instanceof Player player) {
            if (!consumeEV(player, stack)) {
                player.sendOverlayMessage(
                    Component.translatable(Constants.Localizations.Text.SPEAR_NO_EV_ATTACK)
                        .withStyle(ChatFormatting.RED));
                return;
            }
        }

        super.hurtEnemy(stack, target, attacker);

        Level level = target.level();

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        double x = target.getX();
        double y = target.getY();
        double z = target.getZ();

        // Apply AOE damage to nearby entities
        // If a Ara Vitae is nearby, all enemies are sacrificed to it (instant kill)
        // If no altar is nearby, normal AOE damage is dealt
        checkAndDamage(x, y, z, serverLevel, attacker);
    }

    private boolean checkAndDamage(double x, double y, double z, ServerLevel level, LivingEntity attacker) {
        int range = 5;
        boolean hit = false;

        AABB region = new AABB(x - range, y - range, z - range, x + range, y + range, z + range);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, region);

        if (entities.isEmpty()) {
            return false;
        }

        float damage = 6.0F + getMaterial().attackDamageBonus();

        for (LivingEntity target : entities) {
            if (target == null || target.isDeadOrDying() || !(attacker instanceof Player) || attacker == target) {
                continue;
            }

            if (target.canTeleport(level, level) && !(target instanceof Player)) {
                if (target.is(Constants.Tags.DISALLOW_SACRIFICE)) {
                    if (attacker instanceof Player playerAttacker) {
                        playerAttacker.sendOverlayMessage(
                            Component.translatable(Constants.Localizations.Text.SACRIFICE_TOO_POWERFUL));
                    }
                } else {
                    int ev = getEntitySacrificeValue(target);

                    if (ev > 0 && findAndFillAltar(level, target, ev)) {
                        level.playSound(
                            null,
                            target.getX(),
                            target.getY(),
                            target.getZ(),
                            SoundEvents.FIRE_EXTINGUISH,
                            SoundSource.BLOCKS,
                            0.5F,
                            2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F
                        );
                        target.setHealth(-1);
                        target.die(level.damageSources().genericKill());
                        hit = true;
                        continue;
                    }
                }
            }

            boolean result = target.hurtServer(level, level.damageSources().genericKill(), damage);
            if (result) {
                hit = true;
            }
        }

        return hit;
    }


    /**
     * Calculates entity sacrifice value using NeoVitae's entity sacrifice datamap.
     * Full kill = EV per damage × max health (with optional cap for bosses).
     * Values can be customized via datapacks at:
     * data/<namespace>/data_maps/entity_type/entity_sacrifice_value.json
     */
    private int getEntitySacrificeValue(LivingEntity entity) {
        // Use the datamap to calculate full sacrifice value (kill = max health worth of damage)
        int evValue = EntitySacrificeHelper.calculateEV(entity, entity.getMaxHealth());

        // Baby entities give half value
        if (entity.isBaby()) {
            evValue /= 2;
        }

        return Math.max(evValue, 50); // Minimum 50 EV
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        Binding binding = getBinding(stack);
        boolean activated = isActivated(stack);

        if (binding != null) {
            tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SPEAR_BOUND_TO, binding.name())
                .withStyle(ChatFormatting.AQUA));

            if (activated) {
                tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SPEAR_STATUS_ACTIVATED)
                    .withStyle(ChatFormatting.GREEN));
                tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SPEAR_COST)
                    .withStyle(ChatFormatting.GOLD));
                tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SPEAR_FIRST));
                tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SPEAR_SECOND));
            } else {
                tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SPEAR_STATUS_DEACTIVATED)
                    .withStyle(ChatFormatting.GRAY));
                tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SPEAR_BEHAVES_DIAMOND)
                    .withStyle(ChatFormatting.GRAY));
            }

            tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SPEAR_TOGGLE)
                .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SPEAR_UNBOUND)
                .withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.SPEAR_BIND)
                .withStyle(ChatFormatting.YELLOW));
        }

    }

    private boolean findAndFillAltar(Level level, LivingEntity entity, int ev) {
        BlockPos center = entity.blockPosition();
        int searchRadius = 8;

        for (BlockPos pos : BlockPos.betweenClosed(
            center.offset(-searchRadius, -searchRadius, -searchRadius),
            center.offset(searchRadius, searchRadius, searchRadius)
        )) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AraVitaeTile altar) {
                int currentBlood = altar.getCurrentBlood();
                int capacity = altar.getMainCapacity();
                if (currentBlood < capacity) {
                    altar.addSacrificeEV(ev, true);
                    return true;
                }
            }
        }
        return false;
    }
}
