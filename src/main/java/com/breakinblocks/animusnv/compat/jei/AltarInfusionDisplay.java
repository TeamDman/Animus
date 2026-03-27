package com.breakinblocks.animusnv.compat.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

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
    private final int evCost;
    private final Component title;
    private final Component description;

    private final int fromTier;
    private final int toTier;
    private final String requiredOrb;

    public static AltarInfusionDisplay createSpellbookInfusion(
            ItemStack input, ItemStack output, int evCost,
            Component title, Component description) {
        return new AltarInfusionDisplay(
            InfusionType.SPELLBOOK,
            ItemStack.EMPTY,
            ItemStack.EMPTY,
            input,
            List.of(output),
            evCost,
            title,
            description,
            0, 1, "Weak Blood Orb"
        );
    }

    public static AltarInfusionDisplay createSanguineScrollInfusion(
            ItemStack scrollInput, ItemStack slateInput, List<ItemStack> outputs,
            int evCost, Component title, Component description) {
        return new AltarInfusionDisplay(
            InfusionType.SANGUINE_SCROLL,
            scrollInput,
            slateInput,
            ItemStack.EMPTY,
            outputs,
            evCost,
            title,
            description,
            0, 0, ""
        );
    }

    public static AltarInfusionDisplay createSpellbookUpgrade(
            ItemStack input, ItemStack output, int evCost,
            int fromTier, int toTier, String requiredOrb,
            Component title, Component description) {
        return new AltarInfusionDisplay(
            InfusionType.SPELLBOOK_UPGRADE,
            ItemStack.EMPTY,
            ItemStack.EMPTY,
            input,
            List.of(output),
            evCost,
            title,
            description,
            fromTier, toTier, requiredOrb
        );
    }

    private AltarInfusionDisplay(InfusionType type, ItemStack mainHandInput, ItemStack offHandInput,
                                  ItemStack altarInput, List<ItemStack> outputs, int evCost,
                                  Component title, Component description,
                                  int fromTier, int toTier, String requiredOrb) {
        this.type = type;
        this.mainHandInput = mainHandInput;
        this.offHandInput = offHandInput;
        this.altarInput = altarInput;
        this.outputs = outputs;
        this.evCost = evCost;
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
        return evCost;
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
