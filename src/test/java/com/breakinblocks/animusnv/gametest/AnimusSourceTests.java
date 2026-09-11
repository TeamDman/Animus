package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.arsnouveau.SourceJarHelper;
import com.breakinblocks.animusnv.registry.AnimusRituals;
import com.breakinblocks.animusnv.rituals.RitualArsVitae;
import com.breakinblocks.animusnv.rituals.RitualSourceVitaeum;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.common.block.BlockRitualStone;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;
import com.breakinblocks.neovitae.common.blockentity.MasterRitualStoneBlockEntity;
import com.breakinblocks.neovitae.common.datamap.NVDataMaps;
import com.breakinblocks.neovitae.ritual.IMasterRitualStone;
import com.breakinblocks.neovitae.ritual.Ritual;
import com.hollingsworth.arsnouveau.common.block.tile.SourceJarTile;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Proxy;
import java.util.UUID;

@GameTestHolder(Constants.Mod.MODID)
@PrefixGameTestTemplate(false)
public class AnimusSourceTests {
    private static final String TEMPLATE = "empty_5x5x7";

    private IMasterRitualStone stone(GameTestHelper h, BlockPos pos, UUID owner) {
        return (IMasterRitualStone) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{IMasterRitualStone.class}, (proxy, method, args) -> switch (method.getName()) {
            case "getWorldObj", "getLevel" -> h.getLevel();
            case "getMasterBlockPos", "getBlockPos" -> pos;
            case "getOwner" -> owner;
            default -> throw new UnsupportedOperationException(method.getName());
        });
    }

    private SourceJarTile jar(GameTestHelper h, BlockPos pos) {
        h.getLevel().setBlockAndUpdate(pos, block("ars_nouveau:source_jar").defaultBlockState());
        return (SourceJarTile) h.getLevel().getBlockEntity(pos);
    }

    private Block block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
    }

    @GameTest(template = TEMPLATE)
    public void ars_vitae_fills_jar_and_charges_only_accepted_source(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        SourceJarTile jar = jar(h, pos.above());
        UUID owner = UUID.randomUUID();
        var network = NeoVitaeAPI.getInstance().getAnima(owner);
        network.set(AnimaTicket.create(50000), 50000);
        var ritual = new RitualArsVitae();
        ritual.performRitual(stone(h, pos, owner));
        h.assertTrue(jar.getSource() > 0, "running ritual fills empty jar");
        h.assertTrue(network.getCurrentEV() == 50000 - jar.getSource() * AnimusConfig.rituals.sourceVitaeumBaseConversion.get(), "EV cost matches accepted Source");
        jar.setSource(jar.getMaxSource() - 1);
        int before = network.getCurrentEV();
        ritual.performRitual(stone(h, pos, owner));
        h.assertTrue(jar.getSource() == jar.getMaxSource(), "nearly full jar accepts last Source");
        h.assertTrue(network.getCurrentEV() == before - AnimusConfig.rituals.sourceVitaeumBaseConversion.get(), "one Source costs one conversion unit");
        before = network.getCurrentEV();
        ritual.performRitual(stone(h, pos, owner));
        h.assertTrue(network.getCurrentEV() == before, "full jar costs nothing");
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void source_vitaeum_preserves_source_when_altar_full(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        SourceJarTile jar = jar(h, pos.above());
        jar.setSource(1000);
        BlockPos altarPos = pos.offset(2, 0, 0);
        h.getLevel().setBlockAndUpdate(altarPos, block("neovitae:ara_vitae").defaultBlockState());
        var altar = (AraVitaeTile) h.getLevel().getBlockEntity(altarPos);
        altar.addSacrificeEV(altar.getMainCapacity(), false);
        new RitualSourceVitaeum().performRitual(stone(h, pos, UUID.randomUUID()));
        h.assertTrue(jar.getSource() == 1000, "full altar must not consume Source");
        h.succeed();
    }

    @GameTest(template = TEMPLATE)
    public void ars_vitae_activates_and_runs_through_master_stone(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 2, 2));
        var player = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "ArsReview"));
        var network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        network.set(AnimaTicket.create(100000), 100000);
        h.getLevel().setBlockAndUpdate(pos, block("neovitae:master_ritual_stone").defaultBlockState());
        var master = (MasterRitualStoneBlockEntity) h.getLevel().getBlockEntity(pos);
        Ritual ritual = AnimusRituals.ARS_VITAE.get();
        ritual.gatherComponents(component -> {
            Block rune = BuiltInRegistries.BLOCK.stream().filter(b -> b instanceof BlockRitualStone stone && stone.getRuneType() == component.runeType()).findFirst().orElseThrow();
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

    @GameTest(template = TEMPLATE)
    public void conversion_penalty_never_overflows(GameTestHelper h) {
        for (int count = 0; count < 100; count++) {
            h.assertTrue(SourceJarHelper.conversionRate(10, count) > 0, "positive rate with " + count + " neighbors");
        }
        h.succeed();
    }
}
