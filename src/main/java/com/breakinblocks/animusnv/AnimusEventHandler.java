package com.breakinblocks.animusnv;

import com.breakinblocks.animusnv.entities.EntitySentientArrow;
import com.breakinblocks.animusnv.entities.EntityThrownSpear;
import com.breakinblocks.animusnv.events.FragmentHealingEventHandler;
import com.breakinblocks.animusnv.items.ItemSentientBow;
import com.breakinblocks.animusnv.items.ItemSpearSentient;
import com.breakinblocks.animusnv.items.sigils.effects.FreeSoulSigilEffect;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.breakinblocks.animusnv.util.SpiritusWeaponStats;
import com.breakinblocks.neovitae.common.datacomponent.AnointmentHolder;
import com.breakinblocks.neovitae.common.item.sigil.ItemSigilHolding;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.spiritus.ISpiritus;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.spiritus.IPlayerSpiritusHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import com.breakinblocks.animusnv.compat.CompatHandler;
import com.breakinblocks.animusnv.compat.malum.SpiritHarvestHelper;
import com.breakinblocks.animusnv.items.ItemRunicSentientScythe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = Constants.Mod.MODID)
public class AnimusEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Player player = event.getEntity();

        if (player.level() instanceof ServerLevel serverLevel) {
            if (player instanceof ServerPlayer serverPlayer) {
                FreeSoulSigilEffect.tickActiveSpectators(serverPlayer, serverLevel);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();

        FragmentHealingEventHandler.cleanupPlayer(playerId);

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            FreeSoulSigilEffect.onPlayerLogout(serverPlayer);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        ItemStack freeSoulStack = findFreeSoulSigil(player);
        if (freeSoulStack.isEmpty()) {
            return;
        }

        if (FreeSoulSigilEffect.tryPreventDeath(player, event.getSource(), freeSoulStack)) {
            event.setCanceled(true);
        }
    }

    private static ItemStack findFreeSoulSigil(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(AnimusItems.SIGIL_FREE_SOUL.get())) {
                return stack;
            }
            // Check inside Sigil of Holding
            if (!stack.isEmpty() && stack.getItem() instanceof ItemSigilHolding) {
                var holdingInv = ItemSigilHolding.getInternalInventory(stack);
                for (ItemStack heldStack : holdingInv) {
                    if (!heldStack.isEmpty() && heldStack.is(AnimusItems.SIGIL_FREE_SOUL.get())) {
                        return heldStack;
                    }
                }
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.isEmpty() && stack.is(AnimusItems.SIGIL_FREE_SOUL.get())) {
                return stack;
            }
            if (!stack.isEmpty() && stack.getItem() instanceof ItemSigilHolding) {
                var holdingInv = ItemSigilHolding.getInternalInventory(stack);
                for (ItemStack heldStack : holdingInv) {
                    if (!heldStack.isEmpty() && heldStack.is(AnimusItems.SIGIL_FREE_SOUL.get())) {
                        return heldStack;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Apply Malum's scythe proficiency multiplier to our scythes.
     * Malum only applies it to damage sources tagged malum:is_scythe; ours deal ordinary
     * player attack damage, so sources of proficiency such as the Necklace of the Narrow
     * Edge would otherwise do nothing for them.
     */
    @SubscribeEvent
    public static void onScytheDamage(LivingIncomingDamageEvent event) {
        if (!CompatHandler.isMalumLoaded()) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        if (event.getSource().getDirectEntity() != player) {
            return;
        }

        if (!(player.getMainHandItem().getItem() instanceof ItemRunicSentientScythe)) {
            return;
        }

        double proficiency = SpiritHarvestHelper.getScytheProficiency(player);
        if (proficiency != 1.0) {
            event.setAmount((float) (event.getAmount() * proficiency));
        }
    }

    /**
     * Handle NeoVitae anointment bonus damage for thrown spears.
     * NeoVitae's GenericHandler only handles melee attacks (checks player's held item).
     * This handler applies anointment damage bonuses when entities are hit by thrown spears.
     */
    @SubscribeEvent
    public static void onThrownSpearHurt(LivingIncomingDamageEvent event) {
        Entity directEntity = event.getSource().getDirectEntity();
        Entity sourceEntity = event.getSource().getEntity();

        if (!(directEntity instanceof EntityThrownSpear thrownSpear)) {
            return;
        }

        ItemStack spearStack = thrownSpear.getSpearItem();
        if (spearStack.isEmpty()) {
            return;
        }

        AnointmentHolder holder = spearStack.get(NVDataComponents.ANOINTMENT_HOLDER.get());
        if (holder == null || holder.isEmpty()) {
            return;
        }

        Player attackingPlayer = sourceEntity instanceof Player ? (Player) sourceEntity : null;

        // TODO: Apply anointment damage bonuses once AnointmentHolder.getAdditionalDamage is available
    }

    /**
     * Consume anointment durability when thrown spear deals damage.
     * This mirrors NeoVitae's GenericHandler.onLivingDamage behavior.
     */
    @SubscribeEvent
    public static void onThrownSpearDamage(LivingDamageEvent.Post event) {
        Entity directEntity = event.getSource().getDirectEntity();
        Entity sourceEntity = event.getSource().getEntity();

        if (!(directEntity instanceof EntityThrownSpear thrownSpear)) {
            return;
        }

        if (thrownSpear.level().isClientSide()) {
            return;
        }

        if (!(sourceEntity instanceof Player player)) {
            return;
        }

        ItemStack spearStack = thrownSpear.getSpearItem();
        if (spearStack.isEmpty()) {
            return;
        }

        AnointmentHolder holder = spearStack.get(NVDataComponents.ANOINTMENT_HOLDER.get());
        if (holder == null || holder.isEmpty()) {
            return;
        }

        AnointmentHolder consumed = holder.consumeOnAttack();
        spearStack.set(NVDataComponents.ANOINTMENT_HOLDER.get(), consumed);
    }

    /**
     * Handle will drops from Sentient weapons (Spear melee/thrown, Bow arrows) and
     * Ritual of Endless Greed - Intercept mob drops and transfer to container
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity killedEntity = event.getEntity();
        Level level = killedEntity.level();

        if (level.isClientSide()) {
            return;
        }

        handleSentientWeaponSpiritusDrops(event);
    }

    private static void handleSentientWeaponSpiritusDrops(LivingDropsEvent event) {
        LivingEntity killedEntity = event.getEntity();
        DamageSource source = event.getSource();
        Entity sourceEntity = source.getEntity();
        Entity directEntity = source.getDirectEntity();

        if (directEntity instanceof EntityThrownSpear thrownSpear && "sentient".equals(thrownSpear.getVariant())) {
            if (sourceEntity instanceof Player player) {
                SpiritusType spiritusType = thrownSpear.getSpiritusType();
                int spiritusLevel = Math.min(thrownSpear.getSpiritusLevel(), 4);
                int looting = getLootingLevel(player);

                List<ItemStack> spiritusDrops = generateSentientSpiritusDrops(
                    killedEntity, player, spiritusType, spiritusLevel, looting,
                    SpiritusWeaponStats.SOUL_DROP, SpiritusWeaponStats.STATIC_DROP
                );

                addSpiritusDropsToPlayerOrWorld(player, killedEntity, spiritusDrops, event.getDrops());
            }
            return;
        }

        if (directEntity instanceof EntitySentientArrow sentientArrow) {
            if (sourceEntity instanceof Player player) {
                SpiritusType spiritusType = sentientArrow.getSpiritusType();
                int spiritusLevel = Math.min(sentientArrow.getSpiritusLevel(), 4);
                int looting = getLootingLevel(player);

                List<ItemStack> spiritusDrops = generateSentientSpiritusDrops(
                    killedEntity, player, spiritusType, spiritusLevel, looting,
                    SpiritusWeaponStats.SOUL_DROP, SpiritusWeaponStats.STATIC_DROP
                );

                addSpiritusDropsToPlayerOrWorld(player, killedEntity, spiritusDrops, event.getDrops());
            }
            return;
        }

        if (sourceEntity instanceof Player player) {
            ItemStack heldStack = player.getMainHandItem();
            if (heldStack.getItem() instanceof ItemSpearSentient sentientSpear) {
                int looting = getLootingLevel(player);
                List<ItemStack> spiritusDrops = sentientSpear.getRandomSpiritusDrop(
                    killedEntity, player, heldStack, looting
                );

                addSpiritusDropsToPlayerOrWorld(player, killedEntity, spiritusDrops, event.getDrops());
            }
        }
    }

    private static int getLootingLevel(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            ItemStack weapon = player.getMainHandItem();
            var lootingHolder = serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.LOOTING);
            if (lootingHolder.isPresent()) {
                return weapon.getEnchantmentLevel(lootingHolder.get());
            }
        }
        return 0;
    }

    private static List<ItemStack> generateSentientSpiritusDrops(
        LivingEntity killedEntity,
        Player attackingEntity,
        SpiritusType spiritusType,
        int spiritusLevel,
        int looting,
        double[] soulDrop,
        double[] staticDrop
    ) {
        ArrayList<ItemStack> soulList = new ArrayList<>();

        if (killedEntity.getCommandSenderWorld().getDifficulty() != Difficulty.PEACEFUL
            && !(killedEntity instanceof Enemy)) {
            return soulList;
        }

        double spiritusModifier = killedEntity instanceof Slime ? 0.67 : 1;

        ISpiritus soul = SpiritusWeaponStats.getSoulItem(spiritusType);

        for (int i = 0; i <= looting; i++) {
            if (i == 0 || attackingEntity.getCommandSenderWorld().random.nextDouble() < 0.4) {
                double dropAmount = spiritusModifier * (soulDrop[spiritusLevel] * attackingEntity.getCommandSenderWorld().random.nextDouble()
                    + staticDrop[spiritusLevel]) * killedEntity.getMaxHealth() / 20.0;
                ItemStack soulStack = soul.createSpiritus(dropAmount);
                soulList.add(soulStack);
            }
        }

        return soulList;
    }

    /**
     * Add will drops directly to player's Spiritus Gem, drop excess as item entities.
     */
    private static void addSpiritusDropsToPlayerOrWorld(
        Player player,
        LivingEntity killedEntity,
        List<ItemStack> spiritusDrops,
        Collection<ItemEntity> existingDrops
    ) {
        if (spiritusDrops.isEmpty()) {
            return;
        }

        IPlayerSpiritusHandler playerSpiritus = NeoVitaeAPI.getInstance().getPlayerSpiritusHandler();
        for (ItemStack spiritusStack : spiritusDrops) {
            ItemStack remainder = playerSpiritus.addSpiritus(player, spiritusStack);

            if (!remainder.isEmpty()) {
                SpiritusType pickupType = ((ISpiritus) remainder.getItem()).getType(remainder);
                if (((ISpiritus) remainder.getItem()).getSpiritus(pickupType, remainder) >= 0.0001) {
                    existingDrops.add(new ItemEntity(
                        killedEntity.getCommandSenderWorld(),
                        killedEntity.getX(),
                        killedEntity.getY(),
                        killedEntity.getZ(),
                        remainder
                    ));
                }
            }
        }

        player.inventoryMenu.broadcastChanges();
    }

}
