package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.breakinblocks.animusnv.util.AnimusUtil;
import com.breakinblocks.animusnv.util.InventorySearchHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import com.breakinblocks.neovitae.common.item.IBindable;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;
import com.breakinblocks.neovitae.common.datacomponent.Binding;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;

import java.util.UUID;
import java.util.function.Consumer;

public class ItemBloodApple extends Item {
    private static final FoodProperties FOOD_PROPERTIES = new FoodProperties.Builder()
        .nutrition(3)
        .saturationModifier(0.3F)
        .alwaysEdible()
        .build();

    // Search for altars in a 11x21x11 area centered on the player
    private final AreaDescriptor altarRange = new AreaDescriptor.Rectangle(new BlockPos(-5, -10, -5), 11, 21, 11);
    private BlockPos offsetCached = BlockPos.ZERO;

    public ItemBloodApple(Item.Properties props) {
        super(props
            .food(FOOD_PROPERTIES)
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.BLOOD_APPLE_FLAVOUR));
        tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.BLOOD_APPLE_INFO));
        tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.BLOOD_APPLE_EV));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof Player player) {
            AraVitaeTile altar = AnimusUtil.getNearbyAltar(level, altarRange, entity.blockPosition(), offsetCached);

            int bloodAmount = AnimusConfig.general.bloodPerApple.get();

            if (altar != null) {
                // Doubled like in original NeoVitae
                altar.addSacrificeEV(bloodAmount * 2, true);
                offsetCached = altar.getBlockPos();
            } else {
                UUID targetOwner = findBoundKeyOwner(player);

                IAnima network;
                if (targetOwner != null) {
                    network = NeoVitaeAPI.getInstance().getAnima(targetOwner);
                } else {
                    network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
                }

                network.add(AnimaTicket.create(bloodAmount), 10000);
            }
        }

        return super.finishUsingItem(stack, level, entity);
    }

    private UUID findBoundKeyOwner(Player player) {
        var fromInventory = InventorySearchHelper.findFirst(player, stack -> getKeyBindingOwner(stack) != null);
        if (fromInventory.isPresent()) {
            return getKeyBindingOwner(fromInventory.get());
        }

        var curiosOpt = CuriosApi.getCuriosInventory(player);
        if (curiosOpt.isPresent()) {
            var curios = curiosOpt.get();
            var handler = curios.getEquippedCurios();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack itemStack = handler.getStackInSlot(i);
                UUID owner = getKeyBindingOwner(itemStack);
                if (owner != null) {
                    return owner;
                }
            }
        }

        return null;
    }

    private UUID getKeyBindingOwner(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        if (stack.getItem() != AnimusItems.KEY_BINDING.get()) {
            return null;
        }

        if (stack.getItem() instanceof IBindable bindable) {
            Binding binding = bindable.getBinding(stack);
            if (binding != null && !binding.isEmpty()) {
                return binding.uuid();
            }
        }

        return null;
    }
}
