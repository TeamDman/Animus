package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
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
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.tile.TileAltar;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.AreaDescriptor;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

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

    // TODO: Move to config system - AnimusConfig.general.bloodPerApple
    private static final int BLOOD_PER_APPLE = 50;

    public ItemBloodApple() {
        super(new Item.Properties()
            .food(FOOD_PROPERTIES)
        );
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            // Search for nearby altar in range
            TileAltar altar = AnimusUtil.getNearbyAltar(level, altarRange, entity.blockPosition(), offsetCached);

            if (altar != null) {
                // Altar found - add blood to altar (doubled like in original)
                altar.sacrificialDaggerCall(BLOOD_PER_APPLE * 2, true);
                offsetCached = altar.getBlockPos();
            } else {
                // No altar nearby - add blood to player's soul network
                SoulNetwork network = NetworkHelper.getSoulNetwork(player);
                network.add(
                    new SoulTicket(
                        Component.translatable(Constants.Localizations.Text.TICKET_APPLE),
                        BLOOD_PER_APPLE
                    ),
                    10000
                );
            }
        }

        return super.finishUsingItem(stack, level, entity);
    }
}
