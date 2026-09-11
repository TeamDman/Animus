package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.blockentities.BlockEntityAntiLife;
import com.breakinblocks.animusnv.compat.EvilCraftCompat;
import com.breakinblocks.animusnv.compat.evilcraft.BlockEntitySanguineRectifier;
import com.breakinblocks.animusnv.gametest.base.AnimusTestRegistrar;
import com.breakinblocks.animusnv.items.sigils.effects.FreeSoulSigilEffect;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.breakinblocks.animusnv.rituals.RitualPersistence;
import com.breakinblocks.animusnv.rituals.RitualSerenity;
import com.breakinblocks.animusnv.rituals.RitualUnmaking;
import com.breakinblocks.animusnv.util.RitualZoneTracker;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.ritual.Ritual;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AnimusRegressionTests {

    private AnimusRegressionTests() {
    }

    public static void register(AnimusTestRegistrar r) {
        r.add("persistence_releases_chunks_on_stop", AnimusRegressionTests::persistenceReleasesChunksOnStop);
        r.add("equivalency_requires_supplies_before_drops", AnimusRegressionTests::equivalencyRequiresSuppliesBeforeDrops);
        r.add("equivalency_preserves_bedrock", AnimusRegressionTests::equivalencyPreservesBedrock);
        r.add("unmaking_preserves_enchantments_with_insufficient_books", AnimusRegressionTests::unmakingPreservesEnchantmentsWithInsufficientBooks);
        r.add("serenity_removes_zone_on_stop", AnimusRegressionTests::serenityRemovesZoneOnStop);
        r.add("antilife_target_round_trips", AnimusRegressionTests::antilifeTargetRoundTrips);
        r.add("rectifier_marks_committed_fluid_changes", AnimusRegressionTests::rectifierMarksCommittedFluidChanges);
        r.add("free_soul_logout_returns_to_origin", AnimusRegressionTests::freeSoulLogoutReturnsToOrigin);
    }

    static void replace(GameTestHelper h, ServerPlayer player, BlockPos pos, Block original, int evPerBlock) throws Exception {
        Class<?> type = Class.forName("com.breakinblocks.animusnv.items.sigils.effects.EquivalencySigilEffect$ReplacementOperation");
        Constructor<?> constructor = type.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object operation = constructor.newInstance(h.getLevel(), player.getUUID(), List.of(pos), List.of(Blocks.DIRT),
            original, pos, NeoVitaeAPI.getInstance().getAnima(player.getUUID()), evPerBlock);
        Method method = type.getDeclaredMethod("replaceBlock", ServerPlayer.class, BlockPos.class);
        method.setAccessible(true);
        method.invoke(operation, player, pos);
    }

    private static void persistenceReleasesChunksOnStop(GameTestHelper h) {
        try {
            BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
            RitualPersistence ritual = new RitualPersistence();
            Method load = RitualPersistence.class.getDeclaredMethod("loadChunks", ServerLevel.class, BlockPos.class);
            load.setAccessible(true);
            load.invoke(ritual, h.getLevel(), pos);
            Field field = RitualPersistence.class.getDeclaredField("loadedChunks");
            field.setAccessible(true);
            Map<?, ?> tracked = (Map<?, ?>) field.get(null);
            GlobalPos key = GlobalPos.of(h.getLevel().dimension(), pos);
            if (!tracked.containsKey(key)) {
                h.fail("loading did not track the stone");
                return;
            }
            ritual.stopRitual(RegressionTestSupport.stone(h, pos, UUID.randomUUID()), Ritual.BreakType.DEACTIVATE);
            if (tracked.containsKey(key)) {
                h.fail("stop did not release tracking");
                return;
            }
        } catch (ReflectiveOperationException e) {
            h.fail("reflection failed: " + e);
            return;
        } finally {
            RitualPersistence.cleanupAllChunks(h.getLevel());
        }
        h.succeed();
    }

    private static void equivalencyRequiresSuppliesBeforeDrops(GameTestHelper h) {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        h.getLevel().setBlockAndUpdate(pos, Blocks.DIAMOND_BLOCK.defaultBlockState());
        try {
            replace(h, player, pos, Blocks.DIAMOND_BLOCK, 0);
        } catch (Exception e) {
            h.fail("replacement threw: " + e);
            return;
        }
        h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.DIAMOND_BLOCK), "source remains without supplies");
        h.assertTrue(player.getInventory().countItem(Items.DIAMOND_BLOCK) == 0, "no drops without supplies");
        h.succeed();
    }

    private static void equivalencyPreservesBedrock(GameTestHelper h) {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.getInventory().add(new ItemStack(Items.DIRT));
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        h.getLevel().setBlockAndUpdate(pos, Blocks.BEDROCK.defaultBlockState());
        try {
            replace(h, player, pos, Blocks.BEDROCK, 0);
        } catch (Exception e) {
            h.fail("replacement threw: " + e);
            return;
        }
        h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.BEDROCK), "bedrock is protected");
        h.assertTrue(player.getInventory().countItem(Items.DIRT) == 1, "material retained");
        h.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        h.succeed();
    }

    private static void unmakingPreservesEnchantmentsWithInsufficientBooks(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        HolderLookup.RegistryLookup<Enchantment> registry = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        tool.enchant(registry.getOrThrow(Enchantments.EFFICIENCY), 5);
        tool.enchant(registry.getOrThrow(Enchantments.UNBREAKING), 3);
        h.getLevel().addFreshEntity(new ItemEntity(h.getLevel(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, tool));
        h.getLevel().addFreshEntity(new ItemEntity(h.getLevel(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, new ItemStack(Items.BOOK)));
        new RitualUnmaking().performRitual(RegressionTestSupport.stone(h, pos, UUID.randomUUID()));
        h.assertTrue(tool.get(DataComponents.ENCHANTMENTS).size() == 2, "insufficient books preserve both enchantments");
        long books = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2)).stream()
            .filter(e -> e.getItem().is(Items.ENCHANTED_BOOK)).count();
        h.assertTrue(books == 0, "no partial extraction");
        h.succeed();
    }

    private static void serenityRemovesZoneOnStop(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        try {
            Method add = RitualSerenity.class.getDeclaredMethod("addActiveRitual", Level.class, BlockPos.class, AABB.class);
            add.setAccessible(true);
            add.invoke(null, h.getLevel(), pos, new AABB(pos));
            Field field = RitualSerenity.class.getDeclaredField("ZONE_TRACKER");
            field.setAccessible(true);
            RitualZoneTracker tracker = (RitualZoneTracker) field.get(null);
            h.assertTrue(tracker.isInZone(h.getLevel(), pos), "zone registered");
            new RitualSerenity().stopRitual(RegressionTestSupport.stone(h, pos, UUID.randomUUID()), Ritual.BreakType.DEACTIVATE);
            h.assertTrue(!tracker.isInZone(h.getLevel(), pos), "stop removes zone");
        } catch (ReflectiveOperationException e) {
            h.fail("reflection failed: " + e);
            return;
        }
        h.succeed();
    }

    private static void antilifeTargetRoundTrips(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        HolderLookup.Provider registries = h.getLevel().registryAccess();
        BlockState state = AnimusBlocks.BLOCK_ANTILIFE.get().defaultBlockState();
        BlockEntityAntiLife before = new BlockEntityAntiLife(pos, state).setSeeking(Blocks.STONE);
        CompoundTag saved = before.saveWithoutMetadata(registries);
        h.assertTrue("minecraft:stone".equals(saved.getStringOr("seeking", "")), "registry id is saved");

        BlockEntityAntiLife after = new BlockEntityAntiLife(pos, state);
        after.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, saved));
        h.assertTrue(after.getSeeking() == Blocks.STONE, "stone survives reload");

        CompoundTag legacy = new CompoundTag();
        legacy.putString("seeking", "block.minecraft.diamond_block");
        legacy.putInt("range", 3);
        BlockEntityAntiLife migrated = new BlockEntityAntiLife(pos, state);
        migrated.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, legacy));
        h.assertTrue(migrated.getSeeking() == Blocks.DIAMOND_BLOCK, "legacy translation key resolves to the block");

        CompoundTag broken = new CompoundTag();
        broken.putString("seeking", "not a valid id!");
        BlockEntityAntiLife invalid = new BlockEntityAntiLife(pos, state);
        invalid.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, broken));
        h.assertTrue(invalid.getSeeking() == Blocks.AIR, "invalid id falls back to air");
        h.succeed();
    }

    private static void rectifierMarksCommittedFluidChanges(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        BlockState state = EvilCraftCompat.SANGUINE_RECTIFIER.get().defaultBlockState();
        h.getLevel().setBlockAndUpdate(pos, state);
        if (!(h.getLevel().getBlockEntity(pos) instanceof BlockEntitySanguineRectifier be)) {
            h.fail("rectifier block entity missing");
            return;
        }
        Fluid blood = BuiltInRegistries.FLUID.getValue(Identifier.parse("evilcraft:blood"));
        FluidResource resource = FluidResource.of(blood);
        ResourceHandler<FluidResource> tank = be.getBloodTank();
        LevelChunk chunk = h.getLevel().getChunkAt(pos);

        chunk.tryMarkSaved();
        try (Transaction tx = Transaction.openRoot()) {
            h.assertTrue(tank.insert(resource, 1000, tx) == 1000, "aborted insertion is simulated");
        }
        h.assertTrue(tank.getAmountAsInt(0) == 0, "aborted insertion rolls back");
        h.assertTrue(!chunk.isUnsaved(), "aborted insertion does not dirty the chunk");

        try (Transaction tx = Transaction.openRoot()) {
            h.assertTrue(tank.insert(resource, 1000, tx) == 1000, "blood accepted");
            tx.commit();
        }
        h.assertTrue(tank.getAmountAsInt(0) == 1000, "committed insertion stored");
        h.assertTrue(chunk.isUnsaved(), "committed insertion dirties the chunk");

        HolderLookup.Provider registries = h.getLevel().registryAccess();
        CompoundTag saved = be.saveWithoutMetadata(registries);
        BlockEntitySanguineRectifier reloaded = new BlockEntitySanguineRectifier(pos, state);
        reloaded.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, saved));
        h.assertTrue(reloaded.getBloodTank().getAmountAsInt(0) == 1000, "saved contents survive reload");

        chunk.tryMarkSaved();
        try (Transaction tx = Transaction.openRoot()) {
            h.assertTrue(tank.extract(resource, 400, tx) == 400, "blood extracted");
            tx.commit();
        }
        h.assertTrue(tank.getAmountAsInt(0) == 600, "committed extraction stored");
        h.assertTrue(chunk.isUnsaved(), "committed extraction dirties the chunk");
        h.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        h.succeed();
    }

    private static void freeSoulLogoutReturnsToOrigin(GameTestHelper h) {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(pos.getX(), pos.getY(), pos.getZ());
        try {
            Method activate = FreeSoulSigilEffect.class.getDeclaredMethod("activateSpectatorMode", ServerPlayer.class, ServerLevel.class, boolean.class);
            activate.setAccessible(true);
            activate.invoke(null, player, h.getLevel(), false);
        } catch (ReflectiveOperationException e) {
            h.fail("reflection failed: " + e);
            return;
        }
        h.assertTrue(player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR, "spectator mode entered");
        player.setPos(pos.getX() + 100, pos.getY(), pos.getZ());
        FreeSoulSigilEffect.onPlayerLogout(player);
        h.assertTrue(player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL, "survival restored");
        h.assertTrue(player.getX() == pos.getX(), "logout restores original position");
        h.assertTrue(!FreeSoulSigilEffect.isInSpectatorMode(player.getUUID()), "state cleared on logout");
        h.succeed();
    }
}
