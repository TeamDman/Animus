package com.breakinblocks.animusnv.rituals;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.registries.BuiltInRegistries;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.IMasterRitualStone;
import com.breakinblocks.neovitae.ritual.Ritual;
import com.breakinblocks.neovitae.ritual.RitualComponent;
import com.breakinblocks.neovitae.ritual.EnumRuneType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class RitualAnimalLuring extends Ritual {
    public static final String SPAWN_RANGE = "spawn";

    private List<EntityType<?>> targets;

    public RitualAnimalLuring() {
        super(Constants.Rituals.ANIMAL_LURING, 0, 5000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.ANIMAL_LURING);

        addBlockRange(SPAWN_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-4, 1, -4), 9, 3, 9));
        setMaximumVolumeAndDistanceOfRange(SPAWN_RANGE, 0, 15, 10);
    }

    @Override
    public boolean activateRitual(IMasterRitualStone mrs, Player player, UUID owner) {
        return rebuildList(mrs);
    }

    private boolean rebuildList(IMasterRitualStone mrs) {
        try {
            Animus.LOGGER.debug("Rebuilding Ritual of Animal Luring entity list. [{}]", mrs.getMasterBlockPos());

            targets = new ArrayList<>();

            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                if (entityType == null) {
                    continue;
                }

                MobCategory category = entityType.getCategory();
                if (category == MobCategory.CREATURE || category == MobCategory.AMBIENT || category == MobCategory.WATER_CREATURE || category == MobCategory.WATER_AMBIENT) {
                    targets.add(entityType);
                }
            }

            return !targets.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            Animus.LOGGER.debug("Animal Luring ritual creation failed.");
            return false;
        }
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        IAnima network = AnimusRitualHelper.getOwnerNetwork(mrs);
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide) {
            return;
        }

        if (targets == null || targets.isEmpty()) {
            if (!rebuildList(mrs)) {
                mrs.stopRitual(Ritual.BreakType.DEACTIVATE);
                return;
            }
        }

        EntityType<?> entityType = targets.get(level.random.nextInt(targets.size()));

        Entity mob = entityType.create(level);
        if (mob == null) {
            return;
        }

        AreaDescriptor spawnRange = getBlockRange(SPAWN_RANGE);
        AABB spawnAABB = spawnRange.getAABB(masterPos);

        double x = spawnAABB.minX + level.random.nextDouble() * (spawnAABB.maxX - spawnAABB.minX);
        double y = spawnAABB.minY;
        double z = spawnAABB.minZ + level.random.nextDouble() * (spawnAABB.maxZ - spawnAABB.minZ);

        for (int i = 0; i < 16; i++) {
            mob.setPos(x, y, z);
            BlockPos mobPos = mob.blockPosition();

            if (!level.isEmptyBlock(mobPos)) {
                x = spawnAABB.minX + level.random.nextDouble() * (spawnAABB.maxX - spawnAABB.minX);
                z = spawnAABB.minZ + level.random.nextDouble() * (spawnAABB.maxZ - spawnAABB.minZ);
            } else {
                break;
            }
        }

        level.addFreshEntity(mob);
        level.playSound(
            null,
            mob.blockPosition(),
            SoundEvents.SNOW_STEP,
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );

        AnimaTicket ticket = AnimaTicket.create(getRefreshCost());
        network.syphon(ticket);
    }

    @Override
    public int getRefreshCost() {
        return AnimusConfig.rituals.animalLuringCost.get();
    }

    @Override
    public int getRefreshTime() {
        return 400;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 4, 0, 0, EnumRuneType.EARTH);
        addRune(components, 4, 1, 0, EnumRuneType.EARTH);
        addRune(components, 4, 2, 0, EnumRuneType.WATER);
        addRune(components, 0, 1, 4, EnumRuneType.EARTH);
        addRune(components, 0, 2, 4, EnumRuneType.EARTH);
        addRune(components, 0, 3, 4, EnumRuneType.WATER);
        addRune(components, -4, 2, 0, EnumRuneType.EARTH);
        addRune(components, -4, 3, 0, EnumRuneType.EARTH);
        addRune(components, -4, 4, 0, EnumRuneType.WATER);
        addRune(components, 0, 3, -4, EnumRuneType.EARTH);
        addRune(components, 0, 4, -4, EnumRuneType.EARTH);
        addRune(components, 0, 5, -4, EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualAnimalLuring();
    }
}
