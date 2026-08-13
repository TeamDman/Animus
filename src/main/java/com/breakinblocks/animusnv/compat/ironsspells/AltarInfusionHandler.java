package com.breakinblocks.animusnv.compat.ironsspells;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.AnimusConfig;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import com.breakinblocks.neovitae.common.item.BloodOrbItem;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;

/**
 * Handles Ara Vitae interactions for upgrading Blood-Infused Spellbooks.
 *
 * Initial infusion (converting regular spellbooks to Blood-Infused Spellbooks)
 * is now handled via data-driven Ara Vitae recipes with copyInputComponents
 * to preserve spell data from the original spellbook.
 *
 * This handler only manages tier upgrades (1->2, 2->3, etc.) which require
 * checking Orb of Vitae tiers and tier-specific EV costs.
 */
public class AltarInfusionHandler {

    public static void register() {
        NeoForge.EVENT_BUS.register(AltarInfusionHandler.class);
    }

    @SubscribeEvent
    public static void onAltarRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);

        if (!(stack.getItem() instanceof ItemBloodInfusedSpellbook)) {
            return;
        }

        if (!(level.getBlockEntity(pos) instanceof AraVitaeTile altar)) {
            return;
        }

        handleSpellbookUpgrade(event, player, level, pos, hand, stack, altar);
    }

    private static void handleSpellbookUpgrade(PlayerInteractEvent.RightClickBlock event, Player player,
            Level level, BlockPos pos, InteractionHand hand, ItemStack stack, AraVitaeTile altar) {

        if (!ItemBloodInfusedSpellbook.canUpgrade(stack)) {
            player.displayClientMessage(
                Component.literal("This spellbook is already at maximum infusion!")
                    .withStyle(ChatFormatting.RED),
                true
            );
            player.setItemInHand(hand, stack); // Force sync to prevent client desync
            event.setCanceled(true);
            event.setUseBlock(TriState.FALSE);
            event.setUseItem(TriState.FALSE);
            return;
        }

        int currentTier = ItemBloodInfusedSpellbook.getInfusionTier(stack);
        int nextTier = currentTier + 1;
        int evCost = ItemBloodInfusedSpellbook.getUpgradeCost(stack);

        int altarEV = altar.getCurrentBlood();
        if (altarEV < evCost) {
            player.displayClientMessage(
                Component.literal("Altar needs " + evCost + " EV (has " + altarEV + " EV)")
                    .withStyle(ChatFormatting.RED),
                true
            );
            player.setItemInHand(hand, stack); // Force sync to prevent client desync
            event.setCanceled(true);
            event.setUseBlock(TriState.FALSE);
            event.setUseItem(TriState.FALSE);
            return;
        }
        int requiredOrbTier = ItemBloodInfusedSpellbook.getRequiredOrbTier(nextTier);
        if (!hasBloodOrbOfTier(player, requiredOrbTier)) {
            player.displayClientMessage(
                Component.literal("Requires " + ItemBloodInfusedSpellbook.getRequiredOrbName(nextTier) + " or higher!")
                    .withStyle(ChatFormatting.RED),
                true
            );
            player.setItemInHand(hand, stack); // Force sync to prevent client desync
            event.setCanceled(true);
            event.setUseBlock(TriState.FALSE);
            event.setUseItem(TriState.FALSE);
            return;
        }

        altar.addSacrificeEV(-evCost, false);
        ItemBloodInfusedSpellbook.setInfusionTier(stack, nextTier);

        player.displayClientMessage(
            Component.literal("Spellbook infused to Tier " + nextTier + "!")
                .withStyle(ChatFormatting.GOLD),
            true
        );

        spawnInfusionEffects(level, pos, nextTier);
        level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

        Animus.LOGGER.info("Player {} infused spellbook to tier {} for {} EV",
            player.getName().getString(), nextTier, evCost);

        event.setCanceled(true);
        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);
    }

    private static boolean hasBloodOrbOfTier(Player player, int requiredTier) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BloodOrbItem orb) {
                int orbTier = orb.getOrbTier(stack);
                if (orbTier >= requiredTier) {
                    return true;
                }
            }
        }

        for (ItemStack stack : player.getInventory().armor) {
            if (stack.getItem() instanceof BloodOrbItem orb) {
                int orbTier = orb.getOrbTier(stack);
                if (orbTier >= requiredTier) {
                    return true;
                }
            }
        }

        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof BloodOrbItem orb) {
                int orbTier = orb.getOrbTier(stack);
                if (orbTier >= requiredTier) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void spawnInfusionEffects(Level level, BlockPos pos, int tier) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.5;
        double z = pos.getZ() + 0.5;

        int particleCount = 10 + (tier * 5);

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
