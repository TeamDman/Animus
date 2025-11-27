package com.teamdman.animus.items.sigils;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.List;

/**
 * Sigil of Chains - captures entities into soul items
 * Consumes 500 LP to capture a living entity into a mob soul item
 */
public class ItemSigilChains extends AnimusSigilBase {
    public ItemSigilChains() {
        super(Constants.Sigils.CHAINS, 500);
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

        // Consume LP and check if player has enough
        SoulNetwork network = NetworkHelper.getSoulNetwork(player);
        SoulTicket ticket = new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_CHAINS),
            getLpUsed()
        );

        var result = network.syphonAndDamage(player, ticket);
        if (!result.isSuccess()) {
            return InteractionResult.FAIL;
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

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_CHAINS_FLAVOUR));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_CHAINS_INFO));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
