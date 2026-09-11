package com.breakinblocks.animusnv.mixin;

import com.breakinblocks.animusnv.compat.ironsspells.CrimsonWillSpellHandler;
import com.breakinblocks.animusnv.compat.ironsspells.SpellCastingHandler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractSpell.class, remap = false)
public class AbstractSpellMixin {
    @Redirect(method = "canBeCastedBy", at = @At(value = "INVOKE",
        target = "Lio/redspace/ironsspellbooks/api/magic/MagicData;getMana()F"))
    private float animus$checkEV(MagicData data, int spellLevel, CastSource source, MagicData magicData, Player player) {
        int cost = ((AbstractSpell) (Object) this).getManaCost(spellLevel);
        return source.consumesMana() && SpellCastingHandler.canPayWithEV(player, cost)
            ? Math.max(data.getMana(), cost) : data.getMana();
    }

    @Inject(method = "castSpell", at = @At(value = "INVOKE",
        target = "Lio/redspace/ironsspellbooks/api/magic/MagicData;getMana()F"), cancellable = true)
    private void animus$payEV(Level level, int spellLevel, ServerPlayer player, CastSource source,
                             boolean triggerCooldown, CallbackInfo ci, @Local SpellOnCastEvent event) {
        if (!SpellCastingHandler.payAndSupplyMana(player, event.getManaCost())) {
            ci.cancel();
        }
    }

    @WrapMethod(method = "castSpell")
    private void animus$finishPowerModifiers(Level level, int spellLevel, ServerPlayer player,
                                             CastSource source, boolean triggerCooldown, Operation<Void> original) {
        try {
            original.call(level, spellLevel, player, source, triggerCooldown);
        } finally {
            CrimsonWillSpellHandler.finishCast(player);
        }
    }
}
