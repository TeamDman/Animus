package com.teamdman.animus.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Display wrapper for imperfect rituals in JEI
 * Contains all information needed to render a ritual recipe
 */
public class ImperfectRitualDisplay {
    private final String ritualKey;
    private final BlockState triggerBlock;
    private final int lpCost;
    private final Component name;
    private final Component description;
    private final String requiredMod; // null if no mod required

    public ImperfectRitualDisplay(String ritualKey, BlockState triggerBlock, int lpCost,
                                   Component name, Component description, String requiredMod) {
        this.ritualKey = ritualKey;
        this.triggerBlock = triggerBlock;
        this.lpCost = lpCost;
        this.name = name;
        this.description = description;
        this.requiredMod = requiredMod;
    }

    public String getRitualKey() {
        return ritualKey;
    }

    public BlockState getTriggerBlock() {
        return triggerBlock;
    }

    public Block getTriggerBlockType() {
        return triggerBlock.getBlock();
    }

    public ItemStack getTriggerItemStack() {
        return new ItemStack(triggerBlock.getBlock());
    }

    public int getLpCost() {
        return lpCost;
    }

    public Component getName() {
        return name;
    }

    public Component getDescription() {
        return description;
    }

    public String getRequiredMod() {
        return requiredMod;
    }

    public boolean hasRequiredMod() {
        return requiredMod != null;
    }
}
