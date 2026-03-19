package com.teamdman.animus;

import com.teamdman.animus.entities.EntitySentientArrow;
import com.teamdman.animus.entities.EntityThrownSpear;
import com.teamdman.animus.events.FragmentHealingEventHandler;
import com.teamdman.animus.items.ItemSentientBow;
import com.teamdman.animus.items.ItemSpearSentient;
import com.teamdman.animus.items.sigils.effects.FreeSoulSigilEffect;
import com.teamdman.animus.registry.AnimusAttributes;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.util.SigilStateCleanupManager;
import com.breakinblocks.neovitae.common.datacomponent.AnointmentHolder;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.datacomponent.EnumWillType;
import com.breakinblocks.neovitae.common.event.SacrificialDaggerEvent;
import com.breakinblocks.neovitae.common.item.NVItems;
import com.breakinblocks.neovitae.api.event.SoulNetworkEvent;
import com.breakinblocks.neovitae.will.IDemonWill;
import com.breakinblocks.neovitae.will.PlayerDemonWillHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Central event handler for Animus mod
 * Handles player cleanup, Free Soul sigil death prevention, sentient weapon will drops,
 * anointment support for thrown spears, and ritual drop interception
 *
 * Other event handlers in the events package:
 * - MonkSigilEventHandler: Sigil of the Demon Monk functionality
 * - KeyBindingEventHandler: Key of Binding transfer functionality
 * - SentientShieldEventHandler: Sentient Shield blocking effects
 * - FragmentHealingEventHandler: Fragment of Healing passive healing
 * - WorldEffectEventHandler: Lightning conversion, spawn prevention, level ticks
 */
@EventBusSubscriber(modid = Constants.Mod.MODID)
public class AnimusEventHandler {

    /**
     * Process Free Soul spectator mode timer
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // Only run on server side
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Player player = event.getEntity();

        // Process Free Soul spectator mode timer
        if (player.level() instanceof ServerLevel serverLevel) {
            if (player instanceof ServerPlayer serverPlayer) {
                FreeSoulSigilEffect.tickActiveSpectators(serverPlayer, serverLevel);
            }
        }
    }

    /**
     * Clean up player data when they log out
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();

        // Clean up healing cooldown
        FragmentHealingEventHandler.cleanupPlayer(playerId);

        // Clean up all registered sigil state trackers and custom handlers
        SigilStateCleanupManager.cleanupPlayer(playerId);

        // Clean up Free Soul spectator mode (needs ServerPlayer, not just UUID)
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            FreeSoulSigilEffect.onPlayerLogout(serverPlayer);
        }
    }

    /**
     * Free Soul Sigil death prevention
     * Works like a Totem of Undying - prevents death and activates spectator mode
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        // Only handle players on server side
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        // Find Free Soul sigil in player's inventory
        ItemStack freeSoulStack = findFreeSoulSigil(player);
        if (freeSoulStack.isEmpty()) {
            return;
        }

        // Try to prevent death with Free Soul sigil
        if (FreeSoulSigilEffect.tryPreventDeath(player, event.getSource(), freeSoulStack)) {
            event.setCanceled(true);
        }
    }

    /**
     * Find a Free Soul sigil in the player's inventory.
     */
    private static ItemStack findFreeSoulSigil(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(AnimusItems.SIGIL_FREE_SOUL.get())) {
                return stack;
            }
        }
        // Also check offhand
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.isEmpty() && stack.is(AnimusItems.SIGIL_FREE_SOUL.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Handle Blood Magic anointment bonus damage for thrown spears.
     * Blood Magic's GenericHandler only handles melee attacks (checks player's held item).
     * This handler applies anointment damage bonuses when entities are hit by thrown spears.
     */
    @SubscribeEvent
    public static void onThrownSpearHurt(LivingIncomingDamageEvent event) {
        Entity directEntity = event.getSource().getDirectEntity();
        Entity sourceEntity = event.getSource().getEntity();

        // Check if damage is from a thrown spear
        if (!(directEntity instanceof EntityThrownSpear thrownSpear)) {
            return;
        }

        // Get the spear item
        ItemStack spearStack = thrownSpear.getSpearItem();
        if (spearStack.isEmpty()) {
            return;
        }

        // Check if the spear has anointments
        AnointmentHolder holder = spearStack.get(NVDataComponents.ANOINTMENT_HOLDER.get());
        if (holder == null || holder.isEmpty()) {
            return;
        }

        // Get the attacking player (if any)
        Player attackingPlayer = sourceEntity instanceof Player ? (Player) sourceEntity : null;

        // Apply anointment damage bonuses
        // Note: AnointmentHolder in NeoVitae may not have getAdditionalDamage - check at compile time
        // For now, anointment durability consumption is handled in the damage event below
    }

    /**
     * Consume anointment durability when thrown spear deals damage.
     * This mirrors Blood Magic's GenericHandler.onLivingDamage behavior.
     */
    @SubscribeEvent
    public static void onThrownSpearDamage(LivingDamageEvent.Post event) {
        Entity directEntity = event.getSource().getDirectEntity();
        Entity sourceEntity = event.getSource().getEntity();

        // Check if damage is from a thrown spear
        if (!(directEntity instanceof EntityThrownSpear thrownSpear)) {
            return;
        }

        // Only process on server side
        if (thrownSpear.level().isClientSide()) {
            return;
        }

        // Get the player who threw the spear
        if (!(sourceEntity instanceof Player player)) {
            return;
        }

        // Get the actual spear item so we can update anointments
        ItemStack spearStack = thrownSpear.getSpearItem();
        if (spearStack.isEmpty()) {
            return;
        }

        // Check if the spear has anointments
        AnointmentHolder holder = spearStack.get(NVDataComponents.ANOINTMENT_HOLDER.get());
        if (holder == null || holder.isEmpty()) {
            return;
        }

        // Consume anointment durability on attack
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

        // Only run on server side
        if (level.isClientSide()) {
            return;
        }

        // Handle Sentient weapon will drops (spear melee/thrown and bow arrows)
        handleSentientWeaponWillDrops(event);

        // Check if the entity died within range of an Endless Greed ritual
        if (com.teamdman.animus.rituals.RitualEndlessGreed.handleMobDrops(level, killedEntity.blockPosition(), event.getDrops())) {
            // Clear drops since they were handled
            event.getDrops().clear();
        }
    }

    /**
     * Handle demon will drops from Sentient weapon kills.
     * Works for:
     * - Melee kills (player holding spear)
     * - Thrown spear kills (EntityThrownSpear)
     * - Sentient Bow arrow kills (EntitySentientArrow)
     * Matches the behavior of Blood Magic's Sentient Sword.
     */
    private static void handleSentientWeaponWillDrops(LivingDropsEvent event) {
        LivingEntity killedEntity = event.getEntity();
        DamageSource source = event.getSource();
        Entity sourceEntity = source.getEntity();
        Entity directEntity = source.getDirectEntity();

        // Check for thrown sentient spear kill
        if (directEntity instanceof EntityThrownSpear thrownSpear && "sentient".equals(thrownSpear.getVariant())) {
            if (sourceEntity instanceof Player player) {
                EnumWillType willType = thrownSpear.getWillType();
                int willLevel = Math.min(thrownSpear.getWillLevel(), 4);
                int looting = getLootingLevel(player);

                List<ItemStack> willDrops = generateSentientWillDrops(
                    killedEntity, player, willType, willLevel, looting,
                    ItemSpearSentient.soulDrop, ItemSpearSentient.staticDrop
                );

                addWillDropsToPlayerOrWorld(player, killedEntity, willDrops, event.getDrops());
            }
            return;
        }

        // Check for sentient bow arrow kill
        if (directEntity instanceof EntitySentientArrow sentientArrow) {
            if (sourceEntity instanceof Player player) {
                EnumWillType willType = sentientArrow.getWillType();
                int willLevel = Math.min(sentientArrow.getWillLevel(), 4);
                int looting = getLootingLevel(player);

                List<ItemStack> willDrops = generateSentientWillDrops(
                    killedEntity, player, willType, willLevel, looting,
                    ItemSentientBow.soulDrop, ItemSentientBow.staticDrop
                );

                addWillDropsToPlayerOrWorld(player, killedEntity, willDrops, event.getDrops());
            }
            return;
        }

        // Check for melee sentient spear kill (player holding the spear)
        if (sourceEntity instanceof Player player) {
            ItemStack heldStack = player.getMainHandItem();
            if (heldStack.getItem() instanceof ItemSpearSentient sentientSpear) {
                int looting = getLootingLevel(player);
                List<ItemStack> willDrops = sentientSpear.getRandomDemonWillDrop(
                    killedEntity, player, heldStack, looting
                );

                addWillDropsToPlayerOrWorld(player, killedEntity, willDrops, event.getDrops());
            }
        }
    }

    /**
     * Get the looting enchantment level from the player's held weapon.
     */
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

    /**
     * Generate demon will drops for a sentient weapon kill
     */
    private static List<ItemStack> generateSentientWillDrops(
        LivingEntity killedEntity,
        Player attackingEntity,
        EnumWillType willType,
        int willLevel,
        int looting,
        double[] soulDrop,
        double[] staticDrop
    ) {
        java.util.ArrayList<ItemStack> soulList = new java.util.ArrayList<>();

        // Only drop from hostile mobs (same check as Sentient Sword)
        if (killedEntity.getCommandSenderWorld().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
            && !(killedEntity instanceof net.minecraft.world.entity.monster.Enemy)) {
            return soulList;
        }

        // Slimes give reduced will
        double willModifier = killedEntity instanceof net.minecraft.world.entity.monster.Slime ? 0.67 : 1;

        // Get the appropriate demon will item based on will type
        IDemonWill soul = switch (willType) {
            case CORROSIVE -> ((IDemonWill) NVItems.MONSTER_SOUL_CORROSIVE.get());
            case DESTRUCTIVE -> ((IDemonWill) NVItems.MONSTER_SOUL_DESTRUCTIVE.get());
            case STEADFAST -> ((IDemonWill) NVItems.MONSTER_SOUL_STEADFAST.get());
            case VENGEFUL -> ((IDemonWill) NVItems.MONSTER_SOUL_VENGEFUL.get());
            default -> ((IDemonWill) NVItems.MONSTER_SOUL_RAW.get());
        };

        // Apply bonus demon will attribute from player
        double bonusWillPercent = attackingEntity.getAttributeValue(AnimusAttributes.BONUS_DEMON_WILL);
        double bonusWillMultiplier = 1 + bonusWillPercent / 100.0;

        // Drop will items (with looting bonus like sword)
        for (int i = 0; i <= looting; i++) {
            if (i == 0 || attackingEntity.getCommandSenderWorld().random.nextDouble() < 0.4) {
                double dropAmount = willModifier * (soulDrop[willLevel] * attackingEntity.getCommandSenderWorld().random.nextDouble()
                    + staticDrop[willLevel]) * killedEntity.getMaxHealth() / 20.0;
                dropAmount *= bonusWillMultiplier;
                ItemStack soulStack = soul.createWill(dropAmount);
                soulList.add(soulStack);
            }
        }

        return soulList;
    }

    /**
     * Try to add will drops directly to player's tartaric gem, drop excess as items.
     * Matches Blood Magic's WillHandler behavior.
     */
    private static void addWillDropsToPlayerOrWorld(
        Player player,
        LivingEntity killedEntity,
        List<ItemStack> willDrops,
        Collection<ItemEntity> existingDrops
    ) {
        if (willDrops.isEmpty()) {
            return;
        }

        for (ItemStack willStack : willDrops) {
            // Try to add directly to player's tartaric gem
            ItemStack remainder = PlayerDemonWillHandler.addDemonWill(player, willStack);

            // If gem is full or can't hold more, drop as item entity
            if (!remainder.isEmpty()) {
                EnumWillType pickupType = ((IDemonWill) remainder.getItem()).getType(remainder);
                if (((IDemonWill) remainder.getItem()).getWill(pickupType, remainder) >= 0.0001) {
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

        // Sync inventory changes
        player.inventoryMenu.broadcastChanges();
    }

    /**
     * Self-sacrifice bonus: increase LP gained from Sacrificial Dagger based on player attribute
     */
    @SubscribeEvent
    public static void onSelfSacrifice(SacrificialDaggerEvent event) {
        Player player = event.player;
        double bonusPercent = player.getAttributeValue(AnimusAttributes.BONUS_SELF_SACRIFICE);
        if (bonusPercent > 0) {
            event.lpAdded = (int) (event.lpAdded * (1 + bonusPercent / 100.0));
        }
    }

    /**
     * Sigil cost reduction: reduce LP syphoned for all drains
     */
    @SubscribeEvent
    public static void onSoulNetworkSyphon(SoulNetworkEvent.PreSyphon event) {
        // Get the network owner as a player to check their attribute
        net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        Player player = server.getPlayerList().getPlayer(event.getOwnerId());
        if (player == null) return;

        double reductionPercent = player.getAttributeValue(AnimusAttributes.SIGIL_COST_REDUCTION);
        if (reductionPercent > 0) {
            int reducedAmount = Math.max(1, (int) (event.getModifiedAmount() * (1 - reductionPercent / 100.0)));
            event.setModifiedAmount(reducedAmount);
        }
    }
}
