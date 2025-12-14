package com.teamdman.animus.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Display wrapper for special altar infusion recipes in JEI
 * Used for Blood-Infused Spellbook and Sanguine Scrolls
 */
public class AltarInfusionDisplay {

    public enum InfusionType {
        SPELLBOOK,      // Regular altar infusion (item in altar)
        SANGUINE_SCROLL // Special interaction (main hand + offhand + right-click)
    }

    private final InfusionType type;
    private final ItemStack mainHandInput;   // For scrolls: the Iron's Spells scroll
    private final ItemStack offHandInput;    // For scrolls: the slate
    private final ItemStack altarInput;      // For spellbook: the leather spellbook
    private final List<ItemStack> outputs;   // The result(s)
    private final int lpCost;
    private final Component title;
    private final Component description;

    /**
     * Constructor for SPELLBOOK type (regular altar infusion)
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
            description
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
            description
        );
    }

    private AltarInfusionDisplay(InfusionType type, ItemStack mainHandInput, ItemStack offHandInput,
                                  ItemStack altarInput, List<ItemStack> outputs, int lpCost,
                                  Component title, Component description) {
        this.type = type;
        this.mainHandInput = mainHandInput;
        this.offHandInput = offHandInput;
        this.altarInput = altarInput;
        this.outputs = outputs;
        this.lpCost = lpCost;
        this.title = title;
        this.description = description;
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

    public boolean isSanguineScrollType() {
        return type == InfusionType.SANGUINE_SCROLL;
    }
}
