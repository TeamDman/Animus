package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.blockentities.BlockEntityAntiLife;
import com.breakinblocks.animusnv.blocks.BlockAntiLife;
import com.breakinblocks.animusnv.items.sigils.effects.BuilderSigilEffect;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.breakinblocks.animusnv.rituals.RitualUnmaking;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.ritual.IMasterRitualStone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Constants.Mod.MODID)
@PrefixGameTestTemplate(false)
public class AnimusTransactionTests {
    private static final String TEMPLATE = "empty_5x5x7";

    private void replace(GameTestHelper h, ServerPlayer player, BlockPos pos, Block original) throws Exception {
        Class<?> type = Class.forName("com.breakinblocks.animusnv.items.sigils.effects.EquivalencySigilEffect$ReplacementOperation");
        var constructor = type.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        var network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(1000), 1000);
        var operation = constructor.newInstance(h.getLevel(), player.getUUID(), List.of(pos), List.of(Blocks.DIRT), original, pos, network, 25);
        var method = type.getDeclaredMethod("replaceBlock", ServerPlayer.class, BlockPos.class);
        method.setAccessible(true);
        method.invoke(operation, player, pos);
    }

    @GameTest(template = TEMPLATE)
    public void equivalency_canceled_place_preserves_source_and_inventory(GameTestHelper h) throws Exception {
        var player = RegressionTestSupport.player(h.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().add(new ItemStack(Items.DIRT));
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        h.getLevel().setBlockAndUpdate(pos, Blocks.DIAMOND_BLOCK.defaultBlockState());
        Consumer<BlockEvent.EntityPlaceEvent> deny = event -> { if (event.getPos().equals(pos)) event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, BlockEvent.EntityPlaceEvent.class, deny);
        try {
            replace(h, player, pos, Blocks.DIAMOND_BLOCK);
            h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.DIAMOND_BLOCK), "protected source remains");
            h.assertTrue(player.getInventory().countItem(Items.DIRT) == 1, "material retained");
            h.assertTrue(player.getInventory().countItem(Items.DIAMOND_BLOCK) == 0, "no protected drops");
            h.assertTrue(NeoVitaeAPI.getInstance().getAnima(player.getUUID()).getCurrentEV() == 1000, "canceled replacement costs no EV");
        } finally {
            NeoForge.EVENT_BUS.unregister(deny);
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void equivalency_successful_exchange_conserves_items(GameTestHelper h) throws Exception {
        var player = RegressionTestSupport.player(h.getLevel());
        player.getInventory().add(new ItemStack(Items.DIRT));
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        h.getLevel().setBlockAndUpdate(pos, Blocks.DIAMOND_BLOCK.defaultBlockState());
        replace(h, player, pos, Blocks.DIAMOND_BLOCK);
        h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.DIRT), "replacement placed");
        h.assertTrue(player.getInventory().countItem(Items.DIRT) == 0, "one material consumed");
        h.assertTrue(player.getInventory().countItem(Items.DIAMOND_BLOCK) == 1, "one source returned");
        h.assertTrue(NeoVitaeAPI.getInstance().getAnima(player.getUUID()).getCurrentEV() == 975, "successful replacement costs exactly 25 EV");
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void builder_preserves_shulker_contents(GameTestHelper h) {
        var player = RegressionTestSupport.player(h.getLevel());
        BlockPos origin = h.absolutePos(new BlockPos(2, 1, 2));
        player.moveTo(origin.getX(), origin.getY(), origin.getZ(), 0, 0);
        var sigil = new ItemStack(AnimusItems.SIGIL_BUILDER.get());
        var box = new ItemStack(Items.SHULKER_BOX);
        box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND, 7))));
        player.setItemInHand(InteractionHand.MAIN_HAND, sigil);
        player.setItemInHand(InteractionHand.OFF_HAND, box);
        h.assertTrue(new BuilderSigilEffect().useOnAir(h.getLevel(), player, sigil), "placement succeeds");
        BlockPos placed = player.blockPosition().relative(player.getDirection(), 2).above();
        var be = (ShulkerBoxBlockEntity) h.getLevel().getBlockEntity(placed);
        h.assertTrue(be.getItem(0).is(Items.DIAMOND) && be.getItem(0).getCount() == 7, "contents preserved");
        h.assertTrue(box.isEmpty(), "placed box consumed");
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void builder_canceled_placement_preserves_stack(GameTestHelper h) {
        var player = RegressionTestSupport.player(h.getLevel());
        BlockPos origin = h.absolutePos(new BlockPos(2, 1, 2));
        player.moveTo(origin.getX(), origin.getY(), origin.getZ(), 0, 0);
        var sigil = new ItemStack(AnimusItems.SIGIL_BUILDER.get());
        var material = new ItemStack(Items.DIRT);
        player.setItemInHand(InteractionHand.MAIN_HAND, sigil);
        player.setItemInHand(InteractionHand.OFF_HAND, material);
        BlockPos pos = player.blockPosition().relative(player.getDirection(), 2).above();
        Consumer<BlockEvent.EntityPlaceEvent> deny = event -> { if (event.getPos().equals(pos)) event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, BlockEvent.EntityPlaceEvent.class, deny);
        try {
            h.assertTrue(!new BuilderSigilEffect().useOnAir(h.getLevel(), player, sigil), "placement denied");
            h.assertTrue(h.getLevel().isEmptyBlock(pos), "world rolled back");
            h.assertTrue(material.getCount() == 1, "material preserved");
        } finally {
            NeoForge.EVENT_BUS.unregister(deny);
        }
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void antilife_pauses_without_present_owner(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        BlockAntiLife block = (BlockAntiLife) AnimusBlocks.BLOCK_ANTILIFE.get();
        h.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
        var be = (BlockEntityAntiLife) h.getLevel().getBlockEntity(pos);
        be.setSeeking(Blocks.DIAMOND_BLOCK).setPlayerUUID(UUID.randomUUID());
        h.getLevel().setBlockAndUpdate(pos.east(), Blocks.DIAMOND_BLOCK.defaultBlockState());
        block.tick(block.defaultBlockState(), h.getLevel(), pos, h.getLevel().getRandom());
        h.assertTrue(h.getLevel().getBlockState(pos.east()).is(Blocks.DIAMOND_BLOCK), "absent owner cannot spread");
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void unmaking_uses_books_across_multiple_entities(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        var registry = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.enchant(registry.getOrThrow(Enchantments.EFFICIENCY), 5);
        tool.enchant(registry.getOrThrow(Enchantments.UNBREAKING), 3);
        h.getLevel().addFreshEntity(new ItemEntity(h.getLevel(), pos.getX(), pos.getY(), pos.getZ(), tool));
        for (int i = 0; i < 2; i++) h.getLevel().addFreshEntity(new ItemEntity(h.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.BOOK)));
        UUID owner = UUID.randomUUID();
        var network = NeoVitaeAPI.getInstance().getAnima(owner);
        network.set(AnimaTicket.create(10000), 10000);
        IMasterRitualStone mrs = (IMasterRitualStone) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{IMasterRitualStone.class}, (proxy, method, args) -> switch (method.getName()) {
            case "getWorldObj", "getLevel" -> h.getLevel();
            case "getMasterBlockPos", "getBlockPos" -> pos;
            case "getOwner" -> owner;
            case "stopRitual" -> null;
            default -> throw new UnsupportedOperationException(method.getName());
        });
        new RitualUnmaking().performRitual(mrs);
        h.assertTrue(tool.get(DataComponents.ENCHANTMENTS).isEmpty(), "tool fully extracted");
        long books = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2)).stream().filter(e -> e.getItem().is(Items.ENCHANTED_BOOK)).count();
        h.assertTrue(books == 2, "both enchantments recovered");
        h.succeed();
    }
}
