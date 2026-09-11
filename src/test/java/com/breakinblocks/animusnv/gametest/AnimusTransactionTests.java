package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.blockentities.BlockEntityAntiLife;
import com.breakinblocks.animusnv.blocks.BlockAntiLife;
import com.breakinblocks.animusnv.gametest.base.AnimusTestRegistrar;
import com.breakinblocks.animusnv.items.sigils.effects.BuilderSigilEffect;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.breakinblocks.animusnv.rituals.RitualUnmaking;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class AnimusTransactionTests {

    private AnimusTransactionTests() {
    }

    public static void register(AnimusTestRegistrar r) {
        r.add("equivalency_canceled_place_preserves_source", AnimusTransactionTests::equivalencyCanceledPlacePreservesSourceAndInventory);
        r.add("equivalency_exchange_conserves_items", AnimusTransactionTests::equivalencySuccessfulExchangeConservesItems);
        r.add("builder_preserves_shulker_contents", AnimusTransactionTests::builderPreservesShulkerContents);
        r.add("builder_canceled_placement_preserves_stack", AnimusTransactionTests::builderCanceledPlacementPreservesStack);
        r.add("antilife_pauses_without_present_owner", AnimusTransactionTests::antilifePausesWithoutPresentOwner);
        r.add("unmaking_uses_books_across_entities", AnimusTransactionTests::unmakingUsesBooksAcrossMultipleEntities);
    }

    private static IAnima fund(ServerPlayer player, int amount) {
        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(amount), amount);
        return network;
    }

    private static void equivalencyCanceledPlacePreservesSourceAndInventory(GameTestHelper h) {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().add(new ItemStack(Items.DIRT));
        IAnima network = fund(player, 1000);
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        h.getLevel().setBlockAndUpdate(pos, Blocks.DIAMOND_BLOCK.defaultBlockState());
        Consumer<BlockEvent.EntityPlaceEvent> deny = event -> {
            if (event.getPos().equals(pos)) {
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, BlockEvent.EntityPlaceEvent.class, deny);
        try {
            AnimusRegressionTests.replace(h, player, pos, Blocks.DIAMOND_BLOCK, 25);
            h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.DIAMOND_BLOCK), "protected source remains");
            h.assertTrue(player.getInventory().countItem(Items.DIRT) == 1, "material retained");
            h.assertTrue(player.getInventory().countItem(Items.DIAMOND_BLOCK) == 0, "no protected drops");
            h.assertTrue(network.getCurrentEV() == 1000, "canceled replacement costs no EV");
        } catch (Exception e) {
            h.fail("replacement threw: " + e);
            return;
        } finally {
            NeoForge.EVENT_BUS.unregister(deny);
            h.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        h.succeed();
    }

    private static void equivalencySuccessfulExchangeConservesItems(GameTestHelper h) {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().add(new ItemStack(Items.DIRT));
        IAnima network = fund(player, 1000);
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        h.getLevel().setBlockAndUpdate(pos, Blocks.DIAMOND_BLOCK.defaultBlockState());
        try {
            AnimusRegressionTests.replace(h, player, pos, Blocks.DIAMOND_BLOCK, 25);
        } catch (Exception e) {
            h.fail("replacement threw: " + e);
            return;
        }
        h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.DIRT), "replacement placed");
        h.assertTrue(player.getInventory().countItem(Items.DIRT) == 0, "one material consumed");
        h.assertTrue(player.getInventory().countItem(Items.DIAMOND_BLOCK) == 1, "one source returned");
        h.assertTrue(network.getCurrentEV() == 975, "successful replacement costs exactly 25 EV");
        h.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        h.succeed();
    }

    private static void builderPreservesShulkerContents(GameTestHelper h) {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        BlockPos origin = h.absolutePos(new BlockPos(2, 1, 2));
        player.snapTo(origin.getX(), origin.getY(), origin.getZ(), 0, 0);
        ItemStack sigil = new ItemStack(AnimusItems.SIGIL_BUILDER.get());
        ItemStack box = new ItemStack(Items.SHULKER_BOX);
        box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND, 7))));
        player.setItemInHand(InteractionHand.MAIN_HAND, sigil);
        player.setItemInHand(InteractionHand.OFF_HAND, box);
        BlockPos placed = player.blockPosition().relative(player.getDirection(), 2).above();
        h.assertTrue(new BuilderSigilEffect().useOnAir(h.getLevel(), player, sigil), "placement succeeds");
        if (!(h.getLevel().getBlockEntity(placed) instanceof ShulkerBoxBlockEntity be)) {
            h.fail("shulker box was not placed at " + placed);
            return;
        }
        h.assertTrue(be.getItem(0).is(Items.DIAMOND) && be.getItem(0).getCount() == 7, "contents preserved");
        h.assertTrue(player.getOffhandItem().isEmpty(), "placed box consumed");
        h.getLevel().setBlockAndUpdate(placed, Blocks.AIR.defaultBlockState());
        h.succeed();
    }

    private static void builderCanceledPlacementPreservesStack(GameTestHelper h) {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        BlockPos origin = h.absolutePos(new BlockPos(2, 1, 2));
        player.snapTo(origin.getX(), origin.getY(), origin.getZ(), 0, 0);
        ItemStack sigil = new ItemStack(AnimusItems.SIGIL_BUILDER.get());
        ItemStack material = new ItemStack(Items.DIRT);
        player.setItemInHand(InteractionHand.MAIN_HAND, sigil);
        player.setItemInHand(InteractionHand.OFF_HAND, material);
        BlockPos pos = player.blockPosition().relative(player.getDirection(), 2).above();
        Consumer<BlockEvent.EntityPlaceEvent> deny = event -> {
            if (event.getPos().equals(pos)) {
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, BlockEvent.EntityPlaceEvent.class, deny);
        try {
            h.assertTrue(!new BuilderSigilEffect().useOnAir(h.getLevel(), player, sigil), "placement denied");
            h.assertTrue(h.getLevel().isEmptyBlock(pos), "world rolled back");
            h.assertTrue(player.getOffhandItem().getCount() == 1, "material preserved");
        } finally {
            NeoForge.EVENT_BUS.unregister(deny);
        }
        h.succeed();
    }

    private static void antilifePausesWithoutPresentOwner(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        BlockAntiLife block = (BlockAntiLife) AnimusBlocks.BLOCK_ANTILIFE.get();
        h.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
        if (!(h.getLevel().getBlockEntity(pos) instanceof BlockEntityAntiLife be)) {
            h.fail("anti-life block entity missing");
            return;
        }
        be.setSeeking(Blocks.DIAMOND_BLOCK).setPlayerUUID(UUID.randomUUID());
        h.getLevel().setBlockAndUpdate(pos.east(), Blocks.DIAMOND_BLOCK.defaultBlockState());
        block.tick(block.defaultBlockState(), h.getLevel(), pos, h.getLevel().getRandom());
        h.assertTrue(h.getLevel().getBlockState(pos.east()).is(Blocks.DIAMOND_BLOCK), "absent owner cannot spread");
        h.getLevel().setBlockAndUpdate(pos.east(), Blocks.AIR.defaultBlockState());
        h.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        h.succeed();
    }

    private static void unmakingUsesBooksAcrossMultipleEntities(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        HolderLookup.RegistryLookup<Enchantment> registry = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.enchant(registry.getOrThrow(Enchantments.EFFICIENCY), 5);
        tool.enchant(registry.getOrThrow(Enchantments.UNBREAKING), 3);
        h.getLevel().addFreshEntity(new ItemEntity(h.getLevel(), pos.getX(), pos.getY(), pos.getZ(), tool));
        for (int i = 0; i < 2; i++) {
            h.getLevel().addFreshEntity(new ItemEntity(h.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.BOOK)));
        }
        UUID owner = UUID.randomUUID();
        IAnima network = NeoVitaeAPI.getInstance().getAnima(owner);
        network.set(AnimaTicket.create(10000), 10000);
        new RitualUnmaking().performRitual(RegressionTestSupport.stone(h, pos, owner));
        h.assertTrue(tool.get(DataComponents.ENCHANTMENTS).isEmpty(), "tool fully extracted");
        long books = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2)).stream()
            .filter(e -> e.getItem().is(Items.ENCHANTED_BOOK)).count();
        h.assertTrue(books == 2, "both enchantments recovered");
        h.succeed();
    }
}
