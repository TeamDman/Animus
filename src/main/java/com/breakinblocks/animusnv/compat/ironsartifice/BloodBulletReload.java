package com.breakinblocks.animusnv.compat.ironsartifice;

import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.item.ReloadState;
import io.redspace.irons_artifice.item.TopLoadConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class BloodBulletReload {
    private BloodBulletReload() {}

    @Nullable
    public static ReloadResult start(LivingEntity living, ItemStack gun) {
        if (!(gun.getItem() instanceof GunItem gunItem) || !(living instanceof Player player)) {
            return null;
        }
        if (player.level().isClientSide() || BloodBulletModifier.hasInfiniteMaterials(player)) {
            return null;
        }
        ShotProfile profile = GunplayManager.compose(player, gunItem.getGun(), gun);
        if (!BloodBulletModifier.isActive(profile)) {
            return null;
        }
        int capacity = gunItem.magazineCapacity();
        int missing = GunItem.getMagazine(gun).missing(capacity);
        if (missing <= 0) {
            return null;
        }
        int affordable = affordableRounds(player);
        if (affordable <= 0) {
            player.sendOverlayMessage(Component.translatable("text.component.animusnv.blood_bullet.no_ev")
                    .withStyle(ChatFormatting.RED));
            return null;
        }
        int rounds = Math.min(missing, affordable);
        double speed = profile.value(ShotComponents.RELOAD_SPEED_MULTIPLIER);
        TopLoadConfig topLoad = gunItem.getGun().topLoadConfig();
        boolean topOff = topLoad != null && rounds < capacity;
        ReloadState.start(gun, gunItem.getGun().reloadTimeTicks(), speed, rounds, topOff ? topLoad : null);
        GunplayManager.playReloadAnimation(player, gun);
        if (player.isUsingItem()) {
            player.stopUsingItem();
        }
        return ReloadResult.STARTING_RELOAD;
    }

    @Nullable
    public static ReloadResult finish(LivingEntity living, ItemStack gun, int roundsToLoad) {
        if (!(gun.getItem() instanceof GunItem gunItem) || !(living instanceof Player player)) {
            return null;
        }
        if (player.level().isClientSide() || BloodBulletModifier.hasInfiniteMaterials(player)) {
            return null;
        }
        if (!BloodBulletModifier.isActive(GunplayManager.compose(player, gunItem.getGun(), gun))) {
            return null;
        }
        MagazineContents magazine = GunItem.getMagazine(gun);
        int missing = magazine.missing(gunItem.magazineCapacity());
        if (missing <= 0) {
            return null;
        }
        int affordable = affordableRounds(player);
        if (affordable <= 0) {
            return null;
        }
        int toLoad = Math.min(missing, affordable);
        if (roundsToLoad > 0) {
            toLoad = Math.min(toLoad, roundsToLoad);
        }
        int cost = BloodBulletModifier.getEvCost();
        if (cost > 0) {
            NeoVitaeAPI.getInstance().getAnima(player.getUUID()).syphon(AnimaTicket.create(toLoad * cost));
        }
        GunItem.setMagazine(gun, magazine.with(magazine.count() + toLoad));
        return ReloadResult.FINISHED_RELOAD;
    }

    private static int affordableRounds(Player player) {
        int cost = BloodBulletModifier.getEvCost();
        if (cost <= 0) {
            return Integer.MAX_VALUE;
        }
        IAnima anima = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        return anima == null ? 0 : anima.getCurrentEV() / cost;
    }
}
