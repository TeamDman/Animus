package com.teamdman.animus.blocks;

import com.teamdman.animus.blockentities.BlockEntityWillfulStone;
import com.teamdman.animus.items.ItemKeyBinding;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.core.data.Binding;

import java.util.UUID;

/**
 * Willful Stone - A protective block that remembers its placer
 * <p>
 * Features:
 * - Only the placer or creative players can break it
 * - Players with a Key of Binding bound to the owner can also break it
 * - As strong and blast resistant as bedrock
 * - Available in 16 dye colors
 */
public class BlockWillfulStone extends Block implements EntityBlock {

    public BlockWillfulStone() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(-1.0F, 3600000.0F) // Same as bedrock
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityWillfulStone(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide && placer instanceof Player player) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityWillfulStone willfulStone) {
                willfulStone.setOwner(player.getUUID());
            }
        }
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        // Creative players can always break it
        if (player.isCreative()) {
            return super.getDestroyProgress(state, player, level, pos);
        }

        // Check ownership
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BlockEntityWillfulStone willfulStone) {
            UUID owner = willfulStone.getOwner();

            // If no owner set, allow breaking
            if (owner == null) {
                return super.getDestroyProgress(state, player, level, pos);
            }

            // Check if player is the owner
            if (player.getUUID().equals(owner)) {
                return super.getDestroyProgress(state, player, level, pos);
            }

            // Check if player has a Key of Binding bound to the owner
            if (hasKeyOfBinding(player, owner)) {
                return super.getDestroyProgress(state, player, level, pos);
            }

            // Not authorized - make it unbreakable
            return 0.0F;
        }

        // No block entity, allow breaking
        return super.getDestroyProgress(state, player, level, pos);
    }

    /**
     * Checks if the player has a Key of Binding bound to the specified owner
     */
    private boolean hasKeyOfBinding(Player player, UUID owner) {
        // Check main inventory
        for (ItemStack stack : player.getInventory().items) {
            if (isKeyOfBindingForOwner(stack, owner)) {
                return true;
            }
        }

        // Check offhand
        for (ItemStack stack : player.getInventory().offhand) {
            if (isKeyOfBindingForOwner(stack, owner)) {
                return true;
            }
        }

        // Check curios slots
        var curiosOpt = CuriosApi.getCuriosInventory(player).resolve();
        if (curiosOpt.isPresent()) {
            var curios = curiosOpt.get();
            var handler = curios.getEquippedCurios();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (isKeyOfBindingForOwner(stack, owner)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if an ItemStack is a Key of Binding bound to the specified owner
     */
    private boolean isKeyOfBindingForOwner(ItemStack stack, UUID owner) {
        if (stack.isEmpty()) {
            return false;
        }

        // Check if it's a Key of Binding item
        if (stack.getItem() != AnimusItems.KEY_BINDING.get()) {
            return false;
        }

        // Check if bound to the owner using Blood Magic's binding system
        if (stack.getItem() instanceof IBindable bindable) {
            Binding binding = bindable.getBinding(stack);
            if (binding != null) {
                return binding.getOwnerId().equals(owner);
            }
        }

        return false;
    }
}
