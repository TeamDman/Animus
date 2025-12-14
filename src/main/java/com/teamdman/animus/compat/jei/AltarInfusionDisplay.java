package com.teamdman.animus.compat.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Display wrapper for special altar infusion recipes in JEI
 * Used for Blood-Infused Spellbook and Sanguine Scrolls
 */
public class AltarInfusionDisplay {

    public enum InfusionType {
        SPELLBOOK,          // Initial altar infusion (regular spellbook → blood-infused)
        SPELLBOOK_UPGRADE,  // Upgrade tiers (tier N → tier N+1)
        SANGUINE_SCROLL     // Special interaction (main hand + offhand + right-click)
    }

    private final InfusionType type;
    private final ItemStack mainHandInput;   // For scrolls: the Iron's Spells scroll
    private final ItemStack offHandInput;    // For scrolls: the slate
    private final ItemStack altarInput;      // For spellbook: the leather spellbook
    private final List<ItemStack> outputs;   // The result(s)
    private final int lpCost;
    private final Component title;
    private final Component description;

    // Upgrade tier metadata (for SPELLBOOK_UPGRADE type)
    private final int fromTier;
    private final int toTier;
    private final String requiredOrb;

    /**
     * Constructor for SPELLBOOK type (initial altar infusion)
     */
    public static AltarInfusionDisplay createSpellbookInfusion(
            ItemStack input, ItemStack output, int lpCost,
            Component title, Component description) {
        return new AltarInfusionDisplay(
            InfusionType.SPELLBOOK,
            ItemStack.EMPTY,
            ItemStack.EMPTY,
            input,
            List.of(output),
            lpCost,
            title,
            description,
            0, 1, "Weak Blood Orb"
        );
    }

    /**
     * Constructor for SANGUINE_SCROLL type (main hand + offhand interaction)
     */
    public static AltarInfusionDisplay createSanguineScrollInfusion(
            ItemStack scrollInput, ItemStack slateInput, List<ItemStack> outputs,
            int lpCost, Component title, Component description) {
        return new AltarInfusionDisplay(
            InfusionType.SANGUINE_SCROLL,
            scrollInput,
            slateInput,
            ItemStack.EMPTY,
            outputs,
            lpCost,
            title,
            description,
            0, 0, ""
        );
    }

    /**
     * Constructor for SPELLBOOK_UPGRADE type (upgrade tier N to tier N+1)
     */
    public static AltarInfusionDisplay createSpellbookUpgrade(
            ItemStack input, ItemStack output, int lpCost,
            int fromTier, int toTier, String requiredOrb,
            Component title, Component description) {
        return new AltarInfusionDisplay(
            InfusionType.SPELLBOOK_UPGRADE,
            ItemStack.EMPTY,
            ItemStack.EMPTY,
            input,
            List.of(output),
            lpCost,
            title,
            description,
            fromTier, toTier, requiredOrb
        );
    }

    private AltarInfusionDisplay(InfusionType type, ItemStack mainHandInput, ItemStack offHandInput,
                                  ItemStack altarInput, List<ItemStack> outputs, int lpCost,
                                  Component title, Component description,
                                  int fromTier, int toTier, String requiredOrb) {
        this.type = type;
        this.mainHandInput = mainHandInput;
        this.offHandInput = offHandInput;
        this.altarInput = altarInput;
        this.outputs = outputs;
        this.lpCost = lpCost;
        this.title = title;
        this.description = description;
        this.fromTier = fromTier;
        this.toTier = toTier;
        this.requiredOrb = requiredOrb;
    }

    public InfusionType getType() {
        return type;
    }

    public ItemStack getMainHandInput() {
        return mainHandInput;
    }

    public ItemStack getOffHandInput() {
        return offHandInput;
    }

    public ItemStack getAltarInput() {
        return altarInput;
    }

    public List<ItemStack> getOutputs() {
        return outputs;
    }

    public int getLpCost() {
        return lpCost;
    }

    public Component getTitle() {
        return title;
    }

    public Component getDescription() {
        return description;
    }

    public boolean isSpellbookType() {
        return type == InfusionType.SPELLBOOK;
    }

    public boolean isSpellbookUpgradeType() {
        return type == InfusionType.SPELLBOOK_UPGRADE;
    }

    public boolean isSanguineScrollType() {
        return type == InfusionType.SANGUINE_SCROLL;
    }

    public int getFromTier() {
        return fromTier;
    }

    public int getToTier() {
        return toTier;
    }

    public String getRequiredOrb() {
        return requiredOrb;
    }
}
