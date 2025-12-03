package com.teamdman.animus.items.sigils;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.List;

/**
 * Sigil of Chains - captures entities into soul items
 * Consumes 500 LP to capture a living entity into a mob soul item
 *
 * Uses raycasting in use() instead of interactLivingEntity() to work with Sigil of Holding
 */
public class ItemSigilChains extends AnimusSigilBase {
    private static final double CAPTURE_RANGE = 5.0;

    public ItemSigilChains() {
        super(Constants.Sigils.CHAINS, 500);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        // Check if sigil is bound to the player
        var binding = getBinding(stack);
        if (binding == null || !binding.getOwnerId().equals(player.getUUID())) {
            return InteractionResultHolder.fail(stack);
        }

        // Raycast to find entity player is looking at
        LivingEntity target = getTargetEntity(player);
        if (target == null) {
            return InteractionResultHolder.pass(stack);
        }

        // Try to capture the target
        if (captureEntity(player, target, stack)) {
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.PASS;
        }

        // Check if sigil is bound to the player
        var binding = getBinding(stack);
        if (binding == null || !binding.getOwnerId().equals(player.getUUID())) {
            return InteractionResult.FAIL;
        }

        // Try to capture the target
        if (captureEntity(player, target, stack)) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    /**
     * Get the living entity the player is looking at within capture range
     */
    private LivingEntity getTargetEntity(Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 reachVec = eyePos.add(lookVec.scale(CAPTURE_RANGE));

        AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(CAPTURE_RANGE)).inflate(1.0);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
            player,
            eyePos,
            reachVec,
            searchBox,
            entity -> entity instanceof LivingEntity && entity != player && entity.isAlive(),
            CAPTURE_RANGE * CAPTURE_RANGE
        );

        if (hitResult != null && hitResult.getEntity() instanceof LivingEntity living) {
            return living;
        }

        return null;
    }

    /**
     * Attempt to capture the target entity
     * @return true if capture was successful
     */
    private boolean captureEntity(Player player, LivingEntity target, ItemStack stack) {
        // Check if entity can be captured (before consuming LP)
        if (target.getType().is(Constants.Tags.DISALLOW_CAPTURING)) {
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.CHAINS_CAPTURE_FAILED),
                true
            );
            return false;
        }

        // Consume LP and check if player has enough
        SoulNetwork network = NetworkHelper.getSoulNetwork(player);
        SoulTicket ticket = new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_CHAINS),
            getLpUsed()
        );

        var result = network.syphonAndDamage(player, ticket);
        if (!result.isSuccess()) {
            return false;
        }

        // Create mob soul item
        ItemStack soul = new ItemStack(AnimusItems.MOBSOUL.get());
        CompoundTag tag = new CompoundTag();
        CompoundTag targetData = new CompoundTag();

        // Save entity data
        target.saveWithoutId(targetData);

        // Get entity type
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        if (entityId != null) {
            tag.putString(Constants.NBT.SOUL_ENTITY_NAME, entityId.toString());
        }

        // Save custom name if present
        if (target instanceof Mob && target.hasCustomName()) {
            tag.putString(Constants.NBT.SOUL_NAME, target.getCustomName().getString());
        }

        tag.put(Constants.NBT.SOUL_DATA, targetData);
        soul.setTag(tag);

        // Set display name
        String displayName = tag.contains(Constants.NBT.SOUL_NAME)
            ? tag.getString(Constants.NBT.SOUL_NAME)
            : target.getType().getDescription().getString() + " Soul";
        soul.setHoverName(Component.literal(displayName));

        // Give item to player or drop it
        if (!player.getInventory().add(soul)) {
            ItemEntity itemEntity = new ItemEntity(
                player.level(),
                target.getX(),
                target.getY(),
                target.getZ(),
                soul
            );
            player.level().addFreshEntity(itemEntity);
        }

        // Remove the captured entity
        target.discard();

        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_CHAINS_FLAVOUR));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_CHAINS_INFO));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
