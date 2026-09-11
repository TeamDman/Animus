package com.teamdman.animus.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.teamdman.animus.blockentities.BlockEntityWillfulStone;
import com.teamdman.animus.items.sigils.ItemSigilTemporalDominance;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.rituals.RitualPersistence;
import com.teamdman.animus.rituals.RitualSerenity;
import com.teamdman.animus.rituals.RitualUnmaking;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wayoftime.bloodmagic.common.block.BloodMagicBlocks;
import wayoftime.bloodmagic.common.tile.TileMasterRitualStone;
import wayoftime.bloodmagic.core.data.Binding;
import wayoftime.bloodmagic.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.ritual.Ritual;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("animus")
@PrefixGameTestTemplate(false)
public class AnimusRegressionTests {
    static ServerPlayer player(ServerLevel level) {
        return FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "AnimusTest"));
    }

    static UseOnContext context(ServerPlayer player, BlockPos pos) {
        return new UseOnContext(player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private static BlockPos pos(GameTestHelper helper) {
        return helper.absolutePos(new BlockPos(1, 2, 1));
    }

    private static void replace(ServerLevel level, ServerPlayer player, BlockPos pos, Block original, Block replacement) throws Exception {
        Class<?> cls = Class.forName("com.teamdman.animus.items.sigils.ItemSigilEquivalency$ReplacementOperation");
        var constructor = cls.getDeclaredConstructor(ServerLevel.class, UUID.class, List.class, List.class, Block.class, BlockPos.class);
        constructor.setAccessible(true);
        Object operation = constructor.newInstance(level, player.getUUID(), List.of(pos), List.of(replacement), original, pos);
        var method = cls.getDeclaredMethod("replaceBlock", ServerPlayer.class, BlockPos.class);
        method.setAccessible(true);
        method.invoke(operation, player, pos);
    }

    @GameTest(template = "empty")
    public static void equivalencyOnlyAwardsDropsForSuccessfulReplacements(GameTestHelper helper) throws Exception {
        ServerPlayer player = player(helper.getLevel());
        BlockPos pos = pos(helper);
        helper.getLevel().setBlock(pos, Blocks.DIAMOND_BLOCK.defaultBlockState(), 3);
        player.getInventory().add(new ItemStack(Items.DIRT));
        replace(helper.getLevel(), player, pos, Blocks.DIAMOND_BLOCK, Blocks.DIRT);
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(Blocks.DIRT), "Replacement should succeed");
        helper.assertTrue(player.getInventory().countItem(Items.DIAMOND_BLOCK) == 1, "Successful replacement should award one drop");
        BlockPos second = pos.above();
        helper.getLevel().setBlock(second, Blocks.DIAMOND_BLOCK.defaultBlockState(), 3);
        replace(helper.getLevel(), player, second, Blocks.DIAMOND_BLOCK, Blocks.DIRT);
        helper.assertTrue(helper.getLevel().getBlockState(second).is(Blocks.DIAMOND_BLOCK), "Exhausted supplies must leave the block intact");
        helper.assertTrue(player.getInventory().countItem(Items.DIAMOND_BLOCK) == 1, "Exhausted supplies must not duplicate drops");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void equivalencyRespectsBedrockAndWillfulStoneOwnership(GameTestHelper helper) throws Exception {
        ServerPlayer player = player(helper.getLevel());
        BlockPos pos = pos(helper);
        player.getInventory().add(new ItemStack(Items.DIRT, 2));
        helper.getLevel().setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 3);
        replace(helper.getLevel(), player, pos, Blocks.BEDROCK, Blocks.DIRT);
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(Blocks.BEDROCK), "Bedrock must remain");
        Block stone = AnimusBlocks.BLOCK_WILLFUL_STONE_WHITE.get();
        helper.getLevel().setBlock(pos, stone.defaultBlockState(), 3);
        ((BlockEntityWillfulStone) helper.getLevel().getBlockEntity(pos)).setOwner(UUID.randomUUID());
        replace(helper.getLevel(), player, pos, stone, Blocks.DIRT);
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(stone), "Another player's Willful Stone must remain");
        helper.assertTrue(player.getInventory().countItem(Items.DIRT) == 2, "Denied replacements must not consume material");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void equivalencyHonorsProtectionEventsBeforeChangingWorld(GameTestHelper helper) throws Exception {
        ServerPlayer player = player(helper.getLevel());
        BlockPos pos = pos(helper);
        player.getInventory().add(new ItemStack(Items.DIRT));
        helper.getLevel().setBlock(pos, Blocks.DIAMOND_BLOCK.defaultBlockState(), 3);
        Consumer<BlockEvent.BreakEvent> denyBreak = event -> {
            if (event.getPos().equals(pos)) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, BlockEvent.BreakEvent.class, denyBreak);
        try {
            replace(helper.getLevel(), player, pos, Blocks.DIAMOND_BLOCK, Blocks.DIRT);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(denyBreak);
        }
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(Blocks.DIAMOND_BLOCK), "Cancelled break changed the block");
        boolean[] placed = {false};
        Consumer<BlockEvent.EntityPlaceEvent> denyPlace = event -> {
            if (event.getPos().equals(pos)) {
                placed[0] = true;
                helper.assertTrue(event.getPlacedBlock().is(Blocks.DIRT), "Placement event must describe the proposed block");
                helper.assertTrue(helper.getLevel().getBlockState(pos).is(Blocks.DIAMOND_BLOCK), "World changed before protection check");
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, BlockEvent.EntityPlaceEvent.class, denyPlace);
        try {
            replace(helper.getLevel(), player, pos, Blocks.DIAMOND_BLOCK, Blocks.DIRT);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(denyPlace);
        }
        helper.assertTrue(placed[0], "Placement protection event was not fired");
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(Blocks.DIAMOND_BLOCK), "Cancelled placement changed the block");
        helper.assertTrue(player.getInventory().countItem(Items.DIRT) == 1 && player.getInventory().countItem(Items.DIAMOND_BLOCK) == 0,
            "Cancelled operations changed inventory");
        helper.succeed();
    }

    private static IMasterRitualStone stone(GameTestHelper helper, Ritual ritual, BlockPos pos) {
        UUID owner = UUID.randomUUID();
        return (IMasterRitualStone) Proxy.newProxyInstance(AnimusRegressionTests.class.getClassLoader(), new Class<?>[]{IMasterRitualStone.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "getWorldObj" -> helper.getLevel();
                case "getMasterBlockPos" -> pos;
                case "getOwner" -> owner;
                case "getBlockRange" -> ritual.getBlockRange((String) args[0]);
                case "stopRitual" -> null;
                default -> throw new UnsupportedOperationException(method.getName());
            });
    }

    private static ItemEntity drop(GameTestHelper helper, ItemStack stack) {
        BlockPos pos = pos(helper);
        ItemEntity entity = new ItemEntity(helper.getLevel(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, stack);
        helper.getLevel().addFreshEntity(entity);
        return entity;
    }

    private static List<ItemEntity> enchantedBooks(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos(helper)).inflate(2),
            entity -> entity.getItem().is(Items.ENCHANTED_BOOK));
    }

    @GameTest(template = "empty")
    public static void unmakingPreservesUnextractedEnchantments(GameTestHelper helper) {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        EnchantmentHelper.setEnchantments(Map.of(Enchantments.SHARPNESS, 3, Enchantments.UNBREAKING, 3), sword);
        ItemEntity item = drop(helper, sword);
        drop(helper, new ItemStack(Items.BOOK));
        RitualUnmaking ritual = new RitualUnmaking();
        ritual.performRitual(stone(helper, ritual, pos(helper)));
        var remaining = EnchantmentHelper.getEnchantments(item.getItem());
        var books = enchantedBooks(helper);
        helper.assertTrue(remaining.size() == 1 && books.size() == 1, "One blank book must extract exactly one enchantment");
        var extracted = EnchantmentHelper.getEnchantments(books.get(0).getItem());
        helper.assertTrue(extracted.keySet().stream().noneMatch(remaining::containsKey), "Extracted enchantment still on original");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unmakingSplitsStoredBookEnchantments(GameTestHelper helper) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.setEnchantments(Map.of(Enchantments.SHARPNESS, 3), book);
        drop(helper, book);
        drop(helper, new ItemStack(Items.BOOK));
        RitualUnmaking ritual = new RitualUnmaking();
        ritual.performRitual(stone(helper, ritual, pos(helper)));
        var outputs = enchantedBooks(helper);
        helper.assertTrue(outputs.stream().mapToInt(e -> e.getItem().getCount()).sum() == 2, "Split must produce two books");
        helper.assertTrue(outputs.stream().allMatch(e -> EnchantmentHelper.getEnchantments(e.getItem()).equals(Map.of(Enchantments.SHARPNESS, 2))),
            "Split books must contain the reduced enchantment");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unmakingLeavesMultiEnchantmentBookIntactWhenSuppliesAreShort(GameTestHelper helper) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.setEnchantments(Map.of(Enchantments.SHARPNESS, 3, Enchantments.UNBREAKING, 3), book);
        ItemEntity original = drop(helper, book);
        ItemEntity blanks = drop(helper, new ItemStack(Items.BOOK));
        RitualUnmaking ritual = new RitualUnmaking();
        ritual.performRitual(stone(helper, ritual, pos(helper)));
        helper.assertTrue(EnchantmentHelper.getEnchantments(original.getItem()).size() == 2, "Insufficient books must not destroy enchantments");
        helper.assertTrue(blanks.getItem().getCount() == 1, "Insufficient books must not be consumed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void serenityClearsOnActualStopCallback(GameTestHelper helper) throws Exception {
        BlockPos pos = pos(helper);
        var add = RitualSerenity.class.getDeclaredMethod("addActiveRitual", Level.class, BlockPos.class, AABB.class);
        add.setAccessible(true);
        add.invoke(null, helper.getLevel(), pos, new AABB(pos));
        RitualSerenity ritual = new RitualSerenity();
        helper.assertTrue(RitualSerenity.isInSerenityZone(helper.getLevel(), pos), "Zone did not activate");
        ritual.stopRitual(stone(helper, ritual, pos), Ritual.BreakType.DEACTIVATE);
        helper.assertTrue(!RitualSerenity.isInSerenityZone(helper.getLevel(), pos), "Stopped ritual still blocks spawns");
        helper.succeed();
    }

    private static void loadChunks(GameTestHelper helper, RitualPersistence ritual, BlockPos pos) throws Exception {
        var load = RitualPersistence.class.getDeclaredMethod("loadChunks", ServerLevel.class, BlockPos.class);
        load.setAccessible(true);
        load.invoke(ritual, helper.getLevel(), pos);
    }

    private static ForcedChunksSavedData chunkData(GameTestHelper helper) {
        return helper.getLevel().getDataStorage().get(ForcedChunksSavedData::load, "chunks");
    }

    private static int ticketOwners(GameTestHelper helper) {
        ForcedChunksSavedData data = chunkData(helper);
        return data == null ? 0 : data.getBlockForcedChunks().getChunks().size();
    }

    @GameTest(template = "empty")
    public static void persistenceReleasesTicketsOnStopAndRemoval(GameTestHelper helper) throws Exception {
        int before = ticketOwners(helper);
        BlockPos pos = pos(helper);
        RitualPersistence ritual = new RitualPersistence();
        loadChunks(helper, ritual, pos);
        helper.assertTrue(ticketOwners(helper) == before + 1, "Ritual did not acquire tickets");
        ritual.stopRitual(stone(helper, ritual, pos), Ritual.BreakType.DEACTIVATE);
        helper.assertTrue(ticketOwners(helper) == before, "Stopped ritual leaked tickets");
        loadChunks(helper, ritual, pos);
        RitualPersistence.tickLoadedChunks(helper.getLevel());
        helper.assertTrue(ticketOwners(helper) == before, "Removed ritual leaked tickets");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void persistenceValidatesSavedTicketsAndRestoresCleanup(GameTestHelper helper) throws Exception {
        BlockPos pos = pos(helper);
        helper.getLevel().setBlock(pos, BloodMagicBlocks.MASTER_RITUAL_STONE.get().defaultBlockState(), 3);
        TileMasterRitualStone mrs = (TileMasterRitualStone) helper.getLevel().getBlockEntity(pos);
        RitualPersistence ritual = new RitualPersistence();
        mrs.setCurrentRitual(ritual);
        mrs.setActive(true);
        loadChunks(helper, ritual, pos);
        ForcedChunksSavedData data = chunkData(helper);
        var tickets = data.getBlockForcedChunks().getChunks().values().iterator().next();
        var ctor = ForgeChunkManager.TicketHelper.class.getDeclaredConstructor(ForcedChunksSavedData.class, String.class, Map.class, Map.class);
        ctor.setAccessible(true);
        ForgeChunkManager.TicketHelper saved = ctor.newInstance(data, "animus",
            Map.of(pos, Pair.of(new LongOpenHashSet(tickets), new LongOpenHashSet())), Map.of());
        RitualPersistence.forgetLevel(helper.getLevel());
        RitualPersistence.validateTickets(helper.getLevel(), saved);
        helper.assertTrue(ticketOwners(helper) == 1, "Valid saved tickets were discarded");
        ritual.stopRitual(mrs, Ritual.BreakType.DEACTIVATE);
        helper.assertTrue(ticketOwners(helper) == 0, "Restored tickets were not tracked for cleanup");
        loadChunks(helper, ritual, pos);
        mrs.setActive(false);
        RitualPersistence.validateTickets(helper.getLevel(), saved);
        helper.assertTrue(ticketOwners(helper) == 0, "Inactive saved ritual retained tickets");
        RitualPersistence.forgetLevel(helper.getLevel());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void temporalDominanceSeparatesDimensions(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerLevel other = level.getServer().getLevel(Level.NETHER);
        BlockPos pos = new BlockPos(pos(helper).getX(), 80, pos(helper).getZ());
        BlockState oldLevel = level.getBlockState(pos);
        BlockState oldOther = other.getBlockState(pos);
        try {
            for (ServerLevel target : List.of(level, other)) {
                target.setBlock(pos, Blocks.FURNACE.defaultBlockState(), 3);
                ServerPlayer player = player(target);
                player.setShiftKeyDown(true);
                NetworkHelper.getSoulNetwork(player).setCurrentEssence(100000);
                ItemStack stack = new ItemStack(AnimusItems.SIGIL_TEMPORAL_DOMINANCE.get());
                stack.getOrCreateTag().put("binding", new Binding(player.getUUID(), "AnimusTest").serializeNBT());
                player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                helper.assertTrue(stack.getItem().useOn(context(player, pos)).consumesAction(),
                    "Acceleration failed to activate in " + target.dimension().location());
            }
            var states = ItemSigilTemporalDominance.getAcceleratedBlocks();
            helper.assertTrue(states.get(GlobalPos.of(level.dimension(), pos)).level == 1, "First dimension lost acceleration");
            helper.assertTrue(states.get(GlobalPos.of(other.dimension(), pos)).level == 1, "Second dimension inherited the first upgrade");
            ItemSigilTemporalDominance.cleanupLevel(level);
            helper.assertTrue(states.containsKey(GlobalPos.of(other.dimension(), pos)), "Unloading one dimension cleared another");
        } finally {
            ItemSigilTemporalDominance.cleanupLevel(level);
            ItemSigilTemporalDominance.cleanupLevel(other);
            level.setBlock(pos, oldLevel, 3);
            other.setBlock(pos, oldOther, 3);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void bloodWoodStrippingPreservesAxis(GameTestHelper helper) {
        ServerPlayer player = player(helper.getLevel());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
        for (Direction.Axis axis : Direction.Axis.values()) {
            BlockState log = AnimusBlocks.BLOCK_BLOOD_WOOD.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
            BlockState stripped = log.getToolModifiedState(context(player, pos(helper)), ToolActions.AXE_STRIP, false);
            helper.assertTrue(stripped != null && stripped.is(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get())
                && stripped.getValue(RotatedPillarBlock.AXIS) == axis, "Stripping failed or changed log axis");
        }
        helper.succeed();
    }
}
