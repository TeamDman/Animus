package com.teamdman.animus.items.sigils;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import wayoftime.bloodmagic.ritual.AreaDescriptor;

import java.util.List;
import java.util.Optional;

import static com.teamdman.animus.rituals.RitualNaturesLeech.isConsumable;

/**
 * Sigil of Nature's Leech - consumes plants and organic matter to restore hunger
 * When activated, automatically consumes consumable blocks from inventory or nearby world
 * to feed the player
 */
public class ItemSigilLeech extends ItemSigilToggleableBase {
    private final AreaDescriptor eatRange = new AreaDescriptor.Rectangle(new BlockPos(-5, 0, -5), 10);

    public ItemSigilLeech() {
        super(Constants.Sigils.LEECH, 5);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player.isShiftKeyDown()) {
            // Toggle activation
            setActivatedState(stack, !getActivated(stack));
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!getActivated(stack) || !(entity instanceof Player) || entity instanceof FakePlayer) {
            return;
        }

        Player player = (Player) entity;

        // Check if sigil is bound to the player
        var binding = getBinding(stack);
        if (binding == null || !binding.getOwnerId().equals(player.getUUID())) {
            return;
        }

        // Only consume food if player can eat or is sneaking
        if (!player.canEat(false) && !player.isShiftKeyDown()) {
            return;
        }

        // Try to eat from inventory first, then from the world
        if (eatFromInventory(player) || eatFromSurroundingWorld(player, level)) {
            if (!level.isClientSide) {
                // Consume LP
                var network = wayoftime.bloodmagic.util.helper.NetworkHelper.getSoulNetwork(player);
                var ticket = new wayoftime.bloodmagic.core.data.SoulTicket(
                    net.minecraft.network.chat.Component.translatable(Constants.Localizations.Text.TICKET_LEECH),
                    getLpUsed()
                );

                var syphonResult = network.syphonAndDamage(player, ticket);
                if (!syphonResult.isSuccess()) {
                    // Not enough LP - deactivate sigil
                    setActivatedState(stack, false);
                    return;
                }

                // Restore hunger
                int foodAmount = 1 + level.random.nextInt(3);
                player.getFoodData().eat(foodAmount, 2.0F);
            }
        }

        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    private boolean eatFromInventory(Player player) {
        // Find consumable blocks in inventory
        Optional<ItemStack> food = player.getInventory().items.stream()
            .filter(stack -> !stack.isEmpty())
            .filter(stack -> {
                Block block = Block.byItem(stack.getItem());
                return block != Blocks.AIR && isConsumable(block);
            })
            .findFirst();

        if (food.isPresent()) {
            ItemStack foodStack = food.get();
            int shrinkAmount = Math.min(player.level().random.nextInt(4), foodStack.getCount());
            if (shrinkAmount > 0) {
                foodStack.shrink(shrinkAmount);
                return true;
            }
        }

        return false;
    }

    private boolean eatFromSurroundingWorld(Player player, Level level) {
        if (!eatRange.hasNext()) {
            eatRange.resetIterator();
        }

        // Check up to 32 blocks per tick
        for (int i = 0; i < 32 && eatRange.hasNext(); i++) {
            BlockPos eatPos = eatRange.next().offset(player.blockPosition());
            BlockState state = level.getBlockState(eatPos);
            Block eatBlock = state.getBlock();

            if (!isConsumable(eatBlock)) {
                continue;
            }

            // Spawn particles
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.ENCHANT,
                    eatPos.getX() + 0.5,
                    eatPos.getY() + 0.5,
                    eatPos.getZ() + 0.5,
                    5,
                    (level.random.nextDouble() - 0.5) * 2.0,
                    -level.random.nextDouble(),
                    (level.random.nextDouble() - 0.5) * 2.0,
                    0.1
                );
            }

            // Remove the block
            level.removeBlock(eatPos, false);

            // Play sound
            level.playSound(
                null,
                eatPos,
                AnimusSounds.NATURESLEECH.get(),
                SoundSource.BLOCKS,
                0.4F,
                1.0F
            );

            return true;
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_LEECH_FLAVOUR));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_LEECH_INFO));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
