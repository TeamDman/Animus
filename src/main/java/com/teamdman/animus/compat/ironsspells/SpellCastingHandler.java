package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.compat.IronsSpellsCompat;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;
import wayoftime.bloodmagic.common.item.ItemBloodOrb;
import wayoftime.bloodmagic.core.data.Binding;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Handles spell casting events to enable LP-powered spell casting
 *
 * Features:
 * - Intercepts spell casting to use LP instead of mana when mana is insufficient
 * - Checks for Blood Orb requirement (configurable)
 * - Supports hybrid casting (partial mana + partial LP)
 * - Applies LP cost reduction from Blood Infused Spellbook tiers
 * - Provides lifesteal when using Tier 6 Blood Infused Spellbook
 * - Respects all configuration options
 */
public class SpellCastingHandler {

    // Track pending LP costs per player UUID for two-phase LP casting
    private static final Map<UUID, PendingLPCost> pendingLPCosts = new ConcurrentHashMap<>();

    /**
     * Inner class to track pending LP consumption between pre-cast and on-cast events
     */
    private static class PendingLPCost {
        final int lpCost;
        final int manaAdded;
        final SoulNetwork network;
        final boolean isHybrid;

        PendingLPCost(int lpCost, int manaAdded, SoulNetwork network, boolean isHybrid) {
            this.lpCost = lpCost;
            this.manaAdded = manaAdded;
            this.network = network;
            this.isHybrid = isHybrid;
        }
    }

    /**
     * Handle spell pre-cast event to temporarily add mana when LP will be used
     * This fires BEFORE Iron's Spells checks mana, allowing LP substitution to work
     * Priority: HIGHEST to run before mana checks
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSpellPreCast(SpellPreCastEvent event) {
        // Only handle players
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();

        // Only process on server side
        if (player.level().isClientSide()) {
            return;
        }

        // Check if LP casting is enabled
        if (!AnimusConfig.ironsSpells.enableLPCasting.get()) {
            return;
        }

        // Get the player's magic data to check mana
        MagicData magicData = MagicData.getPlayerMagicData(player);
        if (magicData == null) {
            return;
        }

        // Get the mana cost for this spell via SpellRegistry
        AbstractSpell spell = SpellRegistry.getSpell(event.getSpellId());
        if (spell == null) {
            return;
        }
        int manaCost = spell.getManaCost(event.getSpellLevel());
        int currentMana = (int) magicData.getMana();

        // If player has enough mana, let normal casting proceed
        if (currentMana >= manaCost) {
            return;
        }

        // Player doesn't have enough mana - try LP casting
        int manaDeficit = manaCost - currentMana;

        // Check if Blood Orb is required and present
        if (AnimusConfig.ironsSpells.requireBloodOrb.get()) {
            if (!hasBloodOrb(player)) {
                return;
            }
        }

        // Find Blood Infused Spellbook and get the bound owner's network
        ItemStack spellbook = findBloodInfusedSpellbook(player);
        SoulNetwork network;

        if (!spellbook.isEmpty()) {
            Binding binding = ItemBloodInfusedSpellbook.getBindingStatic(spellbook);
            if (binding != null) {
                UUID ownerUUID = binding.getOwnerId();
                network = NetworkHelper.getSoulNetwork(ownerUUID);
                if (network == null) {
                    return;
                }
            } else {
                network = NetworkHelper.getSoulNetwork(player);
                if (network == null) {
                    return;
                }
            }
        } else {
            network = NetworkHelper.getSoulNetwork(player);
            if (network == null) {
                return;
            }
        }

        // Calculate LP cost
        int lpPerMana = AnimusConfig.ironsSpells.lpPerMana.get();
        int lpCost;
        int manaToAdd;
        boolean isHybrid;

        if (AnimusConfig.ironsSpells.allowHybridCasting.get() && currentMana > 0) {
            // Hybrid casting: use available mana + LP for the rest
            manaToAdd = manaDeficit;
            lpCost = manaDeficit * lpPerMana;
            isHybrid = true;
        } else {
            // Pure LP casting: use LP for entire cost
            manaToAdd = manaCost;
            lpCost = manaCost * lpPerMana;
            isHybrid = false;
        }

        // Apply LP cost reduction from Blood Infused Spellbook
        if (!spellbook.isEmpty()) {
            double lpReduction = ItemBloodInfusedSpellbook.getLPCostReduction(spellbook);
            if (lpReduction > 0) {
                lpCost = (int) Math.max(1, lpCost * (1.0 - lpReduction));
            }
        }

        // Check if player has enough LP
        if (network.getCurrentEssence() < lpCost) {
            player.displayClientMessage(
                Component.literal("Not enough Life Points! Required: " + lpCost + " LP")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        // Temporarily add mana so the spell can proceed
        magicData.setMana(currentMana + manaToAdd);

        // Store pending LP cost to consume in onSpellOnCast
        pendingLPCosts.put(player.getUUID(), new PendingLPCost(lpCost, manaToAdd, network, isHybrid));

        Animus.LOGGER.debug("Pre-cast: added {} temporary mana for player {}, pending {} LP",
            manaToAdd, player.getName().getString(), lpCost);
    }

    /**
     * Handle spell on-cast event to consume LP when mana is insufficient
     * This event has the actual mana cost available
     * Priority: HIGH to run before normal mana consumption
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onSpellOnCast(SpellOnCastEvent event) {
        // Only handle players
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();

        // Only process on server side
        if (player.level().isClientSide()) {
            return;
        }

        // Check if there's a pending LP cost from onSpellPreCast
        PendingLPCost pending = pendingLPCosts.remove(player.getUUID());
        if (pending == null) {
            // No pending LP cost - normal mana casting
            return;
        }

        // Consume LP from soul network
        SoulTicket ticket = new SoulTicket(
            Component.literal("Spell Casting"),
            pending.lpCost
        );

        var syphonResult = pending.network.syphonAndDamage(player, ticket);
        if (!syphonResult.isSuccess()) {
            // Failed to consume LP - shouldn't happen since we checked in pre-cast
            player.displayClientMessage(
                Component.literal("Failed to consume Life Points!")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        // Spawn visual and audio feedback
        spawnLPCastFeedback(player, pending.isHybrid);

        // Log success
        Animus.LOGGER.debug("Player {} cast spell using {} LP{}",
            player.getName().getString(),
            pending.lpCost,
            pending.isHybrid ? " (hybrid)" : ""
        );
    }

    /**
     * Handle damage events to apply lifesteal from Blood Infused Spellbook
     * Tier 6 spellbooks grant 5% lifesteal from spell damage
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingDamage(LivingDamageEvent event) {
        // Only process on server side
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        // Check if the damage is from a spell (SpellDamageSource)
        if (!(event.getSource() instanceof SpellDamageSource spellDamageSource)) {
            return;
        }

        // Check if the spell caster is a player
        if (!(spellDamageSource.getEntity() instanceof Player player)) {
            return;
        }

        // Find Blood Infused Spellbook
        ItemStack spellbook = findBloodInfusedSpellbook(player);
        if (spellbook.isEmpty()) {
            return;
        }

        // Get lifesteal percentage
        double lifesteal = ItemBloodInfusedSpellbook.getLifesteal(spellbook);
        if (lifesteal <= 0) {
            return;
        }

        // Calculate healing amount (5% of damage dealt)
        float damage = event.getAmount();
        float healing = damage * (float) lifesteal;

        if (healing > 0) {
            // Apply healing
            player.heal(healing);

            // Visual feedback - heart particles
            if (player.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 5; i++) {
                    double offsetX = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                    double offsetY = serverLevel.random.nextDouble();
                    double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 0.5;

                    serverLevel.sendParticles(
                        ParticleTypes.HEART,
                        player.getX() + offsetX,
                        player.getY() + 1.0 + offsetY,
                        player.getZ() + offsetZ,
                        1,
                        0.1, 0.1, 0.1,
                        0.02
                    );
                }
            }

            Animus.LOGGER.debug("Blood Infused Spellbook lifesteal: healed {} HP from {} damage",
                healing, damage);
        }
    }

    /**
     * Find the Blood Infused Spellbook in player's curios slots
     * @return The spellbook ItemStack, or ItemStack.EMPTY if not found
     */
    private static ItemStack findBloodInfusedSpellbook(Player player) {
        var curiosOpt = CuriosApi.getCuriosInventory(player).resolve();
        if (curiosOpt.isPresent()) {
            var curios = curiosOpt.get();
            var handler = curios.getEquippedCurios();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack.getItem() == IronsSpellsCompat.BLOOD_INFUSED_SPELLBOOK.get()) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Spawn visual and audio feedback when LP is consumed for spell casting
     * @param player The player casting the spell
     * @param isHybrid Whether this was hybrid casting (mana + LP)
     */
    private static void spawnLPCastFeedback(Player player, boolean isHybrid) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Spawn red particles around the player to indicate LP consumption
        // Use crimson spore particles for a blood-like effect
        double x = player.getX();
        double y = player.getY() + player.getEyeHeight() * 0.5;
        double z = player.getZ();

        // Spawn particles in a small ring around player
        int particleCount = isHybrid ? 8 : 15; // Fewer particles for hybrid casting
        for (int i = 0; i < particleCount; i++) {
            double angle = (Math.PI * 2 * i) / particleCount;
            double radius = 0.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            // Crimson spore particles (red, blood-like)
            serverLevel.sendParticles(
                ParticleTypes.CRIMSON_SPORE,
                x + offsetX,
                y,
                z + offsetZ,
                1, // count
                0.1, // deltaX
                0.1, // deltaY
                0.1, // deltaZ
                0.02 // speed
            );
        }

        // Add a few soul particles for magic effect (subtle)
        for (int i = 0; i < 3; i++) {
            serverLevel.sendParticles(
                ParticleTypes.SOUL,
                x + (player.getRandom().nextDouble() - 0.5) * 0.5,
                y + (player.getRandom().nextDouble() - 0.5) * 0.5,
                z + (player.getRandom().nextDouble() - 0.5) * 0.5,
                1,
                0,
                0.1,
                0,
                0.01
            );
        }

        // Play a subtle sound effect
        // Use experience orb pickup sound at very low volume (0.15) for a magical "whoosh"
        serverLevel.playSound(
            null, // null means all players near the location can hear it
            player.blockPosition(),
            SoundEvents.EXPERIENCE_ORB_PICKUP,
            SoundSource.PLAYERS,
            0.15F, // Very low volume as requested
            0.8F + player.getRandom().nextFloat() * 0.4F // Pitch between 0.8 and 1.2
        );
    }

    /**
     * Check if player has a Blood Orb in inventory or curios slot
     */
    private static boolean hasBloodOrb(Player player) {
        // Check main inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ItemBloodOrb) {
                return true;
            }
        }

        // Check armor slots
        for (ItemStack stack : player.getInventory().armor) {
            if (stack.getItem() instanceof ItemBloodOrb) {
                return true;
            }
        }

        // Check offhand
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof ItemBloodOrb) {
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
                if (stack.getItem() instanceof ItemBloodOrb) {
                    return true;
                }
            }
        }

        return false;
    }
}
