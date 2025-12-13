package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.item.Scroll;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import wayoftime.bloodmagic.common.datacomponent.SoulNetwork;
import wayoftime.bloodmagic.util.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.util.helper.SoulNetworkHelper;

import java.util.function.Consumer;

/**
 * Ritual of Arcane Mastery
 * Teaches or upgrades spells from scrolls placed in nearby chests
 *
 * Features:
 * - Searches for chests within 5 blocks of Master Ritual Stone
 * - Looks for Iron's Spells scrolls in chests
 * - Grants spell to player if not known
 * - Upgrades spell level by +1 if already known
 * - LP cost scales with spell rarity and current level
 *
 * Activation Cost: 10,000 LP
 * Refresh Time: 40 ticks (2 seconds)
 * Range: 5 blocks horizontal/vertical
 *
 * Note: Do NOT add @RitualRegister here - registration is done in IronsSpellsCompat
 */
public class RitualArcaneMastery extends Ritual {

    private static final int SEARCH_RANGE = 5;

    // Base LP costs by spell rarity
    private static final int COMMON_LP = 5000;
    private static final int UNCOMMON_LP = 10000;
    private static final int RARE_LP = 25000;
    private static final int EPIC_LP = 50000;
    private static final int LEGENDARY_LP = 100000;

    public RitualArcaneMastery() {
        super(
            Constants.Rituals.ARCANE_MASTERY,
            0,
            10000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.ARCANE_MASTERY
        );
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Get owner
        ServerPlayer owner = (ServerPlayer) level.getPlayerByUUID(mrs.getOwner());
        if (owner == null) {
            return;
        }

        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(mrs.getOwner());
        if (network == null) {
            return;
        }

        // Search for chests in range
        for (BlockPos pos : BlockPos.betweenClosed(
            masterPos.offset(-SEARCH_RANGE, -SEARCH_RANGE, -SEARCH_RANGE),
            masterPos.offset(SEARCH_RANGE, SEARCH_RANGE, SEARCH_RANGE)
        )) {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof ChestBlockEntity chest)) {
                continue;
            }

            // Check chest for spell scrolls
            if (processChest(chest, owner, network, serverLevel, masterPos)) {
                // Successfully processed a scroll, stop for this tick
                return;
            }
        }

        // No scrolls found, emit smoke
        emitSmokeParticles(serverLevel, masterPos);
    }

    /**
     * Process a chest looking for spell scrolls
     * Returns true if a scroll was successfully processed
     */
    private boolean processChest(Container chest, ServerPlayer player, SoulNetwork network,
                                 ServerLevel level, BlockPos ritualPos) {
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);

            if (!(stack.getItem() instanceof Scroll)) {
                continue;
            }

            // Found a scroll, try to process it using ISpellContainer
            SpellData spellData = ISpellContainer.get(stack).getSpellAtIndex(0);
            AbstractSpell spell = spellData.getSpell();
            int scrollLevel = spellData.getLevel();
            String spellId = spell.getSpellId();

            if (spellId == null || spellId.isEmpty()) {
                continue;
            }

            if (spell == null) {
                continue;
            }

            // Determine target level for the new scroll
            // Ritual upgrades the scroll by +1 level (up to max level)
            int maxLevel = spell.getMaxLevel();
            int targetLevel = Math.min(scrollLevel + 1, maxLevel);

            // If already at max level, skip this scroll
            if (scrollLevel >= maxLevel) {
                continue;
            }

            // Calculate LP cost
            int baseCost = getLPCostForRarity(spell.getRarity(targetLevel));
            int totalCost = baseCost * scrollLevel; // Cost scales with current scroll level

            // Check if player has enough LP
            if (network.getCurrentEssence() < totalCost) {
                player.displayClientMessage(
                    Component.literal("Not enough LP! Need " + totalCost + " LP")
                        .withStyle(ChatFormatting.RED),
                    true
                );
                network.causeNausea();
                return false;
            }

            // Consume LP
            network.syphon(new SoulTicket(
                Component.literal("Arcane Mastery: " + spell.getDisplayName(null).getString()),
                totalCost
            ), false);

            // Create an upgraded scroll and give it to the player
            ItemStack upgradedScroll = new ItemStack(io.redspace.ironsspellbooks.registries.ItemRegistry.SCROLL.get());
            ISpellContainer.createScrollContainer(spell, targetLevel, upgradedScroll).save(upgradedScroll);

            // Give the upgraded scroll to the player
            if (!player.getInventory().add(upgradedScroll)) {
                player.drop(upgradedScroll, false);
            }

            // Remove original scroll from chest
            stack.shrink(1);
            chest.setItem(i, stack);

            // Feedback
            player.displayClientMessage(
                Component.literal("Upgraded: ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(spell.getDisplayName(null).copy().withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(Component.literal(" Level " + scrollLevel + " → " + targetLevel).withStyle(ChatFormatting.YELLOW)),
                false
            );

            // Visual and audio effects
            spawnSuccessParticles(level, ritualPos);
            level.playSound(
                null,
                ritualPos,
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.BLOCKS,
                1.0F,
                1.2F
            );

            Animus.LOGGER.info("Ritual of Arcane Mastery: Player {} upgraded spell {} from level {} to {}",
                player.getName().getString(),
                spell.getDisplayName(null).getString(),
                scrollLevel,
                targetLevel);

            return true;
        }

        return false;
    }

    /**
     * Get LP cost based on spell rarity
     */
    private int getLPCostForRarity(io.redspace.ironsspellbooks.api.spells.SpellRarity rarity) {
        return switch (rarity) {
            case COMMON -> COMMON_LP;
            case UNCOMMON -> UNCOMMON_LP;
            case RARE -> RARE_LP;
            case EPIC -> EPIC_LP;
            case LEGENDARY -> LEGENDARY_LP;
        };
    }

    /**
     * Emit smoke particles when no scrolls are found
     */
    private void emitSmokeParticles(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.5;
            level.sendParticles(
                ParticleTypes.SMOKE,
                x, y, z,
                1,
                0.0, 0.05, 0.0,
                0.01
            );
        }
    }

    /**
     * Spawn success particles when spell is learned/upgraded
     */
    private void spawnSuccessParticles(ServerLevel level, BlockPos pos) {
        // Enchantment glyphs
        for (int i = 0; i < 20; i++) {
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 2;
            double y = pos.getY() + 0.5 + level.random.nextDouble() * 2;
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 2;
            level.sendParticles(
                ParticleTypes.ENCHANT,
                x, y, z,
                1,
                0.0, 0.2, 0.0,
                0.5
            );
        }

        // Soul particles for demonic flavor
        for (int i = 0; i < 10; i++) {
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5);
            double y = pos.getY() + 0.5 + level.random.nextDouble();
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5);
            level.sendParticles(
                ParticleTypes.SOUL,
                x, y, z,
                1,
                0, 0.1, 0,
                0.1
            );
        }
    }

    @Override
    public int getRefreshTime() {
        return 40; // 2 seconds
    }

    @Override
    public int getRefreshCost() {
        return 0; // Cost is per spell learned/upgraded
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        // Create a knowledge-themed pattern
        // Dusk runes represent arcane knowledge and darkness
        // Air runes represent mental clarity and understanding

        // Inner circle - Cardinal directions with Dusk runes (arcane knowledge)
        addRune(components, 0, 0, -2, EnumRuneType.DUSK);
        addRune(components, 0, 0, 2, EnumRuneType.DUSK);
        addRune(components, -2, 0, 0, EnumRuneType.DUSK);
        addRune(components, 2, 0, 0, EnumRuneType.DUSK);

        // Middle ring - Diagonals with Air runes (mental clarity)
        addRune(components, -2, 0, -2, EnumRuneType.AIR);
        addRune(components, -2, 0, 2, EnumRuneType.AIR);
        addRune(components, 2, 0, -2, EnumRuneType.AIR);
        addRune(components, 2, 0, 2, EnumRuneType.AIR);

        // Outer ring - Extended pattern for mastery
        addRune(components, 0, 0, -3, EnumRuneType.DUSK);
        addRune(components, 0, 0, 3, EnumRuneType.DUSK);
        addRune(components, -3, 0, 0, EnumRuneType.DUSK);
        addRune(components, 3, 0, 0, EnumRuneType.DUSK);

        // Corner accents - Air runes for the flow of knowledge
        addRune(components, -3, 0, -3, EnumRuneType.AIR);
        addRune(components, -3, 0, 3, EnumRuneType.AIR);
        addRune(components, 3, 0, -3, EnumRuneType.AIR);
        addRune(components, 3, 0, 3, EnumRuneType.AIR);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualArcaneMastery();
    }
}
