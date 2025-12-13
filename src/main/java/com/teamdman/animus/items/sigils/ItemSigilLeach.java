package com.teamdman.animus.items.sigils;

import com.teamdman.animus.AnimusConfig;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.util.SoulTicket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.will.WorldDemonWillHandler;
import wayoftime.bloodmagic.ritual.AreaDescriptor;

import java.util.List;
import java.util.Optional;

import static com.teamdman.animus.rituals.RitualNaturesLeach.isConsumable;

/**
 * Sigil of Nature's Leach - consumes plants and organic matter to restore hunger
 * When activated, automatically consumes consumable blocks from inventory or nearby world
 * to feed the player
 */
public class ItemSigilLeach extends ItemSigilToggleableBase {
    private AreaDescriptor eatRange;
    private int lastConfigRange = -1;

    public ItemSigilLeach() {
        super(Constants.Sigils.LEACH, 5);
    }

    /**
     * Get or create the area descriptor based on current config value
     */
    private AreaDescriptor getEatRange() {
        int configRange = AnimusConfig.sigils.leachRange.get();
        if (eatRange == null || lastConfigRange != configRange) {
            // Range is from -range to +range, so size is range * 2 + 1
            int size = configRange * 2 + 1;
            eatRange = new AreaDescriptor.Rectangle(new BlockPos(-configRange, 0, -configRange), size, 1, size);
            lastConfigRange = configRange;
        }
        return eatRange;
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
        if (binding == null || binding.isEmpty() || !binding.uuid().equals(player.getUUID())) {
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
                var network = wayoftime.bloodmagic.util.helper.SoulNetworkHelper.getSoulNetwork(player);
                var ticket = SoulTicket.create(getLpUsed());

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
        AreaDescriptor range = getEatRange();
        BlockPos playerPos = player.blockPosition();

        // Iterate through contained positions
        int checked = 0;
        for (BlockPos eatPos : range.getContainedPositions(playerPos)) {
            if (checked++ >= 32) break; // Check up to 32 blocks per tick
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

            // Destroy the block (respects protections like FTB Chunks)
            level.destroyBlock(eatPos, false);

            // Play sound
            level.playSound(
                null,
                eatPos,
                AnimusSounds.NATURESLEACH.get(),
                SoundSource.BLOCKS,
                0.4F,
                1.0F
            );

            // TODO: Blood Magic 4.x changed the WorldDemonWillHandler API
            // The fillWillToMaximum method signature has changed - needs investigation
            // Generate corrosive demon will when consuming blocks from the world
            // Each block consumed generates 0.3-0.8 corrosive will
            // if (!level.isClientSide) {
            //     double willToAdd = 0.3 + level.random.nextDouble() * 0.5; // 0.3-0.8
            //     WorldDemonWillHandler.fillWillToMaximum(
            //         level,
            //         player.blockPosition(),
            //         EnumWillType.CORROSIVE,
            //         willToAdd,
            //         100, // max will
            //         true
            //     );
            // }

            return true;
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_LEACH_FLAVOUR));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_LEACH_INFO));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
