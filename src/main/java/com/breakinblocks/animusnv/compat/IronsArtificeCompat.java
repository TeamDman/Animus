package com.breakinblocks.animusnv.compat;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.ironsartifice.BloodBulletModifier;
import com.breakinblocks.animusnv.compat.ironsartifice.IronsArtificeEvents;
import com.breakinblocks.animusnv.compat.ironsartifice.SpiritPowderModifier;
import io.redspace.irons_artifice.modifier.ModifierItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IronsArtificeCompat implements ICompatModule {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.Mod.MODID);

    public static final DeferredItem<ModifierItem> BLOOD_BULLET_MODIFIER = ITEMS.registerItem(
            "blood_bullet_modifier", props -> new ModifierItem(props.stacksTo(1), new BloodBulletModifier()));

    public static final DeferredItem<ModifierItem> SPIRIT_POWDER_MODIFIER = ITEMS.registerItem(
            "spirit_powder_modifier", props -> new ModifierItem(props.stacksTo(1), new SpiritPowderModifier()));

    public static void registerDeferred(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        Animus.LOGGER.debug("Registered Iron's Arms 'n Artifice compatibility registries");
    }

    @Override
    public void init() {
        NeoForge.EVENT_BUS.register(IronsArtificeEvents.class);
        Animus.LOGGER.debug("Initializing Iron's Arms 'n Artifice compatibility");
    }

    @Override
    public String getModId() {
        return "irons_artifice";
    }
}
