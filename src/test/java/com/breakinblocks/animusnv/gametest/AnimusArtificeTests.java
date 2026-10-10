package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.IronsArtificeCompat;
import com.breakinblocks.animusnv.compat.ironsartifice.BloodBulletModifier;
import com.breakinblocks.animusnv.gametest.base.AnimusTestRegistrar;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.effect.NVMobEffects;
import com.breakinblocks.neovitae.common.item.NVItems;
import com.breakinblocks.neovitae.spiritus.ISpiritusGem;
import io.redspace.irons_artifice.api.BulletImpactEvent;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.FireOutcome;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.item.PendingShot;
import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

public final class AnimusArtificeTests {
    private static final Vec3 FORWARD = new Vec3(0.0, 0.0, 1.0);

    private AnimusArtificeTests() {}

    public static void register(AnimusTestRegistrar r) {
        r.add("artifice_blood_bullet_uses_ev", AnimusArtificeTests::bloodBulletUsesEv);
        r.add("artifice_blood_bullet_falls_back_to_bullets", AnimusArtificeTests::bloodBulletFallsBack);
        r.add("artifice_spirit_powder_scales_damage", AnimusArtificeTests::spiritPowderScalesDamage);
        r.add("artifice_spirit_powder_snares_target", AnimusArtificeTests::spiritPowderSnaresTarget);
        r.add("artifice_recipes_load", AnimusArtificeTests::recipesLoad);
    }

    private static ItemStack gunWith(int rounds, Item... modifiers) {
        ItemStack stack = new ItemStack(ItemRegistry.MUSKET.get());
        GunItem.setMagazine(stack, new MagazineContents(rounds));
        GunContainer container = new GunContainer(stack);
        for (int slot = 0; slot < modifiers.length; slot++) {
            container.setItem(slot, new ItemStack(modifiers[slot]));
        }
        container.setChanged();
        return container.getGunStack();
    }

    private static ServerPlayer shooter(GameTestHelper h, ItemStack gun) {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        Vec3 pos = h.absoluteVec(new Vec3(2.5, 2, 2.5));
        player.snapTo(pos.x, pos.y, pos.z, 0, 0);
        player.setItemSlot(EquipmentSlot.MAINHAND, gun);
        FireDelayState.clear(player);
        PendingShot.clear(player);
        return player;
    }

    private static void bloodBulletUsesEv(GameTestHelper h) {
        ServerPlayer player = shooter(h, gunWith(0, IronsArtificeCompat.BLOOD_BULLET_MODIFIER.get()));
        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(1000), 1000);
        int cost = BloodBulletModifier.getEvCost();

        FireOutcome outcome = GunplayManager.tryFire(player, FORWARD);

        h.assertValueEqual(outcome, FireOutcome.FIRED, "empty gun with a Blood Bullet fires");
        h.assertValueEqual(network.getCurrentEV(), 1000 - cost, "one shot drains the configured EV");
        h.assertValueEqual(GunItem.getMagazine(player.getMainHandItem()).count(), 0, "magazine untouched");
        h.assertEntitiesPresent(EntityRegistry.BULLET.get(), 1);
        h.succeed();
    }

    private static void bloodBulletFallsBack(GameTestHelper h) {
        ServerPlayer player = shooter(h, gunWith(1, IronsArtificeCompat.BLOOD_BULLET_MODIFIER.get()));
        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(0), 0);

        FireOutcome first = GunplayManager.tryFire(player, FORWARD);
        h.assertValueEqual(first, FireOutcome.FIRED, "loaded round fires when the network is dry");
        h.assertValueEqual(GunItem.getMagazine(player.getMainHandItem()).count(), 0, "the loaded round was used");
        h.assertValueEqual(network.getCurrentEV(), 0, "no EV taken for a bullet-paid shot");

        FireDelayState.clear(player);
        FireOutcome second = GunplayManager.tryFire(player, FORWARD);
        h.assertValueEqual(second, FireOutcome.EMPTY_MAGAZINE, "no EV and no rounds refuses the shot");
        h.succeed();
    }

    private static void spiritPowderScalesDamage(GameTestHelper h) {
        ServerPlayer player = shooter(h, gunWith(1, IronsArtificeCompat.SPIRIT_POWDER_MODIFIER.get()));
        ItemStack held = player.getMainHandItem();
        GunItem gunItem = (GunItem) held.getItem();
        double plain = GunplayManager.compose(player, gunItem.getGun(), held).value(ShotComponents.DAMAGE);

        ItemStack gem = new ItemStack(NVItems.SPIRITUS_GEM_GRAND.get());
        ((ISpiritusGem) gem.getItem()).setSpiritus(SpiritusType.RAW, gem, 4000);
        player.getInventory().add(gem);
        double empowered = GunplayManager.compose(player, gunItem.getGun(), held).value(ShotComponents.DAMAGE);

        h.assertTrue(plain > 0, "musket has base damage");
        h.assertTrue(Math.abs(empowered - plain * 1.8) < 1e-6,
                "4000 spiritus gives +80% damage, got " + empowered + " from " + plain);
        h.succeed();
    }

    private static void spiritPowderSnaresTarget(GameTestHelper h) {
        ServerPlayer player = shooter(h, gunWith(1, IronsArtificeCompat.SPIRIT_POWDER_MODIFIER.get()));
        ItemStack held = player.getMainHandItem();
        Zombie zombie = h.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 2, 4));

        Bullet bullet = new Bullet(EntityRegistry.BULLET.get(), h.getLevel());
        bullet.setOwner(player);
        bullet.applyProfile(GunplayManager.compose(player, ((GunItem) held.getItem()).getGun(), held));
        bullet.setPos(zombie.position());
        NeoForge.EVENT_BUS.post(new BulletImpactEvent(bullet, new EntityHitResult(zombie), Bullet.HitState.DISCARD));

        h.assertTrue(zombie.hasEffect(NVMobEffects.SPIRITUS_SNARE), "Spirit Powder impact applies the soul snare");
        h.succeed();
    }

    private static void recipesLoad(GameTestHelper h) {
        var recipes = h.getLevel().getServer().getRecipeManager();
        for (String path : new String[] {"ara_vitae/blood_bullet_modifier", "hellfire_forge/spirit_powder_modifier"}) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, Constants.rl(path));
            h.assertTrue(recipes.byKey(key).isPresent(), "recipe " + path + " loaded");
        }
        h.succeed();
    }
}
