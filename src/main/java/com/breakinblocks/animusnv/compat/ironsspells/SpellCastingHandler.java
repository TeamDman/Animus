package com.breakinblocks.animusnv.compat.ironsspells;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.animusnv.util.InventorySearchHelper;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.CuriosApi;
import com.breakinblocks.neovitae.common.item.BloodOrbItem;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;

import java.util.Optional;

/**
 * Handles spell casting events to enable EV-powered spell casting
 *
 * Features:
 * - Intercepts spell casting to use EV instead of mana
 * - Checks for Blood Orb requirement (configurable)
 * - Supports hybrid casting (partial mana + partial EV)
 * - Respects all configuration options
 */
public class SpellCastingHandler {

    public static void register() {
        NeoForge.EVENT_BUS.register(new SpellCastingHandler());
        Animus.LOGGER.debug("Registered Spell Casting Handler for Iron's Spells");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSpellPreCast(SpellPreCastEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!AnimusConfig.ironsSpells.enableEVCasting.get()) {
            return;
        }

        MagicData magicData = MagicData.getPlayerMagicData(player);
        if (magicData == null) {
            return;
        }

        int manaCost = event.getSpellLevel(); // Approximation based on spell level
        int currentMana = (int) magicData.getMana();

        if (currentMana >= manaCost) {
            return;
        }

        int manaDeficit = manaCost - currentMana;

        ItemStack orbStack = findBloodOrb(player);
        if (AnimusConfig.ironsSpells.requireBloodOrb.get()) {
            if (orbStack == null) {
                return;
            }
        }

        // Use the blood orb's binding for network lookup (respects team bindings)
        IAnima network = orbStack != null
            ? AnimusRitualHelper.getNetworkForBoundItem(player, orbStack)
            : NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        if (network == null) {
            return;
        }

        int evPerMana = AnimusConfig.ironsSpells.evPerMana.get();
        int evCost;
        int manaToConsume;

        if (AnimusConfig.ironsSpells.allowHybridCasting.get() && currentMana > 0) {
            manaToConsume = currentMana;
            evCost = manaDeficit * evPerMana;
        } else {
            manaToConsume = 0;
            evCost = manaCost * evPerMana;
        }

        if (network.getCurrentEV() < evCost) {
            player.sendOverlayMessage(
                Component.literal("Not enough EV! Required: " + evCost + " EV")
                    .withStyle(ChatFormatting.RED));
            event.setCanceled(true);
            return;
        }

        AnimaTicket ticket = AnimaTicket.create(evCost);

        var syphonResult = network.syphonAndDamage(player, ticket);
        if (!syphonResult.success()) {
            player.sendOverlayMessage(
                Component.literal("Failed to consume EV!")
                    .withStyle(ChatFormatting.RED));
            event.setCanceled(true);
            return;
        }

        if (manaToConsume > 0) {
            magicData.setMana((int) (magicData.getMana() - manaToConsume));
        }

        spawnEVCastFeedback(player, manaToConsume > 0);

        Animus.LOGGER.debug("Player {} cast spell using {} EV{}",
            player.getName().getString(),
            evCost,
            manaToConsume > 0 ? " (+ " + manaToConsume + " mana)" : ""
        );

        // Don't cancel - mana cost was paid via EV, let the spell cast
    }

    private void spawnEVCastFeedback(Player player, boolean isHybrid) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double x = player.getX();
        double y = player.getY() + player.getEyeHeight() * 0.5;
        double z = player.getZ();

        int particleCount = isHybrid ? 8 : 15;
        for (int i = 0; i < particleCount; i++) {
            double angle = (Math.PI * 2 * i) / particleCount;
            double radius = 0.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

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

        serverLevel.playSound(
            null,
            player.blockPosition(),
            SoundEvents.EXPERIENCE_ORB_PICKUP,
            SoundSource.PLAYERS,
            0.15F,
            0.8F + player.getRandom().nextFloat() * 0.4F
        );
    }

    @javax.annotation.Nullable
    private ItemStack findBloodOrb(Player player) {
        var fromInventory = InventorySearchHelper.findFirst(player, stack -> stack.getItem() instanceof BloodOrbItem);
        if (fromInventory.isPresent()) {
            return fromInventory.get();
        }

        var curiosResult = CuriosApi.getCuriosInventory(player)
            .map(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof BloodOrbItem))
            .orElse(Optional.empty());

        return curiosResult.map(r -> r.stack()).orElse(null);
    }
}
