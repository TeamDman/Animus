package com.teamdman.animus.items;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.util.AnimusUtil;
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
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.common.tile.TileAltar;
import wayoftime.bloodmagic.core.data.Binding;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.AreaDescriptor;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.List;
import java.util.UUID;

/**
 * Blood Apple - food item that provides blood to the player or nearby altar
 */
public class ItemBloodApple extends Item {
    private static final FoodProperties FOOD_PROPERTIES = new FoodProperties.Builder()
        .nutrition(3)
        .saturationMod(0.3F)
        .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 40, 0), 0.75F)
        .alwaysEat()
        .build();

    // Search for altars in a 11x21x11 area centered on the player
    private final AreaDescriptor altarRange = new AreaDescriptor.Rectangle(new BlockPos(-5, -10, -5), 11, 21, 11);
    private BlockPos offsetCached = BlockPos.ZERO;

    public ItemBloodApple() {
        super(new Item.Properties()
            .food(FOOD_PROPERTIES)
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_APPLE_FLAVOUR));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_APPLE_INFO));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_APPLE_LP));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            // Search for nearby altar in range
            TileAltar altar = AnimusUtil.getNearbyAltar(level, altarRange, entity.blockPosition(), offsetCached);

            int bloodAmount = AnimusConfig.general.bloodPerApple.get();

            if (altar != null) {
                // Altar found - add blood to altar (doubled like in original)
                altar.sacrificialDaggerCall(bloodAmount * 2, true);
                offsetCached = altar.getBlockPos();
            } else {
                // Check if player has a bound Key of Binding - if so, redirect LP to key owner
                UUID targetOwner = findBoundKeyOwner(player);

                SoulNetwork network;
                if (targetOwner != null) {
                    // Redirect LP to the Key of Binding owner's network
                    network = NetworkHelper.getSoulNetwork(targetOwner);
                } else {
                    // No key found - add blood to player's own soul network
                    network = NetworkHelper.getSoulNetwork(player);
                }

                network.add(
                    new SoulTicket(
                        Component.translatable(Constants.Localizations.Text.TICKET_APPLE),
                        bloodAmount
                    ),
                    10000
                );
            }
        }

        return super.finishUsingItem(stack, level, entity);
    }

    /**
     * Finds a bound Key of Binding in the player's inventory or Curios slots
     * @return The owner UUID of the bound key, or null if no bound key found
     */
    private UUID findBoundKeyOwner(Player player) {
        // Check main inventory
        for (ItemStack itemStack : player.getInventory().items) {
            UUID owner = getKeyBindingOwner(itemStack);
            if (owner != null) {
                return owner;
            }
        }

        // Check offhand
        for (ItemStack itemStack : player.getInventory().offhand) {
            UUID owner = getKeyBindingOwner(itemStack);
            if (owner != null) {
                return owner;
            }
        }

        // Check curios slots
        var curiosOpt = CuriosApi.getCuriosInventory(player).resolve();
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

    /**
     * Gets the owner UUID from a Key of Binding, if the stack is a bound key
     * @return The owner UUID, or null if not a bound Key of Binding
     */
    private UUID getKeyBindingOwner(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        // Check if it's a Key of Binding item
        if (stack.getItem() != AnimusItems.KEY_BINDING.get()) {
            return null;
        }

        // Check if bound using Blood Magic's binding system
        if (stack.getItem() instanceof IBindable bindable) {
            Binding binding = bindable.getBinding(stack);
            if (binding != null) {
                return binding.getOwnerId();
            }
        }

        return null;
    }
}
