package com.teamdman.animus.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.registry.AnimusSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;
import com.breakinblocks.neovitae.common.datacomponent.EnumWillType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.will.IDemonWillHandler;

import java.util.Optional;

import static com.teamdman.animus.rituals.RitualNaturesLeach.isConsumable;

/**
 * Sigil of Nature's Leach - consumes plants and organic matter to restore hunger.
 * When activated, automatically consumes consumable blocks from inventory or nearby world.
 */
public record LeachSigilEffect() implements ISigilEffect {
    public static final MapCodec<LeachSigilEffect> CODEC = MapCodec.unit(LeachSigilEffect::new);

    // Cached area descriptor
    private static AreaDescriptor cachedEatRange;
    private static int cachedConfigRange = -1;

    @Override
    public MapCodec<? extends ISigilEffect> codec() {
        return CODEC;
    }

    @Override
    public boolean isToggleable() {
        return true;
    }

    @Override
    public void activeTick(Level level, Player player, ItemStack stack, int itemSlot, boolean isSelected) {
        if (player instanceof FakePlayer) {
            return;
        }

        // Only consume food if player can eat or is sneaking
        if (!player.canEat(false) && !player.isShiftKeyDown()) {
            return;
        }

        // Try to eat from inventory first, then from the world
        if (eatFromInventory(player) || eatFromSurroundingWorld(player, level)) {
            if (!level.isClientSide) {
                // Restore hunger
                int foodAmount = 1 + level.random.nextInt(3);
                player.getFoodData().eat(foodAmount, 2.0F);
            }
        }
    }

    /**
     * Get or create the area descriptor based on current config value.
     */
    private static AreaDescriptor getEatRange() {
        int configRange = AnimusConfig.sigils.leachRange.get();
        if (cachedEatRange == null || cachedConfigRange != configRange) {
            int size = configRange * 2 + 1;
            cachedEatRange = new AreaDescriptor.Rectangle(new BlockPos(-configRange, 0, -configRange), size, 1, size);
            cachedConfigRange = configRange;
        }
        return cachedEatRange;
    }

    private boolean eatFromInventory(Player player) {
        Optional<ItemStack> food = player.getInventory().items.stream()
                .filter(s -> !s.isEmpty())
                .filter(s -> {
                    Block block = Block.byItem(s.getItem());
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

        int checked = 0;
        for (BlockPos eatPos : range.getContainedPositions(playerPos)) {
            if (checked++ >= 32) break;
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

            // Destroy the block
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

            // Generate corrosive demon will
            if (!level.isClientSide) {
                double willToAdd = 0.3 + level.random.nextDouble() * 0.5;
                IDemonWillHandler willHandler = NeoVitaeAPI.getInstance().getDemonWillHandler();
                double currentWill = willHandler.getCurrentWill(level, player.blockPosition(), EnumWillType.CORROSIVE);
                double maxWill = 100;
                double actualAdd = Math.min(willToAdd, maxWill - currentWill);
                if (actualAdd > 0) {
                    willHandler.addWill(
                            level,
                            player.blockPosition(),
                            EnumWillType.CORROSIVE,
                            actualAdd
                    );
                }
            }

            return true;
        }

        return false;
    }
}
