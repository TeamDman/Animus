package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Blood Apple - food item that provides blood to the player
 * TODO: Implement altar detection and blood network integration with Blood Magic API
 */
public class ItemBloodApple extends Item {
    private static final FoodProperties FOOD_PROPERTIES = new FoodProperties.Builder()
        .nutrition(3)
        .saturationMod(0.3F)
        .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 40, 0), 0.75F)
        .alwaysEat()
        .build();

    public ItemBloodApple() {
        super(new Item.Properties()
            .food(FOOD_PROPERTIES)
        );
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            // TODO: Implement Blood Magic integration
            // 1. Search for nearby altar using AnimusUtil.getNearbyAltar()
            // 2. If altar found, add blood to altar: altar.sacrificialDaggerCall()
            // 3. Otherwise, add blood to player's soul network: NetworkHelper.getSoulNetwork()
            // 4. Amount should be configurable via AnimusConfig.general.bloodPerApple

            // For now, just consume the item normally
        }

        return super.finishUsingItem(stack, level, entity);
    }
}
