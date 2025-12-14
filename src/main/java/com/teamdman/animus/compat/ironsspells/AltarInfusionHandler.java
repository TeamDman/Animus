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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wayoftime.bloodmagic.common.item.ItemBloodOrb;
import wayoftime.bloodmagic.common.tile.TileAltar;

/**
 * Handles Blood Altar interactions for infusing spellbooks
 */
public class AltarInfusionHandler {

    private static final int INITIAL_INFUSION_COST = 10000; // LP cost to create Blood-Infused Spellbook

    /**
     * Handle right-click on Blood Altar with spellbook
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

        // Get altar tile entity
        if (!(level.getBlockEntity(pos) instanceof TileAltar altar)) {
            return;
        }

        // Check if holding a regular Iron's Spellbooks spellbook (not our Blood-Infused one)
        if (isIronsSpellbook(stack) && !(stack.getItem() instanceof ItemBloodInfusedSpellbook)) {
            handleInitialInfusion(event, player, level, pos, hand, stack, altar);
            return;
        }

        // Check if holding Blood-Infused Spellbook for upgrade
        if (!(stack.getItem() instanceof ItemBloodInfusedSpellbook)) {
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

    /**
     * Check if the item is an Iron's Spellbooks spellbook
     */
    private static boolean isIronsSpellbook(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        // Check if the item's registry name is from irons_spellbooks and contains "spell_book"
        var registryName = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (registryName == null) {
            return false;
        }
        String namespace = registryName.getNamespace();
        String path = registryName.getPath();
        return "irons_spellbooks".equals(namespace) && path.contains("spell_book");
    }

    /**
     * Handle initial infusion of a regular spellbook into Blood-Infused Spellbook
     */
    private static void handleInitialInfusion(PlayerInteractEvent.RightClickBlock event,
            Player player, Level level, BlockPos pos, InteractionHand hand,
            ItemStack stack, TileAltar altar) {

        // Check if altar has enough LP
        int altarLP = altar.getCurrentBlood();
        if (altarLP < INITIAL_INFUSION_COST) {
            player.displayClientMessage(
                Component.literal("Altar needs " + INITIAL_INFUSION_COST + " LP (has " + altarLP + " LP)")
                    .withStyle(ChatFormatting.RED),
                true
            );
            event.setCanceled(true);
            return;
        }

        // Check altar tier (requires tier 3+)
        int altarTier = altar.getTier();
        if (altarTier < 3) {
            player.displayClientMessage(
                Component.literal("Requires Tier 3+ Blood Altar!")
                    .withStyle(ChatFormatting.RED),
                true
            );
            event.setCanceled(true);
            return;
        }

        // Consume LP from altar
        altar.sacrificialDaggerCall(-INITIAL_INFUSION_COST, false);

        // Create Blood-Infused Spellbook with copied NBT data (spells, etc.)
        ItemStack bloodInfusedSpellbook = new ItemStack(
            com.teamdman.animus.compat.IronsSpellsCompat.BLOOD_INFUSED_SPELLBOOK.get()
        );

        // Copy NBT data from original spellbook (preserves spells)
        if (stack.hasTag()) {
            bloodInfusedSpellbook.setTag(stack.getTag().copy());
        }

        // Set initial infusion tier
        ItemBloodInfusedSpellbook.setInfusionTier(bloodInfusedSpellbook, 1);

        // Replace the item in player's hand
        player.setItemInHand(hand, bloodInfusedSpellbook);

        // Success message
        player.displayClientMessage(
            Component.literal("Spellbook infused with blood magic!")
                .withStyle(ChatFormatting.GOLD),
            true
        );

        // Spawn effects
        spawnInfusionEffects(level, pos, 1);

        // Play sound
        level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

        Animus.LOGGER.info("Player {} created Blood-Infused Spellbook for {} LP",
            player.getName().getString(), INITIAL_INFUSION_COST);

        event.setCanceled(true);
    }
}
