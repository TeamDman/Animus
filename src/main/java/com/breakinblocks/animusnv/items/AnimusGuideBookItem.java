package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.Constants;
import com.klikli_dev.modonomicon.item.ModonomiconCustomItemBase;
import net.minecraft.resources.Identifier;

public class AnimusGuideBookItem extends ModonomiconCustomItemBase {
    public AnimusGuideBookItem(Properties props) {
        super(Identifier.fromNamespaceAndPath(Constants.Mod.MODID, "guide"), props.stacksTo(1));
    }
}
