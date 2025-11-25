package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.ritual.Ritual;
import wayoftime.bloodmagic.ritual.RitualComponent;
import wayoftime.bloodmagic.ritual.RitualRegister;
import wayoftime.bloodmagic.ritual.types.RitualType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Ritual of Peace - Spawns peaceful entities
 * Activation Cost: 5000 LP
 * Refresh Cost: Configured (default varies)
 * Refresh Time: 400 ticks
 */
@RitualRegister(Constants.Rituals.PEACE)
public class RitualPeace extends Ritual {
    private List<EntityType<?>> targets;

    public RitualPeace() {
        super(new RitualType(Constants.Rituals.PEACE, 0, 5000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.PEACE));
    }

    @Override
    public boolean activateRitual(IMasterRitualStone mrs, net.minecraft.world.entity.player.Player player, UUID owner) {
        return rebuildList(mrs);
    }

    private boolean rebuildList(IMasterRitualStone mrs) {
        try {
            System.out.println("Rebuilding Ritual of Peace entity list. [" + mrs.getBlockPos().toString() + "]");

            targets = new ArrayList<>();

            // Get all entity types from registry
            for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues()) {
                // Skip null types
                if (entityType == null) {
                    continue;
                }

                // Only include peaceful mobs (not monsters, not misc)
                MobCategory category = entityType.getCategory();
                if (category == MobCategory.CREATURE || category == MobCategory.AMBIENT || category == MobCategory.WATER_CREATURE || category == MobCategory.WATER_AMBIENT) {
                    targets.add(entityType);
                }
            }

            return !targets.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Peace ritual creation failed.");
            return false;
        }
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        SoulNetwork network = NetworkHelper.getSoulNetwork(mrs.getOwner());
        BlockPos masterPos = mrs.getBlockPos();

        if (level.isClientSide) {
            return;
        }

        // Rebuild list if empty or invalid
        if (targets == null || targets.isEmpty()) {
            if (!rebuildList(mrs)) {
                mrs.stopRitual(Ritual.BreakType.DEACTIVATE);
                return;
            }
        }

        // Pick a random entity type
        EntityType<?> entityType = targets.get(level.random.nextInt(targets.size()));

        // Create entity
        Entity mob = entityType.create(level);
        if (mob == null) {
            return;
        }

        // Find a random position near the ritual
        double x = masterPos.getX() + level.random.nextInt(8) - 4 + 0.5;
        double y = masterPos.getY() + 1;
        double z = masterPos.getZ() + level.random.nextInt(8) - 4 + 0.5;

        // Try to find a valid spawn position (max 16 attempts)
        for (int i = 0; i < 16; i++) {
            mob.setPos(x, y, z);
            BlockPos mobPos = mob.blockPosition();

            if (!level.isEmptyBlock(mobPos)) {
                x = masterPos.getX() + level.random.nextInt(8) - 4 + 0.5;
                z = masterPos.getZ() + level.random.nextInt(8) - 4 + 0.5;
            } else {
                break;
            }
        }

        // Spawn the entity
        level.addFreshEntity(mob);

        // Play sound
        level.playSound(
            null,
            mob.blockPosition(),
            SoundEvents.SNOW_STEP,
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );

        // Consume LP
        SoulTicket ticket = new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_PEACE),
            getRefreshCost()
        );
        network.syphon(ticket, false);
    }

    @Override
    public int getRefreshCost() {
        return AnimusConfig.rituals.peaceCost;
    }

    @Override
    public int getRefreshTime() {
        return 400;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 4, 0, 0, RitualType.EnumRuneType.EARTH);
        addRune(components, 4, 1, 0, RitualType.EnumRuneType.EARTH);
        addRune(components, 4, 2, 0, RitualType.EnumRuneType.WATER);
        addRune(components, 0, 1, 4, RitualType.EnumRuneType.EARTH);
        addRune(components, 0, 2, 4, RitualType.EnumRuneType.EARTH);
        addRune(components, 0, 3, 4, RitualType.EnumRuneType.WATER);
        addRune(components, -4, 2, 0, RitualType.EnumRuneType.EARTH);
        addRune(components, -4, 3, 0, RitualType.EnumRuneType.EARTH);
        addRune(components, -4, 4, 0, RitualType.EnumRuneType.WATER);
        addRune(components, 0, 3, -4, RitualType.EnumRuneType.EARTH);
        addRune(components, 0, 4, -4, RitualType.EnumRuneType.EARTH);
        addRune(components, 0, 5, -4, RitualType.EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualPeace();
    }
}
