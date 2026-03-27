package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.Constants;
import com.klikli_dev.modonomicon.item.ModonomiconCustomItemBase;
import net.minecraft.resources.ResourceLocation;

public class AnimusGuideBookItem extends ModonomiconCustomItemBase {
    public AnimusGuideBookItem() {
        super(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "guide"), new Properties().stacksTo(1));
    }
}
