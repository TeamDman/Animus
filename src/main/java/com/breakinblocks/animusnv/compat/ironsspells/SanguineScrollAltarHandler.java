package com.breakinblocks.animusnv.compat.ironsspells;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.compat.IronsSpellsCompat;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import com.breakinblocks.neovitae.common.block.AraVitaeBlock;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;

/**
 * Handles conversion of Iron's Spells scrolls into Sanguine Scrolls at the Ara Vitae
 *
 * Process:
 * 1. Player right-clicks altar with Iron's Spells scroll
 * 2. System checks for required slate in offhand
 * 3. Consumes EV from altar (based on spell rarity and level)
 * 4. Creates Sanguine Scroll with same spell data
 */
public class SanguineScrollAltarHandler {

    private static final int COMMON_LP = 2000;
    private static final int UNCOMMON_LP = 4000;
    private static final int RARE_LP = 8000;
    private static final int EPIC_LP = 12000;
    private static final int LEGENDARY_LP = 16000;

    public static void register() {
        NeoForge.EVENT_BUS.register(SanguineScrollAltarHandler.class);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!AnimusConfig.ironsSpells.enableSanguineScrolls.get()) {
            return;
        }

        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        InteractionHand hand = event.getHand();

        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }

        if (level.isClientSide) {
            return;
        }

        if (!(level.getBlockState(pos).getBlock() instanceof AraVitaeBlock)) {
            return;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AraVitaeTile altar)) {
            return;
        }

        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(mainHand.getItem() instanceof Scroll)) {
            return;
        }

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

        int baseCost = getEVCostForRarity(spell.getRarity(spellLevel));
        int levelMultiplier = spellLevel;
        int totalEVCost = baseCost * levelMultiplier;

        if (altar.getCurrentBlood() < totalEVCost) {
            player.displayClientMessage(
                Component.literal("Not enough EV in altar! Need " + totalEVCost + " EV")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        altar.addSacrificeEV(-totalEVCost, false);
        ItemStack sanguineScroll = getSanguineScrollForSlate(slateType);
        if (sanguineScroll.isEmpty()) {
            Animus.LOGGER.error("Failed to create sanguine scroll for slate type: " + slateType);
            return;
        }

        ItemSanguineScroll.setSpell(sanguineScroll, spellId, spellLevel);

        mainHand.shrink(1);
        offHand.shrink(1);
        if (!player.getInventory().add(sanguineScroll)) {
            player.drop(sanguineScroll, false);
        }

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

        event.setCanceled(true); // Prevent altar GUI from opening
    }

    private static int getEVCostForRarity(io.redspace.ironsspellbooks.api.spells.SpellRarity rarity) {
        return switch (rarity) {
            case COMMON -> COMMON_LP;
            case UNCOMMON -> UNCOMMON_LP;
            case RARE -> RARE_LP;
            case EPIC -> EPIC_LP;
            case LEGENDARY -> LEGENDARY_LP;
        };
    }

    private static ItemSanguineScroll.SlateType getSlateType(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        String id = stack.getItem().toString();

        if (id.contains("blank_slate")) {
            return ItemSanguineScroll.SlateType.BLANK;
        } else if (id.contains("reinforced_slate")) {
            return ItemSanguineScroll.SlateType.REINFORCED;
        } else if (id.contains("imbued_slate")) {
            return ItemSanguineScroll.SlateType.IMBUED;
        } else if (id.contains("demon_slate")) {
            return ItemSanguineScroll.SlateType.DEMON;
        } else if (id.contains("ethereal_slate")) {
            return ItemSanguineScroll.SlateType.ETHEREAL;
        }

        return null;
    }

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
