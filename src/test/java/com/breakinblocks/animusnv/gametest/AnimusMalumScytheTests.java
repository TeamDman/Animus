package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.item.soul.SentientScytheItem;
import com.sammy.malum.common.entity.scythe.ScytheBoomerangEntity;
import com.sammy.malum.registry.common.MalumDamageTypes;
import com.sammy.malum.registry.common.MalumTags;
import com.sammy.malum.registry.common.enchantment.EnchantmentKeys;
import com.sammy.malum.registry.common.item.MalumItems;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Constants.Mod.MODID)
@PrefixGameTestTemplate(false)
public class AnimusMalumScytheTests {
    private static final String TEMPLATE = "empty_5x5x7";

    @GameTest(template = TEMPLATE)
    public void animus_scythes_are_malum_scythes(GameTestHelper h) {
        h.assertTrue(
                new ItemStack(AnimusItems.RUNIC_SENTIENT_SCYTHE.get()).is(MalumTags.ItemTags.SCYTHES),
                "runic sentient scythe is tagged malum:scythe");
        h.assertTrue(
                new ItemStack(AnimusItems.HAND_OF_DEATH.get()).is(MalumTags.ItemTags.SCYTHES),
                "hand of death is tagged malum:scythe");
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void scythe_area_attack_deals_malum_sweep_damage(GameTestHelper h) throws Exception {
        ServerPlayer player = playerAt(h, new BlockPos(2, 2, 1));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimusItems.HAND_OF_DEATH.get()));
        Zombie target = h.spawn(EntityType.ZOMBIE, new BlockPos(2, 2, 3));
        Zombie bystander = h.spawn(EntityType.ZOMBIE, new BlockPos(3, 2, 3));

        Method areaAttack = SentientScytheItem.class.getDeclaredMethod(
                "performAreaAttack", Player.class, LivingEntity.class, SpiritusType.class, int.class);
        areaAttack.setAccessible(true);
        areaAttack.invoke(player.getMainHandItem().getItem(), player, target, SpiritusType.RAW, 0);

        DamageSource source = bystander.getLastDamageSource();
        h.assertTrue(source != null, "bystander was hit by the area attack");
        h.assertTrue(source.is(MalumDamageTypes.SCYTHE_SWEEP), "area attack uses malum:scythe_sweep, got " + source);
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void scythe_vanilla_sweep_deals_malum_sweep_damage(GameTestHelper h) throws Exception {
        ServerPlayer player = playerAt(h, new BlockPos(2, 2, 1));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimusItems.RUNIC_SENTIENT_SCYTHE.get()));
        player.setOnGround(true);
        Field strength = LivingEntity.class.getDeclaredField("attackStrengthTicker");
        strength.setAccessible(true);
        strength.setInt(player, 100);
        Zombie target = h.spawn(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        Zombie bystander = h.spawn(EntityType.ZOMBIE, new BlockPos(3, 2, 2));

        List<DamageSource> bystanderHits = new ArrayList<>();
        Consumer<LivingIncomingDamageEvent> listener = event -> {
            if (event.getEntity() == bystander) {
                bystanderHits.add(event.getSource());
            }
        };
        NeoForge.EVENT_BUS.addListener(listener);
        try {
            player.attack(target);
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }

        h.assertFalse(bystanderHits.isEmpty(), "bystander was caught in the sweep");
        for (DamageSource source : bystanderHits) {
            h.assertFalse(source.is(DamageTypes.PLAYER_ATTACK), "sweep hit used plain player_attack, which Pact of the Reaper punishes");
        }
        h.assertTrue(bystanderHits.stream().anyMatch(source -> source.is(MalumDamageTypes.SCYTHE_SWEEP)), "sweep hits use malum:scythe_sweep");
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void rebound_throws_animus_scythe(GameTestHelper h) {
        ServerPlayer player = playerAt(h, new BlockPos(2, 2, 2));
        ItemStack scythe = enchanted(h, AnimusItems.RUNIC_SENTIENT_SCYTHE.get(), EnchantmentKeys.REBOUND);
        player.setItemInHand(InteractionHand.MAIN_HAND, scythe);

        InteractionResultHolder<ItemStack> result = scythe.use(h.getLevel(), player, InteractionHand.MAIN_HAND);

        List<ScytheBoomerangEntity> thrown = h.getLevel()
                .getEntitiesOfClass(
                        ScytheBoomerangEntity.class, player.getBoundingBox().inflate(6));
        thrown.forEach(ScytheBoomerangEntity::discard);
        h.assertTrue(result.getResult().consumesAction(), "right click is handled by rebound");
        h.assertFalse(thrown.isEmpty(), "rebound spawns a thrown scythe");
        h.assertTrue(
                player.getMainHandItem().is(MalumItems.SOUL_OF_A_SCYTHE.get()),
                "scythe leaves the hand while thrown");
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void ascension_triggers_on_animus_scythe(GameTestHelper h) {
        ServerPlayer player = playerAt(h, new BlockPos(2, 2, 2));
        ItemStack scythe = enchanted(h, AnimusItems.HAND_OF_DEATH.get(), EnchantmentKeys.ASCENSION);
        player.setItemInHand(InteractionHand.MAIN_HAND, scythe);

        InteractionResultHolder<ItemStack> result = scythe.use(h.getLevel(), player, InteractionHand.MAIN_HAND);

        h.assertTrue(result.getResult().consumesAction(), "right click is handled by ascension");
        h.assertTrue(player.getCooldowns().isOnCooldown(scythe.getItem()), "ascension puts the scythe on cooldown");
        h.succeed();
    }

    private static ServerPlayer playerAt(GameTestHelper h, BlockPos relative) {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        BlockPos pos = h.absolutePos(relative);
        player.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
        return player;
    }

    private static ItemStack enchanted(GameTestHelper h, Item item, ResourceKey<Enchantment> key) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(
                h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key), 1);
        return stack;
    }
}
