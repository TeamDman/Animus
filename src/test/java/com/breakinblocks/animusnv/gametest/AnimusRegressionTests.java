package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.blockentities.BlockEntityAntiLife;
import com.breakinblocks.animusnv.compat.EvilCraftCompat;
import com.breakinblocks.animusnv.compat.evilcraft.BlockEntitySanguineRectifier;
import com.breakinblocks.animusnv.items.sigils.effects.FreeSoulSigilEffect;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.breakinblocks.animusnv.rituals.RitualSerenity;
import com.breakinblocks.animusnv.rituals.RitualUnmaking;
import com.breakinblocks.animusnv.rituals.RitualPersistence;
import com.breakinblocks.animusnv.compat.ironsspells.SpellCastingHandler;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.datacomponent.Binding;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import com.breakinblocks.neovitae.ritual.IMasterRitualStone;
import com.breakinblocks.neovitae.ritual.Ritual;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import com.mojang.authlib.GameProfile;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import net.minecraft.core.GlobalPos;
import com.breakinblocks.animusnv.util.RitualZoneTracker;
import java.util.UUID;

// Regression coverage for resource accounting, persistence, and lifecycle boundaries.
@GameTestHolder(Constants.Mod.MODID)
@PrefixGameTestTemplate(false)
public class AnimusRegressionTests {
    private static final String TEMPLATE = "empty_5x5x7";

    @GameTest(template = TEMPLATE)
    public void ev_casting_checks_are_read_only_and_payment_is_exact(GameTestHelper h) {
        ServerPlayer player = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "ReviewMana"));
        ItemStack orb = new ItemStack(AnimusItems.BLOOD_ORB_TRANSCENDENT.get());
        orb.set(NVDataComponents.BINDING.get(), new Binding(player.getUUID(), "ReviewMana"));
        player.getInventory().add(orb);
        var network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(100000), 100000);
        var spell = SpellRegistry.getSpell("irons_spellbooks:firebolt");
        var magic = MagicData.getPlayerMagicData(player);
        magic.setMana(0);
        spell.canBeCastedBy(1, CastSource.SPELLBOOK, magic, player);
        h.assertTrue(magic.getMana() == 0, "eligibility must not grant mana");
        h.assertTrue(SpellCastingHandler.payAndSupplyMana(player, spell.getManaCost(1)), "funded payment succeeds");
        h.assertTrue(network.getCurrentEV() == 100000 - spell.getManaCost(1) * 100, "actual mana deficit is paid");
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void persistence_releases_chunks_on_stop(GameTestHelper h) throws Exception {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        var ritual = new RitualPersistence();
        var load = RitualPersistence.class.getDeclaredMethod("loadChunks", ServerLevel.class, BlockPos.class);
        load.setAccessible(true);
        load.invoke(ritual, h.getLevel(), pos);
        ritual.stopRitual(stone(h, pos), Ritual.BreakType.DEACTIVATE);
        var field = RitualPersistence.class.getDeclaredField("loadedChunks");
        field.setAccessible(true);
        h.assertTrue(!((Map<?, ?>) field.get(null)).containsKey(GlobalPos.of(h.getLevel().dimension(), pos)), "stop releases tracking");
        RitualPersistence.cleanupAllChunks(h.getLevel());
        h.succeed();
    }

    private void replace(GameTestHelper h, ServerPlayer player, BlockPos pos, Block original) throws Exception {
        Class<?> type = Class.forName("com.breakinblocks.animusnv.items.sigils.effects.EquivalencySigilEffect$ReplacementOperation");
        var constructor = type.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object operation = constructor.newInstance(h.getLevel(), player.getUUID(), List.of(pos), List.of(Blocks.DIRT), original, pos, NeoVitaeAPI.getInstance().getAnima(player.getUUID()), 0);
        var method = type.getDeclaredMethod("replaceBlock", ServerPlayer.class, BlockPos.class);
        method.setAccessible(true);
        method.invoke(operation, player, pos);
    }

    private IMasterRitualStone stone(GameTestHelper h, BlockPos pos) {
        UUID owner = UUID.randomUUID();
        return (IMasterRitualStone) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{IMasterRitualStone.class}, (proxy, method, args) -> switch (method.getName()) {
            case "getWorldObj", "getLevel" -> h.getLevel();
            case "getMasterBlockPos", "getBlockPos" -> pos;
            case "getOwner" -> owner;
            case "stopRitual" -> null;
            default -> throw new UnsupportedOperationException(method.getName());
        });
    }

    @GameTest(template = TEMPLATE)
    public void equivalency_requires_supplies_before_drops(GameTestHelper h) throws Exception {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        h.getLevel().setBlockAndUpdate(pos, Blocks.DIAMOND_BLOCK.defaultBlockState());
        replace(h, player, pos, Blocks.DIAMOND_BLOCK);
        h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.DIAMOND_BLOCK), "source remains");
        h.assertTrue(player.getInventory().countItem(Items.DIAMOND_BLOCK) == 0, "no drops without supplies");
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void equivalency_preserves_bedrock(GameTestHelper h) throws Exception {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.getInventory().add(new ItemStack(Items.DIRT));
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        h.getLevel().setBlockAndUpdate(pos, Blocks.BEDROCK.defaultBlockState());
        replace(h, player, pos, Blocks.BEDROCK);
        h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.BEDROCK), "bedrock is protected");
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void unmaking_preserves_enchantments_with_insufficient_books(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        var registry = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        tool.enchant(registry.getOrThrow(Enchantments.EFFICIENCY), 5);
        tool.enchant(registry.getOrThrow(Enchantments.UNBREAKING), 3);
        h.getLevel().addFreshEntity(new ItemEntity(h.getLevel(), pos.getX()+0.5, pos.getY(), pos.getZ()+0.5, tool));
        h.getLevel().addFreshEntity(new ItemEntity(h.getLevel(), pos.getX()+0.5, pos.getY(), pos.getZ()+0.5, new ItemStack(Items.BOOK)));
        new RitualUnmaking().performRitual(stone(h, pos));
        h.assertTrue(tool.get(DataComponents.ENCHANTMENTS).size() == 2, "insufficient books preserve both enchantments");
        long books = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2)).stream().filter(e -> e.getItem().is(Items.ENCHANTED_BOOK)).count();
        h.assertTrue(books == 0, "no partial extraction");
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void serenity_removes_zone_on_stop(GameTestHelper h) throws Exception {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        var add = RitualSerenity.class.getDeclaredMethod("addActiveRitual", Level.class, BlockPos.class, AABB.class);
        add.setAccessible(true);
        add.invoke(null, h.getLevel(), pos, new AABB(pos));
        new RitualSerenity().stopRitual(stone(h, pos), Ritual.BreakType.DEACTIVATE);
        var field = RitualSerenity.class.getDeclaredField("ZONE_TRACKER");
        field.setAccessible(true);
        h.assertTrue(!((RitualZoneTracker) field.get(null)).isInZone(h.getLevel(), pos), "stop removes zone");
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void antilife_target_round_trips(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        var state = AnimusBlocks.BLOCK_ANTILIFE.get().defaultBlockState();
        var before = new BlockEntityAntiLife(pos, state).setSeeking(Blocks.STONE);
        var saved = before.saveWithoutMetadata(h.getLevel().registryAccess());
        var after = new BlockEntityAntiLife(pos, state);
        after.loadWithComponents(saved, h.getLevel().registryAccess());
        h.assertTrue(after.getSeeking() == Blocks.STONE, "stone survives reload");
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void rectifier_marks_fluid_changes_unsaved(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        h.getLevel().setBlockAndUpdate(pos, EvilCraftCompat.SANGUINE_RECTIFIER.get().defaultBlockState());
        var be = (BlockEntitySanguineRectifier) h.getLevel().getBlockEntity(pos);
        var chunk = h.getLevel().getChunkAt(pos);
        chunk.setUnsaved(false);
        int filled = be.getBloodTank().fill(new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("evilcraft:blood")), 1000), IFluidHandler.FluidAction.EXECUTE);
        h.assertTrue(filled == 1000, "blood accepted");
        h.assertTrue(chunk.isUnsaved(), "fluid fill marks chunk unsaved");
        chunk.setUnsaved(true);
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void free_soul_logout_returns_to_origin(GameTestHelper h) throws Exception {
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(pos.getX(), pos.getY(), pos.getZ());
        var activate = FreeSoulSigilEffect.class.getDeclaredMethod("activateSpectatorMode", ServerPlayer.class, ServerLevel.class, boolean.class);
        activate.setAccessible(true);
        activate.invoke(null, player, h.getLevel(), false);
        player.setPos(pos.getX()+100, pos.getY(), pos.getZ());
        FreeSoulSigilEffect.onPlayerLogout(player);
        h.assertTrue(player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL, "survival restored");
        h.assertTrue(player.getX() == pos.getX(), "logout restores original position");
        h.succeed();
    }
}


