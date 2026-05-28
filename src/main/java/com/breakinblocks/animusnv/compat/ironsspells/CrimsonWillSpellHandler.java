package com.breakinblocks.animusnv.compat.ironsspells;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.IronsSpellsCompat;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.animusnv.util.InventorySearchHelper;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import top.theillusivec4.curios.api.CuriosApi;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
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
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.spiritus.IPlayerSpiritusHandler;
import com.breakinblocks.neovitae.common.item.IActivatable;
import com.breakinblocks.neovitae.common.item.sigil.ItemSigilHolding;

/**
 * Handles spell power boosting for Sigil of Crimson Will
 *
 * When active:
 * - Boosts spell power and summon damage by 30% base + up to 20% from Spiritus (50% max)
 * - Applies temporary attribute modifiers during spell casting
 * - Consumes EV based on spell mana cost
 * - Consumes Spiritus from player's Anima
 */
public class CrimsonWillSpellHandler {

    private static final ResourceLocation SPELL_POWER_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "crimson_will_spell_power");
    private static final ResourceLocation SUMMON_DAMAGE_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "crimson_will_summon_damage");

    private static final double BASE_SPELL_POWER_BONUS = 0.30;
    private static final double MAX_SPIRITUS_BONUS = 0.20;
    private static final double MAX_SPIRITUS_AMOUNT = 4096.0;
    private static final double SPIRITUS_CONSUMED_PER_CAST = 5.0;

    private static final Set<UUID> playersWithModifiers = ConcurrentHashMap.newKeySet();

    public static void register() {
        NeoForge.EVENT_BUS.register(new CrimsonWillSpellHandler());
        Animus.LOGGER.debug("Registered Crimson Will Spell Handler for Iron's Spells");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpellCast(SpellPreCastEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        ItemStack activeSigil = findActiveSigil(player);
        if (activeSigil == null) {
            return;
        }

        int spellLevel = event.getSpellLevel();
        int manaCost = spellLevel; // Approximation of mana cost

        int evCost = manaCost * AnimusConfig.ironsSpells.crimsonWillEVPerMana.get();
        IAnima network = AnimusRitualHelper.getNetworkForBoundItem(player, activeSigil);
        if (network == null || network.getCurrentEV() < evCost) {
            player.displayClientMessage(
                Component.literal("Not enough EV! Need " + evCost + " EV")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        IPlayerSpiritusHandler playerSpiritus = NeoVitaeAPI.getInstance().getPlayerSpiritusHandler();
        double currentSpiritus = playerSpiritus.getTotalSpiritus(SpiritusType.RAW, player);

        // Sigil works at 0 will (just with lower bonus)
        double spiritusMultiplier = Math.min(currentSpiritus / MAX_SPIRITUS_AMOUNT, 1.0);
        double spiritusBonus = MAX_SPIRITUS_BONUS * spiritusMultiplier;
        double totalBonus = BASE_SPELL_POWER_BONUS + spiritusBonus;

        applyPowerModifiers(player, totalBonus);
        playersWithModifiers.add(player.getUUID());

        network.syphon(AnimaTicket.create(evCost));
        if (currentSpiritus >= SPIRITUS_CONSUMED_PER_CAST) {
            playerSpiritus.consumeSpiritus(SpiritusType.RAW, player, SPIRITUS_CONSUMED_PER_CAST);
        }

        if (player.level() instanceof ServerLevel serverLevel) {
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

            serverLevel.playSound(
                null,
                player.blockPosition(),
                SoundEvents.EVOKER_PREPARE_ATTACK,
                SoundSource.PLAYERS,
                0.3F,
                1.5F + serverLevel.random.nextFloat() * 0.4F
            );
        }

        Animus.LOGGER.debug("Crimson Will empowered spell: {} EV, {} will, {}% bonus",
            evCost, SPIRITUS_CONSUMED_PER_CAST, (int)(totalBonus * 100));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpellOnCast(SpellOnCastEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        // Always remove regardless of sigil state (handles toggle-off during casting)
        if (playersWithModifiers.remove(player.getUUID())) {
            removePowerModifiers(player);
        }
    }

    /**
     * Clean up modifiers if SpellOnCastEvent never fires (e.g., player cancels cast).
     */
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (playersWithModifiers.isEmpty()) {
            return;
        }

        for (UUID playerId : Set.copyOf(playersWithModifiers)) {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(playerId);
            if (player == null) {
                playersWithModifiers.remove(playerId);
                continue;
            }

            MagicData magicData = MagicData.getPlayerMagicData(player);
            if (!magicData.isCasting()) {
                if (playersWithModifiers.remove(playerId)) {
                    removePowerModifiers(player);
                    Animus.LOGGER.debug("Cleaned up Crimson Will modifiers for {} (casting cancelled)", player.getName().getString());
                }
            }
        }
    }

    private ItemStack findActiveSigil(Player player) {
        Predicate<ItemStack> isActiveCrimsonWill = stack -> {
            if (!stack.is(IronsSpellsCompat.SIGIL_CRIMSON_WILL.get())) return false;
            if (stack.getItem() instanceof IActivatable activatable) {
                return activatable.getActivated(stack);
            }
            return false;
        };

        var fromInventory = InventorySearchHelper.findFirst(player, isActiveCrimsonWill);
        if (fromInventory.isPresent()) {
            return fromInventory.get();
        }

        var curiosResult = CuriosApi.getCuriosInventory(player)
            .map(inv -> inv.findFirstCurio(isActiveCrimsonWill))
            .orElse(Optional.empty());

        if (curiosResult.isPresent()) {
            return curiosResult.get().stack();
        }

        var sigilHoldings = InventorySearchHelper.findAll(player,
            stack -> stack.getItem() instanceof ItemSigilHolding);

        for (ItemStack holdingStack : sigilHoldings) {
            NonNullList<ItemStack> holdingInv =
                ItemSigilHolding.getInternalInventory(holdingStack);
            for (ItemStack heldStack : holdingInv) {
                if (isActiveCrimsonWill.test(heldStack)) {
                    return heldStack;
                }
            }
        }

        return null;
    }

    private void applyPowerModifiers(Player player, double bonus) {
        AttributeInstance spellPowerAttr = player.getAttribute(AttributeRegistry.SPELL_POWER);
        if (spellPowerAttr != null) {
            spellPowerAttr.removeModifier(SPELL_POWER_MODIFIER_ID);
            AttributeModifier spellPowerMod = new AttributeModifier(
                SPELL_POWER_MODIFIER_ID,
                bonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            spellPowerAttr.addTransientModifier(spellPowerMod);
        }

        AttributeInstance summonDamageAttr = player.getAttribute(AttributeRegistry.SUMMON_DAMAGE);
        if (summonDamageAttr != null) {
            summonDamageAttr.removeModifier(SUMMON_DAMAGE_MODIFIER_ID);
            AttributeModifier summonDamageMod = new AttributeModifier(
                SUMMON_DAMAGE_MODIFIER_ID,
                bonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            summonDamageAttr.addTransientModifier(summonDamageMod);
        }
    }

    private void removePowerModifiers(Player player) {
        AttributeInstance spellPowerAttr = player.getAttribute(AttributeRegistry.SPELL_POWER);
        if (spellPowerAttr != null) {
            spellPowerAttr.removeModifier(SPELL_POWER_MODIFIER_ID);
        }

        AttributeInstance summonDamageAttr = player.getAttribute(AttributeRegistry.SUMMON_DAMAGE);
        if (summonDamageAttr != null) {
            summonDamageAttr.removeModifier(SUMMON_DAMAGE_MODIFIER_ID);
        }
    }
}
