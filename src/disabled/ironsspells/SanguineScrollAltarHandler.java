package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.compat.IronsSpellsCompat;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.item.Scroll;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import wayoftime.bloodmagic.common.block.BlockAltar;
import wayoftime.bloodmagic.common.tile.TileAltar;

/**
 * Handles conversion of Iron's Spells scrolls into Sanguine Scrolls at the Blood Altar
 *
 * Process:
 * 1. Player right-clicks altar with Iron's Spells scroll
 * 2. System checks for required slate in offhand
 * 3. Consumes LP from altar (based on spell rarity and level)
 * 4. Creates Sanguine Scroll with same spell data
 */
public class SanguineScrollAltarHandler {

    // LP costs based on spell rarity (scaled so max level 5 legendary = 80k LP)
    private static final int COMMON_LP = 2000;
    private static final int UNCOMMON_LP = 4000;
    private static final int RARE_LP = 8000;
    private static final int EPIC_LP = 12000;
    private static final int LEGENDARY_LP = 16000;

    /**
     * Listen for right-click on Blood Altar
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!AnimusConfig.ironsSpells.enableSanguineScrolls.get()) {
            return;
        }

        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        InteractionHand hand = event.getHand();

        // Only handle main hand interactions
        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }

        // Server-side only
        if (level.isClientSide) {
            return;
        }

        // Check if clicking on Blood Altar
        if (!(level.getBlockState(pos).getBlock() instanceof BlockAltar)) {
            return;
        }

        // Get altar tile entity
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TileAltar altar)) {
            return;
        }

        // Check main hand - must be Iron's Spells scroll
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(mainHand.getItem() instanceof Scroll)) {
            return;
        }

        // Check offhand - must be slate
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        ItemSanguineScroll.SlateType slateType = getSlateType(offHand);
        if (slateType == null) {
            player.displayClientMessage(
                Component.literal("Hold a slate in your offhand to create a Sanguine Scroll")
                    .withStyle(ChatFormatting.GOLD),
                true
            );
            return;
        }

        // Get spell data from scroll using ISpellContainer
        SpellData spellData = ISpellContainer.get(mainHand).getSpellAtIndex(0);
        AbstractSpell spell = spellData.getSpell();
        int spellLevel = spellData.getLevel();
        String spellId = spell.getSpellId();

        if (spellId == null || spellId.isEmpty()) {
            player.displayClientMessage(
                Component.literal("Scroll contains no spell")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        if (spell == null) {
            player.displayClientMessage(
                Component.literal("Invalid spell on scroll")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        // Calculate LP cost based on rarity and level
        int baseCost = getLPCostForRarity(spell.getRarity(spellLevel));
        int levelMultiplier = spellLevel;
        int totalLPCost = baseCost * levelMultiplier;

        // Check if altar has enough LP
        if (altar.getCurrentBlood() < totalLPCost) {
            player.displayClientMessage(
                Component.literal("Not enough LP in altar! Need " + totalLPCost + " LP")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        // Consume LP from altar
        altar.sacrificialDaggerCall(-totalLPCost, false);

        // Create Sanguine Scroll based on slate type
        ItemStack sanguineScroll = getSanguineScrollForSlate(slateType);
        if (sanguineScroll.isEmpty()) {
            Animus.LOGGER.error("Failed to create sanguine scroll for slate type: " + slateType);
            return;
        }

        // Transfer spell data to Sanguine Scroll
        ItemSanguineScroll.setSpell(sanguineScroll, spellId, spellLevel);

        // Consume inputs
        mainHand.shrink(1);
        offHand.shrink(1);

        // Give player the Sanguine Scroll
        if (!player.getInventory().add(sanguineScroll)) {
            player.drop(sanguineScroll, false);
        }

        // Feedback
        level.playSound(
            null,
            pos,
            SoundEvents.ENCHANTMENT_TABLE_USE,
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );

        player.displayClientMessage(
            Component.literal("Created Sanguine Scroll!")
                .withStyle(ChatFormatting.DARK_RED),
            true
        );

        Animus.LOGGER.debug("Created Sanguine Scroll: {} level {} (slate: {})",
            spell.getDisplayName(null).getString(), spellLevel, slateType);

        // Cancel the interaction so we don't open altar GUI
        event.setCanceled(true);
    }

    /**
     * Get LP cost based on spell rarity
     */
    private static int getLPCostForRarity(io.redspace.ironsspellbooks.api.spells.SpellRarity rarity) {
        return switch (rarity) {
            case COMMON -> COMMON_LP;
            case UNCOMMON -> UNCOMMON_LP;
            case RARE -> RARE_LP;
            case EPIC -> EPIC_LP;
            case LEGENDARY -> LEGENDARY_LP;
        };
    }

    /**
     * Determine slate type from itemstack
     */
    private static ItemSanguineScroll.SlateType getSlateType(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        String id = stack.getItem().toString();

        if (id.contains("blankslate")) {
            return ItemSanguineScroll.SlateType.BLANK;
        } else if (id.contains("reinforcedslate")) {
            return ItemSanguineScroll.SlateType.REINFORCED;
        } else if (id.contains("imbuedslate")) {
            return ItemSanguineScroll.SlateType.IMBUED;
        } else if (id.contains("demonslate")) {
            return ItemSanguineScroll.SlateType.DEMON;
        } else if (id.contains("etherealslate")) {
            return ItemSanguineScroll.SlateType.ETHEREAL;
        }

        return null;
    }

    /**
     * Get the appropriate Sanguine Scroll item for the slate type
     */
    private static ItemStack getSanguineScrollForSlate(ItemSanguineScroll.SlateType slateType) {
        return switch (slateType) {
            case BLANK -> new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_BLANK.get());
            case REINFORCED -> new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_REINFORCED.get());
            case IMBUED -> new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_IMBUED.get());
            case DEMON -> new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_DEMON.get());
            case ETHEREAL -> new ItemStack(IronsSpellsCompat.SANGUINE_SCROLL_ETHEREAL.get());
        };
    }
}
