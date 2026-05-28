package com.breakinblocks.animusnv.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.util.FakePlayer;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.spiritus.ISpiritusHandler;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * FakePlayer implementation for Ritual of Culling
 * Used to simulate player kills for player-only drops like blaze rods
 *
 * Based on Mob Grinding Utils implementation:
 * https://github.com/vadis365/Mob-Grinding-Utils/blob/main/MobGrindingUtils/MobGrindingUtils/src/main/java/mob_grinding_utils/tile/TileEntitySaw.java
 */
public class AnimusFakePlayer extends FakePlayer {
    private static WeakReference<AnimusFakePlayer> CACHED_PLAYER = null;

    private AnimusFakePlayer(ServerLevel level, GameProfile profile) {
        super(level, profile);
    }

    public static AnimusFakePlayer get(ServerLevel level, UUID ownerUUID, @Nullable String ownerName) {
        AnimusFakePlayer cached = CACHED_PLAYER != null ? CACHED_PLAYER.get() : null;

        if (cached != null && cached.level() == level && cached.getUUID().equals(ownerUUID)) {
            return cached;
        }

        String name = ownerName != null ? ownerName : "RitualOwner";
        GameProfile profile = new GameProfile(ownerUUID, name);
        AnimusFakePlayer player = new AnimusFakePlayer(level, profile);

        CACHED_PLAYER = new WeakReference<>(player);
        return player;
    }

    /**
     * Each will type with >10 will adds +1 looting level to the sword.
     */
    public static ItemStack createLootingSword(ServerLevel level, BlockPos pos) {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);

        int lootingLevel = 0;

        ISpiritusHandler spiritusHandler = NeoVitaeAPI.getInstance().getSpiritusHandler();
        double ruinaSpiritus = spiritusHandler.getCurrentSpiritus(level, pos, SpiritusType.RUINA);
        double nihilumSpiritus = spiritusHandler.getCurrentSpiritus(level, pos, SpiritusType.NIHILUM);
        double vindictaSpiritus = spiritusHandler.getCurrentSpiritus(level, pos, SpiritusType.VINDICTA);
        double invictusSpiritus = spiritusHandler.getCurrentSpiritus(level, pos, SpiritusType.INVICTUS);

        if (ruinaSpiritus > 10) lootingLevel++;
        if (nihilumSpiritus > 10) lootingLevel++;
        if (vindictaSpiritus > 10) lootingLevel++;
        if (invictusSpiritus > 10) lootingLevel++;

        if (lootingLevel > 0) {
            final int finalLootingLevel = lootingLevel;
            var enchantRegistry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            enchantRegistry.get(Enchantments.LOOTING).ifPresent(holder ->
                sword.enchant(holder, finalLootingLevel)
            );
        }

        return sword;
    }

    // Block all damage to prevent thorns/damage reflection
    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
}
