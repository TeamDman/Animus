package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.compat.IronsSpellsCompat;
import com.teamdman.animus.compat.CompatHandler;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import wayoftime.bloodmagic.event.SoulNetworkEvent;
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

    private static final Map<UUID, PendingLPCost> pendingLPCosts = new ConcurrentHashMap<>();

    private record LPCost(SoulNetwork network, int lpCost, int manaCovered, boolean hybrid) {}
    private record PendingLPCost(String spellId, CastSource source, SoulNetwork network,
                                 int reservedLP, int manaCovered, boolean hybrid) {}

    private static boolean consumesMana(Player player, AbstractSpell spell, CastSource source, MagicData data) {
        return source.consumesMana()
            && (!player.isCreative() || (Boolean) ServerConfigs.CREATIVE_MANA_COST.get())
            && !data.getPlayerRecasts().hasRecastForSpell(spell.getSpellId());
    }

    private static LPCost quote(Player player, int manaCost, MagicData data) {
        if (player.level().isClientSide || !CompatHandler.isIronsSpellsLoaded()
            || !AnimusConfig.ironsSpells.enableLPCasting.get() || data.getMana() >= manaCost
            || (AnimusConfig.ironsSpells.requireBloodOrb.get() && !hasBloodOrb(player))) {
            return null;
        }
        ItemStack spellbook = findCastingSpellbook(player);
        Binding binding = spellbook.isEmpty() ? null : ItemBloodInfusedSpellbook.getBindingStatic(spellbook);
        SoulNetwork network = binding == null ? NetworkHelper.getSoulNetwork(player) : NetworkHelper.getSoulNetwork(binding);
        boolean hybrid = AnimusConfig.ironsSpells.allowHybridCasting.get() && data.getMana() > 0;
        int manaCovered = hybrid ? (int) Math.ceil(manaCost - data.getMana()) : manaCost;
        double reduction = spellbook.isEmpty() ? 0 : ItemBloodInfusedSpellbook.getLPCostReduction(spellbook);
        double cost = (double) manaCovered * AnimusConfig.ironsSpells.lpPerMana.get() * (1 - reduction);
        if (network == null || cost > Integer.MAX_VALUE) {
            return null;
        }
        int lpCost = Math.max(1, (int) cost);
        return network.getCurrentEssence() >= lpCost ? new LPCost(network, lpCost, manaCovered, hybrid) : null;
    }

    /** This is a read-only projection, including when another condition rejects the cast. */
    public static float getManaForCastCheck(Player player, AbstractSpell spell, int spellLevel,
                                             CastSource source, MagicData data) {
        if (!consumesMana(player, spell, source, data)) {
            return data.getMana();
        }
        LPCost cost = quote(player, spell.getManaCost(spellLevel), data);
        return cost == null ? data.getMana() : data.getMana() + cost.manaCovered();
    }

    /** Reserve LP after eligibility succeeds; mana itself is never temporarily increased. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSpellPreCast(SpellPreCastEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        refundPending(player);
        if (!CompatHandler.isIronsSpellsLoaded() || !AnimusConfig.ironsSpells.enableLPCasting.get()) {
            return;
        }
        AbstractSpell spell = SpellRegistry.getSpell(event.getSpellId());
        MagicData data = MagicData.getPlayerMagicData(player);
        if (!consumesMana(player, spell, event.getCastSource(), data)
            || data.getMana() >= spell.getManaCost(event.getSpellLevel())) {
            return;
        }
        LPCost cost = quote(player, spell.getManaCost(event.getSpellLevel()), data);
        if (cost == null) {
            event.setCanceled(true);
            return;
        }
        SoulTicket ticket = new SoulTicket(Component.literal("Spell Casting"), cost.lpCost());
        SoulNetworkEvent.Syphon.User payment = new SoulNetworkEvent.Syphon.User(cost.network(), ticket, player);
        if (MinecraftForge.EVENT_BUS.post(payment)) {
            event.setCanceled(true);
            return;
        }
        int amount = payment.getTicket().getAmount();
        if (amount < 0 || cost.network().getCurrentEssence() < amount
            || cost.network().syphon(payment.getTicket(), true) != amount) {
            event.setCanceled(true);
            return;
        }
        pendingLPCosts.put(player.getUUID(), new PendingLPCost(event.getSpellId(), event.getCastSource(),
            cost.network(), amount, cost.manaCovered(), cost.hybrid()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onPreCastCancelled(SpellPreCastEvent event) {
        if (!event.getEntity().level().isClientSide && event.isCanceled()) {
            refundPending(event.getEntity());
        }
    }

    /** Commit the reservation and let Iron's Spells consume only the mana portion. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSpellOnCast(SpellOnCastEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        PendingLPCost pending = pendingLPCosts.get(player.getUUID());
        if (pending == null || !pending.spellId().equals(event.getSpellId()) || pending.source() != event.getCastSource()) {
            return;
        }
        pendingLPCosts.remove(player.getUUID());
        event.setManaCost(Math.max(0, event.getManaCost() - pending.manaCovered()));
        spawnLPCastFeedback(player, pending.hybrid());
    }

    private static void refundPending(Player player) {
        PendingLPCost pending = pendingLPCosts.remove(player.getUUID());
        if (pending != null) {
            long refunded = (long) pending.network().getCurrentEssence() + pending.reservedLP();
            pending.network().setCurrentEssence((int) Math.min(Integer.MAX_VALUE, refunded));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.side.isClient()
            && pendingLPCosts.containsKey(event.player.getUUID())
            && !MagicData.getPlayerMagicData(event.player).isCasting()) {
            refundPending(event.player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.getEntity().level().isClientSide) {
            refundPending(event.getEntity());
        }
    }

    private static ItemStack findCastingSpellbook(Player player) {
        ItemStack equipped = findBloodInfusedSpellbook(player);
        if (!equipped.isEmpty()) {
            return equipped;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ItemBloodInfusedSpellbook) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
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
