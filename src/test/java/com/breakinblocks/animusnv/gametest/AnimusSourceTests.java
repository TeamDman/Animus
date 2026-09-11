package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.gametest.base.AnimusTestRegistrar;
import com.breakinblocks.animusnv.registry.AnimusRituals;
import com.breakinblocks.animusnv.rituals.RitualArsVitae;
import com.breakinblocks.animusnv.rituals.RitualSourceVitaeum;
import com.breakinblocks.animusnv.util.SourceJarHelper;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.common.block.BlockRitualStone;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;
import com.breakinblocks.neovitae.common.blockentity.MasterRitualStoneBlockEntity;
import com.breakinblocks.neovitae.common.datamap.NVDataMaps;
import com.breakinblocks.neovitae.ritual.Ritual;
import com.hollingsworth.arsnouveau.common.block.tile.SourceJarTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.UUID;

public final class AnimusSourceTests {

    private AnimusSourceTests() {
    }

    public static void register(AnimusTestRegistrar r) {
        r.add("ars_vitae_charges_only_accepted_source", AnimusSourceTests::arsVitaeFillsJarAndChargesOnlyAcceptedSource);
        r.add("source_vitaeum_preserves_source_when_altar_full", AnimusSourceTests::sourceVitaeumPreservesSourceWhenAltarFull);
        r.add("ars_vitae_runs_through_master_stone", AnimusSourceTests::arsVitaeActivatesAndRunsThroughMasterStone);
        r.add("conversion_penalty_never_overflows", AnimusSourceTests::conversionPenaltyNeverOverflows);
    }

    private static SourceJarTile jar(GameTestHelper h, BlockPos pos) {
        h.getLevel().setBlockAndUpdate(pos, block("ars_nouveau:source_jar").defaultBlockState());
        return (SourceJarTile) h.getLevel().getBlockEntity(pos);
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
    }

    private static void arsVitaeFillsJarAndChargesOnlyAcceptedSource(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        SourceJarTile jar = jar(h, pos.above());
        UUID owner = UUID.randomUUID();
        IAnima network = NeoVitaeAPI.getInstance().getAnima(owner);
        network.set(AnimaTicket.create(50000), 50000);
        int rate = AnimusConfig.rituals.sourceVitaeumBaseConversion.get();
        RitualArsVitae ritual = new RitualArsVitae();
        ritual.performRitual(RegressionTestSupport.stone(h, pos, owner));
        h.assertTrue(jar.getSource() > 0, "running ritual fills empty jar");
        h.assertTrue(network.getCurrentEV() == 50000 - jar.getSource() * rate, "EV cost matches accepted Source");
        jar.setSource(jar.getMaxSource() - 1);
        int before = network.getCurrentEV();
        ritual.performRitual(RegressionTestSupport.stone(h, pos, owner));
        h.assertTrue(jar.getSource() == jar.getMaxSource(), "nearly full jar accepts last Source");
        h.assertTrue(network.getCurrentEV() == before - rate, "one Source costs one conversion unit");
        before = network.getCurrentEV();
        ritual.performRitual(RegressionTestSupport.stone(h, pos, owner));
        h.assertTrue(network.getCurrentEV() == before, "full jar costs nothing");
        h.getLevel().setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        h.succeed();
    }

    private static void sourceVitaeumPreservesSourceWhenAltarFull(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        SourceJarTile jar = jar(h, pos.above());
        jar.setSource(1000);
        BlockPos altarPos = pos.offset(2, 0, 0);
        h.getLevel().setBlockAndUpdate(altarPos, NVBlocks.ARA_VITAE.get().defaultBlockState());
        if (!(h.getLevel().getBlockEntity(altarPos) instanceof AraVitaeTile altar)) {
            h.fail("altar block entity missing");
            return;
        }
        altar.addSacrificeEV(altar.getMainCapacity(), false);
        new RitualSourceVitaeum().performRitual(RegressionTestSupport.stone(h, pos, UUID.randomUUID()));
        h.assertTrue(jar.getSource() == 1000, "full altar must not consume Source");
        h.getLevel().setBlockAndUpdate(altarPos, Blocks.AIR.defaultBlockState());
        h.getLevel().setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        h.succeed();
    }

    private static void arsVitaeActivatesAndRunsThroughMasterStone(GameTestHelper h) {
        BlockPos base = h.absolutePos(new BlockPos(2, 2, 2));
        BlockPos pos = base.above(30);
        ServerPlayer player = RegressionTestSupport.player(h.getLevel());
        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(100000), 100000);
        h.getLevel().setBlockAndUpdate(pos, NVBlocks.MASTER_RITUAL_STONE.block().defaultBlockState());
        if (!(h.getLevel().getBlockEntity(pos) instanceof MasterRitualStoneBlockEntity master)) {
            h.fail("master ritual stone block entity missing");
            return;
        }
        Ritual ritual = AnimusRituals.ARS_VITAE.get();
        ritual.gatherComponents(component -> {
            Block rune = BuiltInRegistries.BLOCK.stream()
                .filter(b -> b instanceof BlockRitualStone stone && stone.getRuneType() == component.runeType())
                .findFirst().orElseThrow();
            h.getLevel().setBlockAndUpdate(pos.offset(component.offset()), rune.defaultBlockState());
        });
        SourceJarTile jar = jar(h, pos.above());
        h.assertTrue(master.activateRitual(ritual, player, 4), "ritual activates");
        master.performRitual();
        h.assertTrue(jar.getSource() > 0, "active master stone fills jar");
        h.assertTrue(AnimusRituals.ARS_VITAE.getData(NVDataMaps.RITUAL_STATS) != null, "ritual stats loaded");
        master.stopRitual(Ritual.BreakType.DEACTIVATE);
        h.succeed();
    }

    private static void conversionPenaltyNeverOverflows(GameTestHelper h) {
        for (int count = 0; count < 100; count++) {
            h.assertTrue(SourceJarHelper.conversionRate(10, count) > 0, "positive rate with " + count + " neighbors");
        }
        h.succeed();
    }
}
