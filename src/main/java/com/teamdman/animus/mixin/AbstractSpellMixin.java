package com.teamdman.animus.mixin;

import com.teamdman.animus.compat.ironsspells.SpellCastingHandler;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Let the mana eligibility check consider LP without modifying the player's mana. */
@Mixin(value = AbstractSpell.class, remap = false)
public class AbstractSpellMixin {
    @Redirect(
        method = "canBeCastedBy",
        at = @At(value = "INVOKE", target = "Lio/redspace/ironsspellbooks/api/magic/MagicData;getMana()F")
    )
    private float animus$availableMana(MagicData data, int spellLevel, CastSource source,
                                      MagicData magicData, Player player) {
        return SpellCastingHandler.getManaForCastCheck(player, (AbstractSpell) (Object) this,
            spellLevel, source, data);
    }
}
