package com.teamdman.animus.blocks;

import com.teamdman.animus.blockentities.BlockEntityWillfulStone;
import com.teamdman.animus.items.ItemKeyBinding;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.util.InventorySearchHelper;
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
import com.breakinblocks.neovitae.common.item.IBindable;
import com.breakinblocks.neovitae.common.datacomponent.Binding;

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

    private static final float HARDNESS = 1.5F;

    public BlockWillfulStone() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(HARDNESS, 3600000.0F)
            .sound(SoundType.STONE)
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
        if (player.isCreative()) {
            return super.getDestroyProgress(state, player, level, pos);
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BlockEntityWillfulStone willfulStone) {
            UUID owner = willfulStone.getOwner();

            if (owner == null) {
                return super.getDestroyProgress(state, player, level, pos);
            }

            if (player.getUUID().equals(owner)) {
                return super.getDestroyProgress(state, player, level, pos);
            }

            if (hasKeyOfBinding(player, owner)) {
                return super.getDestroyProgress(state, player, level, pos);
            }

            return 0.0F;
        }

        return super.getDestroyProgress(state, player, level, pos);
    }

    private boolean hasKeyOfBinding(Player player, UUID owner) {
        if (InventorySearchHelper.hasItem(player, stack -> isKeyOfBindingForOwner(stack, owner))) {
            return true;
        }

        var curiosOpt = CuriosApi.getCuriosInventory(player);
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

    private boolean isKeyOfBindingForOwner(ItemStack stack, UUID owner) {
        if (stack.isEmpty()) {
            return false;
        }

        if (stack.getItem() != AnimusItems.KEY_BINDING.get()) {
            return false;
        }

        if (stack.getItem() instanceof IBindable bindable) {
            Binding binding = bindable.getBinding(stack);
            if (binding != null && !binding.isEmpty()) {
                return binding.uuid().equals(owner);
            }
        }

        return false;
    }
}
