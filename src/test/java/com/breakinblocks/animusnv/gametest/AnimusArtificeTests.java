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
import io.redspace.irons_artifice.data.ReloadResult;
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
        r.add("artifice_blood_bullet_fires_from_magazine", AnimusArtificeTests::bloodBulletFiresFromMagazine);
        r.add("artifice_blood_bullet_reloads_with_ev", AnimusArtificeTests::bloodBulletReloadsWithEv);
        r.add("artifice_blood_bullet_partial_reload", AnimusArtificeTests::bloodBulletPartialReload);
        r.add("artifice_blood_bullet_reload_falls_back_to_bullets", AnimusArtificeTests::bloodBulletReloadFallsBack);
        r.add("artifice_spirit_powder_scales_damage", AnimusArtificeTests::spiritPowderScalesDamage);
        r.add("artifice_spirit_powder_snares_target", AnimusArtificeTests::spiritPowderSnaresTarget);
        r.add("artifice_recipes_load", AnimusArtificeTests::recipesLoad);
    }

    private static ItemStack gunWith(int rounds, Item... modifiers) {
        return gunWith(ItemRegistry.MUSKET.get(), rounds, modifiers);
    }

    private static ItemStack gunWith(Item gun, int rounds, Item... modifiers) {
        ItemStack stack = new ItemStack(gun);
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

    private static void bloodBulletFiresFromMagazine(GameTestHelper h) {
        ServerPlayer player = shooter(h, gunWith(1, IronsArtificeCompat.BLOOD_BULLET_MODIFIER.get()));
        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(1000), 1000);

        FireOutcome first = GunplayManager.tryFire(player, FORWARD);
        h.assertValueEqual(first, FireOutcome.FIRED, "loaded round fires");
        h.assertValueEqual(GunItem.getMagazine(player.getMainHandItem()).count(), 0, "the shot used the loaded round");
        h.assertValueEqual(network.getCurrentEV(), 1000, "firing takes no EV");

        FireDelayState.clear(player);
        FireOutcome second = GunplayManager.tryFire(player, FORWARD);
        h.assertValueEqual(second, FireOutcome.EMPTY_MAGAZINE, "an empty Blood Bullet gun needs a reload");
        h.succeed();
    }

    private static void bloodBulletReloadsWithEv(GameTestHelper h) {
        ServerPlayer player = shooter(h, gunWith(
                ItemRegistry.BLACKPOWDER_REVOLVER.get(), 0, IronsArtificeCompat.BLOOD_BULLET_MODIFIER.get()));
        ItemStack held = player.getMainHandItem();
        int capacity = ((GunItem) held.getItem()).magazineCapacity();
        int cost = BloodBulletModifier.getEvCost();
        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(capacity * cost + 50), capacity * cost + 50);

        h.assertValueEqual(GunplayManager.attemptStartReload(player, held), ReloadResult.STARTING_RELOAD,
                "reload starts with no bullets in the inventory");
        h.assertValueEqual(GunplayManager.attemptFinishReload(player, held, capacity), ReloadResult.FINISHED_RELOAD,
                "reload finishes");
        h.assertValueEqual(GunItem.getMagazine(held).count(), capacity, "magazine filled");
        h.assertValueEqual(network.getCurrentEV(), 50, "a full reload costs the per-round EV times the magazine size");
        h.succeed();
    }

    private static void bloodBulletPartialReload(GameTestHelper h) {
        ServerPlayer player = shooter(h, gunWith(
                ItemRegistry.BLACKPOWDER_REVOLVER.get(), 0, IronsArtificeCompat.BLOOD_BULLET_MODIFIER.get()));
        ItemStack held = player.getMainHandItem();
        int cost = BloodBulletModifier.getEvCost();
        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(2 * cost + 10), 2 * cost + 10);

        h.assertValueEqual(GunplayManager.attemptFinishReload(player, held, 0), ReloadResult.FINISHED_RELOAD,
                "reload finishes");
        h.assertValueEqual(GunItem.getMagazine(held).count(), 2, "only the affordable rounds are loaded");
        h.assertValueEqual(network.getCurrentEV(), 10, "two rounds charged");
        h.succeed();
    }

    private static void bloodBulletReloadFallsBack(GameTestHelper h) {
        ServerPlayer player = shooter(h, gunWith(
                ItemRegistry.BLACKPOWDER_REVOLVER.get(), 0, IronsArtificeCompat.BLOOD_BULLET_MODIFIER.get()));
        ItemStack held = player.getMainHandItem();
        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(0), 0);

        h.assertValueEqual(GunplayManager.attemptStartReload(player, held), ReloadResult.NO_AMMO,
                "no EV and no bullets refuses the reload");

        player.getInventory().add(new ItemStack(ItemRegistry.BULLET.get(), 3));
        h.assertValueEqual(GunplayManager.attemptStartReload(player, held), ReloadResult.STARTING_RELOAD,
                "a dry network reloads from bullets");
        h.assertValueEqual(GunplayManager.attemptFinishReload(player, held, 0), ReloadResult.FINISHED_RELOAD,
                "bullet reload finishes");
        h.assertValueEqual(GunItem.getMagazine(held).count(), 3, "the bullets were loaded");
        h.assertValueEqual(GunplayManager.countBullets(player), 0, "the bullets were taken from the inventory");
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
