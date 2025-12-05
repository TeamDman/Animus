package com.teamdman.animus.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.util.FakePlayer;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.demonaura.WorldDemonWillHandler;

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

    /**
     * Get or create a FakePlayer for the given owner
     * Uses caching to avoid creating new FakePlayers constantly
     *
     * @param level The server level
     * @param ownerUUID The owner's UUID (ritual owner)
     * @param ownerName The owner's name (can be null, will use "RitualOwner" as default)
     * @return A FakePlayer instance
     */
    public static AnimusFakePlayer get(ServerLevel level, UUID ownerUUID, @Nullable String ownerName) {
        AnimusFakePlayer cached = CACHED_PLAYER != null ? CACHED_PLAYER.get() : null;

        // Reuse cached player if same level and owner
        if (cached != null && cached.level() == level && cached.getUUID().equals(ownerUUID)) {
            return cached;
        }

        // Create new FakePlayer with owner's UUID
        String name = ownerName != null ? ownerName : "RitualOwner";
        GameProfile profile = new GameProfile(ownerUUID, name);
        AnimusFakePlayer player = new AnimusFakePlayer(level, profile);

        CACHED_PLAYER = new WeakReference<>(player);
        return player;
    }

    /**
     * Create a fake netherite sword with looting enchantment based on demon will levels
     * Each will type (corrosive, destructive, vengeful, steadfast) with >10 will adds +1 looting
     *
     * @param level The server level
     * @param pos Position to check for demon will (usually ritual position)
     * @return ItemStack of netherite sword with appropriate looting level
     */
    public static ItemStack createLootingSword(ServerLevel level, net.minecraft.core.BlockPos pos) {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);

        int lootingLevel = 0;

        // Check each will type - each with >10 will adds +1 looting
        double corrosiveWill = WorldDemonWillHandler.getCurrentWill(level, pos, EnumDemonWillType.CORROSIVE);
        double destructiveWill = WorldDemonWillHandler.getCurrentWill(level, pos, EnumDemonWillType.DESTRUCTIVE);
        double vengefulWill = WorldDemonWillHandler.getCurrentWill(level, pos, EnumDemonWillType.VENGEFUL);
        double steadfastWill = WorldDemonWillHandler.getCurrentWill(level, pos, EnumDemonWillType.STEADFAST);

        if (corrosiveWill > 10) lootingLevel++;
        if (destructiveWill > 10) lootingLevel++;
        if (vengefulWill > 10) lootingLevel++;
        if (steadfastWill > 10) lootingLevel++;

        if (lootingLevel > 0) {
            sword.enchant(Enchantments.MOB_LOOTING, lootingLevel);
        }

        return sword;
    }

    /**
     * Check if thorns damage should be blocked
     * We override hurt to prevent thorns damage from reflecting back
     */
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        // Block all damage to the fake player (prevents thorns, etc.)
        return false;
    }
}
