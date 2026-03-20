package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.util.InventorySearchHelper;
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
import com.breakinblocks.neovitae.common.item.BloodOrbItem;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.ISoulNetwork;
import com.breakinblocks.neovitae.api.soul.SoulTicket;

/**
 * Handles spell casting events to enable LP-powered spell casting
 *
 * Features:
 * - Intercepts spell casting to use LP instead of mana
 * - Checks for Blood Orb requirement (configurable)
 * - Supports hybrid casting (partial mana + partial LP)
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

        if (!AnimusConfig.ironsSpells.enableLPCasting.get()) {
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

        if (AnimusConfig.ironsSpells.requireBloodOrb.get()) {
            if (!hasBloodOrb(player)) {
                return;
            }
        }

        ISoulNetwork network = NeoVitaeAPI.getInstance().getSoulNetwork(player.getUUID());
        if (network == null) {
            return;
        }

        int lpPerMana = AnimusConfig.ironsSpells.lpPerMana.get();
        int lpCost;
        int manaToConsume;

        if (AnimusConfig.ironsSpells.allowHybridCasting.get() && currentMana > 0) {
            manaToConsume = currentMana;
            lpCost = manaDeficit * lpPerMana;
        } else {
            manaToConsume = 0;
            lpCost = manaCost * lpPerMana;
        }

        if (network.getCurrentEssence() < lpCost) {
            player.displayClientMessage(
                Component.literal("Not enough Life Points! Required: " + lpCost + " LP")
                    .withStyle(ChatFormatting.RED),
                true
            );
            event.setCanceled(true);
            return;
        }

        SoulTicket ticket = SoulTicket.create(lpCost);

        var syphonResult = network.syphonAndDamage(player, ticket);
        if (!syphonResult.success()) {
            player.displayClientMessage(
                Component.literal("Failed to consume Life Points!")
                    .withStyle(ChatFormatting.RED),
                true
            );
            event.setCanceled(true);
            return;
        }

        if (manaToConsume > 0) {
            magicData.setMana((int) (magicData.getMana() - manaToConsume));
        }

        spawnLPCastFeedback(player, manaToConsume > 0);

        Animus.LOGGER.debug("Player {} cast spell using {} LP{}",
            player.getName().getString(),
            lpCost,
            manaToConsume > 0 ? " (+ " + manaToConsume + " mana)" : ""
        );

        // Don't cancel - mana cost was paid via LP, let the spell cast
    }

    private void spawnLPCastFeedback(Player player, boolean isHybrid) {
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

    private boolean hasBloodOrb(Player player) {
        if (InventorySearchHelper.hasItem(player, stack -> stack.getItem() instanceof BloodOrbItem)) {
            return true;
        }

        return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
            .map(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof BloodOrbItem).isPresent())
            .orElse(false);
    }
}
