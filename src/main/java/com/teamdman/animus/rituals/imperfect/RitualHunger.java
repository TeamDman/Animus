package com.teamdman.animus.rituals.imperfect;

import com.teamdman.animus.Constants;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import wayoftime.bloodmagic.ritual.RitualRegister;
import wayoftime.bloodmagic.ritual.imperfect.IImperfectRitualStone;
import wayoftime.bloodmagic.ritual.imperfect.ImperfectRitual;

/**
 * Imperfect Ritual of Hunger
 * <p>
 * Requires: Bone Block on top of Imperfect Ritual Stone
 * Cost: 500 LP
 * Effect: Sets player's food level to 1 and saturation to 10 (makes player very hungry)
 */
@RitualRegister.Imperfect(Constants.Rituals.HUNGER)
public class RitualHunger extends ImperfectRitual {

    public RitualHunger() {
        super(
            Constants.Rituals.HUNGER,
            state -> state.is(Blocks.BONE_BLOCK),
            500,
            true,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.HUNGER
        );
    }

    @Override
    public boolean onActivate(IImperfectRitualStone ritualStone, Player player) {
        Level level = ritualStone.getRitualWorld();

        if (level.isClientSide) {
            return false;
        }

        // Set player to very hungry
        player.getFoodData().setFoodLevel(1);
        player.getFoodData().setSaturation(10.0F);

        // Play sound effect
        level.playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            SoundEvents.FIRE_EXTINGUISH,
            SoundSource.BLOCKS,
            0.5F,
            2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F
        );

        return true;
    }
}
