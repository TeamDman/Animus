package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import wayoftime.bloodmagic.common.item.ItemBloodOrb;
import wayoftime.bloodmagic.common.tile.TileAltar;

/**
 * Handles Blood Altar interactions for infusing spellbooks
 */
public class AltarInfusionHandler {

    /**
     * Handle right-click on Blood Altar with Blood-Infused Spellbook
     */
    @SubscribeEvent
    public static void onAltarRightClick(PlayerInteractEvent.RightClickBlock event) {
        // Only server-side
        if (event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);

        // Check if holding Blood-Infused Spellbook
        if (!(stack.getItem() instanceof ItemBloodInfusedSpellbook)) {
            return;
        }

        // Get altar tile entity
        if (!(level.getBlockEntity(pos) instanceof TileAltar altar)) {
            return;
        }

        // Check if spellbook can be upgraded
        if (!ItemBloodInfusedSpellbook.canUpgrade(stack)) {
            player.displayClientMessage(
                Component.literal("This spellbook is already at maximum infusion!")
                    .withStyle(ChatFormatting.RED),
                true
            );
            event.setCanceled(true);
            return;
        }

        int currentTier = ItemBloodInfusedSpellbook.getInfusionTier(stack);
        int nextTier = currentTier + 1;
        int lpCost = ItemBloodInfusedSpellbook.getUpgradeCost(stack);

        // Check if altar has enough LP
        int altarLP = altar.getCurrentBlood();
        if (altarLP < lpCost) {
            player.displayClientMessage(
                Component.literal("Altar needs " + lpCost + " LP (has " + altarLP + " LP)")
                    .withStyle(ChatFormatting.RED),
                true
            );
            event.setCanceled(true);
            return;
        }

        // Check if player has required Blood Orb tier
        int requiredOrbTier = getRequiredOrbTier(nextTier);
        if (!hasBloodOrbOfTier(player, requiredOrbTier)) {
            player.displayClientMessage(
                Component.literal("Requires " + getOrbName(requiredOrbTier) + " or higher!")
                    .withStyle(ChatFormatting.RED),
                true
            );
            event.setCanceled(true);
            return;
        }

        // Perform infusion!
        altar.sacrificialDaggerCall(-lpCost, false); // Negative amount to consume LP
        ItemBloodInfusedSpellbook.setInfusionTier(stack, nextTier);

        // Success message
        player.displayClientMessage(
            Component.literal("Spellbook infused to Tier " + nextTier + "!")
                .withStyle(ChatFormatting.GOLD),
            true
        );

        // Spawn effects
        spawnInfusionEffects(level, pos, nextTier);

        // Play sound
        level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

        Animus.LOGGER.info("Player {} infused spellbook to tier {} for {} LP",
            player.getName().getString(), nextTier, lpCost);

        event.setCanceled(true);
    }

    /**
     * Get required Blood Orb tier for infusion tier
     */
    private static int getRequiredOrbTier(int infusionTier) {
        return infusionTier; // Tier 1 needs orb tier 1, etc.
    }

    /**
     * Get Blood Orb name for display
     */
    private static String getOrbName(int tier) {
        return switch (tier) {
            case 1 -> "Weak Blood Orb";
            case 2 -> "Apprentice Blood Orb";
            case 3 -> "Magician's Blood Orb";
            case 4 -> "Master Blood Orb";
            case 5 -> "Archmage's Blood Orb";
            case 6 -> "Transcendent Blood Orb";
            default -> "Blood Orb";
        };
    }

    /**
     * Check if player has Blood Orb of required tier or higher
     */
    private static boolean hasBloodOrbOfTier(Player player, int requiredTier) {
        // Check inventory for Blood Orb
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ItemBloodOrb orb) {
                int orbTier = orb.getOrb(stack).getTier();
                if (orbTier >= requiredTier) {
                    return true;
                }
            }
        }

        // Check armor slots
        for (ItemStack stack : player.getInventory().armor) {
            if (stack.getItem() instanceof ItemBloodOrb orb) {
                int orbTier = orb.getOrb(stack).getTier();
                if (orbTier >= requiredTier) {
                    return true;
                }
            }
        }

        // Check offhand
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof ItemBloodOrb orb) {
                int orbTier = orb.getOrb(stack).getTier();
                if (orbTier >= requiredTier) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Spawn particle effects for infusion
     */
    private static void spawnInfusionEffects(Level level, BlockPos pos, int tier) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Spawn particles above altar
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.5;
        double z = pos.getZ() + 0.5;

        // Number of particles based on tier
        int particleCount = 10 + (tier * 5);

        // Crimson spore particles (blood-like)
        for (int i = 0; i < particleCount; i++) {
            double offsetX = (serverLevel.random.nextDouble() - 0.5) * 2;
            double offsetY = serverLevel.random.nextDouble();
            double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 2;

            serverLevel.sendParticles(
                ParticleTypes.CRIMSON_SPORE,
                x + offsetX,
                y + offsetY,
                z + offsetZ,
                1,
                0.1, 0.1, 0.1,
                0.05
            );
        }

        // Portal particles for magical effect
        for (int i = 0; i < particleCount / 2; i++) {
            double offsetX = (serverLevel.random.nextDouble() - 0.5) * 1.5;
            double offsetY = serverLevel.random.nextDouble() * 0.5;
            double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 1.5;

            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                x + offsetX,
                y + offsetY,
                z + offsetZ,
                1,
                0, 0.2, 0,
                0.1
            );
        }

        // Enchant particles at higher tiers
        if (tier >= 4) {
            for (int i = 0; i < 20; i++) {
                double angle = (Math.PI * 2 * i) / 20;
                double radius = 1.0;

                serverLevel.sendParticles(
                    ParticleTypes.ENCHANT,
                    x + Math.cos(angle) * radius,
                    y,
                    z + Math.sin(angle) * radius,
                    1,
                    0, 0.5, 0,
                    0.5
                );
            }
        }
    }
}
