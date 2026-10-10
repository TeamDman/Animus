package com.breakinblocks.animusnv.compat.ironsartifice;

import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.modifier.OnShotEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public final class BloodBulletCostOnShot implements OnShotEffect {
    @Override
    public void onShot(ServerLevel level, LivingEntity shooter, ShotProfile profile) {
        int cost = BloodBulletModifier.getEvCost();
        if (cost <= 0 || BloodBulletModifier.hasInfiniteMaterials(shooter) || !BloodBulletModifier.canPay(shooter)) {
            return;
        }
        IAnima anima = NeoVitaeAPI.getInstance().getAnima(shooter.getUUID());
        if (anima != null) {
            anima.syphon(AnimaTicket.create(cost));
        }
    }
}
