package com.teamdman.animus.items;

import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.core.data.Binding;
import WayofTime.bloodmagic.iface.IBindable;
import WayofTime.bloodmagic.util.helper.TextHelper;
import com.teamdman.animus.Constants;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemKeyBinding extends Item implements IBindable, IVariantProvider {
	public ItemKeyBinding() {
		setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(TextHelper.localize(Constants.Localizations.Tooltips.KEY));
		Binding binding = getBinding(stack);
		if (binding == null)
			return;
		tooltip.add(TextHelper.localizeEffect(Constants.Localizations.Tooltips.OWNER, binding.getOwnerName()));
	}

	@Override
	public void gatherVariants(@Nonnull Int2ObjectMap<String> variants) {
		variants.put(0, "type=normal");
	}
}
