package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.util.InventorySearchHelper;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import com.breakinblocks.neovitae.common.datacomponent.EnumWillType;
import com.breakinblocks.neovitae.common.datacomponent.SoulNetwork;
import com.breakinblocks.neovitae.api.soul.SoulTicket;
import com.breakinblocks.neovitae.will.PlayerDemonWillHandler;
import com.breakinblocks.neovitae.util.helper.SoulNetworkHelper;

/**
 * Handles spell power boosting for Sigil of Crimson Will
 *
 * When active:
 * - Boosts spell power and summon damage by 30% base + up to 20% from demon will (50% max)
 * - Applies temporary attribute modifiers during spell casting
 * - Consumes LP based on spell mana cost
 * - Consumes demon will from player's soul network
 */
public class CrimsonWillSpellHandler {

    // ResourceLocations for attribute modifiers (1.21 uses ResourceLocation instead of UUID)
    private static final ResourceLocation SPELL_POWER_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "crimson_will_spell_power");
    private static final ResourceLocation SUMMON_DAMAGE_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "crimson_will_summon_damage");

    // Base bonus: 30%
    private static final double BASE_SPELL_POWER_BONUS = 0.30;

    // Max additional bonus from will: 20% (at 4096 will)
    private static final double MAX_WILL_BONUS = 0.20;
    private static final double MAX_WILL_AMOUNT = 4096.0;

    // Will consumed per spell cast
    private static final double WILL_CONSUMED_PER_CAST = 5.0;

    // Track players who have modifiers applied (for cleanup on cancel/toggle)
    private static final Set<UUID> playersWithModifiers = ConcurrentHashMap.newKeySet();

    /**
     * Register the event handler
     */
    public static void register() {
        NeoForge.EVENT_BUS.register(new CrimsonWillSpellHandler());
        Animus.LOGGER.info("Registered Crimson Will Spell Handler for Iron's Spells");
    }

    /**
     * Hook into spell casting to boost power and consume resources
     * Priority LOWEST to run after other modifications
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpellCast(SpellPreCastEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        // Find active Sigil of Crimson Will
        ItemStack activeSigil = findActiveSigil(player);
        if (activeSigil == null) {
            return;
        }

        // Get spell level (this is the mana cost base)
        int spellLevel = event.getSpellLevel();
        // Note: We use spell level as an approximation of mana cost
        int manaCost = spellLevel; // Approximation

        // Calculate LP cost
        int lpCost = manaCost * AnimusConfig.ironsSpells.crimsonWillLPPerMana.get();

        // Check if player has enough LP
        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(player);
        if (network == null || network.getCurrentEssence() < lpCost) {
            player.displayClientMessage(
                Component.literal("Not enough LP! Need " + lpCost + " LP")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        // Get demon will from player's soul network
        double currentWill = PlayerDemonWillHandler.getTotalDemonWill(EnumWillType.DEFAULT, player);

        // Note: Sigil works at 0 will, just with lower bonus
        // No minimum will requirement check

        // Calculate spell power bonus
        // Base 30% + scaled bonus up to 20% based on will
        double willMultiplier = Math.min(currentWill / MAX_WILL_AMOUNT, 1.0);
        double willBonus = MAX_WILL_BONUS * willMultiplier;
        double totalBonus = BASE_SPELL_POWER_BONUS + willBonus;

        // Apply temporary attribute modifiers for spell power and summon damage
        applyPowerModifiers(player, totalBonus);

        // Track this player as having modifiers applied
        playersWithModifiers.add(player.getUUID());

        // Consume LP using factory method
        network.syphon(SoulTicket.create(lpCost));

        // Consume demon will if player has any
        if (currentWill >= WILL_CONSUMED_PER_CAST) {
            PlayerDemonWillHandler.consumeDemonWill(EnumWillType.DEFAULT, player, WILL_CONSUMED_PER_CAST);
        }

        // Visual and audio feedback
        if (player.level() instanceof ServerLevel serverLevel) {
            // Crimson particles around player
            for (int i = 0; i < 10; i++) {
                double offsetX = (serverLevel.random.nextDouble() - 0.5) * 2;
                double offsetY = serverLevel.random.nextDouble() * 2;
                double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 2;

                serverLevel.sendParticles(
                    ParticleTypes.CRIMSON_SPORE,
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    1,
                    0.1, 0.1, 0.1,
                    0.05
                );
            }

            // Soul particles for demon will consumption
            for (int i = 0; i < 5; i++) {
                double offsetX = (serverLevel.random.nextDouble() - 0.5);
                double offsetY = serverLevel.random.nextDouble();
                double offsetZ = (serverLevel.random.nextDouble() - 0.5);

                serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    1,
                    0, 0.2, 0,
                    0.1
                );
            }

            // Sound effect
            serverLevel.playSound(
                null,
                player.blockPosition(),
                SoundEvents.EVOKER_PREPARE_ATTACK,
                SoundSource.PLAYERS,
                0.3F,
                1.5F + serverLevel.random.nextFloat() * 0.4F
            );
        }

        Animus.LOGGER.debug("Crimson Will empowered spell: {} LP, {} will, {}% bonus",
            lpCost, WILL_CONSUMED_PER_CAST, (int)(totalBonus * 100));
    }

    /**
     * Remove attribute modifiers after spell completes
     * Priority LOWEST to run after spell execution
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpellOnCast(SpellOnCastEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        // Always remove modifiers if player had them applied (regardless of sigil state)
        // This handles the case where the sigil was toggled off during casting
        if (playersWithModifiers.remove(player.getUUID())) {
            removePowerModifiers(player);
        }
    }

    /**
     * Tick handler to clean up modifiers if spell casting was cancelled
     * This handles cases where SpellOnCastEvent is never fired (e.g., player cancels cast)
     */
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (playersWithModifiers.isEmpty()) {
            return;
        }

        // Check all players with modifiers to see if they're still casting
        for (UUID playerId : Set.copyOf(playersWithModifiers)) {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(playerId);
            if (player == null) {
                // Player disconnected, remove from tracking
                playersWithModifiers.remove(playerId);
                continue;
            }

            // Check if player is still casting
            MagicData magicData = MagicData.getPlayerMagicData(player);
            if (!magicData.isCasting()) {
                // Player stopped casting without SpellOnCastEvent firing (cancelled)
                if (playersWithModifiers.remove(playerId)) {
                    removePowerModifiers(player);
                    Animus.LOGGER.debug("Cleaned up Crimson Will modifiers for {} (casting cancelled)", player.getName().getString());
                }
            }
        }
    }

    /**
     * Find active Sigil of Crimson Will in player's inventory
     * @return The active sigil ItemStack, or null if none found
     */
    private ItemStack findActiveSigil(Player player) {
        // Predicate for active Crimson Will sigil
        java.util.function.Predicate<ItemStack> isActiveCrimsonWill = stack ->
            stack.getItem() instanceof ItemSigilCrimsonWill && ItemSigilCrimsonWill.isActive(stack);

        // Check main inventory, armor, and offhand using helper
        var fromInventory = InventorySearchHelper.findFirst(player, isActiveCrimsonWill);
        if (fromInventory.isPresent()) {
            return fromInventory.get();
        }

        // Check Curios slots
        var curiosResult = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
            .map(inv -> inv.findFirstCurio(isActiveCrimsonWill))
            .orElse(java.util.Optional.empty());

        if (curiosResult.isPresent()) {
            return curiosResult.get().stack();
        }

        // Check inside Sigil of Holding (in inventory and hands)
        var sigilHoldings = InventorySearchHelper.findAll(player,
            stack -> stack.getItem() instanceof com.breakinblocks.neovitae.common.item.sigil.ItemSigilHolding);

        for (ItemStack holdingStack : sigilHoldings) {
            net.minecraft.core.NonNullList<ItemStack> holdingInv =
                com.breakinblocks.neovitae.common.item.sigil.ItemSigilHolding.getInternalInventory(holdingStack);
            for (ItemStack heldStack : holdingInv) {
                if (isActiveCrimsonWill.test(heldStack)) {
                    return heldStack;
                }
            }
        }

        return null;
    }

    /**
     * Apply temporary attribute modifiers for spell power and summon damage
     */
    private void applyPowerModifiers(Player player, double bonus) {
        // Apply spell power modifier
        // Note: In 1.21, getAttribute takes Holder<Attribute>
        AttributeInstance spellPowerAttr = player.getAttribute(AttributeRegistry.SPELL_POWER);
        if (spellPowerAttr != null) {
            // Remove existing modifier if present (using ResourceLocation in 1.21)
            spellPowerAttr.removeModifier(SPELL_POWER_MODIFIER_ID);

            // Add new modifier (1.21 uses ResourceLocation-based constructor)
            // Operation.ADD_MULTIPLIED_TOTAL is the 1.21 name for MULTIPLY_TOTAL
            AttributeModifier spellPowerMod = new AttributeModifier(
                SPELL_POWER_MODIFIER_ID,
                bonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            spellPowerAttr.addTransientModifier(spellPowerMod);
        }

        // Apply summon damage modifier
        AttributeInstance summonDamageAttr = player.getAttribute(AttributeRegistry.SUMMON_DAMAGE);
        if (summonDamageAttr != null) {
            // Remove existing modifier if present
            summonDamageAttr.removeModifier(SUMMON_DAMAGE_MODIFIER_ID);

            // Add new modifier
            AttributeModifier summonDamageMod = new AttributeModifier(
                SUMMON_DAMAGE_MODIFIER_ID,
                bonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            summonDamageAttr.addTransientModifier(summonDamageMod);
        }
    }

    /**
     * Remove temporary attribute modifiers
     */
    private void removePowerModifiers(Player player) {
        // Remove spell power modifier
        AttributeInstance spellPowerAttr = player.getAttribute(AttributeRegistry.SPELL_POWER);
        if (spellPowerAttr != null) {
            spellPowerAttr.removeModifier(SPELL_POWER_MODIFIER_ID);
        }

        // Remove summon damage modifier
        AttributeInstance summonDamageAttr = player.getAttribute(AttributeRegistry.SUMMON_DAMAGE);
        if (summonDamageAttr != null) {
            summonDamageAttr.removeModifier(SUMMON_DAMAGE_MODIFIER_ID);
        }
    }
}
